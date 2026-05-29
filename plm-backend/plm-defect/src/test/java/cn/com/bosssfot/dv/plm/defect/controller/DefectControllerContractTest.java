package cn.com.bosssfot.dv.plm.defect.controller;

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
import cn.com.bosssfot.dv.plm.defect.domain.Defect;
import cn.com.bosssfot.dv.plm.defect.service.IDefectService;

/**
 * DefectController API 契约测试 — 同 [ProjectControllerContractTest] 范本。
 * 覆盖 6 核心 endpoint:list / getInfo / add / edit / remove / export。
 */
@ExtendWith(MockitoExtension.class)
class DefectControllerContractTest {

    @Mock
    private IDefectService defectService;

    @InjectMocks
    private DefectController controller;

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
    @DisplayName("GET /business/defect/list")
    class ListTests {
        @Test
        @DisplayName("[TC-Defect-API-001] 空列表 → 200 + rows:[]")
        void listEmpty() throws Exception {
            when(defectService.selectDefectList(any())).thenReturn(List.of());
            mockMvc.perform(get("/business/defect/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-Defect-API-002] 带 projectId+severity 筛选 → 透传 Service")
        void listWithFilter() throws Exception {
            Defect d = new Defect();
            d.setDefectId(1L);
            d.setDefectNo("DEF-2026-0001");
            d.setTitle("登录闪退");
            d.setSeverity("P0");
            d.setStatus("01");
            when(defectService.selectDefectList(any())).thenReturn(List.of(d));

            mockMvc.perform(get("/business/defect/list")
                    .param("projectId", "1")
                    .param("severity", "P0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].defectId").value(1))
                .andExpect(jsonPath("$.rows[0].title").value("登录闪退"))
                .andExpect(jsonPath("$.rows[0].severity").value("P0"));
            verify(defectService, times(1)).selectDefectList(any(Defect.class));
        }
    }

    @Nested
    @DisplayName("GET /business/defect/{defectId}")
    class GetInfoTests {
        @Test
        @DisplayName("[TC-Defect-API-010] 存在的 id → 200 + data")
        void getInfoExists() throws Exception {
            Defect d = new Defect();
            d.setDefectId(1L);
            d.setTitle("登录闪退");
            when(defectService.selectDefectById(1L)).thenReturn(d);

            mockMvc.perform(get("/business/defect/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defectId").value(1))
                .andExpect(jsonPath("$.data.title").value("登录闪退"));
        }

        @Test
        @DisplayName("[TC-Defect-API-011] id 不存在 → 200 + 无 data 字段")
        void getInfoNotFound() throws Exception {
            when(defectService.selectDefectById(999L)).thenReturn(null);
            mockMvc.perform(get("/business/defect/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("[TC-Defect-API-012] id 非数字 → 400")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/defect/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /business/defect")
    class AddTests {
        @Test
        @DisplayName("[TC-Defect-API-020] 合法新增 → 200")
        void addOk() throws Exception {
            when(defectService.insertDefect(any())).thenReturn(1);
            String body = """
                {"projectId":1,"title":"D1","severity":"P1","status":"01"}
                """;
            mockMvc.perform(post("/business/defect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(defectService).insertDefect(any(Defect.class));
        }

        @Test
        @DisplayName("[TC-Defect-API-021] Service 抛 ServiceException(601) → 200 + code:601")
        void addValidationFails() throws Exception {
            when(defectService.insertDefect(any()))
                .thenThrow(new ServiceException("缺陷标题不能为空", 601));
            mockMvc.perform(post("/business/defect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(601))
                .andExpect(jsonPath("$.msg").value("缺陷标题不能为空"));
        }
    }

    @Nested
    @DisplayName("PUT /business/defect")
    class EditTests {
        @Test
        @DisplayName("[TC-Defect-API-030] 合法修改 → 200")
        void editOk() throws Exception {
            when(defectService.updateDefect(any())).thenReturn(1);
            String body = """
                {"defectId":1,"status":"04"}
                """;
            mockMvc.perform(put("/business/defect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-Defect-API-031] 非法状态转换 → 200 + code:701")
        void editIllegalState() throws Exception {
            when(defectService.updateDefect(any()))
                .thenThrow(new ServiceException("缺陷状态转换非法", 701));
            mockMvc.perform(put("/business/defect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"defectId":1,"status":"99"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701));
        }
    }

    @Nested
    @DisplayName("DELETE /business/defect/{defectIds}")
    class RemoveTests {
        @Test
        @DisplayName("[TC-Defect-API-040] 单 id → 200")
        void removeSingle() throws Exception {
            when(defectService.deleteDefectByIds(any())).thenReturn(1);
            mockMvc.perform(delete("/business/defect/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(defectService).deleteDefectByIds(any(Long[].class));
        }

        @Test
        @DisplayName("[TC-Defect-API-041] 批量 → Service 收 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(defectService.deleteDefectByIds(any())).thenReturn(3);
            mockMvc.perform(delete("/business/defect/1,2,3"))
                .andExpect(status().isOk());
            verify(defectService).deleteDefectByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3 && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }

    @Nested
    @DisplayName("POST /business/defect/export")
    class ExportTests {
        @Test
        @DisplayName("[TC-Defect-API-050] 导出 → 200")
        void exportOk() throws Exception {
            when(defectService.selectDefectList(any())).thenReturn(List.of(new Defect()));
            mockMvc.perform(post("/business/defect/export"))
                .andExpect(status().isOk());
        }
    }
}
