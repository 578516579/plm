package cn.com.bosssfot.dv.plm.web.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.service.IProjectService;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementService;
import cn.com.bosssfot.dv.plm.task.domain.Task;
import cn.com.bosssfot.dv.plm.task.service.ITaskService;

/**
 * McpBusinessTools 单元测试 — PRD §2.5/§3.5「PLM 业务能力 MCP 化」。
 *
 * 覆盖:
 *   - list 工具: 过滤参数映射到 domain filter + total/returned/items 元信息 + limit 截断
 *   - get 工具: 命中返回实体 / 未命中返回 {found:false} / 缺 id 抛 IllegalArgumentException
 *   - 参数类型兼容: Integer / Long / 数字字符串 都能解析成 Long
 */
@ExtendWith(MockitoExtension.class)
class McpBusinessToolsTest {

    @Mock private IProjectService projectService;
    @Mock private IRequirementService requirementService;
    @Mock private ITaskService taskService;

    @InjectMocks private McpBusinessTools tools;

    private static List<Project> projects(int n) {
        List<Project> list = new ArrayList<>();
        IntStream.rangeClosed(1, n).forEach(i -> {
            Project p = new Project();
            p.setId((long) i);
            p.setProjectName("项目" + i);
            list.add(p);
        });
        return list;
    }

    @Nested
    @DisplayName("project.list / project.get")
    class ProjectTools {

        @Test
        @DisplayName("过滤参数(status/projectType/projectName)映射到 Project filter")
        void listMapsFilter() {
            when(projectService.selectProjectList(any())).thenReturn(projects(2));
            tools.projectList(Map.of("status", "1", "projectType", "agri", "projectName", "墒情"));

            ArgumentCaptor<Project> cap = ArgumentCaptor.forClass(Project.class);
            verify(projectService).selectProjectList(cap.capture());
            assertThat(cap.getValue().getStatus()).isEqualTo("1");
            assertThat(cap.getValue().getProjectType()).isEqualTo("agri");
            assertThat(cap.getValue().getProjectName()).isEqualTo("墒情");
        }

        @Test
        @DisplayName("返回 total/returned/items;未超 limit 时 truncated=false")
        void listMeta() {
            when(projectService.selectProjectList(any())).thenReturn(projects(3));
            Map<String, Object> r = tools.projectList(Map.of());
            assertThat(r.get("total")).isEqualTo(3);
            assertThat(r.get("returned")).isEqualTo(3);
            assertThat(r.get("truncated")).isEqualTo(false);
            assertThat((List<?>) r.get("items")).hasSize(3);
        }

        @Test
        @DisplayName("结果超 limit → 截断到 limit 条且 truncated=true")
        void listTruncates() {
            when(projectService.selectProjectList(any())).thenReturn(projects(10));
            Map<String, Object> r = tools.projectList(Map.of("limit", 4));
            assertThat(r.get("total")).isEqualTo(10);
            assertThat(r.get("returned")).isEqualTo(4);
            assertThat(r.get("truncated")).isEqualTo(true);
            assertThat((List<?>) r.get("items")).hasSize(4);
        }

        @Test
        @DisplayName("get 命中 → 返回实体")
        void getFound() {
            Project p = new Project();
            p.setId(7L);
            when(projectService.selectProjectById(7L)).thenReturn(p);
            assertThat(tools.projectGet(Map.of("id", 7))).isSameAs(p);
        }

        @Test
        @DisplayName("get 未命中 → {found:false}")
        void getNotFound() {
            when(projectService.selectProjectById(99L)).thenReturn(null);
            Object r = tools.projectGet(Map.of("id", "99"));   // 字符串 id 也能解析
            assertThat(r).isInstanceOf(Map.class);
            assertThat(((Map<?, ?>) r).get("found")).isEqualTo(false);
            assertThat(((Map<?, ?>) r).get("id")).isEqualTo(99L);
        }

        @Test
        @DisplayName("get 缺 id → IllegalArgumentException")
        void getMissingId() {
            assertThatThrownBy(() -> tools.projectGet(Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
        }
    }

    @Nested
    @DisplayName("requirement.list / task.list filter 映射")
    class OtherTools {

        @Test
        @DisplayName("requirement.list 映射 projectId(数字字符串)/status/priority")
        void reqListFilter() {
            when(requirementService.selectRequirementList(any())).thenReturn(List.of());
            tools.requirementList(Map.of("projectId", "12", "status", "01", "priority", "P0"));

            ArgumentCaptor<Requirement> cap = ArgumentCaptor.forClass(Requirement.class);
            verify(requirementService).selectRequirementList(cap.capture());
            assertThat(cap.getValue().getProjectId()).isEqualTo(12L);
            assertThat(cap.getValue().getStatus()).isEqualTo("01");
            assertThat(cap.getValue().getPriority()).isEqualTo("P0");
        }

        @Test
        @DisplayName("task.list 映射 projectId/sprintId/status/assigneeUserId")
        void taskListFilter() {
            when(taskService.selectTaskList(any())).thenReturn(List.of());
            tools.taskList(Map.of("projectId", 1, "sprintId", 2, "status", "03", "assigneeUserId", 5));

            ArgumentCaptor<Task> cap = ArgumentCaptor.forClass(Task.class);
            verify(taskService).selectTaskList(cap.capture());
            assertThat(cap.getValue().getProjectId()).isEqualTo(1L);
            assertThat(cap.getValue().getSprintId()).isEqualTo(2L);
            assertThat(cap.getValue().getStatus()).isEqualTo("03");
            assertThat(cap.getValue().getAssigneeUserId()).isEqualTo(5L);
        }
    }
}
