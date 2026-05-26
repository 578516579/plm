package cn.com.bosssfot.dv.plm.dora.service.impl;

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

import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.dora.domain.DoraMetric;
import cn.com.bosssfot.dv.plm.dora.mapper.DoraMetricMapper;

/**
 * DoraMetricServiceImpl 单元测试 — DevOps 扩展 + 原型 devops.html
 *
 * 覆盖范围:
 *   - generateDoraNo: DORA-YYYY-NNNN / 续号 / 用户传入保留 / 撞号重试
 *   - 校验: metricName / metricType / metricValue / periodType / snapshotDate / authorUserId 必填
 *           + metricType / periodType 白名单 (604)
 *   - 3 状态机: 00→01→02 (02 终态), 跳级非法
 *   - aiSuggest: 生成 aiSuggestions + aiGenerated=Y + chat 一次审计
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class DoraMetricServiceImplTest {

    @Mock
    private DoraMetricMapper doraMapper;

    @Mock
    private AiService aiService;

    @InjectMocks
    private DoraMetricServiceImpl service;

    private DoraMetric sample;

    @BeforeEach
    void setUp() {
        sample = new DoraMetric();
        sample.setMetricName("部署频率");
        sample.setMetricType("deploy_freq");
        sample.setMetricValue(BigDecimal.valueOf(1.5));
        sample.setPeriodType("month");
        sample.setSnapshotDate(new Date());
        sample.setAuthorUserId(10L);
    }

    private DoraMetric existing(String status) {
        DoraMetric d = new DoraMetric();
        d.setDoraId(1L);
        d.setMetricName("旧指标");
        d.setMetricType("deploy_freq");
        d.setStatus(status);
        return d;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateDoraNo (DORA-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateDoraNo (DORA-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无指标,编号为 DORA-YYYY-0001")
        void firstOfYear() {
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(doraMapper.insertDora(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDora(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getDoraNo()).isEqualTo(String.format("DORA-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 6 个,下一个为 0007")
        void nextSequence() {
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(6);
            when(doraMapper.insertDora(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDora(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getDoraNo()).isEqualTo(String.format("DORA-%d-0007", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(null, 1);
            when(doraMapper.insertDora(any()))
                .thenThrow(new DuplicateKeyException("uk_dora_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertDora(sample);
                assertThat(rows).isEqualTo(1);
                verify(doraMapper, times(2)).insertDora(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 doraNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setDoraNo("DORA-CUSTOM-2099");
            when(doraMapper.insertDora(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDora(sample);
            }
            assertThat(sample.getDoraNo()).isEqualTo("DORA-CUSTOM-2099");
            verify(doraMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 必填校验 + 白名单 + 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertDora — 校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("metricName 为空 → 602")
        void nameBlank() {
            sample.setMetricName(null);
            assertThatThrownBy(() -> service.insertDora(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("指标名称");
        }

        @Test
        @DisplayName("metricType 为空 → 602")
        void typeBlank() {
            sample.setMetricType("");
            assertThatThrownBy(() -> service.insertDora(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("指标类型");
        }

        @Test
        @DisplayName("metricType 非白名单 → 604")
        void typeInvalid() {
            sample.setMetricType("cycle_time");
            assertThatThrownBy(() -> service.insertDora(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的指标类型");
        }

        @Test
        @DisplayName("metricValue 为空 → 602")
        void valueNull() {
            sample.setMetricValue(null);
            assertThatThrownBy(() -> service.insertDora(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("指标值");
        }

        @Test
        @DisplayName("periodType 非白名单 → 604")
        void periodInvalid() {
            sample.setPeriodType("year");
            assertThatThrownBy(() -> service.insertDora(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的周期");
        }

        @Test
        @DisplayName("snapshotDate 为空 → 602")
        void dateNull() {
            sample.setSnapshotDate(null);
            assertThatThrownBy(() -> service.insertDora(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("记录日期");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertDora(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("创建者");
        }

        @Test
        @DisplayName("默认 aiGenerated='N' / status='00'")
        void defaultsApplied() {
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(doraMapper.insertDora(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDora(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3 状态机
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 3 态 (00→01→02 终态)")
    class StateMachineTests {

        @Test
        @DisplayName("00 → 01 合法")
        void legal_00_to_01() {
            when(doraMapper.selectDoraById(1L)).thenReturn(existing("00"));
            when(doraMapper.updateDora(any())).thenReturn(1);
            DoraMetric upd = new DoraMetric();
            upd.setDoraId(1L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateDora(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 → 02 合法")
        void legal_01_to_02() {
            when(doraMapper.selectDoraById(1L)).thenReturn(existing("01"));
            when(doraMapper.updateDora(any())).thenReturn(1);
            DoraMetric upd = new DoraMetric();
            upd.setDoraId(1L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateDora(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 终态 → 任意 非法")
        void terminal_02_immutable() {
            when(doraMapper.selectDoraById(1L)).thenReturn(existing("02"));
            DoraMetric upd = new DoraMetric();
            upd.setDoraId(1L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateDora(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("转到");
        }

        @Test
        @DisplayName("00 → 02 跳级非法")
        void illegal_00_to_02() {
            when(doraMapper.selectDoraById(1L)).thenReturn(existing("00"));
            DoraMetric upd = new DoraMetric();
            upd.setDoraId(1L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateDora(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("转到");
        }

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(doraMapper.selectDoraById(99L)).thenReturn(null);
            DoraMetric upd = new DoraMetric();
            upd.setDoraId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateDora(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("改 metricType 非白名单 → 604")
        void updateTypeInvalid() {
            when(doraMapper.selectDoraById(1L)).thenReturn(existing("00"));
            DoraMetric upd = new DoraMetric();
            upd.setDoraId(1L);
            upd.setMetricType("bus_factor");
            assertThatThrownBy(() -> service.updateDora(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的指标类型");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiSuggest
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiSuggest (DORA 持续改进)")
    class AiSuggestTests {

        @Test
        @DisplayName("deploy_freq Elite → aiSuggestions 含等级评估 + aiGenerated=Y")
        void aiSuggestDeployFreq() {
            DoraMetric a = new DoraMetric();
            a.setDoraId(50L);
            a.setDoraNo("DORA-2026-0001");
            a.setMetricName("部署频率");
            a.setMetricType("deploy_freq");
            a.setMetricValue(BigDecimal.valueOf(2.0));
            a.setPeriodType("month");
            a.setSnapshotDate(new Date());
            when(doraMapper.selectDoraById(50L)).thenReturn(a);
            when(doraMapper.updateDora(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            DoraMetric result = service.aiSuggest(50L);
            assertThat(result.getAiSuggestions())
                .isNotBlank()
                .contains("DORA 持续改进建议")
                .contains("Elite");
            assertThat(result.getAiGenerated()).isEqualTo("Y");
            assertThat(result.getAiGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("aiSuggest 不存在 → 404")
        void aiSuggestNotFound() {
            when(doraMapper.selectDoraById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiSuggest(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("aiSuggest 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            DoraMetric a = new DoraMetric();
            a.setDoraId(60L);
            a.setDoraNo("DORA-2026-0002");
            a.setMetricName("MTTR");
            a.setMetricType("mttr");
            a.setMetricValue(BigDecimal.valueOf(2.5));
            a.setPeriodType("month");
            a.setSnapshotDate(new Date());
            when(doraMapper.selectDoraById(60L)).thenReturn(a);
            when(doraMapper.updateDora(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            service.aiSuggest(60L);
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
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
            when(doraMapper.deleteDoraByIds(any())).thenReturn(2);
            int rows = service.deleteDoraByIds(new Long[] { 1L, 2L });
            assertThat(rows).isEqualTo(2);
        }
    }
}
