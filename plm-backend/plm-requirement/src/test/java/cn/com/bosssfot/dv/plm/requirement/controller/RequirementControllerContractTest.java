package cn.com.bosssfot.dv.plm.requirement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementReviewService;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementService;

/**
 * RequirementController API 契约测试 — 同 [ProjectControllerContractTest] 范本。
 * 覆盖 6 核心 endpoint:list / getInfo / add / edit / remove / export。
 * 不覆盖额外 endpoint(/ai/evaluate, /reviews)— 留给单元测试 + E2E。
 * 注意:Controller 同时依赖 IRequirementReviewService,@InjectMocks 需提供 mock。
 */
@ExtendWith(MockitoExtension.class)
class RequirementControllerContractTest {

    @Mock
    private IRequirementService requirementService;

    @Mock
    private IRequirementReviewService requirementReviewService;

    @InjectMocks
    private RequirementController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new TestServiceExceptionAdvice())
            .build();
    }

    @RestControllerAdvice
    static class TestServiceExceptionAdvice {
        @ExceptionHandler(ServiceException.class)
        public AjaxResult handle(ServiceException e) {
            Integer code = e.getCode();
            return code != null ? AjaxResult.error(code, e.getMessage())
                                : AjaxResult.error(e.getMessage());
        }
    }

    @Nested
    @DisplayName("GET /business/requirement/list")
    class ListTests {
        @Test
        @DisplayName("[TC-Requirement-API-001] 空列表 → 200 + rows:[]")
        void listEmpty() throws Exception {
            when(requirementService.selectRequirementList(any())).thenReturn(List.of());
            mockMvc.perform(get("/business/requirement/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-Requirement-API-002] 带 projectId+status 筛选 → 透传 Service")
        void listWithFilter() throws Exception {
            Requirement r = new Requirement();
            r.setRequirementId(1L);
            r.setRequirementNo("REQ-2026-0001");
            r.setTitle("登录优化");
            r.setStatus("00");
            when(requirementService.selectRequirementList(any())).thenReturn(List.of(r));

            mockMvc.perform(get("/business/requirement/list")
                    .param("projectId", "1")
                    .param("status", "00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].requirementId").value(1))
                .andExpect(jsonPath("$.rows[0].title").value("登录优化"));
            verify(requirementService, times(1)).selectRequirementList(any(Requirement.class));
        }
    }

    @Nested
    @DisplayName("GET /business/requirement/{requirementId}")
    class GetInfoTests {
        @Test
        @DisplayName("[TC-Requirement-API-010] 存在的 id → 200 + data")
        void getInfoExists() throws Exception {
            Requirement r = new Requirement();
            r.setRequirementId(1L);
            r.setTitle("登录优化");
            when(requirementService.selectRequirementById(1L)).thenReturn(r);

            mockMvc.perform(get("/business/requirement/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requirementId").value(1))
                .andExpect(jsonPath("$.data.title").value("登录优化"));
        }

        @Test
        @DisplayName("[TC-Requirement-API-011] id 不存在 → 200 + 无 data 字段")
        void getInfoNotFound() throws Exception {
            when(requirementService.selectRequirementById(999L)).thenReturn(null);
            mockMvc.perform(get("/business/requirement/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("[TC-Requirement-API-012] id 非数字 → 400")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/requirement/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /business/requirement")
    class AddTests {
        @Test
        @DisplayName("[TC-Requirement-API-020] 合法新增 → 200")
        void addOk() throws Exception {
            when(requirementService.insertRequirement(any())).thenReturn(1);
            String body = """
                {"projectId":1,"title":"R1","priority":"high","status":"00"}
                """;
            mockMvc.perform(post("/business/requirement")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(requirementService).insertRequirement(any(Requirement.class));
        }

        @Test
        @DisplayName("[TC-Requirement-API-021] Service 抛 ServiceException(601) → 200 + code:601")
        void addValidationFails() throws Exception {
            when(requirementService.insertRequirement(any()))
                .thenThrow(new ServiceException("需求标题不能为空", 601));
            mockMvc.perform(post("/business/requirement")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(601))
                .andExpect(jsonPath("$.msg").value("需求标题不能为空"));
        }
    }

    @Nested
    @DisplayName("PUT /business/requirement")
    class EditTests {
        @Test
        @DisplayName("[TC-Requirement-API-030] 合法修改 → 200")
        void editOk() throws Exception {
            when(requirementService.updateRequirement(any())).thenReturn(1);
            String body = """
                {"requirementId":1,"status":"01"}
                """;
            mockMvc.perform(put("/business/requirement")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-Requirement-API-031] 非法状态转换 → 200 + code:701")
        void editIllegalState() throws Exception {
            when(requirementService.updateRequirement(any()))
                .thenThrow(new ServiceException("需求状态转换非法", 701));
            mockMvc.perform(put("/business/requirement")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"requirementId":1,"status":"99"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701));
        }
    }

    @Nested
    @DisplayName("DELETE /business/requirement/{requirementIds}")
    class RemoveTests {
        @Test
        @DisplayName("[TC-Requirement-API-040] 单 id → 200")
        void removeSingle() throws Exception {
            when(requirementService.deleteRequirementByIds(any())).thenReturn(1);
            mockMvc.perform(delete("/business/requirement/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(requirementService).deleteRequirementByIds(any(Long[].class));
        }

        @Test
        @DisplayName("[TC-Requirement-API-041] 批量 → Service 收 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(requirementService.deleteRequirementByIds(any())).thenReturn(3);
            mockMvc.perform(delete("/business/requirement/1,2,3"))
                .andExpect(status().isOk());
            verify(requirementService).deleteRequirementByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3 && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }

    @Nested
    @DisplayName("POST /business/requirement/export")
    class ExportTests {
        @Test
        @DisplayName("[TC-Requirement-API-050] 导出 → 200")
        void exportOk() throws Exception {
            when(requirementService.selectRequirementList(any())).thenReturn(List.of(new Requirement()));
            mockMvc.perform(post("/business/requirement/export"))
                .andExpect(status().isOk());
        }
    }
}
