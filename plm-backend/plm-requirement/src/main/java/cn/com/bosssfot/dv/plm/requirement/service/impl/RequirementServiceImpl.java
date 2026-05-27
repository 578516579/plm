package cn.com.bosssfot.dv.plm.requirement.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.core.event.EntityChangedEvent.Action;
import cn.com.bosssfot.dv.plm.common.core.event.RequirementChangedEvent;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementReviewService;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementService;

/**
 * 需求 Service 实现
 *
 * 落地：
 * - ADR-0002：generateRequirementNo()，规则 REQ-YYYY-NNNN
 * - PRD §3.3 / API §3.3：4×4 状态机，非法转换抛 ServiceException(601)
 * - API §2.2：FK 校验（projectId 必须在 tb_project 存在）抛 ServiceException(702)
 */
@Service
public class RequirementServiceImpl implements IRequirementService
{
    private static final Logger log = LoggerFactory.getLogger(RequirementServiceImpl.class);

    /**
     * PRD §3.3 状态机转换矩阵
     *
     *              00待评审  01开发中  02已完成  03已取消
     * 00待评审      —        ✅       ❌       ✅
     * 01开发中     ✅        —        ✅       ✅
     * 02已完成     ❌        ❌       —        ❌  (终态)
     * 03已取消     ❌        ❌       ❌       —   (终态)
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01", "03"),       // 待评审 → 开发中 / 已取消
        "01", Set.of("00", "02", "03"), // 开发中 → 待评审（打回） / 已完成 / 已取消
        "02", Set.of(),                 // 已完成（终态）
        "03", Set.of()                  // 已取消（终态）
    );

    @Autowired
    private RequirementMapper requirementMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private IRequirementReviewService requirementReviewService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<Requirement> selectRequirementList(Requirement requirement)
    {
        return requirementMapper.selectRequirementList(requirement);
    }

    @Override
    public Requirement selectRequirementById(Long requirementId)
    {
        return requirementMapper.selectRequirementById(requirementId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRequirement(Requirement requirement)
    {
        // 字段校验（API §2.2）
        if (StringUtils.isBlank(requirement.getTitle())) {
            throw new ServiceException("需求标题不能为空", 602);
        }
        if (requirement.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        // FK 校验：项目必须存在
        Project project = projectMapper.selectProjectById(requirement.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // 默认值 + 新建状态约束
        if (StringUtils.isBlank(requirement.getSource())) {
            requirement.setSource("01");   // 客户反馈
        }
        if (StringUtils.isBlank(requirement.getPriority())) {
            requirement.setPriority("02"); // P2 一般
        }
        if (StringUtils.isBlank(requirement.getStatus())) {
            requirement.setStatus("00");   // 待评审
        } else if (!"00".equals(requirement.getStatus())) {
            throw new ServiceException("新建需求状态必须为「待评审」", 601);
        }

        // ADR-0002：自动生成 requirement_no
        if (StringUtils.isBlank(requirement.getRequirementNo())) {
            requirement.setRequirementNo(generateRequirementNo());
        }

        requirement.setCreateBy(SecurityUtils.getUsername());

        int rows;
        try {
            rows = requirementMapper.insertRequirement(requirement);
        } catch (DuplicateKeyException e) {
            log.warn("requirement_no 重号，重试一次: {}", requirement.getRequirementNo());
            requirement.setRequirementNo(generateRequirementNo());
            rows = requirementMapper.insertRequirement(requirement);
        }
        if (rows > 0 && requirement.getRequirementId() != null) {
            eventPublisher.publishEvent(new RequirementChangedEvent(requirement.getRequirementId(), Action.INSERT));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRequirement(Requirement requirement)
    {
        Requirement old = requirementMapper.selectRequirementById(requirement.getRequirementId());
        if (old == null) {
            throw new ServiceException("需求不存在", 404);
        }

        // 状态机校验（PRD §3.3）
        if (StringUtils.isNotBlank(requirement.getStatus())
                && !requirement.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(requirement.getStatus())) {
                throw new ServiceException(
                    "需求状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(requirement.getStatus()),
                    601
                );
            }
            // PRD §F2.4 评审前置: 00待评审 → 01开发中 必须存在通过的评审记录
            if ("00".equals(old.getStatus()) && "01".equals(requirement.getStatus())) {
                if (!requirementReviewService.hasPassedReview(requirement.getRequirementId())) {
                    throw new ServiceException(
                        "需求推进到「开发中」前,必须存在至少 1 条「通过」的评审记录",
                        701
                    );
                }
            }
        }

        // 若改了 projectId，校验存在
        if (requirement.getProjectId() != null
                && !requirement.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(requirement.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        requirement.setUpdateBy(SecurityUtils.getUsername());
        int rows = requirementMapper.updateRequirement(requirement);
        if (rows > 0) {
            eventPublisher.publishEvent(new RequirementChangedEvent(requirement.getRequirementId(), Action.UPDATE));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRequirementByIds(Long[] requirementIds)
    {
        int rows = requirementMapper.deleteRequirementByIds(requirementIds);
        if (rows > 0 && requirementIds != null) {
            for (Long id : requirementIds) {
                eventPublisher.publishEvent(new RequirementChangedEvent(id, Action.DELETE));
            }
        }
        return rows;
    }

    @Override
    public int countByProjectId(Long projectId)
    {
        return requirementMapper.countByProjectId(projectId);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 私有辅助
    // ─────────────────────────────────────────────────────────────────────

    /** ADR-0002：编号规则 REQ-YYYY-NNNN */
    private String generateRequirementNo() {
        int year = LocalDate.now().getYear();
        String prefix = "REQ-" + year + "-";
        Integer maxSeq = requirementMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "待评审";
            case "01" -> "开发中";
            case "02" -> "已完成";
            case "03" -> "已取消";
            default   -> "未知(" + status + ")";
        };
    }
}
