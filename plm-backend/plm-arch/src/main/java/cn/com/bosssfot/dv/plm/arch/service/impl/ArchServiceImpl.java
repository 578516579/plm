package cn.com.bosssfot.dv.plm.arch.service.impl;

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
import cn.com.bosssfot.dv.plm.arch.domain.Arch;
import cn.com.bosssfot.dv.plm.arch.mapper.ArchMapper;
import cn.com.bosssfot.dv.plm.arch.service.IArchService;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 系统概要设计 HLD Service — PRD §F3.1 + 原型 archdesign.html
 *
 * 落地:
 * - ADR: generateArchNo() — ARCH-YYYY-NNNN
 * - 4 状态机 (含反向边 01→00):
 *   00 草稿 → 01 评审中 → {00 (打回) / 02 已确认} → 03 已废弃 (终态)
 * - ENUM 白名单校验 6 个技术选型字段 → 604
 * - aiGenerate(): 本期 mock — 返回标准 C4 Mermaid + 架构方案 Markdown + NFR 模板
 *   PRD §2.3 arch-design-flow Dify 工作流 Phase 后续接入
 */
@Service
public class ArchServiceImpl implements IArchService
{
    private static final Logger log = LoggerFactory.getLogger(ArchServiceImpl.class);

    private static final Set<String> ALLOWED_MODE      = Set.of("microservice", "monolith", "serverless", "layered");
    private static final Set<String> ALLOWED_STACK     = Set.of("java_sb3", "go_gin", "python_fastapi", "nodejs");
    private static final Set<String> ALLOWED_DATABASE  = Set.of("pg_redis", "mysql_redis", "kingbase");
    private static final Set<String> ALLOWED_AI_ENGINE = Set.of("dify_deepseek", "dify_chatglm", "self_langchain");
    private static final Set<String> ALLOWED_DEPLOY    = Set.of("k8s", "docker_compose", "baremetal");
    private static final Set<String> ALLOWED_IOT       = Set.of("mqtt", "http_longpoll", "websocket");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00", "02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired private ArchMapper archMapper;
    @Autowired private AiService aiService;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Arch> selectArchList(Arch t) { return archMapper.selectArchList(t); }

    @Override
    public Arch selectArchById(Long id) { return archMapper.selectArchById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertArch(Arch t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("架构方案标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("架构师不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        validateEnum(t);

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建架构方案状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getArchNo())) {
            t.setArchNo(generateArchNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return archMapper.insertArch(t);
        } catch (DuplicateKeyException e) {
            log.warn("arch_no 重号,重试一次: {}", t.getArchNo());
            t.setArchNo(generateArchNo());
            return archMapper.insertArch(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateArch(Arch t) {
        Arch old = archMapper.selectArchById(t.getArchId());
        if (old == null) {
            throw new ServiceException("架构方案不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "架构方案状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        validateEnum(t);
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return archMapper.updateArch(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteArchByIds(Long[] ids) {
        return archMapper.deleteArchByIds(ids);
    }

    /**
     * AI 生成架构方案 (PRD §F3.1 + §2.3 arch-design-flow)
     * 本期 mock:返回标准 C4 Mermaid 容器图 + 架构方案 Markdown + NFR 映射
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Arch aiGenerate(Long archId) {
        Arch a = archMapper.selectArchById(archId);
        if (a == null) {
            throw new ServiceException("架构方案不存在", 404);
        }
        aiService.chat(AiChatRequest.builder("")
            .system("你是 PLM 资深架构师,擅长 C4 模型与国产化技术选型")
            .user("请生成 [" + a.getTitle() + "] 的 HLD 架构方案")
            .callerTag("arch#" + archId).build());
        String design = "# " + a.getTitle() + " — 架构方案\n\n"
            + "## 1. 架构模式\n" + label(a.getArchMode()) + "\n\n"
            + "## 2. 技术选型\n- 主语言/框架: " + label(a.getPrimaryStack()) + "\n"
            + "- 数据库: " + label(a.getDatabaseChoice()) + "\n"
            + "- AI 编排: " + label(a.getAiOrchestration()) + "\n"
            + "- 部署: " + label(a.getDeploymentType()) + "\n"
            + "- IoT 协议: " + label(a.getIotProtocol()) + "\n\n"
            + "## 3. 优劣分析\n- 优势:成熟生态 / 国产化适配 / AI 原生\n"
            + "- 风险:弱网场景挑战 / IoT 大流量 / 模型推理成本\n";
        String c4 = "C4Container\n"
            + "  title 系统容器图 — " + a.getTitle() + "\n"
            + "  Person(user, \"用户\", \"农场主/农技人员\")\n"
            + "  System_Boundary(plm, \"AgriPLM·AI 平台\") {\n"
            + "    Container(web, \"前端\", \"Vue 3 + Vite\", \"用户交互\")\n"
            + "    Container(api, \"业务后端\", \"" + label(a.getPrimaryStack()) + "\", \"REST + AI 编排\")\n"
            + "    Container(ai,  \"AI 工作流\", \"" + label(a.getAiOrchestration()) + "\", \"PRD/竞品/测试用例生成\")\n"
            + "    ContainerDb(db, \"数据库\", \"" + label(a.getDatabaseChoice()) + "\", \"业务数据 + 缓存\")\n"
            + "  }\n"
            + "  Rel(user, web, \"HTTPS\")\n  Rel(web, api, \"REST/WebSocket\")\n"
            + "  Rel(api, ai, \"HTTP\")\n  Rel(api, db, \"JDBC/Redis\")\n";
        String nfr = "性能: P95 < 1s,500 并发\n"
            + "可靠性: ≥99.5%,离线弱网兜底\n"
            + "安全: TLS 1.3 + RBAC + 审计 + AI 内容水印\n"
            + "部署: " + label(a.getDeploymentType()) + ",支持私有化";
        a.setDesignContent(design);
        a.setC4DiagramContent(c4);
        a.setNfrMapping(nfr);
        a.setAiGenerated("Y");
        a.setAiGeneratedAt(new Date());
        a.setUpdateBy(SecurityUtils.getUsername());
        archMapper.updateArch(a);
        return a;
    }

    private static String label(String v) { return StringUtils.isBlank(v) ? "(未指定)" : v; }

    private void validateEnum(Arch t) {
        if (StringUtils.isNotBlank(t.getArchMode()) && !ALLOWED_MODE.contains(t.getArchMode())) {
            throw new ServiceException("架构模式值非法 (允许: microservice/monolith/serverless/layered)", 604);
        }
        if (StringUtils.isNotBlank(t.getPrimaryStack()) && !ALLOWED_STACK.contains(t.getPrimaryStack())) {
            throw new ServiceException("技术栈值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getDatabaseChoice()) && !ALLOWED_DATABASE.contains(t.getDatabaseChoice())) {
            throw new ServiceException("数据库选型值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getAiOrchestration()) && !ALLOWED_AI_ENGINE.contains(t.getAiOrchestration())) {
            throw new ServiceException("AI 编排值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getDeploymentType()) && !ALLOWED_DEPLOY.contains(t.getDeploymentType())) {
            throw new ServiceException("部署方式值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getIotProtocol()) && !ALLOWED_IOT.contains(t.getIotProtocol())) {
            throw new ServiceException("IoT 协议值非法", 604);
        }
    }

    private String generateArchNo() {
        int year = LocalDate.now().getYear();
        String prefix = "ARCH-" + year + "-";
        Integer maxSeq = archMapper.selectMaxSeqOfYear(prefix);
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
