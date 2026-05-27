package cn.com.bosssfot.dv.plm.apidesign.service.impl;

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

import cn.com.bosssfot.dv.plm.apidesign.domain.ApiDesign;
import cn.com.bosssfot.dv.plm.apidesign.mapper.ApiDesignMapper;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * ApiDesignServiceImpl 单元测试 — PRD §F3.3 + 原型 apidesign.html
 *
 * 覆盖范围(Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generateApiDesignNo: 格式 APID-YYYY-NNNN / 流水续号 / 撞号重试 1 次 / 用户传 No 时不自动生成
 *   - 字段校验: title 必填 / projectId 必填 / httpMethod 必填 / path 必填 / authorUserId 必填 / 关联项目存在性(702)
 *   - ENUM 白名单: httpMethod ∈ {GET,POST,PUT,DELETE,PATCH,HEAD,OPTIONS}(604)
 *   - method 规范化: 小写转大写 + 7 项全合法
 *   - 默认值填充: mockEnabled=N / aiGenerated=N / status='00' / 新建非草稿拒绝(601)
 *   - 4 状态机(含反向边 01→00): 00↔01 / 01→02 / 02→03 / 03 终态 / 跳级非法
 *   - 唯一键 (project_id, http_method, path) 冲突 → 701
 *   - aiGenerate: OpenAPI 3.0 YAML + JSON Schema + Mock 响应 + AiService 调用 / 不存在抛 404
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class ApiDesignServiceImplTest {

    @Mock
    private ApiDesignMapper apidesignMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ApiDesignServiceImpl service;

    private ApiDesign sample;

    @BeforeEach
    void setUp() {
        sample = new ApiDesign();
        sample.setTitle("用户登录");
        sample.setProjectId(10L);
        sample.setAuthorUserId(1L);
        sample.setHttpMethod("POST");
        sample.setPath("/api/v1/auth/login");
        sample.setDescription("JWT 登录端点");
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateApiDesignNo (ADR APID-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateApiDesignNo (APID-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无接口时,编号为 APID-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDesign(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getApidesignNo()).isEqualTo(String.format("APID-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 27 个时,下一个编号为 0028")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(27);
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDesign(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getApidesignNo()).isEqualTo(String.format("APID-%d-0028", year));
        }

        @Test
        @DisplayName("撞号重试: uk_apidesign_no DuplicateKey 后用新编号成功")
        void retryOnDuplicateNo() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(apidesignMapper.insertApiDesign(any()))
                .thenThrow(new DuplicateKeyException("uk_apidesign_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertApiDesign(sample);
                assertThat(rows).isEqualTo(1);
                verify(apidesignMapper, times(2)).insertApiDesign(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 apidesignNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setApidesignNo("APID-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDesign(sample);
            }
            assertThat(sample.getApidesignNo()).isEqualTo("APID-CUSTOM-2099");
            verify(apidesignMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + 关联项目存在性
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §F3.3)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("接口设计标题");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("httpMethod 必填,空抛 602")
        void httpMethodRequired() {
            sample.setHttpMethod(null);
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("HTTP 方法");
        }

        @Test
        @DisplayName("path 必填,空抛 602")
        void pathRequired() {
            sample.setPath(null);
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("接口路径");
        }

        @Test
        @DisplayName("authorUserId 必填,空抛 602")
        void authorRequired() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("设计者");
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(anyLong())).thenReturn(null);
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HTTP method ENUM 白名单 7 项 + 大小写规范化
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("HTTP method 白名单 + 规范化")
    class MethodWhitelistTests {

        @Test
        @DisplayName("httpMethod 非白名单 → 604")
        void methodOutOfWhitelist() {
            sample.setHttpMethod("CONNECT");
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("HTTP 方法仅支持");
        }

        @Test
        @DisplayName("httpMethod 全 7 项(GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS)合法")
        void allSevenMethodsAccepted() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            String[] methods = { "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS" };
            for (int i = 0; i < methods.length; i++) {
                ApiDesign a = new ApiDesign();
                a.setTitle("接口-" + i);
                a.setProjectId(10L);
                a.setAuthorUserId(1L);
                a.setHttpMethod(methods[i]);
                a.setPath("/api/v1/test/" + i);
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.insertApiDesign(a);
                }
                assertThat(a.getHttpMethod()).isEqualTo(methods[i]);
            }
        }

        @Test
        @DisplayName("httpMethod 小写传入自动转大写后接受 (post → POST)")
        void lowercaseNormalized() {
            sample.setHttpMethod("post");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDesign(sample);
            }
            assertThat(sample.getHttpMethod()).isEqualTo("POST");
        }

        @Test
        @DisplayName("update 时 httpMethod 非白名单抛 604")
        void updateMethodOutOfWhitelist() {
            ApiDesign old = new ApiDesign();
            old.setApidesignId(99L);
            old.setStatus("00");
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(old);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setHttpMethod("TRACE");
            assertThatThrownBy(() -> service.updateApiDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("HTTP 方法值非法");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值 + 新建非草稿拒绝
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertApiDesign)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 mockEnabled 默认 N")
        void defaultMockEnabledN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDesign(sample);
            }
            assertThat(sample.getMockEnabled()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 aiGenerated 默认 N")
        void defaultAiGeneratedN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDesign(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 status 默认 00 草稿")
        void defaultStatusDraft() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidesignMapper.insertApiDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDesign(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }

        @Test
        @DisplayName("新建接口设计 status 非 00 时拒绝 → 601")
        void newApiDesignMustBeDraft() {
            sample.setStatus("02");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertApiDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4 状态机 (含反向边 01→00, apidesign 独有)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 4 态含反向边 (PRD §F3.3)")
    class StateMachineTests {

        private ApiDesign withStatus(String s) {
            ApiDesign a = new ApiDesign();
            a.setApidesignId(99L);
            a.setStatus(s);
            a.setProjectId(10L);
            return a;
        }

        @Test
        @DisplayName("00 草稿 → 01 评审中 合法")
        void draftToReviewing() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("00"));
            when(apidesignMapper.updateApiDesign(any())).thenReturn(1);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateApiDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 00 草稿 反向打回合法 (apidesign 独有反向边)")
        void reviewingBackToDraft() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("01"));
            when(apidesignMapper.updateApiDesign(any())).thenReturn(1);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateApiDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 02 已确认 合法")
        void reviewingToConfirmed() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("01"));
            when(apidesignMapper.updateApiDesign(any())).thenReturn(1);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateApiDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已确认 → 03 已废弃 合法")
        void confirmedToDeprecated() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("02"));
            when(apidesignMapper.updateApiDesign(any())).thenReturn(1);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateApiDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("03 已废弃 → 任何状态 非法 (终态保护)")
        void deprecatedIsTerminal() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("03"));
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateApiDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 02 已确认 跳级非法")
        void draftCannotJumpToConfirmed() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("00"));
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateApiDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 03 已废弃 跳级非法")
        void draftCannotJumpToDeprecated() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("00"));
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("03");
            assertThatThrownBy(() -> service.updateApiDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("02 已确认 → 00 草稿 非法 (确认后不可回打回)")
        void confirmedCannotReverseToDraft() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("02"));
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("00");
            assertThatThrownBy(() -> service.updateApiDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(withStatus("03"));
            when(apidesignMapper.updateApiDesign(any())).thenReturn(1);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setStatus("03");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateApiDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时接口不存在抛 404")
        void updateNotFound() {
            when(apidesignMapper.selectApiDesignById(404L)).thenReturn(null);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(404L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateApiDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("接口设计不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在抛 702")
        void updateProjectIdNotFound() {
            ApiDesign old = withStatus("00");
            old.setProjectId(10L);
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(old);
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updateApiDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 唯一键 (project_id, http_method, path) → 701 (apidesign 独有业务唯一键)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("唯一键 (project_id, http_method, path) → 701")
    class UniqueKeyTests {

        @Test
        @DisplayName("insert 撞 method+path 唯一键 → 701")
        void insertDuplicateMethodPath() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(apidesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidesignMapper.insertApiDesign(any()))
                .thenThrow(new DuplicateKeyException(
                    "Duplicate entry '10-POST-/api/v1/auth/login' for key 'uk_apidesign_project_method_path'"));
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThatThrownBy(() -> service.insertApiDesign(sample))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已存在相同 method+path");
            }
        }

        @Test
        @DisplayName("update 撞 method+path 唯一键 → 701")
        void updateDuplicateMethodPath() {
            ApiDesign old = new ApiDesign();
            old.setApidesignId(99L);
            old.setStatus("00");
            old.setProjectId(10L);
            when(apidesignMapper.selectApiDesignById(99L)).thenReturn(old);
            when(apidesignMapper.updateApiDesign(any()))
                .thenThrow(new DuplicateKeyException(
                    "Duplicate entry for key 'uk_apidesign_project_method_path'"));
            ApiDesign upd = new ApiDesign();
            upd.setApidesignId(99L);
            upd.setPath("/api/v1/auth/login");
            upd.setHttpMethod("POST");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThatThrownBy(() -> service.updateApiDesign(upd))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已存在相同 method+path");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate (PRD §F3.3 AI OpenAPI YAML 生成)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F3.3 AI OpenAPI 3.0)")
    class AiGenerateTests {

        @Test
        @DisplayName("正常生成 → OpenAPI YAML + JSON Schema + Mock + aiGenerated=Y / 时间填充")
        void normalAiGenerate() {
            ApiDesign a = new ApiDesign();
            a.setApidesignId(50L);
            a.setTitle("用户登录");
            a.setHttpMethod("POST");
            a.setPath("/api/v1/auth/login");
            a.setDescription("JWT 登录端点");
            when(apidesignMapper.selectApiDesignById(50L)).thenReturn(a);
            when(apidesignMapper.updateApiDesign(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                ApiDesign result = service.aiGenerate(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getOpenapiSpec())
                    .contains("openapi: 3.0.3")
                    .contains("用户登录")
                    .contains("post:")
                    .contains("/api/v1/auth/login");
                assertThat(result.getRequestSchema()).contains("\"type\":\"object\"");
                assertThat(result.getResponseSchema()).contains("\"code\"");
                assertThat(result.getMockResponse()).contains("\"code\":200");
                assertThat(result.getAiGeneratedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("aiGenerate 时接口不存在抛 404")
        void aiGenerateNotFound() {
            when(apidesignMapper.selectApiDesignById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("接口设计不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次(审计联动)")
        void aiServiceCalledOnce() {
            ApiDesign a = new ApiDesign();
            a.setApidesignId(60L);
            a.setTitle("查询订单");
            a.setHttpMethod("GET");
            a.setPath("/api/v1/orders");
            when(apidesignMapper.selectApiDesignById(60L)).thenReturn(a);
            when(apidesignMapper.updateApiDesign(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.aiGenerate(60L);
            }
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 删除
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("批量删除 (deleteApiDesignByIds)")
    class DeleteTests {

        @Test
        @DisplayName("批量删除转发 mapper")
        void deleteForwards() {
            when(apidesignMapper.deleteApiDesignByIds(any(Long[].class))).thenReturn(3);
            int rows = service.deleteApiDesignByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
            verify(apidesignMapper, times(1)).deleteApiDesignByIds(any(Long[].class));
        }
    }
}
