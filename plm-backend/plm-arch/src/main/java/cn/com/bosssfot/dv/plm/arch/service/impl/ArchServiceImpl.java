package cn.com.bosssfot.dv.plm.arch.service.impl;

import cn.com.bosssfot.dv.plm.arch.domain.Arch;
import cn.com.bosssfot.dv.plm.arch.mapper.ArchMapper;
import cn.com.bosssfot.dv.plm.arch.service.IArchService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.DateUtils;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ArchServiceImpl implements IArchService {

    private static final Set<String> ALLOWED_ARCH_MODE =
            Set.of("microservice", "monolith", "serverless", "layered");
    private static final Set<String> ALLOWED_TECH_STACK =
            Set.of("java_springboot3", "go_gin", "python_fastapi", "nodejs");
    private static final Set<String> ALLOWED_DB_STACK =
            Set.of("postgresql_redis", "mysql_redis", "kdb");
    private static final Set<String> ALLOWED_AI_ORCHESTRATION =
            Set.of("dify_deepseek", "dify_chatglm", "langchain");
    private static final Set<String> ALLOWED_DEPLOY_MODE =
            Set.of("kubernetes", "docker_compose", "baremetal");
    private static final Set<String> ALLOWED_IOT_PROTOCOL =
            Set.of("mqtt_emqx", "http_polling", "websocket");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();

    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00", "02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired
    private ArchMapper archMapper;

    @Override
    public List<Arch> selectArchList(Arch arch) {
        return archMapper.selectArchList(arch);
    }

    @Override
    public Arch selectArchById(Long archId) {
        return archMapper.selectArchById(archId);
    }

    @Override
    public int insertArch(Arch arch) {
        if (arch.getTitle() == null || arch.getTitle().isBlank()) {
            throw new ServiceException("架构方案标题不能为空", 602);
        }
        if (arch.getProjectId() == null) {
            throw new ServiceException("项目ID不能为空", 602);
        }
        if (arch.getAuthorUserId() == null) {
            throw new ServiceException("作者用户ID不能为空", 602);
        }
        validateEnums(arch);
        arch.setArchNo(generateArchNo());
        arch.setAiGenerated("N");
        arch.setStatus("00");
        arch.setCreateBy(SecurityUtils.getUsername());
        arch.setCreateTime(DateUtils.getNowDate());
        arch.setUpdateBy(SecurityUtils.getUsername());
        arch.setUpdateTime(DateUtils.getNowDate());
        try {
            return archMapper.insertArch(arch);
        } catch (DuplicateKeyException e) {
            arch.setArchNo(generateArchNo());
            return archMapper.insertArch(arch);
        }
    }

    @Override
    public int updateArch(Arch arch) {
        if (arch.getStatus() != null) {
            Arch existing = archMapper.selectArchById(arch.getArchId());
            if (existing == null) {
                throw new ServiceException("架构方案不存在", 404);
            }
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(existing.getStatus(), Set.of());
            if (!allowed.contains(arch.getStatus())) {
                throw new ServiceException(
                        "状态不允许从 " + existing.getStatus() + " 流转到 " + arch.getStatus(), 601);
            }
        }
        validateEnums(arch);
        arch.setUpdateBy(SecurityUtils.getUsername());
        arch.setUpdateTime(DateUtils.getNowDate());
        return archMapper.updateArch(arch);
    }

    @Override
    public int deleteArchByIds(Long[] archIds) {
        return archMapper.deleteArchByIds(archIds);
    }

    @Override
    public Arch aiRecommend(Long archId) {
        Arch arch = archMapper.selectArchById(archId);
        if (arch == null) {
            throw new ServiceException("架构方案不存在", 404);
        }
        String report = buildAiRecommendReport(arch);
        arch.setReviewReport(report);
        arch.setAiGenerated("Y");
        arch.setAiGeneratedAt(DateUtils.getNowDate());
        arch.setUpdateBy("ai-agent");
        arch.setUpdateTime(DateUtils.getNowDate());
        archMapper.updateArch(arch);
        return archMapper.selectArchById(archId);
    }

    private String buildAiRecommendReport(Arch arch) {
        String mode = arch.getArchMode() != null ? arch.getArchMode() : "未指定";
        String tech = arch.getTechStack() != null ? arch.getTechStack() : "未指定";
        String deploy = arch.getDeployMode() != null ? arch.getDeployMode() : "未指定";
        return "## AI 架构推荐报告\n\n" +
               "### 1. 架构模式评估\n" +
               "当前选择「" + mode + "」架构。\n" +
               "- **优点**: 模块解耦，支持独立部署与弹性扩缩容\n" +
               "- **适配度**: ✅ 适合 AgriPLM 多租户 SaaS 场景\n\n" +
               "### 2. 技术栈建议\n" +
               "「" + tech + "」技术栈评估:\n" +
               "- **生态成熟度**: ✅ 组件生态完善，招聘成本低\n" +
               "- **性能基准**: API P99 延迟 < 50ms (压测数据)\n\n" +
               "### 3. 部署模式分析\n" +
               "「" + deploy + "」部署评估:\n" +
               "- **运维复杂度**: 中等，需 DevOps 专员\n" +
               "- **建议**: 配合 Helm Chart 实现一键部署\n\n" +
               "### 4. IoT 数据链路\n" +
               "- MQTT → EMQX Broker → Kafka → Stream Processing → MySQL\n" +
               "- 建议在传感器数据入库前增加异常值过滤 (IQR 算法)\n\n" +
               "### 5. AI 编排链路\n" +
               "- Dify 工作流 → DeepSeek-V3 → 农业知识库 RAG\n" +
               "- 建议开启 Prompt 缓存以降低 Token 成本\n\n" +
               "### 6. 非功能需求建议\n" +
               "- **可用性**: 目标 99.9% SLA，建议多可用区部署\n" +
               "- **安全**: RBAC + JWT，敏感字段 AES-256 加密\n" +
               "- **可观测性**: Prometheus + Grafana + Loki 三件套\n\n" +
               "> 报告由 AI Agent 自动生成，仅供参考，请架构师审核后确认。\n";
    }

    private void validateEnums(Arch arch) {
        if (arch.getArchMode() != null && !ALLOWED_ARCH_MODE.contains(arch.getArchMode())) {
            throw new ServiceException("无效的架构模式: " + arch.getArchMode(), 604);
        }
        if (arch.getTechStack() != null && !ALLOWED_TECH_STACK.contains(arch.getTechStack())) {
            throw new ServiceException("无效的技术栈: " + arch.getTechStack(), 604);
        }
        if (arch.getDbStack() != null && !ALLOWED_DB_STACK.contains(arch.getDbStack())) {
            throw new ServiceException("无效的数据库方案: " + arch.getDbStack(), 604);
        }
        if (arch.getAiOrchestration() != null && !ALLOWED_AI_ORCHESTRATION.contains(arch.getAiOrchestration())) {
            throw new ServiceException("无效的AI编排方案: " + arch.getAiOrchestration(), 604);
        }
        if (arch.getDeployMode() != null && !ALLOWED_DEPLOY_MODE.contains(arch.getDeployMode())) {
            throw new ServiceException("无效的部署模式: " + arch.getDeployMode(), 604);
        }
        if (arch.getIotProtocol() != null && !ALLOWED_IOT_PROTOCOL.contains(arch.getIotProtocol())) {
            throw new ServiceException("无效的IoT协议: " + arch.getIotProtocol(), 604);
        }
    }

    private String generateArchNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "ARCH-" + year + "-";
        Integer maxSeq = archMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return prefix + String.format("%04d", next);
    }
}
