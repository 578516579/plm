package cn.com.bosssfot.dv.plm.task.controller;

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
import cn.com.bosssfot.dv.plm.task.domain.Task;
import cn.com.bosssfot.dv.plm.task.service.ITaskService;

/**
 * TaskController API 契约测试 — 同 [ProjectControllerContractTest] / [SprintControllerContractTest] 范本。
 * 覆盖 6 核心 endpoint:list / getInfo / add / edit / remove / export。
 * 不覆盖额外 endpoint(/my, /kanban)— 留给单元测试 + E2E。
 */
@ExtendWith(MockitoExtension.class)
class TaskControllerContractTest {

    @Mock
    private ITaskService taskService;

    @InjectMocks
    private TaskController controller;

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
    @DisplayName("GET /business/task/list")
    class ListTests {
        @Test
        @DisplayName("[TC-Task-API-001] 空列表 → 200 + rows:[]")
        void listEmpty() throws Exception {
            when(taskService.selectTaskList(any())).thenReturn(List.of());
            mockMvc.perform(get("/business/task/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-Task-API-002] 带 projectId+status+priority 筛选 → 透传 Service")
        void listWithFilter() throws Exception {
            Task t = new Task();
            t.setTaskId(1L);
            t.setTaskNo("TSK-2026-0001");
            t.setTitle("修复登录");
            t.setStatus("01");
            t.setPriority("high");
            when(taskService.selectTaskList(any())).thenReturn(List.of(t));

            mockMvc.perform(get("/business/task/list")
                    .param("projectId", "1")
                    .param("status", "01")
                    .param("priority", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].taskId").value(1))
                .andExpect(jsonPath("$.rows[0].title").value("修复登录"))
                .andExpect(jsonPath("$.rows[0].status").value("01"));
            verify(taskService, times(1)).selectTaskList(any(Task.class));
        }
    }

    @Nested
    @DisplayName("GET /business/task/{taskId}")
    class GetInfoTests {
        @Test
        @DisplayName("[TC-Task-API-010] 存在的 id → 200 + data")
        void getInfoExists() throws Exception {
            Task t = new Task();
            t.setTaskId(1L);
            t.setTitle("修复登录");
            when(taskService.selectTaskById(1L)).thenReturn(t);

            mockMvc.perform(get("/business/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(1))
                .andExpect(jsonPath("$.data.title").value("修复登录"));
        }

        @Test
        @DisplayName("[TC-Task-API-011] id 不存在 → 200 + 无 data 字段")
        void getInfoNotFound() throws Exception {
            when(taskService.selectTaskById(999L)).thenReturn(null);
            mockMvc.perform(get("/business/task/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("[TC-Task-API-012] id 非数字 → 400")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/task/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /business/task")
    class AddTests {
        @Test
        @DisplayName("[TC-Task-API-020] 合法新增 → 200")
        void addOk() throws Exception {
            when(taskService.insertTask(any())).thenReturn(1);
            String body = """
                {"projectId":1,"title":"T1","status":"00","priority":"medium"}
                """;
            mockMvc.perform(post("/business/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(taskService).insertTask(any(Task.class));
        }

        @Test
        @DisplayName("[TC-Task-API-021] Service 抛 ServiceException(601) 字段校验失败 → 200 + code:601")
        void addValidationFails() throws Exception {
            when(taskService.insertTask(any()))
                .thenThrow(new ServiceException("任务标题不能为空", 601));
            mockMvc.perform(post("/business/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(601))
                .andExpect(jsonPath("$.msg").value("任务标题不能为空"));
        }
    }

    @Nested
    @DisplayName("PUT /business/task")
    class EditTests {
        @Test
        @DisplayName("[TC-Task-API-030] 合法状态转换 → 200")
        void editOk() throws Exception {
            when(taskService.updateTask(any())).thenReturn(1);
            String body = """
                {"taskId":1,"status":"02"}
                """;
            mockMvc.perform(put("/business/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-Task-API-031] 非法状态转换 → 200 + code:701")
        void editIllegalState() throws Exception {
            when(taskService.updateTask(any()))
                .thenThrow(new ServiceException("状态转换非法", 701));
            mockMvc.perform(put("/business/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"taskId":1,"status":"05"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701));
        }
    }

    @Nested
    @DisplayName("DELETE /business/task/{taskIds}")
    class RemoveTests {
        @Test
        @DisplayName("[TC-Task-API-040] 单 id → 200")
        void removeSingle() throws Exception {
            when(taskService.deleteTaskByIds(any())).thenReturn(1);
            mockMvc.perform(delete("/business/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(taskService).deleteTaskByIds(any(Long[].class));
        }

        @Test
        @DisplayName("[TC-Task-API-041] 批量 → Service 收 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(taskService.deleteTaskByIds(any())).thenReturn(3);
            mockMvc.perform(delete("/business/task/1,2,3"))
                .andExpect(status().isOk());
            verify(taskService).deleteTaskByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3 && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }

    @Nested
    @DisplayName("POST /business/task/export")
    class ExportTests {
        @Test
        @DisplayName("[TC-Task-API-050] 导出 → 200")
        void exportOk() throws Exception {
            when(taskService.selectTaskList(any())).thenReturn(List.of(new Task()));
            mockMvc.perform(post("/business/task/export"))
                .andExpect(status().isOk());
        }
    }
}
