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

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * ProjectServiceImpl 单元测试。
 *
 * 覆盖范围（按 Phase 04 Gate B.0 强制项）：
 *   - ADR-0001 generateProjectNo: 格式 / 跨年边界 / 撞号重试
 *   - PRD §3.3 状态机: 5x5 矩阵的关键转换 / 终态保护
 *   - API §3.2 字段校验: 必填 / 日期逻辑 / 初始状态
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
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateProjectNo (ADR-0001)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateProjectNo (ADR-0001)")
    class GenerateProjectNoTests {

        @Test
        @DisplayName("当年无项目时，编号为 PRJ-YYYY-0001")
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
        @DisplayName("当年已有 5 个项目时，下一个编号为 0006")
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
        @DisplayName("4 位流水号填充：第 9999 个仍为 9999")
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
        @DisplayName("撞号重试：DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);   // 第 2 次返回 1
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
        @DisplayName("用户传了 projectNo 时，不自动生成")
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
    // 字段校验 (API §3.2)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §3.2)")
    class ValidationTests {

        @Test
        @DisplayName("projectName 必填，空抛 601")
        void nameRequired() {
            sample.setProjectName(null);
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("项目名称不能为空");
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
        @DisplayName("新建时状态非 0 抛 701（已实现初始状态保护）")
        void initialStatusMustBeZero() {
            sample.setStatus("1");
            assertThatThrownBy(() -> service.insertProject(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("「未启动」");
        }

        @Test
        @DisplayName("未指定 status 时默认设为 0")
        void defaultStatusZero() {
            sample.setStatus(null);
            when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(projectMapper.insertProject(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertProject(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("0");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (PRD §3.3, API §3.3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机转换 (PRD §3.3)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 0→1 (未启动→进行中)")
        void legal_0_to_1() {
            Project old = newOld("0");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("1");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateProject(update);
                assertThat(rows).isEqualTo(1);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("合法转换 1→3 (进行中→已完成)")
        void legal_1_to_3() {
            Project old = newOld("1");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("3");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateProject(update);
            }
            verify(projectMapper).updateProject(any());
        }

        @Test
        @DisplayName("非法转换 1→0 抛 701")
        void illegal_1_to_0() {
            Project old = newOld("1");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("0");
            assertThatThrownBy(() -> service.updateProject(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("进行中")
                .hasMessageContaining("未启动");
            verify(projectMapper, never()).updateProject(any());
        }

        @Test
        @DisplayName("终态保护：3→1 (已完成→进行中) 抛 701")
        void terminal_3_to_1() {
            Project old = newOld("3");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("1");
            assertThatThrownBy(() -> service.updateProject(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已完成");
        }

        @Test
        @DisplayName("终态保护：4→任意 抛 701")
        void terminal_4_to_any() {
            Project old = newOld("4");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);

            for (String to : new String[] {"0", "1", "2", "3"}) {
                Project update = new Project();
                update.setId(1L);
                update.setStatus(to);
                assertThatThrownBy(() -> service.updateProject(update))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已取消");
            }
        }

        @Test
        @DisplayName("status 与原值相同时不触发校验（允许同状态更新其他字段）")
        void sameStatusNoCheck() {
            Project old = newOld("1");
            when(projectMapper.selectProjectById(1L)).thenReturn(old);
            when(projectMapper.updateProject(any())).thenReturn(1);

            Project update = new Project();
            update.setId(1L);
            update.setStatus("1");
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
            update.setStatus("1");
            assertThatThrownBy(() -> service.updateProject(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("项目不存在");
        }

        private Project newOld(String status) {
            Project p = new Project();
            p.setId(1L);
            p.setStatus(status);
            p.setProjectName("旧");
            return p;
        }
    }
}
