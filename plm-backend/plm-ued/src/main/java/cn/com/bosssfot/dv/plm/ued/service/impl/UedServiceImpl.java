package cn.com.bosssfot.dv.plm.ued.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.ued.domain.Ued;
import cn.com.bosssfot.dv.plm.ued.mapper.UedMapper;
import cn.com.bosssfot.dv.plm.ued.service.IUedService;

/**
 * UED 设计协同 Service — PRD §F2.3 + 原型 ued.html
 *
 * 落地:
 * - ADR: generateUedNo() — UED-YYYY-NNNN
 * - 4 状态机 (含反向边 01→00): 00→01→{00,02}→03 已废弃
 * - aiReview() mock: 返回 aiReviewScore (85.0) + complianceCheck JSON + 可用性建议
 *   Dify 工作流 ued-review-flow Phase 后续接入
 */
@Service
public class UedServiceImpl implements IUedService
{
    private static final Logger log = LoggerFactory.getLogger(UedServiceImpl.class);

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00", "02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired private UedMapper uedMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Ued> selectUedList(Ued t) { return uedMapper.selectUedList(t); }

    @Override
    public Ued selectUedById(Long id) { return uedMapper.selectUedById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUed(Ued t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("设计稿名称不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getDesignerUserId() == null) {
            throw new ServiceException("设计师不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建 UED 状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getUedNo())) {
            t.setUedNo(generateUedNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return uedMapper.insertUed(t);
        } catch (DuplicateKeyException e) {
            log.warn("ued_no 重号,重试一次: {}", t.getUedNo());
            t.setUedNo(generateUedNo());
            return uedMapper.insertUed(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUed(Ued t) {
        Ued old = uedMapper.selectUedById(t.getUedId());
        if (old == null) {
            throw new ServiceException("UED 设计不存在", 404);
        }
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "UED 状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return uedMapper.updateUed(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUedByIds(Long[] ids) {
        return uedMapper.deleteUedByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ued aiReview(Long uedId) {
        Ued u = uedMapper.selectUedById(uedId);
        if (u == null) {
            throw new ServiceException("UED 设计不存在", 404);
        }
        String report = "# UED 设计评审报告:" + u.getTitle() + "\n\n"
            + "## 设计规范遵从度\n- 布局:符合 8px 栅格规范\n- 配色:符合主题色规范\n- 字体:正文 14px 符合无障碍\n\n"
            + "## 可用性建议\n1. 移动端核心操作按钮需 ≥44pt 触控热区\n"
            + "2. 农业场景下大屏需考虑户外强光对比度\n"
            + "3. 弱网场景下加载提示需明确\n";
        String compliance = "{\"layout\":\"pass\",\"color\":\"pass\",\"typography\":\"pass\",\"accessibility\":\"warn\"}";
        String usability = "1. 主要操作按钮触控热区不足\n2. 错误提示文案过技术化\n3. 加载状态视觉反馈缺失";
        u.setAiReviewReport(report);
        u.setComplianceCheck(compliance);
        u.setUsabilityIssues(usability);
        u.setAiReviewScore(new BigDecimal("85.00"));
        u.setAiGenerated("Y");
        u.setAiGeneratedAt(new Date());
        u.setUpdateBy(SecurityUtils.getUsername());
        uedMapper.updateUed(u);
        return u;
    }

    private String generateUedNo() {
        int year = LocalDate.now().getYear();
        String prefix = "UED-" + year + "-";
        Integer maxSeq = uedMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "评审中";
            case "02": return "已确认";
            case "03": return "已废弃";
            default:   return "未知(" + status + ")";
        }
    }
}
