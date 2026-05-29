package cn.com.bosssfot.dv.plm.testcase.controller;

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
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;
import cn.com.bosssfot.dv.plm.testcase.service.ITestCaseService;

/**
 * TestCaseController API 契约测试 — 同 [ProjectControllerContractTest] 范本。
 * 覆盖 6 核心 endpoint:list / getInfo / add / edit / remove / export。
 * 不覆盖额外 endpoint(/{id}/execute, /ai/generate)— 留给单元测试 + E2E。
 */
@ExtendWith(MockitoExtension.class)
class TestCaseControllerContractTest {

    @Mock
    private ITestCaseService testcaseService;

    @InjectMocks
    private TestCaseController controller;

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
    @DisplayName("GET /business/testcase/list")
    class ListTests {
        @Test
        @DisplayName("[TC-TestCase-API-001] 空列表 → 200 + rows:[]")
        void listEmpty() throws Exception {
            when(testcaseService.selectTestCaseList(any())).thenReturn(List.of());
            mockMvc.perform(get("/business/testcase/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-TestCase-API-002] 带 projectId+category 筛选 → 透传 Service")
        void listWithFilter() throws Exception {
            TestCase tc = new TestCase();
            tc.setTestcaseId(1L);
            tc.setTestcaseNo("TC-2026-0001");
            tc.setTitle("登录测试");
            tc.setStatus("01");
            when(testcaseService.selectTestCaseList(any())).thenReturn(List.of(tc));

            mockMvc.perform(get("/business/testcase/list")
                    .param("projectId", "1")
                    .param("category", "function"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].testcaseId").value(1))
                .andExpect(jsonPath("$.rows[0].title").value("登录测试"));
            verify(testcaseService, times(1)).selectTestCaseList(any(TestCase.class));
        }
    }

    @Nested
    @DisplayName("GET /business/testcase/{testcaseId}")
    class GetInfoTests {
        @Test
        @DisplayName("[TC-TestCase-API-010] 存在的 id → 200 + data")
        void getInfoExists() throws Exception {
            TestCase tc = new TestCase();
            tc.setTestcaseId(1L);
            tc.setTitle("登录测试");
            when(testcaseService.selectTestCaseById(1L)).thenReturn(tc);

            mockMvc.perform(get("/business/testcase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.testcaseId").value(1))
                .andExpect(jsonPath("$.data.title").value("登录测试"));
        }

        @Test
        @DisplayName("[TC-TestCase-API-011] id 不存在 → 200 + 无 data 字段")
        void getInfoNotFound() throws Exception {
            when(testcaseService.selectTestCaseById(999L)).thenReturn(null);
            mockMvc.perform(get("/business/testcase/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("[TC-TestCase-API-012] id 非数字 → 400")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/testcase/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /business/testcase")
    class AddTests {
        @Test
        @DisplayName("[TC-TestCase-API-020] 合法新增 → 200")
        void addOk() throws Exception {
            when(testcaseService.insertTestCase(any())).thenReturn(1);
            String body = """
                {"projectId":1,"title":"T1","category":"function","priority":"high"}
                """;
            mockMvc.perform(post("/business/testcase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(testcaseService).insertTestCase(any(TestCase.class));
        }

        @Test
        @DisplayName("[TC-TestCase-API-021] Service 抛 ServiceException(601) → 200 + code:601")
        void addValidationFails() throws Exception {
            when(testcaseService.insertTestCase(any()))
                .thenThrow(new ServiceException("用例标题不能为空", 601));
            mockMvc.perform(post("/business/testcase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(601))
                .andExpect(jsonPath("$.msg").value("用例标题不能为空"));
        }
    }

    @Nested
    @DisplayName("PUT /business/testcase")
    class EditTests {
        @Test
        @DisplayName("[TC-TestCase-API-030] 合法修改 → 200")
        void editOk() throws Exception {
            when(testcaseService.updateTestCase(any())).thenReturn(1);
            String body = """
                {"testcaseId":1,"status":"02"}
                """;
            mockMvc.perform(put("/business/testcase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-TestCase-API-031] 非法状态转换 → 200 + code:701")
        void editIllegalState() throws Exception {
            when(testcaseService.updateTestCase(any()))
                .thenThrow(new ServiceException("用例状态转换非法", 701));
            mockMvc.perform(put("/business/testcase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"testcaseId":1,"status":"99"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701));
        }
    }

    @Nested
    @DisplayName("DELETE /business/testcase/{testcaseIds}")
    class RemoveTests {
        @Test
        @DisplayName("[TC-TestCase-API-040] 单 id → 200")
        void removeSingle() throws Exception {
            when(testcaseService.deleteTestCaseByIds(any())).thenReturn(1);
            mockMvc.perform(delete("/business/testcase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(testcaseService).deleteTestCaseByIds(any(Long[].class));
        }

        @Test
        @DisplayName("[TC-TestCase-API-041] 批量 → Service 收 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(testcaseService.deleteTestCaseByIds(any())).thenReturn(3);
            mockMvc.perform(delete("/business/testcase/1,2,3"))
                .andExpect(status().isOk());
            verify(testcaseService).deleteTestCaseByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3 && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }

    @Nested
    @DisplayName("POST /business/testcase/export")
    class ExportTests {
        @Test
        @DisplayName("[TC-TestCase-API-050] 导出 → 200")
        void exportOk() throws Exception {
            when(testcaseService.selectTestCaseList(any())).thenReturn(List.of(new TestCase()));
            mockMvc.perform(post("/business/testcase/export"))
                .andExpect(status().isOk());
        }
    }
}
