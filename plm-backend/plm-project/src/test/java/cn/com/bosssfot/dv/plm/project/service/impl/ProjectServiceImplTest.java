package cn.com.bosssfot.dv.plm.project.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
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

/**
 * ProjectServiceImpl 单元测试
 *
 * 覆盖范围 (v2 PRD-align,引用 commit 20b5bb6 PRD-MAPPING §2 字段表):
 *   - ADR-0001 generateProjectNo:格式 / 跨年边界 / 撞号重试
 *   - PRD-MAPPING §3 状态机:status (4 态) / lifecyclePhase (4 态,status=00 时演进)
 *   - 字段校验:必填 / 白名单 / 进度范围 / 日期逻辑
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl service;

    private Project sample;

    @BeforeEach
    void setUp() {
        sample = new Project();
        sample.setProjectName("测试项目");
        sample.setBusinessLine("precision_agri");   // PRD-align 必填字段
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateProjectNo (ADR-0001)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateProjectNo (ADR-0001)")
    class GenerateProjectNoTests {

        @Test
        @DisplayName("当年无项目时,编号为 PRJ-YYYY-0001")
        void firstProjectOfYear() {
            when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(projectMapper.insertProject(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertProject(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getProjectNo()).isEqualTo(String.format("PRJ-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 5 个项目时,下一个编号为 0006")
        void nextSequence() {
            when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(5);
            when(projectMapper.insertProject(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertProject(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getProjectNo()).isEqualTo(String.format("PRJ-%d-0006", year));
        }

        @Test
        @DisplayName("4 位流水号填充:第 9999 个仍为 9999")
        void fourDigitPadding() {
            when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(9998);
            when(projectMapper.insertProject(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertProject(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getProjectNo()).isEqualTo(String.format("PRJ-%d-9999", year));
        }

        @Test
        @DisplayName("撞号重试:DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(projectMapper.insertProject(any()))
                .thenThrow(new DuplicateKeyException("uk_project_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertProject(sample);
                assertThat(rows).isEqualTo(1);
                verify(projectMapper, times(2)).insertProject(any());
            }
        }

        @Test
        @DisplayName("用户传了 projectNo 时,不自动生成")
        void userProvidedNoIsKept() {
            sample.setProjectNo("USER-CUSTOM-001");
            when(projectMapper.insertProject(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertProject(sample);
            }
            assertThat(sample.getProjectNo()).isEqualTo("USER-CUSTOM-001");
            verify(projectMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验")
    class ValidationTests {

        @Test
        @DisplayName("projectName 必填,空抛 601")
        void nameRequired() {
            sample.setProjectName(null);
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("项目名称不能为空");
        }

        @Test
        @DisplayName("businessLine 必填,空抛 601 (PRD §F1.2)")
        void businessLineRequired() {
            sample.setBusinessLine(null);
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("业务线不能为空");
        }

        @Test
        @DisplayName("非法 businessLine 抛 604")
        void illegalBusinessLine() {
            sample.setBusinessLine("not_a_real_line");
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("业务线");
        }

        @Test
        @DisplayName("startDate > endDate 时抛 604")
        void datesIllegal() throws Exception {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sample.setStartDate(sdf.parse("2026-12-31"));
            sample.setEndDate(sdf.parse("2026-01-01"));
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("起始日期不能晚于结束日期");
        }

        @Test
        @DisplayName("progress 越界 (>100) 抛 604")
        void progressOutOfRange() {
            sample.setProgress(101);
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("0-100");
        }

        @Test
        @DisplayName("progress 负数抛 604")
        void progressNegative() {
            sample.setProgress(-1);
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("0-100");
        }

        @Test
        @DisplayName("新建时 status 非 00 抛 701")
        void initialStatusMustBeZero() {
            sample.setStatus("01");
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("「进行中」");
        }

        @Test
        @DisplayName("新建时 lifecyclePhase 非 00 抛 701")
        void initialPhaseMustBeZero() {
            sample.setLifecyclePhase("01");
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("「规划中」");
        }

        @Test
        @DisplayName("未指定时默认 status='00' / lifecyclePhase='00' / progress=0")
        void defaultsApplied() {
            when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(projectMapper.insertProject(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertProject(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
            assertThat(sample.getLifecyclePhase()).isEqualTo("00");
            assertThat(sample.getProgress()).isEqualTo(0);
        }

        @Test
        @DisplayName("非法 health 值抛 604")
        void illegalHealth() {
            sample.setHealth("yellow");
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("健康度");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 总状态机 (status, PRD-MAPPING §3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("总状态机 (status)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 (进行中→暂停)")
        void legal_00_to_01() {
            Project old = newOld("00", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateProject(update);
                assertThat(rows).isEqualTo(1);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("合法转换 00→02 (进行中→已完成)")
        void legal_00_to_02() {
            Project old = newOld("00", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateProject(update);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("合法转换 01→00 (暂停→进行中,反向边)")
        void legal_01_to_00() {
            Project old = newOld("01", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateProject(update);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("非法转换 01→02 (暂停→已完成,需先恢复) 抛 601")
        void illegal_01_to_02() {
            Project old = newOld("01", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("02");
            assertThatThrownBy(() -> service.updateProject(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("暂停")
                .hasMessageContaining("已完成");
            verify(projectMapper, never()).updateProject(any());
        }

        @Test
        @DisplayName("终态保护:02→任意 抛 601")
        void terminal_02_to_any() {
            Project old = newOld("02", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            for (String to : new String[] {"00", "01", "03"}) {
                Project update = new Project();
                update.setId(1L);
                update.setStatus(to);
                assertThatThrownBy(() -> service.updateProject(update))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已完成");
            }
        }

        @Test
        @DisplayName("终态保护:03→任意 抛 601")
        void terminal_03_to_any() {
            Project old = newOld("03", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            for (String to : new String[] {"00", "01", "02"}) {
                Project update = new Project();
                update.setId(1L);
                update.setStatus(to);
                assertThatThrownBy(() -> service.updateProject(update))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已取消");
            }
        }

        @Test
        @DisplayName("status 与原值相同时不触发校验")
        void sameStatusNoCheck() {
            Project old = newOld("00", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("00");
            update.setProjectName("改名字");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateProject(update);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("项目不存在抛 404")
        void notFound() {
            when(projectMapper.selectProjectById(99L)).thenReturn(null);
            Project update = new Project();
            update.setId(99L);
            update.setStatus("01");
            assertThatThrownBy(() -> service.updateProject(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("项目不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 交付阶段状态机 (lifecyclePhase)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("交付阶段状态机 (lifecyclePhase)")
    class PhaseMachineTests {

        @Test
        @DisplayName("合法 00→01 (规划→研发)")
        void legalPhase_00_to_01() {
            Project old = newOld("00", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setLifecyclePhase("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateProject(update);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("合法 01→00 (研发回退到规划,反向边)")
        void legalPhase_01_to_00() {
            Project old = newOld("00", "01");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setLifecyclePhase("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateProject(update);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("非法跨级 00→02 (规划→测试) 抛 601")
        void illegalPhase_00_to_02() {
            Project old = newOld("00", "00");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            Project update = new Project();
            update.setId(1L);
            update.setLifecyclePhase("02");
            assertThatThrownBy(() -> service.updateProject(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("规划中")
                .hasMessageContaining("测试中");
        }

        @Test
        @DisplayName("status=01 (暂停) 时拒绝改 phase 抛 601")
        void phaseFrozenWhenPaused() {
            Project old = newOld("01", "01");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            Project update = new Project();
            update.setId(1L);
            update.setLifecyclePhase("02");
            assertThatThrownBy(() -> service.updateProject(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("阶段已冻结");
        }

        @Test
        @DisplayName("同请求改 status 01→00 + phase 01→02 顺序生效 (status 先,phase 后)")
        void simultaneousStatusAndPhase() {
            Project old = newOld("01", "01");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("00");           // 恢复到进行中
            update.setLifecyclePhase("02");   // 同时把阶段推进到测试

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateProject(update);
            }
            verify(projectMapper).updateProject(any());
        }
    }

    private Project newOld(String status, String phase) {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("旧");
        p.setBusinessLine("precision_agri");
        p.setStatus(status);
        p.setLifecyclePhase(phase);
        return p;
    }
}
