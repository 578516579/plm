package cn.com.bosssfot.dv.plm.sprint.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.common.api.ITaskQueryService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;
import cn.com.bosssfot.dv.plm.sprint.mapper.SprintMapper;

/**
 * SprintServiceImpl 单元测试
 *
 * 覆盖范围（Phase 04 Gate B.0 强制项）：
 *   - ADR-0004 generateSprintNo: SPR-YYYY-NNNN 格式 / 序号递增 / 撞号重试
 *   - PRD §3.3 状态机: 4×4 矩阵关键转换 / 终态保护
 *   - API §2.3 字段校验: 必填 / 日期区间 / 初始状态保护
 *   - 业务硬规则 703: 项目单一活跃迭代约束
 *   - API §2.5 删除前置检查: 关联任务存在 → 704
 */
@ExtendWith(MockitoExtension.class)
class SprintServiceImplTest {

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ITaskQueryService taskQueryService;

    @InjectMocks
    private SprintServiceImpl service;

    private Sprint sample;

    @BeforeEach
    void setUp() {
        sample = new Sprint();
        sample.setName("Sprint-1");
        sample.setProjectId(1L);
        sample.setPlannedStartDate(toDate(LocalDate.of(2026, 5, 1)));
        sample.setPlannedEndDate(toDate(LocalDate.of(2026, 5, 14)));
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateSprintNo (ADR-0004)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateSprintNo (ADR-0004)")
    class GenerateSprintNoTests {

        @Test
        @DisplayName("当年无迭代时，编号为 SPR-YYYY-0001")
        void firstSprintOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(sprintMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(sprintMapper.insertSprint(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSprint(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getSprintNo()).isEqualTo(String.format("SPR-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 2 个迭代时，下一个编号为 0003")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(sprintMapper.selectMaxSeqOfYear(anyString())).thenReturn(2);
            when(sprintMapper.insertSprint(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSprint(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getSprintNo()).isEqualTo(String.format("SPR-%d-0003", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(sprintMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(sprintMapper.insertSprint(any()))
                .thenThrow(new DuplicateKeyException("uk_sprint_no"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertSprint(sample);
                assertThat(rows).isEqualTo(1);
                verify(sprintMapper, times(2)).insertSprint(any());
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
        @DisplayName("name 必填，空抛 602")
        void nameRequired() {
            sample.setName(null);
            assertThatThrownBy(() -> service.insertSprint(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("迭代名称不能为空");
        }

        @Test
        @DisplayName("projectId 必填，null 抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertSprint(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不能为空");
        }

        @Test
        @DisplayName("plannedStartDate 必填，null 抛 602")
        void plannedStartDateRequired() {
            sample.setPlannedStartDate(null);
            assertThatThrownBy(() -> service.insertSprint(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("计划开始日期不能为空");
        }

        @Test
        @DisplayName("endDate < startDate 抛 604")
        void datesIllegal() {
            sample.setPlannedStartDate(toDate(LocalDate.of(2026, 5, 31)));
            sample.setPlannedEndDate(toDate(LocalDate.of(2026, 5, 1)));
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());

            assertThatThrownBy(() -> service.insertSprint(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("计划结束日期不能早于开始日期");
        }

        @Test
        @DisplayName("projectId FK 不存在抛 702")
        void projectFkNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertSprint(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态非 00 抛 601")
        void initialStatusMustBe00() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertSprint(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("计划中");
        }

        @Test
        @DisplayName("endDate 为 null 时自动补 startDate + 14 天")
        void defaultEndDatePlus14Days() {
            sample.setPlannedEndDate(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(sprintMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(sprintMapper.insertSprint(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSprint(sample);
            }

            assertThat(sample.getPlannedEndDate()).isNotNull();
            // 2026-05-01 + 14 = 2026-05-15
            LocalDate expectedEnd = LocalDate.of(2026, 5, 1).plusDays(14);
            LocalDate actualEnd = sample.getPlannedEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
            assertThat(actualEnd).isEqualTo(expectedEnd);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (PRD §3.3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机转换 (PRD §3.3)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01（计划中→进行中）")
        void legal_00_to_01() {
            Sprint old = existingSprint("00");
            when(sprintMapper.selectSprintById(1L)).thenReturn(old);
            when(sprintMapper.countActiveByProject(1L, 1L)).thenReturn(0);
            when(sprintMapper.updateSprint(any())).thenReturn(1);

            Sprint upd = updateSprint(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateSprint(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("合法转换 01→02（进行中→已完成）并自动填 actualEndDate")
        void legal_01_to_02_fillsActualEndDate() {
            Sprint old = existingSprint("01");
            when(sprintMapper.selectSprintById(1L)).thenReturn(old);
            when(sprintMapper.updateSprint(any())).thenReturn(1);

            Sprint upd = updateSprint(1L, "02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateSprint(upd);
            }

            assertThat(upd.getActualEndDate()).isNotNull();
        }

        @Test
        @DisplayName("非法转换 01→00 抛 601（进行中不能回计划中）")
        void illegal_01_to_00() {
            Sprint old = existingSprint("01");
            when(sprintMapper.selectSprintById(1L)).thenReturn(old);

            Sprint upd = updateSprint(1L, "00");
            assertThatThrownBy(() -> service.updateSprint(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("进行中")
                .hasMessageContaining("计划中");
            verify(sprintMapper, never()).updateSprint(any());
        }

        @Test
        @DisplayName("终态保护：02→01 抛 601")
        void terminal_02_to_01() {
            Sprint old = existingSprint("02");
            when(sprintMapper.selectSprintById(1L)).thenReturn(old);

            Sprint upd = updateSprint(1L, "01");
            assertThatThrownBy(() -> service.updateSprint(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已完成");
        }

        @Test
        @DisplayName("业务规则 703：进入 01 时项目已有活跃迭代抛 703")
        void rule703_activeSprintAlreadyExists() {
            Sprint old = existingSprint("00");
            when(sprintMapper.selectSprintById(1L)).thenReturn(old);
            when(sprintMapper.countActiveByProject(1L, 1L)).thenReturn(1);

            Sprint upd = updateSprint(1L, "01");
            assertThatThrownBy(() -> service.updateSprint(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已有进行中的迭代");
        }

        @Test
        @DisplayName("迭代不存在抛 404")
        void notFound() {
            when(sprintMapper.selectSprintById(99L)).thenReturn(null);
            Sprint upd = updateSprint(99L, "01");
            assertThatThrownBy(() -> service.updateSprint(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("迭代不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 删除前置检查 (API §2.5)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("删除前置检查 (API §2.5)")
    class DeleteTests {

        @Test
        @DisplayName("迭代下有关联任务时抛 704")
        void deleteBlockedByTask() {
            when(taskQueryService.countBySprintId(1L)).thenReturn(3);
            assertThatThrownBy(() -> service.deleteSprintByIds(new Long[]{1L}))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("3 个关联任务");
        }

        @Test
        @DisplayName("无关联任务时删除成功")
        void deleteSuccess() {
            when(taskQueryService.countBySprintId(1L)).thenReturn(0);
            when(sprintMapper.deleteSprintByIds(any())).thenReturn(1);
            int rows = service.deleteSprintByIds(new Long[]{1L});
            assertThat(rows).isEqualTo(1);
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

    private Sprint existingSprint(String status) {
        Sprint s = new Sprint();
        s.setSprintId(1L);
        s.setName("旧迭代");
        s.setStatus(status);
        s.setProjectId(1L);
        s.setPlannedStartDate(toDate(LocalDate.of(2026, 4, 1)));
        s.setPlannedEndDate(toDate(LocalDate.of(2026, 4, 14)));
        return s;
    }

    private Sprint updateSprint(Long id, String newStatus) {
        Sprint s = new Sprint();
        s.setSprintId(id);
        s.setStatus(newStatus);
        return s;
    }

    private static Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
