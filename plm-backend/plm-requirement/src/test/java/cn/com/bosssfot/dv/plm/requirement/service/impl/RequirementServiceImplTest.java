package cn.com.bosssfot.dv.plm.requirement.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
 * 覆盖范围 (引用 PRD-MAPPING.md §2 commit 1afe0ba):
 *   - ADR-0002 generateRequirementNo: 格式 / 跨年 / 撞号重试
 *   - 状态机 (PRD-MAPPING §3 ADR-A 4 态实用版): 合法 / 非法 / 终态保护
 *   - 字段校验: 必填 / 白名单 (status/aiValue/priority/source) / FK 702
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
    class GenerateNoTests {

        @Test
        @DisplayName("当年无记录时,编号为 REQ-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
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
        @DisplayName("当年已有 5 个时,下一个为 0006")
        void nextSeq() {
            when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
            when(requirementMapper.selectMaxSeqOfYear(anyString())).thenReturn(5);
            when(requirementMapper.insertRequirement(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRequirement(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getRequirementNo()).isEqualTo(String.format("REQ-%d-0006", year));
        }

        @Test
        @DisplayName("撞号重试")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
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
    // 字段校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("需求标题");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不能为空");
        }

        @Test
        @DisplayName("projectId 不存在抛 702 (FK 校验)")
        void projectNotFound() {
            when(projectMapper.selectProjectById(99L)).thenReturn(null);
            sample.setProjectId(99L);
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("非法 status 抛 604")
        void illegalStatus() {
            sample.setStatus("99");
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("非法状态值");
        }

        @Test
        @DisplayName("非法 aiValue 抛 604")
        void illegalAiValue() {
            sample.setAiValue("X");
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("AI 价值");
        }

        @Test
        @DisplayName("非法 priority 抛 604")
        void illegalPriority() {
            sample.setPriority("99");
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("优先级");
        }

        @Test
        @DisplayName("非法 source 抛 604")
        void illegalSource() {
            sample.setSource("99");
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("来源");
        }

        @Test
        @DisplayName("新建时 status 非 00 抛 601")
        void initialStatusMustBeZero() {
            when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
            sample.setStatus("01");
            assertThatThrownBy(() -> service.insertRequirement(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("待评审");
        }

        @Test
        @DisplayName("合法 aiValue=H 通过白名单")
        void legalAiValueH() {
            when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
            when(requirementMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(requirementMapper.insertRequirement(any())).thenReturn(1);
            sample.setAiValue("H");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertRequirement(sample);
                assertThat(rows).isEqualTo(1);
                assertThat(sample.getAiValue()).isEqualTo("H");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (ADR-A 4 态实用版)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (4 态实用版 - ADR-A)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法 00→01 (待评审→开发中)")
        void legal_00_to_01() {
            Requirement old = newOld("00");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);
            when(requirementMapper.updateRequirement(any())).thenReturn(1);

            Requirement update = new Requirement();
            update.setRequirementId(1L);
            update.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateRequirement(update);
            }
            verify(requirementMapper).updateRequirement(any());
        }

        @Test
        @DisplayName("合法 01→00 反向边 (开发中→待评审,评审打回)")
        void legal_01_to_00() {
            Requirement old = newOld("01");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);
            when(requirementMapper.updateRequirement(any())).thenReturn(1);

            Requirement update = new Requirement();
            update.setRequirementId(1L);
            update.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateRequirement(update);
            }
            verify(requirementMapper).updateRequirement(any());
        }

        @Test
        @DisplayName("非法跨级 00→02 抛 601")
        void illegal_00_to_02() {
            Requirement old = newOld("00");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);

            Requirement update = new Requirement();
            update.setRequirementId(1L);
            update.setStatus("02");
            assertThatThrownBy(() -> service.updateRequirement(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("待评审")
                .hasMessageContaining("已完成");
            verify(requirementMapper, never()).updateRequirement(any());
        }

        @Test
        @DisplayName("终态保护:02→任意 抛 601")
        void terminal_02() {
            Requirement old = newOld("02");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);
            for (String to : new String[] {"00", "01", "03"}) {
                Requirement update = new Requirement();
                update.setRequirementId(1L);
                update.setStatus(to);
                assertThatThrownBy(() -> service.updateRequirement(update))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已完成");
            }
        }

        @Test
        @DisplayName("更新 aiValue 不触发状态机校验")
        void updateAiValueOnly() {
            Requirement old = newOld("01");
            when(requirementMapper.selectRequirementById(1L)).thenReturn(old);
            when(requirementMapper.updateRequirement(any())).thenReturn(1);

            Requirement update = new Requirement();
            update.setRequirementId(1L);
            update.setAiValue("M");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateRequirement(update);
            }
            verify(requirementMapper).updateRequirement(any());
        }

        @Test
        @DisplayName("需求不存在抛 404")
        void notFound() {
            when(requirementMapper.selectRequirementById(99L)).thenReturn(null);
            Requirement update = new Requirement();
            update.setRequirementId(99L);
            update.setStatus("01");
            assertThatThrownBy(() -> service.updateRequirement(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("需求不存在");
        }
    }

    private Requirement newOld(String status) {
        Requirement r = new Requirement();
        r.setRequirementId(1L);
        r.setTitle("旧");
        r.setProjectId(1L);
        r.setStatus(status);
        return r;
    }
}
