package cn.com.bosssfot.dv.plm.project.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
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
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.project.service.IProjectService;

/**
 * 项目 Service 实现
 *
 * 落地:
 *   - ADR-0001 generateProjectNo,规则 PRJ-YYYY-NNNN
 *   - PRD-MAPPING.md §2 "Project (F1.2)" 字段对照表 (commit 20b5bb6)
 *   - PRD-MAPPING.md §3 状态机:双字段 status + lifecyclePhase
 *
 * v2 (2026-05-17) PRD-align 重写:
 *   - 字典值 0/1/2/3/4 → 00/01/02/03 (4 态,默认 00 进行中)
 *   - 加 lifecyclePhase 状态机 + 字段白名单校验
 */
@Service
public class ProjectServiceImpl implements IProjectService
{
    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    /** 总状态机 (PRD-MAPPING §3): 00 进行中 / 01 暂停 / 02 已完成 / 03 已取消 */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01", "02", "03"));   // 进行中 → 暂停 / 已完成 / 已取消
        STATUS_TRANSITIONS.put("01", Set.of("00", "03"));          // 暂停 → 进行中 / 已取消
        STATUS_TRANSITIONS.put("02", Set.of());                    // 已完成 终态
        STATUS_TRANSITIONS.put("03", Set.of());                    // 已取消 终态
    }

    /** 交付阶段状态机 (PRD-MAPPING §3): 00 规划 / 01 研发 / 02 测试 / 03 验收;仅 status=00 时可演进 */
    private static final Map<String, Set<String>> PHASE_TRANSITIONS = new HashMap<>();
    static {
        PHASE_TRANSITIONS.put("00", Set.of("01"));                 // 规划 → 研发
        PHASE_TRANSITIONS.put("01", Set.of("00", "02"));           // 研发 → 规划(回退) / 测试
        PHASE_TRANSITIONS.put("02", Set.of("01", "03"));           // 测试 → 研发(回退) / 验收
        PHASE_TRANSITIONS.put("03", Set.of("02"));                 // 验收 → 测试(回退);验收完成请改 status=02
    }

    /** 字段白名单(604 校验) */
    private static final Set<String> VALID_STATUS = Set.of("00", "01", "02", "03");
    private static final Set<String> VALID_PHASE = Set.of("00", "01", "02", "03");
    private static final Set<String> VALID_HEALTH = Set.of("green", "amber", "red");
    private static final Set<String> VALID_BUSINESS_LINE = Set.of(
        "plant_protection", "precision_agri", "agri_supply", "quality_trace"
    );
    private static final Set<String> VALID_PRIORITY = Set.of("P0", "P1", "P2", "P3");
    private static final Set<String> VALID_PROJECT_TYPE = Set.of("rnd", "upgrade", "ops");

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<Project> selectProjectList(Project project)
    {
        return projectMapper.selectProjectList(project);
    }

    @Override
    public Project selectProjectById(Long id)
    {
        return projectMapper.selectProjectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProject(Project project)
    {
        // 必填校验 (601)
        if (StringUtils.isBlank(project.getProjectName())) {
            throw new ServiceException("项目名称不能为空", 601);
        }
        if (StringUtils.isBlank(project.getBusinessLine())) {
            throw new ServiceException("业务线不能为空", 601);
        }

        // 字段白名单 (604)
        validateWhitelist(project);

        // 日期逻辑 (604)
        if (project.getStartDate() != null && project.getEndDate() != null
                && project.getStartDate().after(project.getEndDate())) {
            throw new ServiceException("起始日期不能晚于结束日期", 604);
        }

        // 进度范围 (604)
        if (project.getProgress() != null
                && (project.getProgress() < 0 || project.getProgress() > 100)) {
            throw new ServiceException("进度必须在 0-100 之间", 604);
        }

        // 默认值
        if (StringUtils.isBlank(project.getStatus())) {
            project.setStatus("00");
        } else if (!"00".equals(project.getStatus())) {
            throw new ServiceException("新建项目状态必须为「进行中」(00)", 701);
        }
        if (StringUtils.isBlank(project.getLifecyclePhase())) {
            project.setLifecyclePhase("00");
        } else if (!"00".equals(project.getLifecyclePhase())) {
            throw new ServiceException("新建项目阶段必须为「规划中」(00)", 701);
        }
        if (project.getProgress() == null) {
            project.setProgress(0);
        }

        // ADR-0001 自动生成 project_no
        if (StringUtils.isBlank(project.getProjectNo())) {
            project.setProjectNo(generateProjectNo());
        }

        project.setCreateBy(SecurityUtils.getUsername());

        try {
            return projectMapper.insertProject(project);
        } catch (DuplicateKeyException e) {
            log.warn("project_no 重号,重试一次: {}", project.getProjectNo());
            project.setProjectNo(generateProjectNo());
            return projectMapper.insertProject(project);   // 仅重试 1 次
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProject(Project project)
    {
        // 字段白名单 (604)
        validateWhitelist(project);

        // 进度范围 (604)
        if (project.getProgress() != null
                && (project.getProgress() < 0 || project.getProgress() > 100)) {
            throw new ServiceException("进度必须在 0-100 之间", 604);
        }

        // 加载旧记录以做状态机校验
        boolean needsOld = StringUtils.isNotBlank(project.getStatus())
                || StringUtils.isNotBlank(project.getLifecyclePhase());
        Project old = null;
        if (needsOld) {
            old = projectMapper.selectProjectById(project.getId());
            if (old == null) {
                throw new ServiceException("项目不存在", 404);
            }
        }

        // 总状态机校验 (601)
        if (StringUtils.isNotBlank(project.getStatus())) {
            String oldStatus = old.getStatus();
            String newStatus = project.getStatus();
            if (!oldStatus.equals(newStatus)) {
                Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of());
                if (!allowed.contains(newStatus)) {
                    throw new ServiceException(
                        "状态 " + statusLabel(oldStatus) + " 不能直接转到 " + statusLabel(newStatus),
                        601
                    );
                }
            }
        }

        // 交付阶段状态机校验 (601):仅当 status 当前/目标都为 00 时允许阶段演进
        if (StringUtils.isNotBlank(project.getLifecyclePhase())) {
            String effectiveStatus = StringUtils.isNotBlank(project.getStatus())
                    ? project.getStatus() : old.getStatus();
            if (!"00".equals(effectiveStatus)) {
                throw new ServiceException(
                    "项目当前为「" + statusLabel(effectiveStatus) + "」,阶段已冻结,不可演进",
                    601
                );
            }
            String oldPhase = old.getLifecyclePhase();
            String newPhase = project.getLifecyclePhase();
            if (!oldPhase.equals(newPhase)) {
                Set<String> allowed = PHASE_TRANSITIONS.getOrDefault(oldPhase, Set.of());
                if (!allowed.contains(newPhase)) {
                    throw new ServiceException(
                        "阶段 " + phaseLabel(oldPhase) + " 不能直接转到 " + phaseLabel(newPhase),
                        601
                    );
                }
            }
        }

        project.setUpdateBy(SecurityUtils.getUsername());
        return projectMapper.updateProject(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProjectByIds(Long[] ids)
    {
        return projectMapper.deleteProjectByIds(ids);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 私有辅助
    // ─────────────────────────────────────────────────────────────────────

    private void validateWhitelist(Project p) {
        if (StringUtils.isNotBlank(p.getStatus()) && !VALID_STATUS.contains(p.getStatus())) {
            throw new ServiceException("非法状态值: " + p.getStatus(), 604);
        }
        if (StringUtils.isNotBlank(p.getLifecyclePhase()) && !VALID_PHASE.contains(p.getLifecyclePhase())) {
            throw new ServiceException("非法阶段值: " + p.getLifecyclePhase(), 604);
        }
        if (StringUtils.isNotBlank(p.getHealth()) && !VALID_HEALTH.contains(p.getHealth())) {
            throw new ServiceException("非法健康度值: " + p.getHealth(), 604);
        }
        if (StringUtils.isNotBlank(p.getBusinessLine()) && !VALID_BUSINESS_LINE.contains(p.getBusinessLine())) {
            throw new ServiceException("非法业务线值: " + p.getBusinessLine(), 604);
        }
        if (StringUtils.isNotBlank(p.getPriority()) && !VALID_PRIORITY.contains(p.getPriority())) {
            throw new ServiceException("非法优先级值: " + p.getPriority(), 604);
        }
        if (StringUtils.isNotBlank(p.getProjectType()) && !VALID_PROJECT_TYPE.contains(p.getProjectType())) {
            throw new ServiceException("非法项目类型值: " + p.getProjectType(), 604);
        }
    }

    /**
     * ADR-0001 编号规则 PRJ-YYYY-NNNN
     * 算法:查当年内最大流水号 + 1;DB UNIQUE 索引兜底,撞号重试 1 次。
     */
    private String generateProjectNo() {
        int year = LocalDate.now().getYear();
        String prefix = "PRJ-" + year + "-";
        Integer maxSeq = projectMapper.selectMaxSeqOfYear(prefix);   // 形如 "PRJ-2026-%"
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);   // PRJ-2026-0001
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "进行中";
            case "01": return "暂停";
            case "02": return "已完成";
            case "03": return "已取消";
            default:   return "未知(" + status + ")";
        }
    }

    private static String phaseLabel(String phase) {
        switch (phase) {
            case "00": return "规划中";
            case "01": return "研发中";
            case "02": return "测试中";
            case "03": return "验收中";
            default:   return "未知(" + phase + ")";
        }
    }
}
