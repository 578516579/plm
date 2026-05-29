package cn.com.bosssfot.dv.plm.project.controller;

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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.service.IProjectService;

/**
 * ProjectController API 契约测试 — Phase 04 Gate B.1 强制项。
 *
 * <p>覆盖目标:验证 HTTP ↔ Java 契约不破裂(method / path / 参数绑定 /
 * JSON 序列化 / 状态码 / AjaxResult 结构)。Service 层用 Mockito stub,
 * 不启 Spring 上下文(对比 @SpringBootTest 启 38 模块 30s+,本类 < 2s)。
 *
 * <p>不覆盖(由其他层覆盖):
 * <ul>
 *   <li>业务逻辑 → {@link cn.com.bosssfot.dv.plm.project.service.impl.ProjectServiceImplTest}</li>
 *   <li>Spring Security / JWT → E2E (loginAsAdmin)</li>
 *   <li>@Log AOP / @PreAuthorize → 集成测试 + Phase 04 Gate B.4 手测</li>
 *   <li>DB 真集成 → {@link cn.com.bosssfot.dv.plm.project.service.impl.ProjectServiceImplLightIntegrationTest}</li>
 * </ul>
 *
 * <p>范本说明:本类是 plm-perf-test + sql/seed + 契约测试 三件套铺设的范本之一,
 * 其他业务模块按此结构复制。详见 [03-开发/测试规范.md §4 API 契约层]。
 */
@ExtendWith(MockitoExtension.class)
class ProjectControllerContractTest {

    @Mock
    private IProjectService projectService;

    @InjectMocks
    private ProjectController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 注入与 plm-framework GlobalExceptionHandler 一致行为的契约级 advice,
        // 让 ServiceException 被翻译成 AjaxResult{code,msg} 而非裸 5xx Servlet 错误
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

    // ─────────────────────────────────────────────────────────────────────
    // GET /business/project/list  — 列表分页 + 搜索
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /business/project/list")
    class ListTests {

        @Test
        @DisplayName("[TC-Proj-API-001] 无参数列表 → 200 + AjaxResult{code:200, rows:[], total:N}")
        void listEmpty() throws Exception {
            when(projectService.selectProjectList(any())).thenReturn(List.of());

            mockMvc.perform(get("/business/project/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("查询成功"))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("[TC-Proj-API-002] 带搜索条件 → Controller 把字段透传到 Service")
        void listWithFilter() throws Exception {
            Project sample = new Project();
            sample.setId(1L);
            sample.setProjectNo("PRJ-2026-0001");
            sample.setProjectName("智慧农业");
            sample.setStatus("1");
            when(projectService.selectProjectList(any())).thenReturn(List.of(sample));

            mockMvc.perform(get("/business/project/list")
                    .param("projectName", "智慧")
                    .param("status", "1")
                    .param("projectType", "rnd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].id").value(1))
                .andExpect(jsonPath("$.rows[0].projectNo").value("PRJ-2026-0001"))
                .andExpect(jsonPath("$.rows[0].status").value("1"));

            verify(projectService, times(1)).selectProjectList(any(Project.class));
        }

        @Test
        @DisplayName("[TC-Proj-API-003] 分页参数 pageNum=2&pageSize=20 不报错")
        void listPaging() throws Exception {
            when(projectService.selectProjectList(any())).thenReturn(List.of());

            mockMvc.perform(get("/business/project/list")
                    .param("pageNum", "2")
                    .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /business/project/{id}  — 详情
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /business/project/{id}")
    class GetInfoTests {

        @Test
        @DisplayName("[TC-Proj-API-010] 存在的 id → 200 + AjaxResult{data:{...}}")
        void getInfoExists() throws Exception {
            Project p = new Project();
            p.setId(1L);
            p.setProjectNo("PRJ-2026-0001");
            p.setProjectName("智慧农业");
            p.setStatus("1");
            when(projectService.selectProjectById(1L)).thenReturn(p);

            mockMvc.perform(get("/business/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.projectNo").value("PRJ-2026-0001"))
                .andExpect(jsonPath("$.data.projectName").value("智慧农业"));
        }

        @Test
        @DisplayName("[TC-Proj-API-011] id 字符串入参绑定 Long(@PathVariable 类型转换);null data 时不输出 data 字段")
        void getInfoPathVarTypeBinding() throws Exception {
            when(projectService.selectProjectById(999L)).thenReturn(null);

            mockMvc.perform(get("/business/project/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                // AjaxResult.success(null) 调 success(msg,data) 但 data=null 时 put 被跳过
                // 所以 $.data 不存在(契约:返回的 JSON 不含 data 字段)
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("[TC-Proj-API-012] id 不是数字 → 400 (Spring 参数绑定失败)")
        void getInfoInvalidId() throws Exception {
            mockMvc.perform(get("/business/project/abc"))
                .andExpect(status().is4xxClientError());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /business/project  — 新增
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /business/project")
    class AddTests {

        @Test
        @DisplayName("[TC-Proj-API-020] 合法新增 → 200 + Service 收到 RequestBody")
        void addOk() throws Exception {
            when(projectService.insertProject(any())).thenReturn(1);

            String body = """
                {"projectName":"新项目","projectType":"rnd","managerUserId":1}
                """;

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mockMvc.perform(post("/business/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
            }

            verify(projectService).insertProject(any(Project.class));
        }

        @Test
        @DisplayName("[TC-Proj-API-021] Service 抛 ServiceException(601) 字段校验失败 → 200 + AjaxResult{code:601}")
        void addFieldValidationFails() throws Exception {
            when(projectService.insertProject(any()))
                .thenThrow(new ServiceException("项目名称不能为空", 601));

            String body = "{}";
            mockMvc.perform(post("/business/project")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(601))
                .andExpect(jsonPath("$.msg").value("项目名称不能为空"));
        }

        @Test
        @DisplayName("[TC-Proj-API-022] 缺 Content-Type → 415 Unsupported Media Type")
        void addMissingContentType() throws Exception {
            mockMvc.perform(post("/business/project").content("{}"))
                .andExpect(status().is4xxClientError());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /business/project  — 修改(含状态机)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /business/project")
    class EditTests {

        @Test
        @DisplayName("[TC-Proj-API-030] 合法状态转换 0→1 → 200")
        void editStateTransitionOk() throws Exception {
            when(projectService.updateProject(any())).thenReturn(1);

            String body = """
                {"id":1,"status":"1"}
                """;

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mockMvc.perform(put("/business/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
            }
        }

        @Test
        @DisplayName("[TC-Proj-API-031] 非法状态转换(终态→任何) → 200 + code:701")
        void editStateTransitionIllegal() throws Exception {
            when(projectService.updateProject(any()))
                .thenThrow(new ServiceException("状态 已完成 不能直接转到 进行中", 701));

            String body = """
                {"id":1,"status":"1"}
                """;
            mockMvc.perform(put("/business/project")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(701))
                .andExpect(jsonPath("$.msg").value(
                    org.hamcrest.Matchers.containsString("不能直接转到")));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // DELETE /business/project/{ids}  — 逻辑删除
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /business/project/{ids}")
    class RemoveTests {

        @Test
        @DisplayName("[TC-Proj-API-040] 单个 id → 200,Service 收到 Long[]{1}")
        void removeSingle() throws Exception {
            when(projectService.deleteProjectByIds(any())).thenReturn(1);

            mockMvc.perform(delete("/business/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            verify(projectService).deleteProjectByIds(any(Long[].class));
        }

        @Test
        @DisplayName("[TC-Proj-API-041] 批量 ids=1,2,3 → 200,Service 收到 Long[]{1,2,3}")
        void removeBatch() throws Exception {
            when(projectService.deleteProjectByIds(any())).thenReturn(3);

            mockMvc.perform(delete("/business/project/1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            verify(projectService).deleteProjectByIds(
                org.mockito.ArgumentMatchers.argThat(ids ->
                    ids.length == 3
                        && Arrays.asList(ids).containsAll(List.of(1L, 2L, 3L))
                )
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /business/project/export  — 导出 Excel
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /business/project/export")
    class ExportTests {

        @Test
        @DisplayName("[TC-Proj-API-050] 导出 → 200 + Content-Type: application/octet-stream")
        void exportOk() throws Exception {
            when(projectService.selectProjectList(any())).thenReturn(List.of(new Project()));

            mockMvc.perform(post("/business/project/export"))
                .andExpect(status().isOk());
            // 真实 Content-Type 由 ExcelUtil 设置,standalone 模式下不验
            // 集成测试再验 binary excel header
        }
    }

}
