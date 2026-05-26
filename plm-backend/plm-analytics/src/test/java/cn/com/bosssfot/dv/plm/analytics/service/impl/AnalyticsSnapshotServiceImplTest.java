package cn.com.bosssfot.dv.plm.analytics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import cn.com.bosssfot.dv.plm.analytics.domain.AnalyticsSnapshot;
import cn.com.bosssfot.dv.plm.analytics.mapper.AnalyticsSnapshotMapper;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;

/**
 * AnalyticsSnapshotServiceImpl 单元测试 — PRD §F6 + 原型 analytics.html
 *
 * 覆盖范围:
 *   - generateSnapshotNo: AS-YYYY-NNNN / 续号 / 用户传入保留 / 撞号重试
 *   - 校验: title / periodType / snapshotDate / authorUserId 必填 + period 白名单
 *   - 3 状态机: 00→01→02 (02 终态), 跳级非法
 *   - aiRecommend: 生成 aiRecommendations + aiGenerated=Y + chat 一次审计
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsSnapshotServiceImplTest {

    @Mock
    private AnalyticsSnapshotMapper analyticsMapper;

    @Mock
    private AiService aiService;

    @InjectMocks
    private AnalyticsSnapshotServiceImpl service;

    private AnalyticsSnapshot sample;

    @BeforeEach
    void setUp() {
        sample = new AnalyticsSnapshot();
        sample.setTitle("2026 Q2 研发效能快照");
        sample.setPeriodType("quarter");
        sample.setSnapshotDate(new Date());
        sample.setAuthorUserId(10L);
    }

    private AnalyticsSnapshot existing(String status) {
        AnalyticsSnapshot s = new AnalyticsSnapshot();
        s.setSnapshotId(1L);
        s.setTitle("旧快照");
        s.setStatus(status);
        s.setPeriodType("quarter");
        return s;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateSnapshotNo (AS-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateSnapshotNo (AS-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无快照,编号为 AS-YYYY-0001")
        void firstOfYear() {
            when(analyticsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(analyticsMapper.insertAnalytics(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAnalytics(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getSnapshotNo()).isEqualTo(String.format("AS-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 2 个,下一个为 0003")
        void nextSequence() {
            when(analyticsMapper.selectMaxSeqOfYear(anyString())).thenReturn(2);
            when(analyticsMapper.insertAnalytics(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAnalytics(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getSnapshotNo()).isEqualTo(String.format("AS-%d-0003", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(analyticsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null, 1);
            when(analyticsMapper.insertAnalytics(any()))
                .thenThrow(new DuplicateKeyException("uk_snapshot_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertAnalytics(sample);
                assertThat(rows).isEqualTo(1);
                verify(analyticsMapper, times(2)).insertAnalytics(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 snapshotNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setSnapshotNo("AS-CUSTOM-2099");
            when(analyticsMapper.insertAnalytics(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAnalytics(sample);
            }
            assertThat(sample.getSnapshotNo()).isEqualTo("AS-CUSTOM-2099");
            verify(analyticsMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 必填校验 + period 白名单 + 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertAnalytics — 校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("title 为空 → 602")
        void titleBlank() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertAnalytics(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("快照标题");
        }

        @Test
        @DisplayName("periodType 为空 → 602")
        void periodBlank() {
            sample.setPeriodType("");
            assertThatThrownBy(() -> service.insertAnalytics(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("快照周期");
        }

        @Test
        @DisplayName("snapshotDate 为空 → 602")
        void snapshotDateNull() {
            sample.setSnapshotDate(null);
            assertThatThrownBy(() -> service.insertAnalytics(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("快照日期");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertAnalytics(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("作者");
        }

        @Test
        @DisplayName("periodType 非白名单 → 604")
        void periodInvalid() {
            sample.setPeriodType("week");
            assertThatThrownBy(() -> service.insertAnalytics(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的周期");
        }

        @Test
        @DisplayName("默认 aiGenerated='N' / status='00'")
        void defaultsApplied() {
            when(analyticsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(analyticsMapper.insertAnalytics(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAnalytics(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3 状态机 (00→01→02, 02 终态)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 3 态 (00 草稿→01 已发布→02 已归档)")
    class StateMachineTests {

        @Test
        @DisplayName("00 → 01 合法")
        void legal_00_to_01() {
            when(analyticsMapper.selectAnalyticsById(1L)).thenReturn(existing("00"));
            when(analyticsMapper.updateAnalytics(any())).thenReturn(1);
            AnalyticsSnapshot upd = new AnalyticsSnapshot();
            upd.setSnapshotId(1L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAnalytics(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 → 02 合法")
        void legal_01_to_02() {
            when(analyticsMapper.selectAnalyticsById(1L)).thenReturn(existing("01"));
            when(analyticsMapper.updateAnalytics(any())).thenReturn(1);
            AnalyticsSnapshot upd = new AnalyticsSnapshot();
            upd.setSnapshotId(1L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAnalytics(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已归档 → 任意 非法 (终态)")
        void terminal_02_immutable() {
            when(analyticsMapper.selectAnalyticsById(1L)).thenReturn(existing("02"));
            AnalyticsSnapshot upd = new AnalyticsSnapshot();
            upd.setSnapshotId(1L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateAnalytics(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 → 02 跳级非法")
        void illegal_00_to_02() {
            when(analyticsMapper.selectAnalyticsById(1L)).thenReturn(existing("00"));
            AnalyticsSnapshot upd = new AnalyticsSnapshot();
            upd.setSnapshotId(1L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateAnalytics(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(analyticsMapper.selectAnalyticsById(99L)).thenReturn(null);
            AnalyticsSnapshot upd = new AnalyticsSnapshot();
            upd.setSnapshotId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateAnalytics(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("改 periodType 非白名单 → 604")
        void updatePeriodInvalid() {
            when(analyticsMapper.selectAnalyticsById(1L)).thenReturn(existing("00"));
            AnalyticsSnapshot upd = new AnalyticsSnapshot();
            upd.setSnapshotId(1L);
            upd.setPeriodType("decade");
            assertThatThrownBy(() -> service.updateAnalytics(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的周期");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiRecommend
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiRecommend (PRD §F6 AI 复盘)")
    class AiRecommendTests {

        @Test
        @DisplayName("正常 → aiRecommendations 非空 + aiGenerated=Y + aiGeneratedAt")
        void normalAiRecommend() {
            AnalyticsSnapshot a = new AnalyticsSnapshot();
            a.setSnapshotId(50L);
            a.setSnapshotNo("AS-2026-0001");
            a.setPeriodType("quarter");
            a.setSnapshotDate(new Date());
            when(analyticsMapper.selectAnalyticsById(50L)).thenReturn(a);
            when(analyticsMapper.updateAnalytics(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            AnalyticsSnapshot result = service.aiRecommend(50L);
            assertThat(result.getAiRecommendations())
                .isNotBlank()
                .contains("AI 复盘改进建议");
            assertThat(result.getAiGenerated()).isEqualTo("Y");
            assertThat(result.getAiGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("aiRecommend 不存在 → 404")
        void aiRecommendNotFound() {
            when(analyticsMapper.selectAnalyticsById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiRecommend(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("aiRecommend 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            AnalyticsSnapshot a = new AnalyticsSnapshot();
            a.setSnapshotId(60L);
            a.setSnapshotNo("AS-2026-0002");
            a.setPeriodType("month");
            a.setSnapshotDate(new Date());
            when(analyticsMapper.selectAnalyticsById(60L)).thenReturn(a);
            when(analyticsMapper.updateAnalytics(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            service.aiRecommend(60L);
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
            when(analyticsMapper.deleteAnalyticsByIds(any())).thenReturn(2);
            int rows = service.deleteAnalyticsByIds(new Long[] { 1L, 2L });
            assertThat(rows).isEqualTo(2);
        }
    }
}
