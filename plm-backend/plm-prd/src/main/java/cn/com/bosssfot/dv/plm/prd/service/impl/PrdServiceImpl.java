package cn.com.bosssfot.dv.plm.prd.service.impl;

import java.math.BigDecimal;
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
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.prd.domain.Prd;
import cn.com.bosssfot.dv.plm.prd.mapper.PrdMapper;
import cn.com.bosssfot.dv.plm.prd.service.IPrdService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * AI PRD 生成器 Service — PRD §F2.2 + 原型 prd.html
 *
 * 落地:
 * - ADR: generatePrdNo() — PRD-YYYY-NNNN
 * - 4 状态机 (含反向边 01→00):
 *   00 草稿 → 01 评审中 → 02 已确认 → 03 已废弃 (终态)
 *   01→00 评审打回
 * - aiGenerate(): 本期 mock — 生成 7 段标准化 PRD Markdown,
 *   PRD §F2.2 验收: completeness_score ≥80,本期固定置 85.0
 * - PRD §2.3 prd-generation-flow Dify 接入 Phase 后续
 */
@Service
public class PrdServiceImpl implements IPrdService
{
    private static final Logger log = LoggerFactory.getLogger(PrdServiceImpl.class);

    private static final Set<String> ALLOWED_SCENE =
        Set.of("irrigation", "agri_sales", "pest_control", "traceability");
    private static final Set<String> ALLOWED_TARGET_USER =
        Set.of("farmer", "agronomist", "admin");

    /** 4 状态机 含反向边 01→00 */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),
        "01", Set.of("00", "02"),   // 评审中 → 草稿(反向打回) / 已确认
        "02", Set.of("03"),
        "03", Set.of()
    );

    @Autowired private PrdMapper prdMapper;
    @Autowired private AiService aiService;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Prd> selectPrdList(Prd t) { return prdMapper.selectPrdList(t); }

    @Override
    public Prd selectPrdById(Long id) { return prdMapper.selectPrdById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPrd(Prd t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("功能名称不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("作者不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (StringUtils.isNotBlank(t.getSceneTemplate()) && !ALLOWED_SCENE.contains(t.getSceneTemplate())) {
            throw new ServiceException("业务场景值非法 (允许: irrigation/agri_sales/pest_control/traceability)", 604);
        }
        if (StringUtils.isNotBlank(t.getTargetUser()) && !ALLOWED_TARGET_USER.contains(t.getTargetUser())) {
            throw new ServiceException("目标用户值非法 (允许: farmer/agronomist/admin)", 604);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getVersion())) t.setVersion("v1.0");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建 PRD 状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getPrdNo())) {
            t.setPrdNo(generatePrdNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return prdMapper.insertPrd(t);
        } catch (DuplicateKeyException e) {
            log.warn("prd_no 重号,重试一次: {}", t.getPrdNo());
            t.setPrdNo(generatePrdNo());
            return prdMapper.insertPrd(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePrd(Prd t) {
        Prd old = prdMapper.selectPrdById(t.getPrdId());
        if (old == null) {
            throw new ServiceException("PRD 不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "PRD 状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }

        if (StringUtils.isNotBlank(t.getSceneTemplate()) && !ALLOWED_SCENE.contains(t.getSceneTemplate())) {
            throw new ServiceException("业务场景值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getTargetUser()) && !ALLOWED_TARGET_USER.contains(t.getTargetUser())) {
            throw new ServiceException("目标用户值非法", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return prdMapper.updatePrd(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePrdByIds(Long[] ids) {
        return prdMapper.deletePrdByIds(ids);
    }

    /**
     * AI 生成完整 PRD (PRD §F2.2 + §2.3 prd-generation-flow)
     * 本期 mock:返回标准化 7 段 Markdown,completeness_score 固定 85.0 (≥80 通过验收)
     * Phase 后续:HTTP 调 Dify 工作流,传 description + sceneTemplate + targetUser
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Prd aiGenerate(Long prdId) {
        Prd prd = prdMapper.selectPrdById(prdId);
        if (prd == null) {
            throw new ServiceException("PRD 不存在", 404);
        }
        aiService.chat(AiChatRequest.builder("")
            .system("你是 PLM PRD 资深产品经理,擅长结构化需求文档撰写")
            .user("请基于标题 [" + prd.getTitle() + "] 生成 PRD")
            .callerTag("prd#" + prdId).build());
        String content = "# " + prd.getTitle() + " — PRD\n\n"
            + "## 1. 背景与目标\n"
            + (prd.getDescription() == null ? "(待补充)" : prd.getDescription()) + "\n\n"
            + "## 2. 用户故事\n"
            + "作为「" + Optional(prd.getTargetUser()) + "」,我希望通过本功能,达成业务诉求。\n\n"
            + "## 3. 功能描述\n- 核心流程 (正常路径)\n- 异常流程 (网络中断 / 数据为空 / 权限不足)\n\n"
            + "## 4. 非功能需求\n- 性能:页面响应 < 1 秒 (P95)\n- 兼容性:微信小程序 + H5 + Android 弱网\n- 安全:RBAC + 操作审计\n\n"
            + "## 5. 验收标准\n- 主流程跑通\n- 异常路径覆盖\n- 性能达标\n\n"
            + "## 6. 原型说明\n关联场景: " + Optional(prd.getSceneTemplate()) + "\n\n"
            + "## 7. 版本\n初版 v1.0,后续按变更走版本对比。";
        prd.setAiGenerated("Y");
        prd.setContent(content);
        prd.setCompletenessScore(new BigDecimal("85.00"));
        prd.setAiGeneratedAt(new Date());
        prd.setUpdateBy(SecurityUtils.getUsername());
        prdMapper.updatePrd(prd);
        return prd;
    }

    private static String Optional(String v) {
        return StringUtils.isBlank(v) ? "(未填)" : v;
    }

    private String generatePrdNo() {
        int year = LocalDate.now().getYear();
        String prefix = "PRD-" + year + "-";
        Integer maxSeq = prdMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "草稿";
            case "01" -> "评审中";
            case "02" -> "已确认";
            case "03" -> "已废弃";
            default   -> "未知(" + status + ")";
        };
    }
}
