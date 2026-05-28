package cn.com.bosssfot.dv.plm.competitive.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.AiTexts;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.competitive.domain.Competitive;
import cn.com.bosssfot.dv.plm.competitive.mapper.CompetitiveMapper;
import cn.com.bosssfot.dv.plm.competitive.service.ICompetitiveService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 竞品情报 Service — PRD §F1.3 + 原型 competitive.html
 *
 * 落地:
 * - ADR: generateCompetitiveNo() — COMP-YYYY-NNNN
 * - 3 状态机: 00 草稿 → 01 已发布 → 02 已归档 (终态)
 * - ENUM 校验: pricingTier ∈ {free, midrange, enterprise} → 604
 * - aiAnalyze(): 本期 mock — 返回标准 SWOT 模板 + 综合报告
 *   Dify 工作流 competitive-analysis-flow 占位 (Phase 后续接入实爬虫 + RAG)
 */
@Service
public class CompetitiveServiceImpl implements ICompetitiveService
{
    private static final Logger log = LoggerFactory.getLogger(CompetitiveServiceImpl.class);

    private static final Set<String> ALLOWED_TIER = Set.of("free", "midrange", "enterprise");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),
        "01", Set.of("02"),
        "02", Set.of()
    );

    @Autowired private CompetitiveMapper competitiveMapper;
    @Autowired private AiService aiService;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Competitive> selectCompetitiveList(Competitive t) {
        return competitiveMapper.selectCompetitiveList(t);
    }

    @Override
    public Competitive selectCompetitiveById(Long id) {
        return competitiveMapper.selectCompetitiveById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCompetitive(Competitive t) {
        if (StringUtils.isBlank(t.getCompetitorName())) {
            throw new ServiceException("竞品名称不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("创建人不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (StringUtils.isNotBlank(t.getPricingTier()) && !ALLOWED_TIER.contains(t.getPricingTier())) {
            throw new ServiceException("价格档值非法 (允许: free/midrange/enterprise)", 604);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getMonitorEnabled())) t.setMonitorEnabled("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建竞品状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getCompetitiveNo())) {
            t.setCompetitiveNo(generateCompetitiveNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return competitiveMapper.insertCompetitive(t);
        } catch (DuplicateKeyException e) {
            log.warn("competitive_no 重号,重试一次: {}", t.getCompetitiveNo());
            t.setCompetitiveNo(generateCompetitiveNo());
            return competitiveMapper.insertCompetitive(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompetitive(Competitive t) {
        Competitive old = competitiveMapper.selectCompetitiveById(t.getCompetitiveId());
        if (old == null) {
            throw new ServiceException("竞品不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "竞品状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }

        if (StringUtils.isNotBlank(t.getPricingTier()) && !ALLOWED_TIER.contains(t.getPricingTier())) {
            throw new ServiceException("价格档值非法", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return competitiveMapper.updateCompetitive(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCompetitiveByIds(Long[] ids) {
        return competitiveMapper.deleteCompetitiveByIds(ids);
    }

    /**
     * AI 生成竞品 SWOT + 综合报告 (PRD §F1.3 + §2.3 competitive-analysis-flow)
     * 本期 mock:返回标准 SWOT 模板 + 综合报告 Markdown
     * Phase 后续: HTTP 调 Dify 工作流 → 实爬虫 + RAG 检索 + 综合分析
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Competitive aiAnalyze(Long competitiveId) {
        Competitive comp = competitiveMapper.selectCompetitiveById(competitiveId);
        if (comp == null) {
            throw new ServiceException("竞品不存在", 404);
        }
        // P0-1: 真 provider 时 aiAnalysisReport 采用 LLM 输出;mock/失败时下方模板兜底。
        // SWOT 四象限为结构化辅助字段,LLM 返回散文不便拆分,仍用启发式模板。
        AiChatRequest aiReq = AiChatRequest.builder("")
            .system("你是 PLM 资深竞品分析师,擅长 SWOT 与定位拆解")
            .user("请分析竞品 [" + comp.getCompetitorName() + "] 的 SWOT 与威胁机会")
            .callerTag("competitive#" + competitiveId)
            .build();
        String name = comp.getCompetitorName();
        comp.setStrengths("- 品牌知名度高\n- 生态完整\n- 用户基础广泛");
        comp.setWeaknesses("- 农业垂直能力弱\n- 私有化部署成本高\n- AI 集成深度浅");
        comp.setOpportunities("- 国产替代红利\n- AI 原生研发工具爆发期\n- 中小企业数字化转型加速");
        comp.setThreats("- 国际厂商加速本土化\n- 开源生态竞争激烈\n- 客户对 AI 期望值过高");
        String report = "# 竞品分析报告:" + name + "\n\n"
            + "## SWOT 矩阵\n\n"
            + "### S 优势\n" + comp.getStrengths() + "\n\n"
            + "### W 劣势\n" + comp.getWeaknesses() + "\n\n"
            + "### O 机会\n" + comp.getOpportunities() + "\n\n"
            + "### T 威胁\n" + comp.getThreats() + "\n\n"
            + "## 关键差异化建议\n"
            + "1. **垂直深耕**:聚焦农业行业知识库 (AgriKB) 形成壁垒\n"
            + "2. **AI 原生**:Dify 深度编排,非简单 AI 点缀\n"
            + "3. **私有化**:满足农业客户数据合规要求\n"
            + "4. **离线支持**:适应农村/农场弱网场景\n";
        comp.setAiAnalysisReport(AiTexts.generate(aiService,aiReq, () -> report));
        comp.setAiGenerated("Y");
        comp.setAiGeneratedAt(new Date());
        comp.setUpdateBy(SecurityUtils.getUsername());
        competitiveMapper.updateCompetitive(comp);
        return comp;
    }

    private String generateCompetitiveNo() {
        int year = LocalDate.now().getYear();
        String prefix = "COMP-" + year + "-";
        Integer maxSeq = competitiveMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "草稿";
            case "01" -> "已发布";
            case "02" -> "已归档";
            default   -> "未知(" + status + ")";
        };
    }
}
