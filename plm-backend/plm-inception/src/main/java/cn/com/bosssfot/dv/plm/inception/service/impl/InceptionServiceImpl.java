package cn.com.bosssfot.dv.plm.inception.service.impl;

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
import cn.com.bosssfot.dv.plm.inception.domain.Inception;
import cn.com.bosssfot.dv.plm.inception.mapper.InceptionMapper;
import cn.com.bosssfot.dv.plm.inception.service.IInceptionService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.service.IProjectService;

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
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),
        "01", Set.of("02", "04"),
        "02", Set.of("03", "04"),
        "03", Set.of(),
        "04", Set.of("00")              // 已驳回 → 打回草稿 (反向边)
    );

    @Autowired private InceptionMapper inceptionMapper;
    @Autowired private AiService aiService;
    @Autowired private IProjectService projectService;

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
     * 本期实现:服务端 mock,返回标准化 Markdown 模板 + 风险列表
     * Phase 后续:通过 Dify HTTP API 转发 background + estimated* → 实模型生成
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Inception aiGenerate(Long inceptionId) {
        Inception inc = inceptionMapper.selectInceptionById(inceptionId);
        if (inc == null) {
            throw new ServiceException("立项单不存在", 404);
        }
        // V3 审计:走一次 AiService 产生 invocation log,业务输出仍用下方 mock(保 E2E)。
        // 当 plm.ai.default-provider 切到真厂商时,审计表能立刻看到真实 tokens/elapsed,
        // 业务侧可逐步替换 result.getText() (本期保持 mock 输出兼容现有 E2E 断言)。
        AiChatRequest aiReq = AiChatRequest.builder("")
            .system("你是 PLM 资深立项专家,擅长农业 IoT 项目可行性分析")
            .user("请生成项目 [" + inc.getProjectName() + "] 的立项建议书,业务线:" + inc.getBusinessLine())
            .callerTag("inception#" + inceptionId)
            .build();
        // P0-1: 真 provider 时 aiProposalContent 采用 LLM 输出;mock/失败时下方模板兜底(aiRisks 保持模板)
        String proposal = "# 立项建议书:" + inc.getProjectName() + "\n\n"
            + "## 1. 背景与诉求\n" + (inc.getBackground() == null ? "(待补充)" : inc.getBackground()) + "\n\n"
            + "## 2. 业务价值\n- 业务线:" + inc.getBusinessLine() + "\n- 项目类型:" + inc.getInceptionType() + "\n\n"
            + "## 3. 资源预估\n- 预计工期:" + inc.getEstimatedDurationMonths() + " 个月\n"
            + "- 团队规模:" + inc.getEstimatedTeam() + "\n\n"
            + "## 4. 建议结论\n建议立项,进入产品设计阶段。\n";
        String risks = "1. 农业季节性强,需对齐农时窗口\n"
            + "2. 弱网/离线场景比例较高,需评估离线能力\n"
            + "3. 跨部门协同 (产品/算法/实施) 沟通成本";
        inc.setAiGenerated("Y");
        inc.setAiProposalContent(AiTexts.generate(aiService,aiReq, () -> proposal));
        inc.setAiRisks(risks);
        inc.setAiGeneratedAt(new Date());
        inc.setUpdateBy(SecurityUtils.getUsername());
        inceptionMapper.updateInception(inc);
        return inc;
    }

    /**
     * Proposal 0028 P0-2 — 立项晋升项目
     *
     * 字段映射:
     *   inception.projectName       → project.projectName
     *   inception.background        → project.description
     *   inception.submitterUserId   → project.managerUserId(立项提交人 = 项目负责人默认值)
     *   project.projectType         = "iteration"(默认;inception.inceptionType 字典与 project 字典不同,
     *                                 保守起见用通用值,人工后续在项目详情可改)
     *   project.status              = "0"(未启动 — 项目状态字典 0/1/2/3/4)
     *
     * 校验:
     *   - inception 不存在        → 404
     *   - inception.status != 03  → 601
     *   - 幂等:若 inception.projectId 不为空且对应 project 仍在 → 直接返回旧 projectId,不重建
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long promoteToProject(Long inceptionId) {
        Inception inc = inceptionMapper.selectInceptionById(inceptionId);
        if (inc == null) {
            throw new ServiceException("立项单不存在", 404);
        }
        if (!"03".equals(inc.getStatus())) {
            throw new ServiceException(
                "立项状态 " + statusLabel(inc.getStatus()) + " 不能晋升项目,必须先到「已批准」",
                601
            );
        }

        // 幂等:已晋升过且 project 仍在 → 直接返回旧 projectId
        if (inc.getProjectId() != null) {
            Project existing = projectService.selectProjectById(inc.getProjectId());
            if (existing != null) {
                log.info("inception#{} 已晋升过项目 project#{},直接返回幂等结果",
                    inceptionId, inc.getProjectId());
                return inc.getProjectId();
            }
            // 旧 projectId 指向已删除 project → 重新建
            log.warn("inception#{} 旧 projectId={} 对应项目已不存在,重新晋升",
                inceptionId, inc.getProjectId());
        }

        // 建项目(项目编号 generateProjectNo 由 ProjectServiceImpl 自动填充)
        Project project = new Project();
        project.setProjectName(inc.getProjectName());
        project.setDescription(inc.getBackground());
        project.setManagerUserId(inc.getSubmitterUserId());
        project.setProjectType("iteration");
        project.setStatus("0");
        project.setCreateBy(SecurityUtils.getUsername());
        projectService.insertProject(project);

        // 回填 inception.projectId
        Inception back = new Inception();
        back.setInceptionId(inceptionId);
        back.setProjectId(project.getId());
        back.setUpdateBy(SecurityUtils.getUsername());
        inceptionMapper.updateInception(back);

        log.info("inception#{} 晋升项目成功,新 projectId={}", inceptionId, project.getId());
        return project.getId();
    }

    private String generateInceptionNo() {
        int year = LocalDate.now().getYear();
        String prefix = "INC-" + year + "-";
        Integer maxSeq = inceptionMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "草稿";
            case "01" -> "已提交";
            case "02" -> "审批中";
            case "03" -> "已批准";
            case "04" -> "已驳回";
            default   -> "未知(" + status + ")";
        };
    }
}
