package cn.com.bosssfot.dv.plm.dora.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import cn.com.bosssfot.dv.plm.common.spi.DoraAggregationSource;
import cn.com.bosssfot.dv.plm.common.spi.DoraAggregationSource.DoraAggregationData;
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

    // ─────────────────────────────────────────────────────────────────────
    // Proposal 0028 P0-3B: computeMetrics 真聚合
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("computeMetrics — 真聚合 4 个 DORA 指标")
    class ComputeMetrics {

        /** 构造 30 天聚合窗口 */
        private Date[] window30d() {
            Date end = new Date();
            Date start = new Date(end.getTime() - TimeUnit.DAYS.toMillis(30));
            return new Date[] { start, end };
        }

        /** 反射注入 aggregationSources Map(@InjectMocks 不会注入这个字段) */
        private void injectSources(Map<String, DoraAggregationSource> map) {
            try {
                Field f = DoraMetricServiceImpl.class.getDeclaredField("aggregationSources");
                f.setAccessible(true);
                f.set(service, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private DoraAggregationSource sourceOf(String name, DoraAggregationData data) {
            return new DoraAggregationSource() {
                @Override public String entityType() { return name; }
                @Override public DoraAggregationData aggregate(Long pid, Date s, Date e) { return data; }
            };
        }

        @Test
        @DisplayName("4 个指标都算出 & is_computed='Y' / computedAt 设置 & 各自指标值正确")
        void testComputeOk_4_metrics() {
            DoraAggregationData pipe = new DoraAggregationData();
            pipe.deployCount = 60;          // 30 天 60 次成功 → 频率 2.00 次/天
            pipe.failedCount = 15;          // 总运行 75,失败率 20.00%
            pipe.totalRunCount = 75;

            DoraAggregationData rel = new DoraAggregationData();
            // 2 个发布,各耗 6 小时 → SUM = 12*3600000 ms,AVG = 6h
            rel.totalLeadTimeMs = BigDecimal.valueOf(12L * 3_600_000L);
            rel.leadTimeSampleCnt = 2;

            DoraAggregationData defect = new DoraAggregationData();
            // 3 个 P0/P1 缺陷,各耗 2 小时 → SUM = 6*3600000 ms,AVG = 2h
            defect.totalRecoverMs = BigDecimal.valueOf(6L * 3_600_000L);
            defect.recoverSampleCnt = 3;

            Map<String, DoraAggregationSource> map = new HashMap<>();
            map.put("pipeline", sourceOf("pipeline", pipe));
            map.put("release",  sourceOf("release",  rel));
            map.put("defect",   sourceOf("defect",   defect));
            injectSources(map);

            // 都不存在 → insert
            when(doraMapper.selectByProjectTypePeriod(any(), anyString(), any())).thenReturn(null);
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(0);
            when(doraMapper.insertDora(any())).thenReturn(1);

            Date[] w = window30d();
            List<DoraMetric> result = service.computeMetrics(100L, w[0], w[1]);

            assertThat(result).hasSize(4);
            // 顺序:deploy_freq → lead_time → mttr → change_fail_rate
            assertThat(result.get(0).getMetricType()).isEqualTo("deploy_freq");
            assertThat(result.get(0).getMetricValue()).isEqualByComparingTo("2.00");
            assertThat(result.get(1).getMetricType()).isEqualTo("lead_time");
            assertThat(result.get(1).getMetricValue()).isEqualByComparingTo("6.00");
            assertThat(result.get(2).getMetricType()).isEqualTo("mttr");
            assertThat(result.get(2).getMetricValue()).isEqualByComparingTo("2.00");
            assertThat(result.get(3).getMetricType()).isEqualTo("change_fail_rate");
            assertThat(result.get(3).getMetricValue()).isEqualByComparingTo("20.00");

            // 元数据
            for (DoraMetric m : result) {
                assertThat(m.getIsComputed()).isEqualTo("Y");
                assertThat(m.getComputedAt()).isNotNull();
                assertThat(m.getPeriodStart()).isEqualTo(w[0]);
                assertThat(m.getPeriodEnd()).isEqualTo(w[1]);
                assertThat(m.getPeriodDays()).isEqualTo(30);
                assertThat(m.getStatus()).isEqualTo("00");
            }
        }

        @Test
        @DisplayName("pipeline/release/defect 都无数据时 4 个指标均为 0")
        void testComputeNoData() {
            // 不注入任何 source → aggregationSources=null → 全部零值
            injectSources(null);

            when(doraMapper.selectByProjectTypePeriod(any(), anyString(), any())).thenReturn(null);
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(0);
            when(doraMapper.insertDora(any())).thenReturn(1);

            Date[] w = window30d();
            List<DoraMetric> result = service.computeMetrics(100L, w[0], w[1]);

            assertThat(result).hasSize(4);
            for (DoraMetric m : result) {
                assertThat(m.getMetricValue()).isEqualByComparingTo("0");
            }
        }

        @Test
        @DisplayName("已存在 is_computed='N'(人工录入)→ 跳过不动")
        void testComputeManualPreserved() {
            DoraAggregationData pipe = new DoraAggregationData();
            pipe.deployCount = 60;
            pipe.totalRunCount = 60;
            // 其它 source 无数据
            Map<String, DoraAggregationSource> map = new HashMap<>();
            map.put("pipeline", sourceOf("pipeline", pipe));
            injectSources(map);

            DoraMetric manual = new DoraMetric();
            manual.setDoraId(999L);
            manual.setMetricType("deploy_freq");
            manual.setMetricValue(BigDecimal.valueOf(99.99));   // 人工值
            manual.setIsComputed("N");

            // 只 deploy_freq 有人工值,其余 3 个均 null
            when(doraMapper.selectByProjectTypePeriod(any(), eq("deploy_freq"), any())).thenReturn(manual);
            when(doraMapper.selectByProjectTypePeriod(any(), eq("lead_time"), any())).thenReturn(null);
            when(doraMapper.selectByProjectTypePeriod(any(), eq("mttr"), any())).thenReturn(null);
            when(doraMapper.selectByProjectTypePeriod(any(), eq("change_fail_rate"), any())).thenReturn(null);
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(0);
            when(doraMapper.insertDora(any())).thenReturn(1);

            Date[] w = window30d();
            List<DoraMetric> result = service.computeMetrics(100L, w[0], w[1]);

            // deploy_freq 返回的是人工 manual,值仍 99.99,is_computed='N'
            assertThat(result.get(0).getMetricValue()).isEqualByComparingTo("99.99");
            assertThat(result.get(0).getIsComputed()).isEqualTo("N");
            // 没调 update / insert 覆盖人工的那条
            verify(doraMapper, never()).updateDora(argThat(m ->
                m != null && m.getDoraId() != null && m.getDoraId().equals(999L)));
        }

        @Test
        @DisplayName("已存在 is_computed='Y'(自动)→ update 覆盖")
        void testComputeUpsertReplacesOld() {
            DoraAggregationData pipe = new DoraAggregationData();
            pipe.deployCount = 30;        // 30/30 = 1.0
            pipe.totalRunCount = 30;
            Map<String, DoraAggregationSource> map = new HashMap<>();
            map.put("pipeline", sourceOf("pipeline", pipe));
            injectSources(map);

            DoraMetric oldComputed = new DoraMetric();
            oldComputed.setDoraId(500L);
            oldComputed.setMetricType("deploy_freq");
            oldComputed.setMetricValue(BigDecimal.valueOf(0.50));  // 老值
            oldComputed.setIsComputed("Y");

            when(doraMapper.selectByProjectTypePeriod(any(), eq("deploy_freq"), any())).thenReturn(oldComputed);
            when(doraMapper.selectByProjectTypePeriod(any(), eq("lead_time"), any())).thenReturn(null);
            when(doraMapper.selectByProjectTypePeriod(any(), eq("mttr"), any())).thenReturn(null);
            when(doraMapper.selectByProjectTypePeriod(any(), eq("change_fail_rate"), any())).thenReturn(null);
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(0);
            when(doraMapper.insertDora(any())).thenReturn(1);
            when(doraMapper.updateDora(any())).thenReturn(1);

            Date[] w = window30d();
            List<DoraMetric> result = service.computeMetrics(100L, w[0], w[1]);

            // deploy_freq 被覆盖
            DoraMetric updated = result.get(0);
            assertThat(updated.getDoraId()).isEqualTo(500L);
            assertThat(updated.getMetricValue()).isEqualByComparingTo("1.00");  // 30/30=1.00
            assertThat(updated.getIsComputed()).isEqualTo("Y");
            assertThat(updated.getUpdateBy()).isEqualTo("dora-compute");
            // 调到 update 一次(只 deploy_freq 是 update,其他 3 个走 insert)
            verify(doraMapper, times(1)).updateDora(any());
            verify(doraMapper, times(3)).insertDora(any());
        }

        @Test
        @DisplayName("MTTR 只算 severity ∈ {00 P0,01 P1} 的 defect — 由 source 实现保证")
        void testComputeMTTRFiltersSeverity() {
            // 这一条对 service 而言是 contract 测试:
            //   service 完全信任 source 返回的 totalRecoverMs / recoverSampleCnt,
            //   过滤 severity ∈ {00,01} AND status='03' 由 DefectMapper.sumRecoverMsInPeriod
            //   在 SQL 中保证(WHERE severity in ('00','01') and status='03')。
            // 这里验证:source 给一组"已过滤"数据,service 正确转 hour 单位。

            DoraAggregationData defect = new DoraAggregationData();
            // 4 个已过滤的 P0/P1 defect,总恢复 8 小时 → AVG = 2h
            defect.totalRecoverMs = BigDecimal.valueOf(8L * 3_600_000L);
            defect.recoverSampleCnt = 4;

            Map<String, DoraAggregationSource> map = new HashMap<>();
            map.put("defect", sourceOf("defect", defect));
            injectSources(map);

            when(doraMapper.selectByProjectTypePeriod(any(), anyString(), any())).thenReturn(null);
            when(doraMapper.selectMaxSeqOfYear(anyString())).thenReturn(0);
            when(doraMapper.insertDora(any())).thenReturn(1);

            Date[] w = window30d();
            List<DoraMetric> result = service.computeMetrics(100L, w[0], w[1]);

            DoraMetric mttr = result.get(2);
            assertThat(mttr.getMetricType()).isEqualTo("mttr");
            assertThat(mttr.getMetricValue()).isEqualByComparingTo("2.00");
            assertThat(mttr.getMetricUnit()).isEqualTo("小时");
        }

        @Test
        @DisplayName("projectId / 窗口 入参非法 → ServiceException")
        void testInvalidParams() {
            Date now = new Date();
            assertThatThrownBy(() -> service.computeMetrics(null, now, now))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("projectId");

            assertThatThrownBy(() -> service.computeMetrics(1L, null, now))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("窗口");

            Date earlier = new Date(now.getTime() - 1000);
            assertThatThrownBy(() -> service.computeMetrics(1L, now, earlier))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("periodEnd");
        }
    }
}
