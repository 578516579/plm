package cn.com.bosssfot.dv.plm.task.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;
import cn.com.bosssfot.dv.plm.sprint.mapper.SprintMapper;
import cn.com.bosssfot.dv.plm.task.domain.Task;
import cn.com.bosssfot.dv.plm.task.mapper.TaskMapper;

/**
 * TaskServiceImpl 单元测试
 *
 * 覆盖范围（Phase 04 Gate B.0 强制项）：
 *   - ADR-0003 generateTaskNo: TASK-YYYY-NNNN 格式 / 序号递增 / 撞号重试
 *   - PRD §3.3 状态机: 6×6 矩阵关键转换 / 终态保护 / 反向边（评审打回）
 *   - API §2.4 进入「已完成」必填 actualHours → 602
 *   - API §2.3 MR 链接格式校验 → 604
 *   - API §2.3 三层 FK 校验 (project / requirement / sprint) → 702
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private RequirementMapper requirementMapper;

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskServiceImpl service;

    private Task sample;

    @BeforeEach
    void setUp() {
        sample = new Task();
        sample.setTitle("实现登录功能");
        sample.setProjectId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateTaskNo (ADR-0003)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateTaskNo (ADR-0003)")
    class GenerateTaskNoTests {

        @Test
        @DisplayName("当年无任务时，编号为 TASK-YYYY-0001")
        void firstTaskOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(taskMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(taskMapper.insertTask(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTask(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTaskNo()).isEqualTo(String.format("TASK-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 10 个任务时，下一个编号为 0011")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(taskMapper.selectMaxSeqOfYear(anyString())).thenReturn(10);
            when(taskMapper.insertTask(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTask(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTaskNo()).isEqualTo(String.format("TASK-%d-0011", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(taskMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(taskMapper.insertTask(any()))
                .thenThrow(new DuplicateKeyException("uk_task_no"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertTask(sample);
                assertThat(rows).isEqualTo(1);
                verify(taskMapper, times(2)).insertTask(any());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 (API §2.3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §2.3)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填，空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertTask(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("任务标题不能为空");
        }

        @Test
        @DisplayName("projectId 必填，null 抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertTask(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不能为空");
        }

        @Test
        @DisplayName("projectId FK 不存在抛 702")
        void projectFkNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertTask(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("requirementId FK 不存在抛 702")
        void requirementFkNotFound() {
            sample.setRequirementId(99L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(requirementMapper.selectRequirementById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertTask(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联需求不存在");
        }

        @Test
        @DisplayName("sprintId FK 不存在抛 702")
        void sprintFkNotFound() {
            sample.setSprintId(88L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(sprintMapper.selectSprintById(88L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertTask(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联迭代不存在");
        }

        @Test
        @DisplayName("MR URL 格式非 http(s):// 抛 604")
        void mrUrlFormatInvalid() {
            sample.setMrUrl("git@gitlab.com:user/repo.git");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertTask(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("MR 链接格式错误");
        }

        @Test
        @DisplayName("MR URL 以 https:// 开头时正常通过")
        void mrUrlValidHttps() {
            sample.setMrUrl("https://gitlab.com/user/repo/-/merge_requests/1");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(taskMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(taskMapper.insertTask(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertTask(sample);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("新建状态非 00 抛 601")
        void initialStatusMustBe00() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertTask(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("待开发");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (PRD §3.3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机转换 (PRD §3.3)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01（待开发→开发中）")
        void legal_00_to_01() {
            Task old = existingTask("00");
            when(taskMapper.selectTaskById(1L)).thenReturn(old);
            when(taskMapper.updateTask(any())).thenReturn(1);

            Task upd = updateTask(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTask(upd);
            }
            verify(taskMapper).updateTask(any());
        }

        @Test
        @DisplayName("合法反向边 02→01（代码评审→开发中 打回）")
        void legal_02_to_01_reviewRollback() {
            Task old = existingTask("02");
            when(taskMapper.selectTaskById(1L)).thenReturn(old);
            when(taskMapper.updateTask(any())).thenReturn(1);

            Task upd = updateTask(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTask(upd);
            }
            verify(taskMapper).updateTask(any());
        }

        @Test
        @DisplayName("合法反向边 03→02（测试中→代码评审 打回）")
        void legal_03_to_02_testRollback() {
            Task old = existingTask("03");
            when(taskMapper.selectTaskById(1L)).thenReturn(old);
            when(taskMapper.updateTask(any())).thenReturn(1);

            Task upd = updateTask(1L, "02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTask(upd);
            }
            verify(taskMapper).updateTask(any());
        }

        @Test
        @DisplayName("非法跳级 00→02 抛 601")
        void illegal_00_to_02() {
            Task old = existingTask("00");
            when(taskMapper.selectTaskById(1L)).thenReturn(old);

            Task upd = updateTask(1L, "02");
            assertThatThrownBy(() -> service.updateTask(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("待开发")
                .hasMessageContaining("代码评审");
            verify(taskMapper, never()).updateTask(any());
        }

        @Test
        @DisplayName("终态保护 04→任意 抛 601（已完成终态）")
        void terminal_04_to_any() {
            Task old = existingTask("04");
            when(taskMapper.selectTaskById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01", "02", "03"}) {
                Task upd = updateTask(1L, to);
                assertThatThrownBy(() -> service.updateTask(upd))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已完成");
            }
        }

        @Test
        @DisplayName("进入 04（已完成）时未填 actualHours 抛 602")
        void completionRequiresActualHours() {
            Task old = existingTask("03");
            old.setActualHours(null);
            when(taskMapper.selectTaskById(1L)).thenReturn(old);

            Task upd = updateTask(1L, "04");
            upd.setActualHours(null);
            assertThatThrownBy(() -> service.updateTask(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("实际工时");
        }

        @Test
        @DisplayName("进入 04（已完成）且已填 actualHours 正常完成")
        void completionWithActualHours() {
            Task old = existingTask("03");
            old.setActualHours(null);
            when(taskMapper.selectTaskById(1L)).thenReturn(old);
            when(taskMapper.updateTask(any())).thenReturn(1);

            Task upd = updateTask(1L, "04");
            upd.setActualHours(new BigDecimal("8.0"));
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTask(upd);
            }
            verify(taskMapper).updateTask(any());
        }

        @Test
        @DisplayName("任务不存在抛 404")
        void notFound() {
            when(taskMapper.selectTaskById(99L)).thenReturn(null);
            Task upd = updateTask(99L, "01");
            assertThatThrownBy(() -> service.updateTask(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("任务不存在");
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

    private Task existingTask(String status) {
        Task t = new Task();
        t.setTaskId(1L);
        t.setTitle("旧任务");
        t.setStatus(status);
        t.setProjectId(1L);
        return t;
    }

    private Task updateTask(Long id, String newStatus) {
        Task t = new Task();
        t.setTaskId(id);
        t.setStatus(newStatus);
        return t;
    }
}
