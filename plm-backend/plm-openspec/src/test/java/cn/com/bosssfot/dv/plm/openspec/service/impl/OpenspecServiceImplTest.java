package cn.com.bosssfot.dv.plm.openspec.service.impl;

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

import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;
import cn.com.bosssfot.dv.plm.openspec.mapper.OpenspecMapper;

/**
 * OpenspecServiceImpl 单元测试 — PRD §F3.5 + 原型 aispec.html
 *
 * 覆盖范围:
 *   - generateOpenspecNo: SPEC-YYYY-NNNN / 续号 / 用户传入保留
 *   - 校验: specName / specType / version / authorUserId 必填 + specType 白名单
 *   - 唯一键 (specName, version): 双次撞键 → 701
 *   - 3 状态机: 00→01→02 (02 终态), 跳级非法
 *   - aiGenerate: openapi/asyncapi/ai_function/graphql 4 类 specContent + aiGenerated=Y + chat 一次
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class OpenspecServiceImplTest {

    @Mock
    private OpenspecMapper openspecMapper;

    @Mock
    private AiService aiService;

    @InjectMocks
    private OpenspecServiceImpl service;

    private Openspec sample;

    @BeforeEach
    void setUp() {
        sample = new Openspec();
        sample.setSpecName("土壤墒情查询规约");
        sample.setSpecType("openapi");
        sample.setVersion("1.0.0");
        sample.setAuthorUserId(10L);
    }

    private Openspec existing(String status) {
        Openspec o = new Openspec();
        o.setOpenspecId(1L);
        o.setSpecName("旧规约");
        o.setSpecType("openapi");
        o.setVersion("1.0.0");
        o.setStatus(status);
        return o;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateOpenspecNo (SPEC-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateOpenspecNo (SPEC-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无规约,编号为 SPEC-YYYY-0001")
        void firstOfYear() {
            when(openspecMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(openspecMapper.insertOpenspec(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertOpenspec(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getOpenspecNo()).isEqualTo(String.format("SPEC-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 3 个,下一个为 0004")
        void nextSequence() {
            when(openspecMapper.selectMaxSeqOfYear(anyString())).thenReturn(3);
            when(openspecMapper.insertOpenspec(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertOpenspec(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getOpenspecNo()).isEqualTo(String.format("SPEC-%d-0004", year));
        }

        @Test
        @DisplayName("用户显式传入 openspecNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setOpenspecNo("SPEC-CUSTOM-2099");
            when(openspecMapper.insertOpenspec(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertOpenspec(sample);
            }
            assertThat(sample.getOpenspecNo()).isEqualTo("SPEC-CUSTOM-2099");
            verify(openspecMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 必填校验 + specType 白名单 + 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertOpenspec — 校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("specName 为空 → 602")
        void specNameBlank() {
            sample.setSpecName(null);
            assertThatThrownBy(() -> service.insertOpenspec(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("规范名称");
        }

        @Test
        @DisplayName("specType 为空 → 602")
        void specTypeBlank() {
            sample.setSpecType("");
            assertThatThrownBy(() -> service.insertOpenspec(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("规范类型");
        }

        @Test
        @DisplayName("specType 非白名单 → 604")
        void specTypeInvalid() {
            sample.setSpecType("protobuf");
            assertThatThrownBy(() -> service.insertOpenspec(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的规范类型");
        }

        @Test
        @DisplayName("version 为空 → 602")
        void versionBlank() {
            sample.setVersion(null);
            assertThatThrownBy(() -> service.insertOpenspec(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("版本号");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertOpenspec(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("创建者");
        }

        @Test
        @DisplayName("默认 aiGenerated='N' / status='00'")
        void defaultsApplied() {
            when(openspecMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(openspecMapper.insertOpenspec(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertOpenspec(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
            assertThat(sample.getStatus()).isEqualTo("00");
        }

        @Test
        @DisplayName("4 类 specType 全合法 (openapi/asyncapi/ai_function/graphql)")
        void allTypesValid() {
            when(openspecMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(openspecMapper.insertOpenspec(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                for (String type : new String[] { "openapi", "asyncapi", "ai_function", "graphql" }) {
                    Openspec s = new Openspec();
                    s.setSpecName("规约-" + type);
                    s.setSpecType(type);
                    s.setVersion("1.0.0");
                    s.setAuthorUserId(10L);
                    assertThat(service.insertOpenspec(s)).isEqualTo(1);
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 唯一键 (specName, version)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("唯一键 (specName, version)")
    class UniqueKeyTests {

        @Test
        @DisplayName("撞 no 一次,重试后成功")
        void retryOnceSucceeds() {
            when(openspecMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(openspecMapper.insertOpenspec(any()))
                .thenThrow(new DuplicateKeyException("uk_openspec_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertOpenspec(sample);
                assertThat(rows).isEqualTo(1);
                verify(openspecMapper, times(2)).insertOpenspec(any());
            }
        }

        @Test
        @DisplayName("双次撞键 (specName+version 真重复) → 701")
        void doubleDuplicateThrows701() {
            when(openspecMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(openspecMapper.insertOpenspec(any()))
                .thenThrow(new DuplicateKeyException("uk_spec_name_version"));
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThatThrownBy(() -> service.insertOpenspec(sample))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已存在");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3 状态机 (00→01→02, 02 终态)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 3 态 (00→01→02, 02 终态)")
    class StateMachineTests {

        @Test
        @DisplayName("00 草稿 → 01 已发布 合法")
        void legal_00_to_01() {
            when(openspecMapper.selectOpenspecById(1L)).thenReturn(existing("00"));
            when(openspecMapper.updateOpenspec(any())).thenReturn(1);
            Openspec upd = new Openspec();
            upd.setOpenspecId(1L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateOpenspec(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 已发布 → 02 已弃用 合法")
        void legal_01_to_02() {
            when(openspecMapper.selectOpenspecById(1L)).thenReturn(existing("01"));
            when(openspecMapper.updateOpenspec(any())).thenReturn(1);
            Openspec upd = new Openspec();
            upd.setOpenspecId(1L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateOpenspec(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已弃用 → 任意 非法 (终态)")
        void terminal_02_immutable() {
            when(openspecMapper.selectOpenspecById(1L)).thenReturn(existing("02"));
            Openspec upd = new Openspec();
            upd.setOpenspecId(1L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateOpenspec(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("转到");
        }

        @Test
        @DisplayName("00 → 02 跳级非法")
        void illegal_00_to_02() {
            when(openspecMapper.selectOpenspecById(1L)).thenReturn(existing("00"));
            Openspec upd = new Openspec();
            upd.setOpenspecId(1L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateOpenspec(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("转到");
        }

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(openspecMapper.selectOpenspecById(99L)).thenReturn(null);
            Openspec upd = new Openspec();
            upd.setOpenspecId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateOpenspec(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("改 specType 非白名单 → 604")
        void updateSpecTypeInvalid() {
            when(openspecMapper.selectOpenspecById(1L)).thenReturn(existing("00"));
            Openspec upd = new Openspec();
            upd.setOpenspecId(1L);
            upd.setSpecType("thrift");
            assertThatThrownBy(() -> service.updateOpenspec(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无效的规范类型");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F3.5 Spec as Code)")
    class AiGenerateTests {

        @Test
        @DisplayName("openapi 类型 → specContent 含 openapi 3.1.0 + aiGenerated=Y")
        void aiGenerateOpenapi() {
            Openspec a = new Openspec();
            a.setOpenspecId(50L);
            a.setSpecName("土壤墒情查询规约");
            a.setSpecType("openapi");
            a.setVersion("1.0.0");
            when(openspecMapper.selectOpenspecById(50L)).thenReturn(a);
            when(openspecMapper.updateOpenspec(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            Openspec result = service.aiGenerate(50L);
            assertThat(result.getSpecContent())
                .isNotBlank()
                .contains("openapi: 3.1.0")
                .contains("土壤墒情查询规约");
            assertThat(result.getAiGenerated()).isEqualTo("Y");
            assertThat(result.getAiGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("graphql 类型 → specContent 含 GraphQL Schema")
        void aiGenerateGraphql() {
            Openspec a = new Openspec();
            a.setOpenspecId(51L);
            a.setSpecName("IoT 设备数据规约");
            a.setSpecType("graphql");
            a.setVersion("2.0.0");
            when(openspecMapper.selectOpenspecById(51L)).thenReturn(a);
            when(openspecMapper.updateOpenspec(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            Openspec result = service.aiGenerate(51L);
            assertThat(result.getSpecContent())
                .isNotBlank()
                .contains("type Query")
                .contains("SoilReading");
        }

        @Test
        @DisplayName("aiGenerate 不存在 → 404")
        void aiGenerateNotFound() {
            when(openspecMapper.selectOpenspecById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            Openspec a = new Openspec();
            a.setOpenspecId(60L);
            a.setSpecName("测试规约");
            a.setSpecType("ai_function");
            a.setVersion("1.0.0");
            when(openspecMapper.selectOpenspecById(60L)).thenReturn(a);
            when(openspecMapper.updateOpenspec(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            service.aiGenerate(60L);
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
            when(openspecMapper.deleteOpenspecByIds(any())).thenReturn(2);
            int rows = service.deleteOpenspecByIds(new Long[] { 1L, 2L });
            assertThat(rows).isEqualTo(2);
        }
    }
}
