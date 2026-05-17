package cn.com.bosssfot.dv.plm.task.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;
import cn.com.bosssfot.dv.plm.task.domain.Task;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.sprint.mapper.SprintMapper;
import cn.com.bosssfot.dv.plm.task.mapper.TaskMapper;
import cn.com.bosssfot.dv.plm.task.service.ITaskService;

/**
 * 任务 Service 实现
 *
 * 落地：
 * - ADR-0003：generateTaskNo()，规则 TASK-YYYY-NNNN
 * - PRD §3.3 / API §2.4：6×6 状态机，含 02 ↔ 01 反向边（评审打回 + 测试打回到评审）
 * - API §2.4：进入「已完成」必须填 actualHours（错误码 602）
 * - API §2.3：MR 链接格式校验 http(s)://（错误码 604）
 * - API §2.3：projectId / requirementId / sprintId FK 全部校验（错误码 702）
 */
@Service
public class TaskServiceImpl implements ITaskService
{
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    /** MR 链接简易格式 */
    private static final Pattern MR_URL_PATTERN = Pattern.compile("^https?://.+");

    /**
     * PRD §3.3 状态机转换矩阵（6×6）
     *
     *              00待开发  01开发中  02代码评审  03测试中  04已完成  05已取消
     * 00待开发     —        ✅       ❌         ❌        ❌        ✅
     * 01开发中    ✅        —        ✅         ❌        ❌        ✅
     * 02代码评审  ❌        ✅       —         ✅        ❌        ✅   ← 评审打回 01
     * 03测试中    ❌        ❌       ✅         —         ✅        ✅   ← 测试打回 02
     * 04已完成    ❌        ❌       ❌         ❌        —         ❌   (终态)
     * 05已取消    ❌        ❌       ❌         ❌        ❌        —    (终态)
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01", "05"),
        "01", Set.of("00", "02", "05"),
        "02", Set.of("01", "03", "05"),
        "03", Set.of("02", "04", "05"),
        "04", Set.of(),
        "05", Set.of()
    );

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private RequirementMapper requirementMapper;

    @Autowired
    private SprintMapper sprintMapper;

    @Override
    public List<Task> selectTaskList(Task task)
    {
        return taskMapper.selectTaskList(task);
    }

    @Override
    public Task selectTaskById(Long taskId)
    {
        return taskMapper.selectTaskById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTask(Task task)
    {
        // 字段校验（API §2.3）
        if (StringUtils.isBlank(task.getTitle())) {
            throw new ServiceException("任务标题不能为空", 602);
        }
        if (task.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        // FK 校验
        Project project = projectMapper.selectProjectById(task.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (task.getRequirementId() != null) {
            Requirement req = requirementMapper.selectRequirementById(task.getRequirementId());
            if (req == null) {
                throw new ServiceException("关联需求不存在", 702);
            }
        }
        if (task.getSprintId() != null) {
            Sprint sprint = sprintMapper.selectSprintById(task.getSprintId());
            if (sprint == null) {
                throw new ServiceException("关联迭代不存在", 702);
            }
        }

        // MR URL 格式校验
        if (StringUtils.isNotBlank(task.getMrUrl()) && !MR_URL_PATTERN.matcher(task.getMrUrl()).matches()) {
            throw new ServiceException("MR 链接格式错误，必须以 http(s):// 开头", 604);
        }

        // 默认值 + 新建状态约束
        if (StringUtils.isBlank(task.getPriority())) {
            task.setPriority("02");
        }
        if (StringUtils.isBlank(task.getStatus())) {
            task.setStatus("00");
        } else if (!"00".equals(task.getStatus())) {
            throw new ServiceException("新建任务状态必须为「待开发」", 601);
        }

        // ADR-0003
        if (StringUtils.isBlank(task.getTaskNo())) {
            task.setTaskNo(generateTaskNo());
        }

        task.setCreateBy(SecurityUtils.getUsername());

        try {
            return taskMapper.insertTask(task);
        } catch (DuplicateKeyException e) {
            log.warn("task_no 重号，重试一次: {}", task.getTaskNo());
            task.setTaskNo(generateTaskNo());
            return taskMapper.insertTask(task);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTask(Task task)
    {
        Task old = taskMapper.selectTaskById(task.getTaskId());
        if (old == null) {
            throw new ServiceException("任务不存在", 404);
        }

        // 状态机校验
        if (StringUtils.isNotBlank(task.getStatus())
                && !task.getStatus().equals(old.getStatus())) {
            String oldStatus = old.getStatus();
            String newStatus = task.getStatus();

            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of());
            if (!allowed.contains(newStatus)) {
                throw new ServiceException(
                    "任务状态 " + statusLabel(oldStatus) + " 不能直接转到 " + statusLabel(newStatus),
                    601
                );
            }

            // 进入已完成必填 actualHours
            if ("04".equals(newStatus)) {
                if (task.getActualHours() == null && old.getActualHours() == null) {
                    throw new ServiceException("请填写实际工时", 602);
                }
            }
        }

        // 改 FK 时全部 re-validate
        if (task.getProjectId() != null && !task.getProjectId().equals(old.getProjectId())) {
            if (projectMapper.selectProjectById(task.getProjectId()) == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        if (task.getRequirementId() != null && !task.getRequirementId().equals(old.getRequirementId())) {
            if (requirementMapper.selectRequirementById(task.getRequirementId()) == null) {
                throw new ServiceException("关联需求不存在", 702);
            }
        }
        if (task.getSprintId() != null && !task.getSprintId().equals(old.getSprintId())) {
            if (sprintMapper.selectSprintById(task.getSprintId()) == null) {
                throw new ServiceException("关联迭代不存在", 702);
            }
        }

        // MR URL 格式
        if (StringUtils.isNotBlank(task.getMrUrl()) && !MR_URL_PATTERN.matcher(task.getMrUrl()).matches()) {
            throw new ServiceException("MR 链接格式错误，必须以 http(s):// 开头", 604);
        }

        task.setUpdateBy(SecurityUtils.getUsername());
        return taskMapper.updateTask(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTaskByIds(Long[] taskIds)
    {
        return taskMapper.deleteTaskByIds(taskIds);
    }

    @Override
    public List<Task> selectMyTasks(Task taskFilter)
    {
        Long userId = SecurityUtils.getUserId();
        taskFilter.setAssigneeUserId(userId);
        return taskMapper.selectTaskList(taskFilter);
    }

    @Override
    public Map<String, Object> kanban(Long projectId, Long sprintId)
    {
        if (projectId == null) {
            throw new ServiceException("projectId 不能为空", 602);
        }
        List<Task> all = taskMapper.selectKanbanTasks(projectId, sprintId);

        // 按状态分组（保持顺序 00-04，05 已取消不显示）
        String[] statuses = {"00", "01", "02", "03", "04"};
        String[] labels = {"待开发", "开发中", "代码评审", "测试中", "已完成"};

        List<Map<String, Object>> columns = new ArrayList<>();
        for (int i = 0; i < statuses.length; i++) {
            final String st = statuses[i];
            List<Task> bucket = new ArrayList<>();
            for (Task t : all) {
                if (st.equals(t.getStatus())) {
                    bucket.add(t);
                    if (bucket.size() >= 50) break;  // 每列最多 50 条
                }
            }
            Map<String, Object> col = new LinkedHashMap<>();
            col.put("status", st);
            col.put("label", labels[i]);
            col.put("tasks", bucket);
            col.put("count", bucket.size());
            columns.add(col);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("columns", columns);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 私有辅助
    // ─────────────────────────────────────────────────────────────────────

    /** ADR-0003：编号规则 TASK-YYYY-NNNN */
    private String generateTaskNo() {
        int year = LocalDate.now().getYear();
        String prefix = "TASK-" + year + "-";
        Integer maxSeq = taskMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "待开发";
            case "01" -> "开发中";
            case "02" -> "代码评审";
            case "03" -> "测试中";
            case "04" -> "已完成";
            case "05" -> "已取消";
            default   -> "未知(" + status + ")";
        };
    }
}
