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

        String specType = t.getSpecType() != null ? t.getSpecType() : "openapi";
        t.setSpecContent(buildAiSpec(specType, t.getSpecName(), t.getVersion(), t.getAgriKbRef()));
        t.setAiGenerated("Y");
        t.setAiGeneratedAt(new Date());
        t.setUpdateBy("ai-agent");
        openspecMapper.updateOpenspec(t);
        return openspecMapper.selectOpenspecById(id);
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
