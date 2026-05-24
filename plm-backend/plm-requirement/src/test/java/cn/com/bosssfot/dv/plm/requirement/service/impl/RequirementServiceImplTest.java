package cn.com.bosssfot.dv.plm.requirement.service.impl;

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
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;

/**
 * RequirementServiceImpl 单元测试
 *
 * 覆盖范围（Phase 04 Gate B.0 强制项）：
 *   - ADR-0002 generateRequirementNo: 格式 / 序号递增 / 撞号重试
 *   - PRD §3.3 状态机: 4×4 矩阵关键转换 / 终态保护 / 打回边
 *   - API §2.2 字段校验: 必填 / 初始状态保护
 *   - FK 校验: projectId 不存在 → 702
 */
@ExtendWith(MockitoExtension.class)
class RequirementServiceImplTest {

    @Mock
    private RequirementMapper requirementMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private RequirementServiceImpl service;

    private Requirement sample;

    @BeforeEach
    void setUp() {
        sample = new Requirement();
        sample.setTitle("测试需求");
        sample.setProjectId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateRequirementNo (ADR-0002)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateRequirementNo (ADR-0002)")
    class GenerateRequirementNoTests {

        @Test
        @DisplayName("当年无需求时，编号为 REQ-YYYY-0001")
        void firstRequirementOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(requirementMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(requirementMapper.insertRequirement(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRequirement(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getRequirementNo()).isEqualTo(String.format("REQ-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 3 个需求时，下一个编号为 0004")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(requirementMapper.selectMaxSeqOfYear(anyString())).thenReturn(3);
            when(requirementMapper.insertRequirement(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRequirement(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getRequirementNo()).isEqualTo(String.format("REQ-%d-0004", year));
        }

        @Test
        @DisplayName("用户自定义了 requirementNo，不自动生成")
        void userProvidedNoIsKept() {
            sample.setRequirementNo("REQ-CUSTOM-001");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(requirementMapper.insertRequirement(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRequirement(sample);
            }

            assertThat(sample.getRequirementNo()).isEqualTo("REQ-CUSTOM-001");
            verify(requirementMapper, never()).selectMaxSeqOfYear(anyString());
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(requirementMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(requirementMapper.insertRequirement(any()))
                .thenThrow(new DuplicateKeyException("uk_requirement_no"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertRequirement(sample);
                assertThat(rows).isEqualTo(1);
                verify(requirementMapper, times(2)).insertRequirement(any());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 (API §2.2)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §2.2)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填，空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("需求标题不能为空");
        }

        @Test
        @DisplayName("projectId 必填，null 抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不能为空");
        }

        @Test
        @DisplayName("projectId 不存在抛 702")
        void projectIdFkNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态非 00 抛 601")
        void initialStatusMustBe00() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("待评审");
        }

        @Test
        @DisplayName("未指定 status 时默认设为 00（待评审）")
        void defaultStatusIsZero() {
            sample.setStatus(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(requirementMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(requirementMapper.insertRequirement(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRequirement(sample);
            }

            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (PRD §3.3, API §3.3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机转换 (PRD §3.3)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01（待评审→开发中）")
        void legal_00_to_01() {
            Requirement old = existingReq("00");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);
            when(requirementMapper.updateRequirement(any())).thenReturn(1);

            Requirement upd = updateReq(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateRequirement(upd);
                assertThat(rows).isEqualTo(1);
            }
            verify(requirementMapper).updateRequirement(any());
        }

        @Test
        @DisplayName("合法打回边 01→00（开发中→待评审）")
        void legal_01_to_00_rollback() {
            Requirement old = existingReq("01");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);
            when(requirementMapper.updateRequirement(any())).thenReturn(1);

            Requirement upd = updateReq(1L, "00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateRequirement(upd);
            }
            verify(requirementMapper).updateRequirement(any());
        }

        @Test
        @DisplayName("非法跳级 00→02 抛 601")
        void illegal_00_to_02() {
            Requirement old = existingReq("00");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);

            Requirement upd = updateReq(1L, "02");
            assertThatThrownBy(() -> service.updateRequirement(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("待评审")
                .hasMessageContaining("已完成");
            verify(requirementMapper, never()).updateRequirement(any());
        }

        @Test
        @DisplayName("终态保护 02→00 抛 601（已完成不可逆）")
        void terminal_02_to_00() {
            Requirement old = existingReq("02");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);

            Requirement upd = updateReq(1L, "00");
            assertThatThrownBy(() -> service.updateRequirement(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已完成");
        }

        @Test
        @DisplayName("终态保护 03→任意 抛 601（已取消终态）")
        void terminal_03_to_any() {
            Requirement old = existingReq("03");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01", "02"}) {
                Requirement upd = updateReq(1L, to);
                assertThatThrownBy(() -> service.updateRequirement(upd))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已取消");
            }
        }

        @Test
        @DisplayName("需求不存在抛 404")
        void notFound() {
            when(requirementMapper.selectRequirementById(99L)).thenReturn(null);
            Requirement upd = updateReq(99L, "01");
            assertThatThrownBy(() -> service.updateRequirement(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("需求不存在");
        }

        @Test
        @DisplayName("update 时改 projectId FK 不存在抛 702")
        void updateFkProjectNotFound() {
            Requirement old = existingReq("00");
            old.setProjectId(1L);
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);
            when(projectMapper.selectProjectById(99L)).thenReturn(null);

            Requirement upd = new Requirement();
            upd.setRequirementId(1L);
            upd.setProjectId(99L);   // 改 FK

            assertThatThrownBy(() -> service.updateRequirement(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
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

    private Requirement existingReq(String status) {
        Requirement r = new Requirement();
        r.setRequirementId(1L);
        r.setTitle("旧需求");
        r.setStatus(status);
        r.setProjectId(1L);
        return r;
    }

    private Requirement updateReq(Long id, String newStatus) {
        Requirement r = new Requirement();
        r.setRequirementId(id);
        r.setStatus(newStatus);
        return r;
    }
}
