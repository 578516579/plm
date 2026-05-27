package cn.com.bosssfot.dv.plm.openspec.service.impl;

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
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;
import cn.com.bosssfot.dv.plm.openspec.mapper.OpenspecMapper;
import cn.com.bosssfot.dv.plm.openspec.service.IOpenspecService;

/**
 * OpenSpec Service — PRD §F3.5 + 原型 aispec.html
 * Spec as Code: OpenAPI 3.1 / AsyncAPI 3.0 / AI Function / GraphQL; AgriKB 增强
 * 3 状态: 草稿→已发布→已弃用; (specName, version) 唯一
 */
@Service
public class OpenspecServiceImpl implements IOpenspecService {
    private static final Logger log = LoggerFactory.getLogger(OpenspecServiceImpl.class);

    private static final Set<String> ALLOWED_TYPE = Set.of("openapi","asyncapi","ai_function","graphql");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of());
    }

    @Autowired private OpenspecMapper openspecMapper;
    @Autowired private AiService aiService;

    @Override
    public List<Openspec> selectOpenspecList(Openspec t) { return openspecMapper.selectOpenspecList(t); }

    @Override
    public Openspec selectOpenspecById(Long id) { return openspecMapper.selectOpenspecById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOpenspec(Openspec t) {
        if (StringUtils.isBlank(t.getSpecName())) throw new ServiceException("规范名称不能为空", 602);
        if (StringUtils.isBlank(t.getSpecType())) throw new ServiceException("规范类型不能为空", 602);
        if (!ALLOWED_TYPE.contains(t.getSpecType()))
            throw new ServiceException("无效的规范类型: " + t.getSpecType(), 604);
        if (StringUtils.isBlank(t.getVersion()))  throw new ServiceException("版本号不能为空", 602);
        if (t.getAuthorUserId() == null)          throw new ServiceException("创建者不能为空", 602);

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus()))      t.setStatus("00");
        if (StringUtils.isBlank(t.getOpenspecNo()))  t.setOpenspecNo(generateOpenspecNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return openspecMapper.insertOpenspec(t);
        } catch (DuplicateKeyException e) {
            // 可能是 openspec_no 或 (spec_name, version) 重复
            log.warn("OpenSpec 唯一键冲突: {}", e.getMessage());
            // 优先重试 no
            t.setOpenspecNo(generateOpenspecNo());
            try {
                return openspecMapper.insertOpenspec(t);
            } catch (DuplicateKeyException e2) {
                throw new ServiceException("规范 " + t.getSpecName() + " v" + t.getVersion() + " 已存在", 701);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOpenspec(Openspec t) {
        Openspec old = openspecMapper.selectOpenspecById(t.getOpenspecId());
        if (old == null) throw new ServiceException("OpenSpec 不存在", 404);
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus()))
                throw new ServiceException("状态不能从 " + old.getStatus() + " 转到 " + t.getStatus(), 601);
        }
        if (t.getSpecType() != null && !ALLOWED_TYPE.contains(t.getSpecType()))
            throw new ServiceException("无效的规范类型: " + t.getSpecType(), 604);
        t.setUpdateBy(SecurityUtils.getUsername());
        return openspecMapper.updateOpenspec(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOpenspecByIds(Long[] ids) { return openspecMapper.deleteOpenspecByIds(ids); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Openspec aiGenerate(Long id) {
        Openspec t = openspecMapper.selectOpenspecById(id);
        if (t == null) throw new ServiceException("OpenSpec 不存在", 404);

        String specType = StringUtils.isNotBlank(t.getSpecType()) ? t.getSpecType() : "openapi";

        AiChatResult result = aiService.chat(AiChatRequest.builder("")
            .system(buildSystemPrompt(specType))
            .user(buildUserPrompt(t, specType))
            .temperature(0.3)
            .maxTokens(2048)
            .callerTag("openspec#" + id)
            .build());

        // 真 provider 调用失败 → 抛 708(与 AiAgentServiceImpl 一致,不静默吞错);Mock 永远 success
        if (!result.isSuccess()) {
            log.warn("[OpenSpec#{}] AI 生成失败 provider={}, error={}",
                    t.getOpenspecNo(), result.getProvider(), result.getError());
            throw new ServiceException("AI 规约生成失败: " + result.getError(), 708);
        }

        // 真 LLM(非 mock)且返回非空 → 直接落库其输出;否则(本地 mock / 空响应)退回确定性模板,
        // 保证 dev/CI/E2E 零外部依赖时仍生成可读规约。
        boolean fromRealAi = !"mock".equalsIgnoreCase(result.getProvider())
                && StringUtils.isNotBlank(result.getText());
        String content = fromRealAi
                ? stripCodeFence(result.getText())
                : buildAiSpec(specType, t.getSpecName(), t.getVersion(), t.getAgriKbRef());

        t.setSpecContent(content);
        t.setAiGenerated("Y");
        t.setAiGeneratedAt(new Date());
        t.setUpdateBy("ai-agent");
        openspecMapper.updateOpenspec(t);

        log.info("[OpenSpec#{}] AI 生成完成 provider={}, source={}, len={}",
                t.getOpenspecNo(), result.getProvider(),
                fromRealAi ? "llm" : "template", content.length());
        return openspecMapper.selectOpenspecById(id);
    }

    /** 按规约类型给 LLM 设定输出格式约束(只输出规约正文,不带解释/围栏) */
    private static String buildSystemPrompt(String specType) {
        String fmt;
        switch (specType) {
            case "openapi":     fmt = "OpenAPI 3.1.0 YAML"; break;
            case "asyncapi":    fmt = "AsyncAPI 3.0.0 YAML"; break;
            case "ai_function": fmt = "AI Function Spec JSON(含 JSON Schema parameters)"; break;
            default:            fmt = "GraphQL SDL"; break;
        }
        return "你是 PLM AI 规约专家,擅长 OpenAPI/AsyncAPI/JSON Schema/GraphQL 与农业 IoT 数据建模。"
             + "请只输出一份合法的 " + fmt + " 规约正文,不要任何解释文字,不要 Markdown 代码块围栏。";
    }

    /** 把规约元信息拼成 LLM 输入 */
    private static String buildUserPrompt(Openspec t, String specType) {
        StringBuilder sb = new StringBuilder();
        sb.append("请生成一份 ").append(specType).append(" 规约。\n");
        sb.append("名称: ").append(t.getSpecName()).append("\n");
        sb.append("版本: ").append(StringUtils.isNotBlank(t.getVersion()) ? t.getVersion() : "1.0.0").append("\n");
        if (StringUtils.isNotBlank(t.getDescription())) sb.append("描述: ").append(t.getDescription()).append("\n");
        if (StringUtils.isNotBlank(t.getAgriKbRef()))   sb.append("AgriKB 引用: ").append(t.getAgriKbRef()).append("\n");
        return sb.toString();
    }

    /** LLM 偶尔会用 ```yaml ... ``` 包裹输出,落库前剥掉围栏 */
    private static String stripCodeFence(String text) {
        String s = text.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            if (firstNl > 0) s = s.substring(firstNl + 1);
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }

    private String buildAiSpec(String type, String name, String version, String kbRef) {
        String kb = kbRef != null ? kbRef : "agrikb://soil-sensor/v1";
        switch (type) {
            case "openapi":
                return "openapi: 3.1.0\n" +
                       "info:\n  title: " + name + "\n  version: \"" + version + "\"\n" +
                       "  description: AI Generated by AgriPLM (PRD §F3.5)\n" +
                       "  x-agrikb-ref: \"" + kb + "\"\n" +
                       "paths:\n" +
                       "  /business/soil-moisture:\n" +
                       "    get:\n" +
                       "      summary: 查询土壤墒情\n" +
                       "      responses:\n" +
                       "        '200':\n          description: OK\n";
            case "asyncapi":
                return "asyncapi: 3.0.0\n" +
                       "info:\n  title: " + name + "\n  version: \"" + version + "\"\n" +
                       "channels:\n" +
                       "  iot/soil/{fieldCode}:\n" +
                       "    address: 'agriplm/soil/{fieldCode}'\n" +
                       "    messages:\n      soilReading:\n        payload:\n          type: object\n";
            case "ai_function":
                return "{\n  \"name\": \"" + name + "\",\n" +
                       "  \"description\": \"AI Function Spec — AgriPLM\",\n" +
                       "  \"version\": \"" + version + "\",\n" +
                       "  \"x-agrikb-ref\": \"" + kb + "\",\n" +
                       "  \"parameters\": {\n    \"type\": \"object\",\n" +
                       "    \"properties\": { \"fieldCode\": { \"type\": \"string\" } },\n" +
                       "    \"required\": [\"fieldCode\"]\n  }\n}\n";
            default: // graphql
                return "# AI Generated GraphQL Schema — " + name + " v" + version + "\n" +
                       "type SoilReading {\n  fieldCode: String!\n  moisture: Float\n  temp: Float\n}\n" +
                       "type Query {\n  soilByField(fieldCode: String!): [SoilReading!]!\n}\n";
        }
    }

    private String generateOpenspecNo() {
        int year = LocalDate.now().getYear();
        String prefix = "SPEC-" + year + "-";
        Integer maxSeq = openspecMapper.selectMaxSeqOfYear(prefix);
        return String.format("%s%04d", prefix, (maxSeq == null ? 0 : maxSeq) + 1);
    }
}
