package cn.com.bosssfot.dv.plm.dashboard.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;
import cn.com.bosssfot.dv.plm.dashboard.mapper.DashboardMapper;

/**
 * DashboardServiceImpl 单元测试。
 *
 * 覆盖范围 — Phase 03 Gate B.4 关键路径 + Phase 04 B.0 关键单测:
 *   - aggregate(): 返回 6 类 widget 完整 (UI §4.2)
 *   - generateDashboardNo: 格式 DASH-YYYY-NNNN / 流水续号 / 撞号重试
 *   - 字段校验: title / ownerUserId 必填
 *   - 默认值填充: isDefault=N / status=00 / refreshInterval=60 / widgetTypes
 *   - 同用户默认唯一性: isDefault=Y 时 clearDefaultForOwner
 *   - 更新场景: 切换为默认 / 不存在抛 404
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DashboardMapper dashboardMapper;

    @InjectMocks
    private DashboardServiceImpl service;

    private Dashboard sample;

    @BeforeEach
    void setUp() {
        sample = new Dashboard();
        sample.setTitle("我的工作台");
        sample.setOwnerUserId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // aggregate() — UI §4.2 6 类 widget
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aggregate (UI §4.2 首屏聚合)")
    class AggregateTests {

        @Test
        @DisplayName("返回 6 类 widget 健全")
        void returnsAllSixWidgets() {
            Map<String, Object> result = service.aggregate(1L);

            assertThat(result).containsKeys(
                "stats", "activeProjects", "myTodos",
                "qualitySnapshot", "aiMetrics", "lifecycle", "ownerUserId"
            );
            assertThat(result.get("ownerUserId")).isEqualTo(1L);
        }

        @Test
        @DisplayName("stats 4 大顶部卡片字段齐全")
        @SuppressWarnings("unchecked")
        void statsCardsComplete() {
            Map<String, Object> result = service.aggregate(1L);
            Map<String, Object> stats = (Map<String, Object>) result.get("stats");

            assertThat(stats).containsKeys(
                "activeProjects", "aiDocsGenerated", "currentDefects", "autoTestCoverage"
            );
            assertThat((Integer) stats.get("activeProjects")).isPositive();
            assertThat((Double) stats.get("autoTestCoverage")).isBetween(0.0, 100.0);
        }

        @Test
        @DisplayName("activeProjects 与 myTodos 是非空列表")
        @SuppressWarnings("unchecked")
        void listsNotEmpty() {
            Map<String, Object> result = service.aggregate(1L);

            List<Map<String, Object>> projects = (List<Map<String, Object>>) result.get("activeProjects");
            List<Map<String, Object>> todos    = (List<Map<String, Object>>) result.get("myTodos");

            assertThat(projects).isNotEmpty();
            assertThat(projects.get(0)).containsKeys("name", "progress", "color");
            assertThat(todos).isNotEmpty();
            assertThat(todos.get(0)).containsKeys("title", "priority", "dueDate");
        }

        @Test
        @DisplayName("lifecycle 是 PRD 定义的 17 阶段静态数组")
        @SuppressWarnings("unchecked")
        void lifecycleSeventeenStages() {
            Map<String, Object> result = service.aggregate(1L);
            List<String> lifecycle = (List<String>) result.get("lifecycle");
            assertThat(lifecycle).hasSize(17);
            assertThat(lifecycle).startsWith("立项").endsWith("运维");
        }

        @Test
        @DisplayName("ownerUserId 为 null 时仍能返回(全局聚合视图)")
        void ownerUserIdNullable() {
            Map<String, Object> result = service.aggregate(null);
            assertThat(result.get("ownerUserId")).isNull();
            assertThat(result).containsKey("stats");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateDashboardNo
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateDashboardNo (DASH-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无工作台时,编号为 DASH-YYYY-0001")
        void firstOfYear() {
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getDashboardNo()).isEqualTo(String.format("DASH-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 5 个时,下一个编号为 0006")
        void nextSequence() {
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(5);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getDashboardNo()).isEqualTo(String.format("DASH-%d-0006", year));
        }

        @Test
        @DisplayName("撞号重试:DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(dashboardMapper.insertDashboard(any()))
                .thenThrow(new DuplicateKeyException("uk_dashboard_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertDashboard(sample);
                assertThat(rows).isEqualTo(1);
                verify(dashboardMapper, times(2)).insertDashboard(any());
            }
        }

        @Test
        @DisplayName("用户传了 dashboardNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setDashboardNo("USER-CUSTOM-001");
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            assertThat(sample.getDashboardNo()).isEqualTo("USER-CUSTOM-001");
            verify(dashboardMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §3.2)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertDashboard(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("工作台名称");
        }

        @Test
        @DisplayName("ownerUserId 必填,空抛 602")
        void ownerRequired() {
            sample.setOwnerUserId(null);
            assertThatThrownBy(() -> service.insertDashboard(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("所属用户");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值填充
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertDashboard)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 isDefault 时默认设为 N")
        void defaultIsDefaultN() {
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            assertThat(sample.getIsDefault()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 status 时默认设为 00")
        void defaultStatus00() {
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }

        @Test
        @DisplayName("未指定 refreshInterval 时默认 60 秒")
        void defaultRefreshInterval60() {
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            assertThat(sample.getRefreshInterval()).isEqualTo(60);
        }

        @Test
        @DisplayName("未指定 widgetTypes 时默认 stats,active_projects,my_todos")
        void defaultWidgetTypes() {
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            assertThat(sample.getWidgetTypes()).isEqualTo("stats,active_projects,my_todos");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 同用户默认唯一性
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("同用户默认工作台唯一性")
    class DefaultUniquenessTests {

        @Test
        @DisplayName("insertDashboard 当 isDefault=Y 时调用 clearDefaultForOwner")
        void insertSetsDefaultClearsOthers() {
            sample.setIsDefault("Y");
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            when(dashboardMapper.clearDefaultForOwner(1L)).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            verify(dashboardMapper).clearDefaultForOwner(1L);
        }

        @Test
        @DisplayName("insertDashboard 当 isDefault=N 时不调用 clearDefaultForOwner")
        void insertWithoutDefaultNoClear() {
            sample.setIsDefault("N");
            when(dashboardMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dashboardMapper.insertDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDashboard(sample);
            }
            verify(dashboardMapper, never()).clearDefaultForOwner(any());
        }

        @Test
        @DisplayName("updateDashboard 切换为默认时清同 owner 其他默认")
        void updateSwitchToDefaultClearsOthers() {
            Dashboard old = new Dashboard();
            old.setDashboardId(10L);
            old.setOwnerUserId(1L);
            old.setIsDefault("N");

            Dashboard update = new Dashboard();
            update.setDashboardId(10L);
            update.setIsDefault("Y");

            when(dashboardMapper.selectDashboardById(10L)).thenReturn(old);
            when(dashboardMapper.clearDefaultForOwner(1L)).thenReturn(1);
            when(dashboardMapper.updateDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDashboard(update);
            }
            verify(dashboardMapper).clearDefaultForOwner(1L);
        }

        @Test
        @DisplayName("updateDashboard 已是默认时不再 clear")
        void updateAlreadyDefaultNoClear() {
            Dashboard old = new Dashboard();
            old.setDashboardId(10L);
            old.setOwnerUserId(1L);
            old.setIsDefault("Y");

            Dashboard update = new Dashboard();
            update.setDashboardId(10L);
            update.setIsDefault("Y");
            update.setTitle("改名字");

            when(dashboardMapper.selectDashboardById(10L)).thenReturn(old);
            when(dashboardMapper.updateDashboard(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDashboard(update);
            }
            verify(dashboardMapper, never()).clearDefaultForOwner(any());
        }

        @Test
        @DisplayName("updateDashboard 工作台不存在抛 404")
        void updateNotFound() {
            Dashboard update = new Dashboard();
            update.setDashboardId(99L);
            when(dashboardMapper.selectDashboardById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateDashboard(update))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("工作台不存在");
        }
    }
}
