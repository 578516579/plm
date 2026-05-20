package cn.com.bosssfot.dv.plm.project.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
 * 落地：
 * - ADR-0001：generateProjectNo()，规则 PRJ-YYYY-NNNN
 * - PRD §3.3 / API §3.3：状态机转换矩阵，非法转换抛 ServiceException(701)
 */
@Service
public class ProjectServiceImpl implements IProjectService
{
    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    /** PRD §3.3 状态机：key=当前态, value=允许的目标态集合。终态映射到 Set.of() */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "0", Set.of("1", "4"),       // 未启动 → 进行中 / 已取消
        "1", Set.of("2", "3", "4"),  // 进行中 → 暂停 / 已完成 / 已取消
        "2", Set.of("1", "4"),       // 暂停 → 进行中 / 已取消
        "3", Set.of(),               // 已完成 → 终态
        "4", Set.of()                // 已取消 → 终态
    );

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
        // 字段校验（API §3.2）
        if (StringUtils.isBlank(project.getProjectName())) {
            throw new ServiceException("项目名称不能为空", 601);
        }
        if (project.getStartDate() != null && project.getEndDate() != null
                && project.getStartDate().after(project.getEndDate())) {
            throw new ServiceException("起始日期不能晚于结束日期", 604);
        }

        // 状态默认值：未启动
        if (StringUtils.isBlank(project.getStatus())) {
            project.setStatus("0");
        } else if (!"0".equals(project.getStatus())) {
            throw new ServiceException("新建项目状态必须为「未启动」", 701);
        }

        // ADR-0001：自动生成 project_no（如未提供）
        if (StringUtils.isBlank(project.getProjectNo())) {
            project.setProjectNo(generateProjectNo());
        }

        project.setCreateBy(SecurityUtils.getUsername());

        try {
            return projectMapper.insertProject(project);
        } catch (DuplicateKeyException e) {
            log.warn("project_no 重号，重试一次: {}", project.getProjectNo());
            project.setProjectNo(generateProjectNo());
            return projectMapper.insertProject(project);   // 仅重试 1 次
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProject(Project project)
    {
        // 状态机校验（PRD §3.3）
        if (StringUtils.isNotBlank(project.getStatus())) {
            Project old = projectMapper.selectProjectById(project.getId());
            if (old == null) {
                throw new ServiceException("项目不存在", 404);
            }
            String oldStatus = old.getStatus();
            String newStatus = project.getStatus();
            if (!oldStatus.equals(newStatus)) {
                Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of());
                if (!allowed.contains(newStatus)) {
                    throw new ServiceException(
                        "状态 " + statusLabel(oldStatus) + " 不能直接转到 " + statusLabel(newStatus),
                        701
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

    /**
     * ADR-0001：编号规则 PRJ-YYYY-NNNN
     * 算法：查当年内最大流水号 + 1；DB UNIQUE 索引兜底，撞号重试 1 次。
     */
    private String generateProjectNo() {
        int year = LocalDate.now().getYear();
        String prefix = "PRJ-" + year + "-";
        Integer maxSeq = projectMapper.selectMaxSeqOfYear(prefix);   // 形如 "PRJ-2026-%"
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);   // PRJ-2026-0001
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "0" -> "未启动";
            case "1" -> "进行中";
            case "2" -> "暂停";
            case "3" -> "已完成";
            case "4" -> "已取消";
            default  -> "未知(" + status + ")";
        };
    }
}
