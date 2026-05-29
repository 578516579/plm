package cn.com.bosssfot.dv.plm.defect.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.defect.domain.Defect;
import cn.com.bosssfot.dv.plm.defect.mapper.DefectMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;
import cn.com.bosssfot.dv.plm.sprint.mapper.SprintMapper;
import cn.com.bosssfot.dv.plm.task.domain.Task;
import cn.com.bosssfot.dv.plm.task.mapper.TaskMapper;
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;
import cn.com.bosssfot.dv.plm.testcase.mapper.TestCaseMapper;

/**
 * DefectServiceImpl 单元测试
 *
 * 覆盖范围（Phase 04 Gate B.0 强制项）：
 *   - ADR-0005 generateDefectNo: DEFECT-YYYY-NNNN 格式 / 序号递增 / 撞号重试
 *   - PRD §3.4 状态机: 5×5 矩阵关键转换 / 终态保护 / 反向边 03→01
 *   - 进入 03（已解决）必填 resolution → 705
 *   - API §2 三层 FK 校验 (project 必填 / sprint/task 可空) → 702
 *   - reporter_user_id 默认 = 当前 userId
 */
@ExtendWith(MockitoExtension.class)
class DefectServiceImplTest {

    @Mock
    private DefectMapper defectMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TestCaseMapper testCaseMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DefectServiceImpl service;

    private Defect sample;

    @BeforeEach
    void setUp() {
        sample = new Defect();
        sample.setTitle("登录按钮无响应");
        sample.setProjectId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateDefectNo (ADR-0005)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateDefectNo (ADR-0005)")
    class GenerateDefectNoTests {

        @Test
        @DisplayName("当年无缺陷时，编号为 DEFECT-YYYY-0001")
        void firstDefectOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(defectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(defectMapper.insertDefect(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDefect(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getDefectNo()).isEqualTo(String.format("DEFECT-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 5 个缺陷时，下一个编号为 0006")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(defectMapper.selectMaxSeqOfYear(anyString())).thenReturn(5);
            when(defectMapper.insertDefect(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDefect(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getDefectNo()).isEqualTo(String.format("DEFECT-%d-0006", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(defectMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(defectMapper.insertDefect(any()))
                .thenThrow(new DuplicateKeyException("uk_defect_no"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                int rows = service.insertDefect(sample);
                assertThat(rows).isEqualTo(1);
                verify(defectMapper, times(2)).insertDefect(any());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 (API §2)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §2)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填，空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("缺陷标题不能为空");
        }

        @Test
        @DisplayName("projectId 必填，null 抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不能为空");
        }

        @Test
        @DisplayName("projectId FK 不存在抛 702")
        void projectFkNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("可选 sprintId FK 不存在抛 702")
        void sprintFkNotFound() {
            sample.setSprintId(99L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(sprintMapper.selectSprintById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联迭代不存在");
        }

        @Test
        @DisplayName("可选 taskId FK 不存在抛 702")
        void taskFkNotFound() {
            sample.setTaskId(88L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(taskMapper.selectTaskById(88L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联任务不存在");
        }

        @Test
        @DisplayName("新建状态非 00 抛 601")
        void initialStatusMustBe00() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("新建");
        }

        @Test
        @DisplayName("reporterUserId 未传时默认 = 当前 userId")
        void defaultReporterUserId() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(defectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(defectMapper.insertDefect(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(42L);
                service.insertDefect(sample);
            }

            assertThat(sample.getReporterUserId()).isEqualTo(42L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 跨模块 FK testcaseId — Proposal 0028 P0-1 (同 projectId 强约束)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("跨模块 FK testcaseId (Proposal 0028 P0-1)")
    class TestcaseFkTests {

        @Test
        @DisplayName("testFkOk_当目标存在且同 projectId 时插入成功")
        void testFkOk() {
            sample.setTestcaseId(200L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testCaseMapper.selectTestCaseById(200L)).thenReturn(testCaseWithProject(200L, 1L));
            when(defectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(defectMapper.insertDefect(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                int rows = service.insertDefect(sample);
                assertThat(rows).isEqualTo(1);
            }
            assertThat(sample.getTestcaseId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("testFkNullOk_当 testcaseId 为 null 时跳过校验")
        void testFkNullOk() {
            sample.setTestcaseId(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(defectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(defectMapper.insertDefect(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                int rows = service.insertDefect(sample);
                assertThat(rows).isEqualTo(1);
            }
            verify(testCaseMapper, never()).selectTestCaseById(any());
        }

        @Test
        @DisplayName("testFkNotFound_当目标 TestCase 不存在 → 702")
        void testFkNotFound() {
            sample.setTestcaseId(999L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testCaseMapper.selectTestCaseById(999L)).thenReturn(null);

            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试用例不存在");
        }

        @Test
        @DisplayName("testFkDifferentProject_当目标 projectId 不同 → 702")
        void testFkDifferentProject() {
            sample.setTestcaseId(200L);  // defect.projectId = 1L
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testCaseMapper.selectTestCaseById(200L)).thenReturn(testCaseWithProject(200L, 2L)); // 不同项目

            assertThatThrownBy(() -> service.insertDefect(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("同一项目");
        }

        private TestCase testCaseWithProject(Long testcaseId, Long projectId) {
            TestCase tc = new TestCase();
            tc.setTestcaseId(testcaseId);
            tc.setProjectId(projectId);
            return tc;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (PRD §3.4)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机转换 (PRD §3.4)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01（新建→已确认）")
        void legal_00_to_01() {
            Defect old = existingDefect("00");
            when(defectMapper.selectDefectById(1L)).thenReturn(old);
            when(defectMapper.updateDefect(any())).thenReturn(1);

            Defect upd = updateDefect(1L, "01", null);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDefect(upd);
            }
            verify(defectMapper).updateDefect(any());
        }

        @Test
        @DisplayName("合法反向边 03→01（已解决→已确认 回归打回）")
        void legal_03_to_01_regression() {
            Defect old = existingDefect("03");
            when(defectMapper.selectDefectById(1L)).thenReturn(old);
            when(defectMapper.updateDefect(any())).thenReturn(1);

            Defect upd = updateDefect(1L, "01", null);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDefect(upd);
            }
            verify(defectMapper).updateDefect(any());
        }

        @Test
        @DisplayName("非法转换 00→02 抛 601")
        void illegal_00_to_02() {
            Defect old = existingDefect("00");
            when(defectMapper.selectDefectById(1L)).thenReturn(old);

            Defect upd = updateDefect(1L, "02", null);
            assertThatThrownBy(() -> service.updateDefect(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("新建")
                .hasMessageContaining("处理中");
            verify(defectMapper, never()).updateDefect(any());
        }

        @Test
        @DisplayName("终态保护 04→任意 抛 601（已关闭终态）")
        void terminal_04_to_any() {
            Defect old = existingDefect("04");
            when(defectMapper.selectDefectById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01", "02", "03"}) {
                Defect upd = updateDefect(1L, to, null);
                assertThatThrownBy(() -> service.updateDefect(upd))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已关闭");
            }
        }

        @Test
        @DisplayName("进入 03（已解决）必须有 resolution，否则抛 705")
        void resolvedRequiresResolution() {
            Defect old = existingDefect("02");
            old.setResolution(null);
            when(defectMapper.selectDefectById(1L)).thenReturn(old);

            Defect upd = updateDefect(1L, "03", null);  // resolution 为 null
            assertThatThrownBy(() -> service.updateDefect(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已解决")
                .hasMessageContaining("解决说明");
        }

        @Test
        @DisplayName("进入 03（已解决）有 resolution 正常完成")
        void resolvedWithResolution() {
            Defect old = existingDefect("02");
            old.setResolution(null);
            when(defectMapper.selectDefectById(1L)).thenReturn(old);
            when(defectMapper.updateDefect(any())).thenReturn(1);

            Defect upd = updateDefect(1L, "03", "修复了 null 指针");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDefect(upd);
            }
            verify(defectMapper).updateDefect(any());
        }

        @Test
        @DisplayName("缺陷不存在抛 404")
        void notFound() {
            when(defectMapper.selectDefectById(99L)).thenReturn(null);
            Defect upd = updateDefect(99L, "01", null);
            assertThatThrownBy(() -> service.updateDefect(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("缺陷不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 辅助方法
    // ─────────────────────────────────────────────────────────────────────

    private Project existingProject() {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("测试项目");
        return p;
    }

    private Defect existingDefect(String status) {
        Defect d = new Defect();
        d.setDefectId(1L);
        d.setTitle("旧缺陷");
        d.setStatus(status);
        d.setProjectId(1L);
        return d;
    }

    private Defect updateDefect(Long id, String newStatus, String resolution) {
        Defect d = new Defect();
        d.setDefectId(id);
        d.setStatus(newStatus);
        d.setResolution(resolution);
        return d;
    }
}
