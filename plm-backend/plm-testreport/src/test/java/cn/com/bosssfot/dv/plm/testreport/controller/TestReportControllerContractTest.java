package cn.com.bosssfot.dv.plm.testreport.controller;

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
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;
import cn.com.bosssfot.dv.plm.testreport.service.ITestReportService;

/**
 * TestReportController API 契约测试 — 同 [ProjectControllerContractTest] 范本。
 * 覆盖 6 核心 endpoint:list / getInfo / add / edit / remove / export。
 * 不覆盖额外 endpoint(/{id}/refresh-aggregate)— 留给单元测试。
 * 注意:Controller PathVariable 用 {id}/{ids},Service 参数仍叫 testreportId。
 */
@ExtendWith(MockitoExtension.class)
class TestReportControllerContractTest {

    @Mock
    private ITestReportService testreportService;

    @InjectMocks
    private TestReportController controller;

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
    @DisplayName("GET /business/testreport/list")
    class ListTests {
        @Test
        @DisplayName("[TC-TestReport-API-001] 空列表 → 200 + rows:[]")
        void listEmpty() throws Exception {
            when(testreportService.selectTestReportList(any())).thenReturn(List.of());
            mockMvc.perform(get("/business/testreport/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-TestReport-API-002] 带 projectId+status 筛选 → 透传 Service")
        void listWithFilter() throws Exception {
            TestReport tr = new TestReport();
            tr.setTestreportId(1L);
            tr.setTestreportNo("TR-2026-0001");
            tr.setTitle("Sprint-1 测试报告");
            tr.setStatus("01");
            when(testreportService.selectTestReportList(any())).thenReturn(List.of(tr));

            mockMvc.perform(get("/business/testreport/list")
                    .param("projectId", "1")
                    .param("status", "01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].testreportId").value(1))
                .andExpect(jsonPath("$.rows[0].title").value("Sprint-1 测试报告"));
            verify(testreportService, times(1)).selectTestReportList(any(TestReport.class));
        }
    }

    @Nested
    @DisplayName("GET /business/testreport/{id}")
    class GetInfoTests {
        @Test
        @DisplayName("[TC-TestReport-API-010] 存在的 id → 200 + data")
        void getInfoExists() throws Exception {
            TestReport tr = new TestReport();
            tr.setTestreportId(1L);
            tr.setTitle("Sprint-1 测试报告");
            when(testreportService.selectTestReportById(1L)).thenReturn(tr);

            mockMvc.perform(get("/business/testreport/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.testreportId").value(1))
                .andExpect(jsonPath("$.data.title").value("Sprint-1 测试报告"));
        }

        @Test
        @DisplayName("[TC-TestReport-API-011] id 不存在 → 200 + 无 data 字段")
        void getInfoNotFound() throws Exception {
            when(testreportService.selectTestReportById(999L)).thenReturn(null);
            mockMvc.perform(get("/business/testreport/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("[TC-TestReport-API-012] id 非数字 → 400")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/testreport/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /business/testreport")
    class AddTests {
        @Test
        @DisplayName("[TC-TestReport-API-020] 合法新增 → 200")
        void addOk() throws Exception {
            when(testreportService.insertTestReport(any())).thenReturn(1);
            String body = """
                {"projectId":1,"title":"TR1","status":"00"}
                """;
            mockMvc.perform(post("/business/testreport")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(testreportService).insertTestReport(any(TestReport.class));
        }

        @Test
        @DisplayName("[TC-TestReport-API-021] Service 抛 ServiceException(601) → 200 + code:601")
        void addValidationFails() throws Exception {
            when(testreportService.insertTestReport(any()))
                .thenThrow(new ServiceException("测试报告标题不能为空", 601));
            mockMvc.perform(post("/business/testreport")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(601))
                .andExpect(jsonPath("$.msg").value("测试报告标题不能为空"));
        }
    }

    @Nested
    @DisplayName("PUT /business/testreport")
    class EditTests {
        @Test
        @DisplayName("[TC-TestReport-API-030] 合法修改 → 200")
        void editOk() throws Exception {
            when(testreportService.updateTestReport(any())).thenReturn(1);
            String body = """
                {"testreportId":1,"status":"01"}
                """;
            mockMvc.perform(put("/business/testreport")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-TestReport-API-031] 非法状态转换 → 200 + code:701")
        void editIllegalState() throws Exception {
            when(testreportService.updateTestReport(any()))
                .thenThrow(new ServiceException("测试报告状态转换非法", 701));
            mockMvc.perform(put("/business/testreport")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"testreportId":1,"status":"99"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701));
        }
    }

    @Nested
    @DisplayName("DELETE /business/testreport/{ids}")
    class RemoveTests {
        @Test
        @DisplayName("[TC-TestReport-API-040] 单 id → 200")
        void removeSingle() throws Exception {
            when(testreportService.deleteTestReportByIds(any())).thenReturn(1);
            mockMvc.perform(delete("/business/testreport/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(testreportService).deleteTestReportByIds(any(Long[].class));
        }

        @Test
        @DisplayName("[TC-TestReport-API-041] 批量 → Service 收 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(testreportService.deleteTestReportByIds(any())).thenReturn(3);
            mockMvc.perform(delete("/business/testreport/1,2,3"))
                .andExpect(status().isOk());
            verify(testreportService).deleteTestReportByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3 && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }

    @Nested
    @DisplayName("POST /business/testreport/export")
    class ExportTests {
        @Test
        @DisplayName("[TC-TestReport-API-050] 导出 → 200")
        void exportOk() throws Exception {
            when(testreportService.selectTestReportList(any())).thenReturn(List.of(new TestReport()));
            mockMvc.perform(post("/business/testreport/export"))
                .andExpect(status().isOk());
        }
    }
}
