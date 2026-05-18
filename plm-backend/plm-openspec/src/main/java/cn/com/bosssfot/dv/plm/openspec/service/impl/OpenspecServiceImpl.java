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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;
import cn.com.bosssfot.dv.plm.openspec.mapper.OpenspecMapper;
import cn.com.bosssfot.dv.plm.openspec.service.IOpenspecService;

/**
 * AI规范中心 Service 实现
 *
 * 落地:
 * - generateOpenspecNo() — OSP-YYYY-NNNN
 * - specType 白名单: openapi31/asyncapi30/graphql/ai_function
 * - 状态机: 00→{01}, 01→{02,00}, 02→{03}, 03→{}
 * - aiGenerate(): 生成 OpenAPI 3.1 YAML（含 x-agrikb-ref）, 状态→01
 */
@Service
public class OpenspecServiceImpl implements IOpenspecService
{
    private static final Logger log = LoggerFactory.getLogger(OpenspecServiceImpl.class);

    private static final Set<String> ALLOWED_SPEC_TYPE =
        Set.of("openapi31", "asyncapi30", "graphql", "ai_function");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02", "00"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired private OpenspecMapper openspecMapper;

    @Override
    public List<Openspec> selectOpenspecList(Openspec openspec) {
        return openspecMapper.selectOpenspecList(openspec);
    }

    @Override
    public Openspec selectOpenspecById(Long openspecId) {
        return openspecMapper.selectOpenspecById(openspecId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOpenspec(Openspec openspec) {
        if (StringUtils.isBlank(openspec.getSpecName())) {
            throw new ServiceException("规范名称不能为空", 602);
        }
        if (openspec.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (StringUtils.isBlank(openspec.getSpecType())) {
            throw new ServiceException("规范类型不能为空", 602);
        }
        if (!ALLOWED_SPEC_TYPE.contains(openspec.getSpecType())) {
            throw new ServiceException("规范类型仅支持 openapi31/asyncapi30/graphql/ai_function", 604);
        }
        if (StringUtils.isBlank(openspec.getAiEnhanced())) openspec.setAiEnhanced("N");
        if (StringUtils.isBlank(openspec.getAgrikbRef())) openspec.setAgrikbRef("N");
        if (StringUtils.isBlank(openspec.getAiGenerated())) openspec.setAiGenerated("N");
        if (StringUtils.isBlank(openspec.getStatus())) {
            openspec.setStatus("00");
        } else if (!"00".equals(openspec.getStatus())) {
            throw new ServiceException("新建规范状态必须为「草稿」", 601);
        }
        if (StringUtils.isBlank(openspec.getOpenspecNo())) {
            openspec.setOpenspecNo(generateOpenspecNo());
        }
        openspec.setCreateBy(SecurityUtils.getUsername());
        return openspecMapper.insertOpenspec(openspec);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOpenspec(Openspec openspec) {
        Openspec old = openspecMapper.selectOpenspecById(openspec.getOpenspecId());
        if (old == null) {
            throw new ServiceException("规范不存在", 404);
        }
        if (StringUtils.isNotBlank(openspec.getStatus()) && !openspec.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(openspec.getStatus())) {
                throw new ServiceException(
                    "规范状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(openspec.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(openspec.getSpecType())
                && !ALLOWED_SPEC_TYPE.contains(openspec.getSpecType())) {
            throw new ServiceException("规范类型仅支持 openapi31/asyncapi30/graphql/ai_function", 604);
        }
        openspec.setUpdateBy(SecurityUtils.getUsername());
        return openspecMapper.updateOpenspec(openspec);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOpenspecByIds(Long[] openspecIds) {
        return openspecMapper.deleteOpenspecByIds(openspecIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Openspec aiGenerate(Long openspecId) {
        Openspec s = openspecMapper.selectOpenspecById(openspecId);
        if (s == null) {
            throw new ServiceException("规范不存在", 404);
        }
        String specName = s.getSpecName() == null ? "AgriPLM API" : s.getSpecName();
        String version  = s.getVersion()  == null ? "1.0.0"       : s.getVersion();
        String content =
            "openapi: '3.1.0'\n"
            + "info:\n"
            + "  title: " + specName + "\n"
            + "  version: '" + version + "'\n"
            + "  x-agrikb-ref: true\n"
            + "  x-agrikb-domain: agri-plm\n"
            + "  x-ai-enhanced: true\n"
            + "paths:\n"
            + "  /api/v1/resources:\n"
            + "    get:\n"
            + "      summary: 查询资源列表\n"
            + "      operationId: listResources\n"
            + "      tags: [resources]\n"
            + "      parameters:\n"
            + "        - name: pageNum\n"
            + "          in: query\n"
            + "          schema: { type: integer, default: 1 }\n"
            + "        - name: pageSize\n"
            + "          in: query\n"
            + "          schema: { type: integer, default: 10 }\n"
            + "      responses:\n"
            + "        '200':\n"
            + "          description: 操作成功\n"
            + "          content:\n"
            + "            application/json:\n"
            + "              schema:\n"
            + "                type: object\n"
            + "                properties:\n"
            + "                  code: { type: integer, example: 200 }\n"
            + "                  msg:  { type: string,  example: '操作成功' }\n"
            + "                  data: { type: object }\n"
            + "components:\n"
            + "  schemas:\n"
            + "    AgriResource:\n"
            + "      type: object\n"
            + "      x-agrikb-entity: resource\n"
            + "      properties:\n"
            + "        id:   { type: integer }\n"
            + "        name: { type: string }\n";
        s.setContent(content);
        s.setAiGenerated("Y");
        s.setAiGeneratedAt(new Date());
        s.setAiEnhanced("Y");
        s.setAgrikbRef("Y");
        s.setStatus("01");
        s.setUpdateBy(SecurityUtils.getUsername());
        openspecMapper.updateOpenspec(s);
        return s;
    }

    private String generateOpenspecNo() {
        int year = LocalDate.now().getYear();
        String prefix = "OSP-" + year + "-";
        Integer maxSeq = openspecMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "审核中";
            case "02": return "已发布";
            case "03": return "已废弃";
            default:   return "未知(" + status + ")";
        }
    }
}
