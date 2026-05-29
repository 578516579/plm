package cn.com.bosssfot.dv.plm.prd.controller;

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
import cn.com.bosssfot.dv.plm.prd.domain.Prd;
import cn.com.bosssfot.dv.plm.prd.service.IPrdService;

/**
 * PrdController API 契约测试 — 同 [ProjectControllerContractTest] 范本。
 * 覆盖 6 核心 endpoint:list / getInfo / add / edit / remove / export。
 * 不覆盖额外 endpoint(/ai/generate)— 留给单元测试 + E2E。
 * 注意:Controller PathVariable 用 {id}/{ids},Service 参数仍叫 prdId/prdIds。
 */
@ExtendWith(MockitoExtension.class)
class PrdControllerContractTest {

    @Mock
    private IPrdService prdService;

    @InjectMocks
    private PrdController controller;

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
    @DisplayName("GET /business/prd/list")
    class ListTests {
        @Test
        @DisplayName("[TC-Prd-API-001] 空列表 → 200 + rows:[]")
        void listEmpty() throws Exception {
            when(prdService.selectPrdList(any())).thenReturn(List.of());
            mockMvc.perform(get("/business/prd/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-Prd-API-002] 带 projectId+status 筛选 → 透传 Service")
        void listWithFilter() throws Exception {
            Prd p = new Prd();
            p.setPrdId(1L);
            p.setPrdNo("PRD-2026-0001");
            p.setTitle("登录 PRD");
            p.setStatus("00");
            when(prdService.selectPrdList(any())).thenReturn(List.of(p));

            mockMvc.perform(get("/business/prd/list")
                    .param("projectId", "1")
                    .param("status", "00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].prdId").value(1))
                .andExpect(jsonPath("$.rows[0].title").value("登录 PRD"));
            verify(prdService, times(1)).selectPrdList(any(Prd.class));
        }
    }

    @Nested
    @DisplayName("GET /business/prd/{id}")
    class GetInfoTests {
        @Test
        @DisplayName("[TC-Prd-API-010] 存在的 id → 200 + data")
        void getInfoExists() throws Exception {
            Prd p = new Prd();
            p.setPrdId(1L);
            p.setTitle("登录 PRD");
            when(prdService.selectPrdById(1L)).thenReturn(p);

            mockMvc.perform(get("/business/prd/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prdId").value(1))
                .andExpect(jsonPath("$.data.title").value("登录 PRD"));
        }

        @Test
        @DisplayName("[TC-Prd-API-011] id 不存在 → 200 + 无 data 字段")
        void getInfoNotFound() throws Exception {
            when(prdService.selectPrdById(999L)).thenReturn(null);
            mockMvc.perform(get("/business/prd/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("[TC-Prd-API-012] id 非数字 → 400")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/prd/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /business/prd")
    class AddTests {
        @Test
        @DisplayName("[TC-Prd-API-020] 合法新增 → 200")
        void addOk() throws Exception {
            when(prdService.insertPrd(any())).thenReturn(1);
            String body = """
                {"projectId":1,"title":"P1","status":"00"}
                """;
            mockMvc.perform(post("/business/prd")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(prdService).insertPrd(any(Prd.class));
        }

        @Test
        @DisplayName("[TC-Prd-API-021] Service 抛 ServiceException(601) → 200 + code:601")
        void addValidationFails() throws Exception {
            when(prdService.insertPrd(any()))
                .thenThrow(new ServiceException("PRD 标题不能为空", 601));
            mockMvc.perform(post("/business/prd")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(601))
                .andExpect(jsonPath("$.msg").value("PRD 标题不能为空"));
        }
    }

    @Nested
    @DisplayName("PUT /business/prd")
    class EditTests {
        @Test
        @DisplayName("[TC-Prd-API-030] 合法修改 → 200")
        void editOk() throws Exception {
            when(prdService.updatePrd(any())).thenReturn(1);
            String body = """
                {"prdId":1,"status":"01"}
                """;
            mockMvc.perform(put("/business/prd")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-Prd-API-031] 非法状态转换 → 200 + code:701")
        void editIllegalState() throws Exception {
            when(prdService.updatePrd(any()))
                .thenThrow(new ServiceException("PRD 状态转换非法", 701));
            mockMvc.perform(put("/business/prd")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"prdId":1,"status":"99"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701));
        }
    }

    @Nested
    @DisplayName("DELETE /business/prd/{ids}")
    class RemoveTests {
        @Test
        @DisplayName("[TC-Prd-API-040] 单 id → 200")
        void removeSingle() throws Exception {
            when(prdService.deletePrdByIds(any())).thenReturn(1);
            mockMvc.perform(delete("/business/prd/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(prdService).deletePrdByIds(any(Long[].class));
        }

        @Test
        @DisplayName("[TC-Prd-API-041] 批量 → Service 收 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(prdService.deletePrdByIds(any())).thenReturn(3);
            mockMvc.perform(delete("/business/prd/1,2,3"))
                .andExpect(status().isOk());
            verify(prdService).deletePrdByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3 && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }

    @Nested
    @DisplayName("POST /business/prd/export")
    class ExportTests {
        @Test
        @DisplayName("[TC-Prd-API-050] 导出 → 200")
        void exportOk() throws Exception {
            when(prdService.selectPrdList(any())).thenReturn(List.of(new Prd()));
            mockMvc.perform(post("/business/prd/export"))
                .andExpect(status().isOk());
        }
    }
}
