package cn.com.bosssfot.dv.plm.sprint.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.api.ITaskQueryService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.sprint.mapper.SprintMapper;
import cn.com.bosssfot.dv.plm.sprint.service.ISprintService;

/**
 * 迭代 Service 实现
 *
 * 落地：
 * - ADR-0004：generateSprintNo()，规则 SPR-YYYY-NNNN
 * - PRD §3.3 / API §2.4：4×4 状态机 + actual 日期自动填充 + 业务硬规则 703 单一活跃
 * - API §2.5：删除前检查关联任务数（错误码 704）
 * - 注：通过 plm-common 的 ITaskQueryService 接口反向调用 plm-task,打破编译期循环
 */
@Service
public class SprintServiceImpl implements ISprintService
{
    private static final Logger log = LoggerFactory.getLogger(SprintServiceImpl.class);

    /**
     * PRD §3.3 状态机转换矩阵（4×4）
     *
     *              00计划中  01进行中  02已完成  03已取消
     * 00计划中      —        ✅       ❌       ✅
     * 01进行中     ❌        —        ✅       ✅
     * 02已完成     ❌        ❌       —        ❌
     * 03已取消     ❌        ❌       ❌       —
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01", "03"),   // 计划中 → 进行中 / 已取消
        "01", Set.of("02", "03"),   // 进行中 → 已完成 / 已取消
        "02", Set.of(),             // 已完成（终态）
        "03", Set.of()              // 已取消（终态）
    );

    @Autowired
    private SprintMapper sprintMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /** 跨模块反向依赖通过 plm-common 接口注入,Spring 自动找到 plm-task 中的 @Service 实现 */
    @Autowired
    private ITaskQueryService taskQueryService;

    @Override
    public List<Sprint> selectSprintList(Sprint sprint)
    {
        return sprintMapper.selectSprintList(sprint);
    }

    @Override
    public Sprint selectSprintById(Long sprintId)
    {
        return sprintMapper.selectSprintById(sprintId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSprint(Sprint sprint)
    {
        // 字段校验（API §2.3）
        if (StringUtils.isBlank(sprint.getName())) {
            throw new ServiceException("迭代名称不能为空", 602);
        }
        if (sprint.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (sprint.getPlannedStartDate() == null) {
            throw new ServiceException("计划开始日期不能为空", 602);
        }
        // FK 校验
        Project project = projectMapper.selectProjectById(sprint.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // 默认 14 天周期
        if (sprint.getPlannedEndDate() == null) {
            LocalDate start = toLocalDate(sprint.getPlannedStartDate());
            sprint.setPlannedEndDate(toDate(start.plusDays(14)));
        }
        if (sprint.getPlannedEndDate().before(sprint.getPlannedStartDate())) {
            throw new ServiceException("计划结束日期不能早于开始日期", 604);
        }
        if (sprint.getDurationDays() == null) {
            long days = ChronoUnit.DAYS.between(
                toLocalDate(sprint.getPlannedStartDate()),
                toLocalDate(sprint.getPlannedEndDate())) + 1;
            sprint.setDurationDays((int) days);
        }

        // 新建状态必须为 00
        if (StringUtils.isBlank(sprint.getStatus())) {
            sprint.setStatus("00");
        } else if (!"00".equals(sprint.getStatus())) {
            throw new ServiceException("新建迭代状态必须为「计划中」", 601);
        }

        // ADR-0004
        if (StringUtils.isBlank(sprint.getSprintNo())) {
            sprint.setSprintNo(generateSprintNo());
        }

        sprint.setCreateBy(SecurityUtils.getUsername());

        try {
            return sprintMapper.insertSprint(sprint);
        } catch (DuplicateKeyException e) {
            log.warn("sprint_no 重号，重试一次: {}", sprint.getSprintNo());
            sprint.setSprintNo(generateSprintNo());
            return sprintMapper.insertSprint(sprint);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSprint(Sprint sprint)
    {
        Sprint old = sprintMapper.selectSprintById(sprint.getSprintId());
        if (old == null) {
            throw new ServiceException("迭代不存在", 404);
        }

        // 状态机校验 + 业务硬规则 703 + actual 日期自动填充
        if (StringUtils.isNotBlank(sprint.getStatus())
                && !sprint.getStatus().equals(old.getStatus())) {
            String oldStatus = old.getStatus();
            String newStatus = sprint.getStatus();

            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of());
            if (!allowed.contains(newStatus)) {
                throw new ServiceException(
                    "迭代状态 " + statusLabel(oldStatus) + " 不能直接转到 " + statusLabel(newStatus),
                    601
                );
            }

            // 703：进入「进行中」前，校验项目级单一活跃
            if ("01".equals(newStatus)) {
                int activeCount = sprintMapper.countActiveByProject(
                    old.getProjectId(), old.getSprintId());
                if (activeCount > 0) {
                    throw new ServiceException(
                        "项目 " + old.getProjectId() + " 已有进行中的迭代，请先完成或取消", 703);
                }
                // 00 → 01：自动填 actual_start_date
                if (sprint.getActualStartDate() == null) {
                    sprint.setActualStartDate(toDate(LocalDate.now()));
                }
            }
            // 01 → 02：自动填 actual_end_date
            if ("02".equals(newStatus) && "01".equals(oldStatus)
                    && sprint.getActualEndDate() == null) {
                sprint.setActualEndDate(toDate(LocalDate.now()));
            }
        }

        // 若改了 projectId，校验存在
        if (sprint.getProjectId() != null
                && !sprint.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(sprint.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        // 日期前后顺序校验
        Date start = sprint.getPlannedStartDate() != null ? sprint.getPlannedStartDate() : old.getPlannedStartDate();
        Date end = sprint.getPlannedEndDate() != null ? sprint.getPlannedEndDate() : old.getPlannedEndDate();
        if (start != null && end != null && end.before(start)) {
            throw new ServiceException("计划结束日期不能早于开始日期", 604);
        }

        sprint.setUpdateBy(SecurityUtils.getUsername());
        return sprintMapper.updateSprint(sprint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSprintByIds(Long[] sprintIds)
    {
        // API §2.5：删除前置检查 — 任意 sprint 下不允许有关联任务
        for (Long sprintId : sprintIds) {
            int taskCount = taskQueryService.countBySprintId(sprintId);
            if (taskCount > 0) {
                throw new ServiceException(
                    "迭代 " + sprintId + " 下有 " + taskCount + " 个关联任务，请先解除关联或迁移", 704);
            }
        }
        return sprintMapper.deleteSprintByIds(sprintIds);
    }

    @Override
    public Sprint selectCurrentByProject(Long projectId)
    {
        if (projectId == null) {
            throw new ServiceException("projectId 不能为空", 602);
        }
        return sprintMapper.selectCurrentByProject(projectId);
    }

    @Override
    public Map<String, Object> selectSprintStats(Long sprintId)
    {
        Sprint sprint = sprintMapper.selectSprintById(sprintId);
        if (sprint == null) {
            throw new ServiceException("迭代不存在", 404);
        }

        int planned   = taskQueryService.countBySprintId(sprintId);
        int done      = taskQueryService.countByStatusAndSprint(sprintId, "04");
        int inProg    = taskQueryService.countByStatusAndSprint(sprintId, "01")
                       + taskQueryService.countByStatusAndSprint(sprintId, "02")
                       + taskQueryService.countByStatusAndSprint(sprintId, "03");
        int remaining = Math.max(planned - done - inProg, 0);
        double rate   = planned == 0 ? 0.0 : Math.round((done * 1000.0 / planned)) / 1000.0;

        // onTime：actual_end_date <= planned_end_date + 2 days（PRD §1.2）
        boolean onTime = true;
        long daysOverPlan = 0;
        if (sprint.getActualEndDate() != null && sprint.getPlannedEndDate() != null) {
            long delta = ChronoUnit.DAYS.between(
                toLocalDate(sprint.getPlannedEndDate()),
                toLocalDate(sprint.getActualEndDate()));
            daysOverPlan = delta;
            onTime = delta <= 2;
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("sprintId", sprintId);
        stats.put("plannedTaskCount", planned);
        stats.put("completedTaskCount", done);
        stats.put("inProgressTaskCount", inProg);
        stats.put("remainingTaskCount", remaining);
        stats.put("completeRate", rate);
        stats.put("onTime", onTime);
        stats.put("daysOverPlan", daysOverPlan);
        return stats;
    }

    @Override
    public boolean checkExists(Long sprintId)
    {
        return sprintId != null && sprintMapper.selectSprintById(sprintId) != null;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 私有辅助
    // ─────────────────────────────────────────────────────────────────────

    /** ADR-0004：编号规则 SPR-YYYY-NNNN */
    private String generateSprintNo() {
        int year = LocalDate.now().getYear();
        String prefix = "SPR-" + year + "-";
        Integer maxSeq = sprintMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "计划中";
            case "01" -> "进行中";
            case "02" -> "已完成";
            case "03" -> "已取消";
            default   -> "未知(" + status + ")";
        };
    }

    private static LocalDate toLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
