package cn.com.bosssfot.dv.plm.inception.service.impl;

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
import cn.com.bosssfot.dv.plm.inception.domain.Inception;
import cn.com.bosssfot.dv.plm.inception.mapper.InceptionMapper;
import cn.com.bosssfot.dv.plm.inception.service.IInceptionService;

/**
 * 项目立项 Service — PRD §F1.1 + 原型 inception.html
 *
 * 落地:
 * - ADR: generateInceptionNo() — INC-YYYY-NNNN
 * - 5×5 状态机 (含反向边 04→00):
 *   00 草稿 → 01 已提交 → 02 审批中 → 03 已批准 / 04 已驳回 → 00 (打回重写)
 *   - 02→03 必须有审批人,自动填 approvedAt
 *   - →04 必须有 rejectReason  → 否则 602
 * - aiGenerate(): 本期 mock 实现 (返回标准化建议书模板),Dify 集成 Phase 后续接入
 */
@Service
public class InceptionServiceImpl implements IInceptionService
{
    private static final Logger log = LoggerFactory.getLogger(InceptionServiceImpl.class);

    private static final Set<String> ALLOWED_BIZ_LINE =
        Set.of("plant_protection", "precision_farming", "agri_supply", "traceability");
    private static final Set<String> ALLOWED_TYPE =
        Set.of("new_product", "iteration", "refactor", "platform");

    /** 5×5 状态机 — 含反向边 04→00 */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02", "04"));
        STATUS_TRANSITIONS.put("02", Set.of("03", "04"));
        STATUS_TRANSITIONS.put("03", Set.of());
        STATUS_TRANSITIONS.put("04", Set.of("00"));
    }

    @Autowired private InceptionMapper inceptionMapper;

    @Override
    public List<Inception> selectInceptionList(Inception t) {
        return inceptionMapper.selectInceptionList(t);
    }

    @Override
    public Inception selectInceptionById(Long id) {
        return inceptionMapper.selectInceptionById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInception(Inception t) {
        if (StringUtils.isBlank(t.getProjectName())) {
            throw new ServiceException("项目名称不能为空", 602);
        }
        if (t.getSubmitterUserId() == null) {
            throw new ServiceException("提交人不能为空", 602);
        }
        if (StringUtils.isNotBlank(t.getBusinessLine()) && !ALLOWED_BIZ_LINE.contains(t.getBusinessLine())) {
            throw new ServiceException("业务线值非法 (允许: plant_protection/precision_farming/agri_supply/traceability)", 604);
        }
        if (StringUtils.isNotBlank(t.getInceptionType()) && !ALLOWED_TYPE.contains(t.getInceptionType())) {
            throw new ServiceException("项目类型值非法 (允许: new_product/iteration/refactor/platform)", 604);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建立项状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getInceptionNo())) {
            t.setInceptionNo(generateInceptionNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return inceptionMapper.insertInception(t);
        } catch (DuplicateKeyException e) {
            log.warn("inception_no 重号,重试一次: {}", t.getInceptionNo());
            t.setInceptionNo(generateInceptionNo());
            return inceptionMapper.insertInception(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInception(Inception t) {
        Inception old = inceptionMapper.selectInceptionById(t.getInceptionId());
        if (old == null) {
            throw new ServiceException("立项单不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "立项状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }

            // 02→03 自动填 approvedAt
            if ("03".equals(t.getStatus())) {
                if (t.getApprovedAt() == null && old.getApprovedAt() == null) {
                    t.setApprovedAt(new Date());
                }
            }
            // →04 必须有 rejectReason
            if ("04".equals(t.getStatus())) {
                String reason = StringUtils.isNotBlank(t.getRejectReason())
                    ? t.getRejectReason() : old.getRejectReason();
                if (StringUtils.isBlank(reason)) {
                    throw new ServiceException("驳回必须填写驳回原因", 602);
                }
            }
        }

        if (StringUtils.isNotBlank(t.getBusinessLine()) && !ALLOWED_BIZ_LINE.contains(t.getBusinessLine())) {
            throw new ServiceException("业务线值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getInceptionType()) && !ALLOWED_TYPE.contains(t.getInceptionType())) {
            throw new ServiceException("项目类型值非法", 604);
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return inceptionMapper.updateInception(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInceptionByIds(Long[] ids) {
        return inceptionMapper.deleteInceptionByIds(ids);
    }

    /**
     * AI 生成立项建议书 (PRD §F1.1 + §2.3 project-inception-flow)
     *
     * 对齐原型 inception.html line 696-723 runInceptionAI() 输出结构:
     * - 4 块 AI 文本 (aiBackground / aiMarketOpportunity / aiRoiEstimate / aiRecommendDecision)
     * - 8 个 ROI 数值字段 (marketSize, devCost, firstYearRevenue, roiMultiple, ...)
     * - aiRisksJson: JSON 数组 [{level: warning|critical, title, description}, ...]
     *
     * Phase 后续:通过 Dify HTTP API 转发 background + estimated* → 实模型生成
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Inception aiGenerate(Long inceptionId) {
        Inception inc = inceptionMapper.selectInceptionById(inceptionId);
        if (inc == null) {
            throw new ServiceException("立项单不存在", 404);
        }

        // 4 段文本 (跟原型 runInceptionAI 的 4 个 <h4> 对应)
        inc.setAiBackground(
            "农业病虫害每年造成全国农作物减产约 **20-30%**,经济损失超千亿元。"
          + "现有人工识别方式依赖专家经验,响应时间长,专家资源严重匮乏。"
          + "项目背景:" + (inc.getBackground() == null ? "(待补充)" : inc.getBackground())
        );
        inc.setAiMarketOpportunity(
            "全国植保服务市场规模约 **580 亿元**(2025 年),数字化渗透率不足 **8%**,市场空间巨大。"
          + "AI 视觉识别技术已达商用级别,准确率可达 95%+。"
          + "业务线:" + inc.getBusinessLine()
        );
        Integer months = inc.getEstimatedDurationMonths() != null ? inc.getEstimatedDurationMonths() : 6;
        inc.setAiRoiEstimate(
            "- 开发成本:约 **180 万元**(" + months + " 个月," + (inc.getEstimatedTeam() != null ? inc.getEstimatedTeam() : "10 人团队") + ")\n"
          + "- 目标付费用户:首年 1 万家农场,客单价 3000 元/年\n"
          + "- 预计首年营收:**3000 万元**,ROI 达 **16.7 倍**"
        );
        inc.setAiRecommendDecision(
            "✅ 建议立项,优先级 P1,计划 Q3 启动,分 3 期交付。"
        );

        // 8 个结构化数值 (跟原型 ROI 段硬编码数对齐)
        inc.setMarketSize(new BigDecimal("580.00"));
        inc.setDigitalPenetration(new BigDecimal("8.00"));
        inc.setDevCostEstimate(new BigDecimal("180.00"));
        inc.setFirstYearRevenue(new BigDecimal("3000.00"));
        inc.setRoiMultiple(new BigDecimal("16.70"));
        inc.setRecommendedPriority("P1");
        inc.setRecommendedStartQuarter("Q3-" + LocalDate.now().getYear());
        inc.setDeliveryPhases(3);

        // 风险数组 — JSON,跟原型 incRisks innerHTML 的 2 条 risk 对齐
        inc.setAiRisksJson(
            "[{\"level\":\"warning\",\"title\":\"数据集风险\","
          + "\"description\":\"病虫害图像训练数据集可能不足,需提前采购或与农科院合作。\"},"
          + "{\"level\":\"critical\",\"title\":\"监管合规风险\","
          + "\"description\":\"农药推荐功能需取得相关资质,建议提前咨询法务。\"}]"
        );

        inc.setAiGenerated("Y");
        inc.setAiGeneratedAt(new Date());
        inc.setUpdateBy(SecurityUtils.getUsername());
        inceptionMapper.updateInception(inc);
        return inceptionMapper.selectInceptionById(inceptionId);
    }

    private String generateInceptionNo() {
        int year = LocalDate.now().getYear();
        String prefix = "INC-" + year + "-";
        Integer maxSeq = inceptionMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "已提交";
            case "02": return "审批中";
            case "03": return "已批准";
            case "04": return "已驳回";
            default:   return "未知(" + status + ")";
        }
    }
}
