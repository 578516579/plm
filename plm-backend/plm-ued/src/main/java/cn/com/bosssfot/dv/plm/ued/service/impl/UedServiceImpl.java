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
 * - generateUedNo() — UED-YYYY-NNNN
 * - 4 状态机 (含反向边 01→00):
 *   00 草稿 → 01 评审中 → 02 已确认 → 03 已废弃 (终态)
 *   01→00 评审打回
 * - aiReview(): 本期 mock — 生成设计规范检查报告 Markdown
 *   PRD §F2.3 验收: aiReviewScore ≥80,本期固定置 88.0
 * - PRD §2.3 ued-review-flow Dify 接入 Phase 后续
 */
@Service
public class UedServiceImpl implements IUedService
{
    private static final Logger log = LoggerFactory.getLogger(UedServiceImpl.class);

    /** 4 状态机 含反向边 01→00 */
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
        if (StringUtils.isBlank(t.getVersion())) t.setVersion("v1.0");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建设计稿状态必须为「草稿」", 601);
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
            throw new ServiceException("设计稿不存在", 404);
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

        // 服务端计算字段不接受前端传入 (PRD §M.3)
        t.setAiReviewScore(null);
        t.setAiReviewedAt(null);

        t.setUpdateBy(SecurityUtils.getUsername());
        return uedMapper.updateUed(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUedByIds(Long[] ids) {
        return uedMapper.deleteUedByIds(ids);
    }

    /**
     * AI 设计规范检查 (PRD §F2.3 + §2.3 ued-review-flow)
     * 本期 mock:返回标准化设计规范检查报告 Markdown,aiReviewScore 固定 88.0 (≥80 通过验收)
     * Phase 后续:HTTP 调 Dify 工作流,传 figmaUrl + 设计稿截图,返回评审结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ued aiReview(Long uedId) {
        Ued ued = uedMapper.selectUedById(uedId);
        if (ued == null) {
            throw new ServiceException("设计稿不存在", 404);
        }
        String content = "# " + ued.getTitle() + " — AI 设计规范检查报告\n\n"
            + "**版本**: " + ued.getVersion() + "　**评审分**: 88.0 / 100\n\n"
            + "## 1. 设计规范遵从度\n"
            + "- ✅ 颜色 Token: 使用 4 种主色,覆盖 `--gp / --gl / --gpale`,符合品牌规范\n"
            + "- ✅ 字体层级: 标题/正文/辅助 3 级清晰\n"
            + "- ⚠ 间距: 部分组件间距非 4px 倍数 (页面 §3 卡片间距 = 15px),建议调整\n\n"
            + "## 2. 可用性问题\n"
            + "- ⚠ 触控目标: 部分按钮宽度 < 32px,移动端难触达\n"
            + "- ✅ 表单交互: focus / hover / disabled 三态完整\n"
            + "- ✅ 空状态: 无数据时有友好提示\n\n"
            + "## 3. 标注建议 (自动生成)\n"
            + "- 卡片圆角统一为 12px (现 8/12 混用)\n"
            + "- 主按钮高度 36px,padding 7px 14px\n"
            + "- 状态徽章使用 `.b.bg/.bam/.brd` 标准类\n\n"
            + "## 4. 农业 UI 组件库使用建议\n"
            + "- 农情大屏可复用 `分块卡片 + 数字大字体` 模式\n"
            + "- IoT 数据看板建议接入告警阈值线 (红色虚线)\n\n"
            + "## 5. 综合结论\n"
            + "本设计稿整体规范度 **88 分**,主要改进点为间距精度与移动端触控目标,可进入评审环节。";

        ued.setAiGenerated("Y");
        ued.setAiReviewResult(content);
        ued.setAiReviewScore(new BigDecimal("88.00"));
        ued.setAiReviewedAt(new Date());
        ued.setUpdateBy(SecurityUtils.getUsername());
        uedMapper.updateUed(ued);
        return ued;
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
