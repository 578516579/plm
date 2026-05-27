package cn.com.bosssfot.dv.plm.featureflag.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

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
import cn.com.bosssfot.dv.plm.featureflag.domain.FeatureFlag;
import cn.com.bosssfot.dv.plm.featureflag.mapper.FeatureFlagMapper;

/**
 * FeatureFlagServiceImpl 单元测试 — DevOps 扩展 + 原型 featureflag.html
 *
 * 覆盖范围:
 *   - generateFlagNo: FF-YYYY-NNNN / 流水续号 / 用户传入保留
 *   - 字段校验: flagKey / title / environment / authorUserId 必填 / env 白名单 / flagKey snake_case
 *   - 灰度策略 ↔ 百分比一致性: all_on=100 / all_off=0 / canary=1-99
 *   - 唯一键 (flagKey, environment): 双次撞键 → 701
 *   - isEnabled 灰度判定: all_on / all_off / canary 哈希 / status 关闭 / 不存在
 *   - update: 404 / env / strategy / percent 校验
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceImplTest {

    @Mock
    private FeatureFlagMapper flagMapper;

    @InjectMocks
    private FeatureFlagServiceImpl service;

    private FeatureFlag sample;

    @BeforeEach
    void setUp() {
        sample = new FeatureFlag();
        sample.setFlagKey("new_dashboard");
        sample.setTitle("新农情工作台灰度");
        sample.setEnvironment("staging");
        sample.setAuthorUserId(10L);
    }

    private FeatureFlag existing(String status, String strategy, int percent) {
        FeatureFlag f = new FeatureFlag();
        f.setFlagId(1L);
        f.setFlagKey("new_dashboard");
        f.setEnvironment("staging");
        f.setStatus(status);
        f.setRolloutStrategy(strategy);
        f.setRolloutPercentage(percent);
        return f;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateFlagNo (FF-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateFlagNo (FF-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无 flag,编号为 FF-YYYY-0001")
        void firstOfYear() {
            when(flagMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(flagMapper.insertFeatureFlag(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertFeatureFlag(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getFlagNo()).isEqualTo(String.format("FF-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 8 个,下一个为 0009")
        void nextSequence() {
            when(flagMapper.selectMaxSeqOfYear(anyString())).thenReturn(8);
            when(flagMapper.insertFeatureFlag(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertFeatureFlag(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getFlagNo()).isEqualTo(String.format("FF-%d-0009", year));
        }

        @Test
        @DisplayName("用户显式传入 flagNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setFlagNo("FF-CUSTOM-2099");
            when(flagMapper.insertFeatureFlag(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertFeatureFlag(sample);
            }
            assertThat(sample.getFlagNo()).isEqualTo("FF-CUSTOM-2099");
            verify(flagMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 必填校验 + 白名单 + snake_case
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertFeatureFlag — 校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("flagKey 为空 → 602")
        void flagKeyBlank() {
            sample.setFlagKey(null);
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Flag Key");
        }

        @Test
        @DisplayName("title 为空 → 602")
        void titleBlank() {
            sample.setTitle("");
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("功能说明");
        }

        @Test
        @DisplayName("environment 为空 → 602")
        void envBlank() {
            sample.setEnvironment(null);
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("环境");
        }

        @Test
        @DisplayName("environment 非白名单 → 604")
        void envInvalid() {
            sample.setEnvironment("uat");
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的环境");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("创建者");
        }

        @Test
        @DisplayName("flagKey 非 snake_case → 604")
        void flagKeyNotSnakeCase() {
            sample.setFlagKey("NewDashboard");
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("snake_case");
        }

        @Test
        @DisplayName("默认 rolloutStrategy=all_off / percentage=0 / status=01(关闭)")
        void defaultsApplied() {
            when(flagMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(flagMapper.insertFeatureFlag(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertFeatureFlag(sample);
            }
            assertThat(sample.getRolloutStrategy()).isEqualTo("all_off");
            assertThat(sample.getRolloutPercentage()).isZero();
            assertThat(sample.getStatus()).isEqualTo("01");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 灰度策略 ↔ 百分比 一致性
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("灰度策略 ↔ 百分比一致性")
    class StrategyPercentTests {

        @Test
        @DisplayName("all_on 策略百分比必须 100,否则 604")
        void allOnMustBe100() {
            sample.setRolloutStrategy("all_on");
            sample.setRolloutPercentage(50);
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("all_on");
        }

        @Test
        @DisplayName("all_off 策略百分比必须 0,否则 604")
        void allOffMustBe0() {
            sample.setRolloutStrategy("all_off");
            sample.setRolloutPercentage(10);
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("all_off");
        }

        @Test
        @DisplayName("canary 策略百分比必须 1-99,否则 604")
        void canaryMustBe1to99() {
            sample.setRolloutStrategy("canary");
            sample.setRolloutPercentage(0);
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("canary");
        }

        @Test
        @DisplayName("canary 20% 合法")
        void canary20Valid() {
            sample.setRolloutStrategy("canary");
            sample.setRolloutPercentage(20);
            when(flagMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(flagMapper.insertFeatureFlag(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertFeatureFlag(sample);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("rolloutPercentage 越界 (>100) → 604")
        void percentOutOfRange() {
            sample.setRolloutStrategy("canary");
            sample.setRolloutPercentage(150);
            assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("0-100");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 唯一键 (flagKey, environment)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("唯一键 (flagKey, environment)")
    class UniqueKeyTests {

        @Test
        @DisplayName("撞 no 一次,重试后成功")
        void retryOnceSucceeds() {
            when(flagMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(flagMapper.insertFeatureFlag(any()))
                .thenThrow(new DuplicateKeyException("uk_flag_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertFeatureFlag(sample);
                assertThat(rows).isEqualTo(1);
                verify(flagMapper, times(2)).insertFeatureFlag(any());
            }
        }

        @Test
        @DisplayName("双次撞键 (flagKey@env 真重复) → 701")
        void doubleDuplicateThrows701() {
            when(flagMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(flagMapper.insertFeatureFlag(any()))
                .thenThrow(new DuplicateKeyException("uk_flag_key_env"));
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThatThrownBy(() -> service.insertFeatureFlag(sample))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已存在");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // update
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateFeatureFlag")
    class UpdateTests {

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(flagMapper.selectFeatureFlagById(99L)).thenReturn(null);
            FeatureFlag upd = new FeatureFlag();
            upd.setFlagId(99L);
            assertThatThrownBy(() -> service.updateFeatureFlag(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("改 environment 非白名单 → 604")
        void updateEnvInvalid() {
            when(flagMapper.selectFeatureFlagById(1L)).thenReturn(existing("01", "all_off", 0));
            FeatureFlag upd = new FeatureFlag();
            upd.setFlagId(1L);
            upd.setEnvironment("dev_local");
            assertThatThrownBy(() -> service.updateFeatureFlag(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的环境");
        }

        @Test
        @DisplayName("改 strategy=canary 但 percent 沿用旧 0 → 604 (一致性)")
        void updateStrategyPercentInconsistent() {
            when(flagMapper.selectFeatureFlagById(1L)).thenReturn(existing("01", "all_off", 0));
            FeatureFlag upd = new FeatureFlag();
            upd.setFlagId(1L);
            upd.setRolloutStrategy("canary");
            assertThatThrownBy(() -> service.updateFeatureFlag(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("canary");
        }

        @Test
        @DisplayName("开启 all_on 100% 合法")
        void updateEnableAllOn() {
            when(flagMapper.selectFeatureFlagById(1L)).thenReturn(existing("01", "all_off", 0));
            when(flagMapper.updateFeatureFlag(any())).thenReturn(1);
            FeatureFlag upd = new FeatureFlag();
            upd.setFlagId(1L);
            upd.setStatus("00");
            upd.setRolloutStrategy("all_on");
            upd.setRolloutPercentage(100);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateFeatureFlag(upd);
                assertThat(rows).isEqualTo(1);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // isEnabled 灰度判定
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isEnabled 灰度判定")
    class IsEnabledTests {

        @Test
        @DisplayName("flag 不存在 → false")
        void notFoundDisabled() {
            when(flagMapper.selectFeatureFlagList(any())).thenReturn(List.of());
            assertThat(service.isEnabled("missing", "prod", 1L)).isFalse();
        }

        @Test
        @DisplayName("status 关闭 (01) → false")
        void statusClosedDisabled() {
            when(flagMapper.selectFeatureFlagList(any())).thenReturn(List.of(existing("01", "all_on", 100)));
            assertThat(service.isEnabled("new_dashboard", "staging", 1L)).isFalse();
        }

        @Test
        @DisplayName("all_on 且开启 → true")
        void allOnEnabled() {
            when(flagMapper.selectFeatureFlagList(any())).thenReturn(List.of(existing("00", "all_on", 100)));
            assertThat(service.isEnabled("new_dashboard", "staging", 1L)).isTrue();
        }

        @Test
        @DisplayName("all_off 且开启 → false")
        void allOffDisabled() {
            when(flagMapper.selectFeatureFlagList(any())).thenReturn(List.of(existing("00", "all_off", 0)));
            assertThat(service.isEnabled("new_dashboard", "staging", 1L)).isFalse();
        }

        @Test
        @DisplayName("canary 100% → 任何 userId 命中 true")
        void canaryFullRollout() {
            when(flagMapper.selectFeatureFlagList(any())).thenReturn(List.of(existing("00", "canary", 100)));
            assertThat(service.isEnabled("new_dashboard", "staging", 12345L)).isTrue();
        }

        @Test
        @DisplayName("canary 但 userId 为空 → false")
        void canaryNullUser() {
            when(flagMapper.selectFeatureFlagList(any())).thenReturn(List.of(existing("00", "canary", 50)));
            assertThat(service.isEnabled("new_dashboard", "staging", null)).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 删除
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("批量删除转发到 mapper")
        void deleteByIds() {
            when(flagMapper.deleteFeatureFlagByIds(any())).thenReturn(2);
            int rows = service.deleteFeatureFlagByIds(new Long[] { 1L, 2L });
            assertThat(rows).isEqualTo(2);
        }
    }
}
