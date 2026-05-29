package cn.com.bosssfot.dv.plm.sprint.controller;

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
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;
import cn.com.bosssfot.dv.plm.sprint.service.ISprintService;

/**
 * SprintController API 契约测试 — 同 [ProjectControllerContractTest] 范本。
 * 覆盖 6 核心 endpoint:list / getInfo / add / edit / remove / export。
 * 不覆盖额外 endpoint(/current, /{id}/stats)— 留给单元测试 + E2E。
 */
@ExtendWith(MockitoExtension.class)
class SprintControllerContractTest {

    @Mock
    private ISprintService sprintService;

    @InjectMocks
    private SprintController controller;

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
    @DisplayName("GET /business/sprint/list")
    class ListTests {
        @Test
        @DisplayName("[TC-Sprint-API-001] 空列表 → 200 + rows:[]")
        void listEmpty() throws Exception {
            when(sprintService.selectSprintList(any())).thenReturn(List.of());
            mockMvc.perform(get("/business/sprint/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-Sprint-API-002] 带 projectId+status 筛选 → 透传 Service")
        void listWithFilter() throws Exception {
            Sprint s = new Sprint();
            s.setSprintId(1L);
            s.setName("Sprint-1");
            s.setStatus("1");
            when(sprintService.selectSprintList(any())).thenReturn(List.of(s));

            mockMvc.perform(get("/business/sprint/list")
                    .param("projectId", "1")
                    .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].name").value("Sprint-1"));
            verify(sprintService, times(1)).selectSprintList(any(Sprint.class));
        }
    }

    @Nested
    @DisplayName("GET /business/sprint/{sprintId}")
    class GetInfoTests {
        @Test
        @DisplayName("[TC-Sprint-API-010] 存在的 id → 200 + data")
        void getInfoExists() throws Exception {
            Sprint s = new Sprint();
            s.setSprintId(1L);
            s.setName("Sprint-1");
            when(sprintService.selectSprintById(1L)).thenReturn(s);

            mockMvc.perform(get("/business/sprint/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sprintId").value(1))
                .andExpect(jsonPath("$.data.name").value("Sprint-1"));
        }

        @Test
        @DisplayName("[TC-Sprint-API-011] id 非数字 → 400")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/sprint/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("POST /business/sprint")
    class AddTests {
        @Test
        @DisplayName("[TC-Sprint-API-020] 合法新增 → 200")
        void addOk() throws Exception {
            when(sprintService.insertSprint(any())).thenReturn(1);
            String body = """
                {"projectId":1,"name":"S1","startDate":"2026-06-01","endDate":"2026-06-15"}
                """;
            mockMvc.perform(post("/business/sprint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
            verify(sprintService).insertSprint(any(Sprint.class));
        }

        @Test
        @DisplayName("[TC-Sprint-API-021] Service 抛 703 单一活跃约束 → 200 + code:703")
        void addSingleActiveConstraint() throws Exception {
            when(sprintService.insertSprint(any()))
                .thenThrow(new ServiceException("项目已有进行中的迭代", 703));
            mockMvc.perform(post("/business/sprint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(703));
        }
    }

    @Nested
    @DisplayName("PUT /business/sprint")
    class EditTests {
        @Test
        @DisplayName("[TC-Sprint-API-030] 合法状态转换 → 200")
        void editOk() throws Exception {
            when(sprintService.updateSprint(any())).thenReturn(1);
            String body = """
                {"sprintId":1,"status":"1"}
                """;
            mockMvc.perform(put("/business/sprint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-Sprint-API-031] 非法状态转换 → 200 + code:701")
        void editIllegalState() throws Exception {
            when(sprintService.updateSprint(any()))
                .thenThrow(new ServiceException("状态转换非法", 701));
            mockMvc.perform(put("/business/sprint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"sprintId":1,"status":"5"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701));
        }
    }

    @Nested
    @DisplayName("DELETE /business/sprint/{sprintIds}")
    class RemoveTests {
        @Test
        @DisplayName("[TC-Sprint-API-040] 单 id → 200")
        void removeSingle() throws Exception {
            when(sprintService.deleteSprintByIds(any())).thenReturn(1);
            mockMvc.perform(delete("/business/sprint/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("[TC-Sprint-API-041] 批量 → Service 收 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(sprintService.deleteSprintByIds(any())).thenReturn(3);
            mockMvc.perform(delete("/business/sprint/1,2,3"))
                .andExpect(status().isOk());
            verify(sprintService).deleteSprintByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3 && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }
}
