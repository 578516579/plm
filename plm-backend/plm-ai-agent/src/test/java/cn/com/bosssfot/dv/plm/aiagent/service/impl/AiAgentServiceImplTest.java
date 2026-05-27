package cn.com.bosssfot.dv.plm.aiagent.service.impl;

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

import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;
import cn.com.bosssfot.dv.plm.aiagent.mapper.AiAgentMapper;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;

/**
 * AiAgentServiceImpl 单元测试 — PRD §F3.5 + 原型 aiagents.html
 *
 * 覆盖范围:
 *   - generateAgentNo: AGT-YYYY-NNNN / 续号 / 用户传入保留 / 撞号重试
 *   - 校验: agentName / agentType / authorUserId 必填 + agentType / provider 白名单
 *   - 3 状态机分支: 00→{01,02} / 01→{00} / 02→{00,01}, 01→02 非法
 *   - invoke: 不存在 404 / 非运行中 601 / 成功累加 totalCalls+successRate / 失败 708
 *   - buildChatRequest: 不存在 404 / 非运行中 601 / 正常返回 request
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class AiAgentServiceImplTest {

    @Mock
    private AiAgentMapper aiAgentMapper;

    @Mock
    private AiService aiService;

    @InjectMocks
    private AiAgentServiceImpl service;

    private AiAgent sample;

    @BeforeEach
    void setUp() {
        sample = new AiAgent();
        sample.setAgentName("需求分析 Agent");
        sample.setAgentType("requirement");
        sample.setAuthorUserId(10L);
        sample.setProvider("mock");
    }

    private AiAgent existing(String status) {
        AiAgent a = new AiAgent();
        a.setAgentId(1L);
        a.setAgentNo("AGT-2026-0001");
        a.setAgentName("需求分析 Agent");
        a.setAgentType("requirement");
        a.setProvider("mock");
        a.setStatus(status);
        a.setTotalCalls(0L);
        a.setSuccessRate(BigDecimal.ZERO);
        return a;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateAgentNo (AGT-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateAgentNo (AGT-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无 Agent,编号为 AGT-YYYY-0001")
        void firstOfYear() {
            when(aiAgentMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(aiAgentMapper.insertAiAgent(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAiAgent(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getAgentNo()).isEqualTo(String.format("AGT-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 5 个,下一个为 0006")
        void nextSequence() {
            when(aiAgentMapper.selectMaxSeqOfYear(anyString())).thenReturn(5);
            when(aiAgentMapper.insertAiAgent(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAiAgent(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getAgentNo()).isEqualTo(String.format("AGT-%d-0006", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(aiAgentMapper.selectMaxSeqOfYear(anyString())).thenReturn(null, 1);
            when(aiAgentMapper.insertAiAgent(any()))
                .thenThrow(new DuplicateKeyException("uk_agent_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertAiAgent(sample);
                assertThat(rows).isEqualTo(1);
                verify(aiAgentMapper, times(2)).insertAiAgent(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 agentNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setAgentNo("AGT-CUSTOM-2099");
            when(aiAgentMapper.insertAiAgent(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAiAgent(sample);
            }
            assertThat(sample.getAgentNo()).isEqualTo("AGT-CUSTOM-2099");
            verify(aiAgentMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 校验 + 白名单 + 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertAiAgent — 校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("agentName 为空 → 602")
        void nameBlank() {
            sample.setAgentName(null);
            assertThatThrownBy(() -> service.insertAiAgent(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Agent 名称");
        }

        @Test
        @DisplayName("agentType 为空 → 602")
        void typeBlank() {
            sample.setAgentType("");
            assertThatThrownBy(() -> service.insertAiAgent(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Agent 类型");
        }

        @Test
        @DisplayName("agentType 非白名单 → 604")
        void typeInvalid() {
            sample.setAgentType("marketing");
            assertThatThrownBy(() -> service.insertAiAgent(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的 Agent 类型");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertAiAgent(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("创建者");
        }

        @Test
        @DisplayName("provider 非白名单 → 604")
        void providerInvalid() {
            sample.setProvider("gemini");
            assertThatThrownBy(() -> service.insertAiAgent(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的 provider");
        }

        @Test
        @DisplayName("默认 provider=mock / totalCalls=0 / successRate=0 / status=00")
        void defaultsApplied() {
            sample.setProvider(null);
            when(aiAgentMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(aiAgentMapper.insertAiAgent(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAiAgent(sample);
            }
            assertThat(sample.getProvider()).isEqualTo("mock");
            assertThat(sample.getTotalCalls()).isZero();
            assertThat(sample.getSuccessRate()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3 状态机分支 (00→{01,02} / 01→{00} / 02→{00,01})
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机分支 (00 运行中 / 01 已停止 / 02 错误)")
    class StateMachineTests {

        @Test
        @DisplayName("00 运行中 → 01 已停止 合法")
        void legal_00_to_01() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("00"));
            when(aiAgentMapper.updateAiAgent(any())).thenReturn(1);
            AiAgent upd = new AiAgent();
            upd.setAgentId(1L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAiAgent(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("00 运行中 → 02 错误 合法")
        void legal_00_to_02() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("00"));
            when(aiAgentMapper.updateAiAgent(any())).thenReturn(1);
            AiAgent upd = new AiAgent();
            upd.setAgentId(1L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAiAgent(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 已停止 → 00 运行中 合法 (重启)")
        void legal_01_to_00() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("01"));
            when(aiAgentMapper.updateAiAgent(any())).thenReturn(1);
            AiAgent upd = new AiAgent();
            upd.setAgentId(1L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAiAgent(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 错误 → 01 已停止 合法")
        void legal_02_to_01() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("02"));
            when(aiAgentMapper.updateAiAgent(any())).thenReturn(1);
            AiAgent upd = new AiAgent();
            upd.setAgentId(1L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAiAgent(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 已停止 → 02 错误 非法")
        void illegal_01_to_02() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("01"));
            AiAgent upd = new AiAgent();
            upd.setAgentId(1L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateAiAgent(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("转到");
        }

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(aiAgentMapper.selectAiAgentById(99L)).thenReturn(null);
            AiAgent upd = new AiAgent();
            upd.setAgentId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateAiAgent(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("改 provider 非白名单 → 604")
        void updateProviderInvalid() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("00"));
            AiAgent upd = new AiAgent();
            upd.setAgentId(1L);
            upd.setProvider("cohere");
            assertThatThrownBy(() -> service.updateAiAgent(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的 provider");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // invoke (真实调用 + 统计 + 失败 708)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("invoke (AiService 路由 + 成功率统计)")
    class InvokeTests {

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(aiAgentMapper.selectAiAgentById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.invoke(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("非运行中 (status=01) → 601")
        void notRunningCannotInvoke() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("01"));
            assertThatThrownBy(() -> service.invoke(1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不可调用");
        }

        @Test
        @DisplayName("成功调用 → totalCalls+1 + successRate=100 + lastInvokedAt + chat 一次")
        void successInvoke() {
            AiAgent a = existing("00");
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(a);
            when(aiAgentMapper.updateAiAgent(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-model", "ok"));

            AiAgent result = service.invoke(1L);
            assertThat(result.getTotalCalls()).isEqualTo(1L);
            assertThat(result.getSuccessRate()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(result.getLastInvokedAt()).isNotNull();
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
        }

        @Test
        @DisplayName("AI 调用失败 → 708 (统计仍累加)")
        void failedInvokeThrows708() {
            AiAgent a = existing("00");
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(a);
            when(aiAgentMapper.updateAiAgent(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.fail("mock", "provider timeout"));

            assertThatThrownBy(() -> service.invoke(1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("AI 调用失败");
            verify(aiAgentMapper, times(1)).updateAiAgent(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // buildChatRequest
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buildChatRequest")
    class BuildChatRequestTests {

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(aiAgentMapper.selectAiAgentById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.buildChatRequest(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("非运行中 → 601")
        void notRunning() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("02"));
            assertThatThrownBy(() -> service.buildChatRequest(1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不可调用");
        }

        @Test
        @DisplayName("运行中 → 返回非空 AiChatRequest")
        void runningReturnsRequest() {
            when(aiAgentMapper.selectAiAgentById(1L)).thenReturn(existing("00"));
            AiChatRequest req = service.buildChatRequest(1L);
            assertThat(req).isNotNull();
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
            when(aiAgentMapper.deleteAiAgentByIds(any())).thenReturn(2);
            int rows = service.deleteAiAgentByIds(new Long[] { 1L, 2L });
            assertThat(rows).isEqualTo(2);
        }
    }
}
