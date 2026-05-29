package cn.com.bosssfot.dv.plm.apidesign.service.impl;

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
import cn.com.bosssfot.dv.plm.apidesign.domain.ApiDesign;
import cn.com.bosssfot.dv.plm.apidesign.mapper.ApiDesignMapper;
import cn.com.bosssfot.dv.plm.apidesign.service.IApiDesignService;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.AiTexts;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * LLD 接口详细设计 Service — PRD §F3.3 + 原型 apidesign.html
 *
 * 落地:
 * - ADR: generateApiDesignNo() — APID-YYYY-NNNN
 * - 4 状态机 (含反向边 01→00) 同 arch/dbdesign 模式
 * - HTTP method 白名单 → 604
 * - 唯一键 (project_id, http_method, path) 冲突 → 701
 * - aiGenerate() mock: 返回标准 OpenAPI 3.0 YAML 片段 + Mock 响应
 */
@Service
public class ApiDesignServiceImpl implements IApiDesignService
{
    private static final Logger log = LoggerFactory.getLogger(ApiDesignServiceImpl.class);

    private static final Set<String> ALLOWED_METHOD =
        Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),
        "01", Set.of("00", "02"),   // 评审中 → 草稿(反向打回) / 已确认
        "02", Set.of("03"),
        "03", Set.of()
    );

    @Autowired private ApiDesignMapper apidesignMapper;
    @Autowired private AiService aiService;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<ApiDesign> selectApiDesignList(ApiDesign t) { return apidesignMapper.selectApiDesignList(t); }

    @Override
    public ApiDesign selectApiDesignById(Long id) { return apidesignMapper.selectApiDesignById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertApiDesign(ApiDesign t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("接口设计标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (StringUtils.isBlank(t.getHttpMethod())) {
            throw new ServiceException("HTTP 方法不能为空", 602);
        }
        if (StringUtils.isBlank(t.getPath())) {
            throw new ServiceException("接口路径不能为空", 602);
        }
        if (!ALLOWED_METHOD.contains(t.getHttpMethod().toUpperCase())) {
            throw new ServiceException("HTTP 方法仅支持 GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS", 604);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("设计者不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        t.setHttpMethod(t.getHttpMethod().toUpperCase());
        if (StringUtils.isBlank(t.getMockEnabled())) t.setMockEnabled("N");
        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建接口设计状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getApidesignNo())) {
            t.setApidesignNo(generateApiDesignNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return apidesignMapper.insertApiDesign(t);
        } catch (DuplicateKeyException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("uk_apidesign_project_method_path")) {
                throw new ServiceException(
                    "项目内已存在相同 method+path 的接口 (" + t.getHttpMethod() + " " + t.getPath() + ")",
                    701
                );
            }
            log.warn("apidesign_no 重号,重试一次: {}", t.getApidesignNo());
            t.setApidesignNo(generateApiDesignNo());
            return apidesignMapper.insertApiDesign(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateApiDesign(ApiDesign t) {
        ApiDesign old = apidesignMapper.selectApiDesignById(t.getApidesignId());
        if (old == null) {
            throw new ServiceException("接口设计不存在", 404);
        }
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "接口设计状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(t.getHttpMethod())) {
            String upper = t.getHttpMethod().toUpperCase();
            if (!ALLOWED_METHOD.contains(upper)) {
                throw new ServiceException("HTTP 方法值非法", 604);
            }
            t.setHttpMethod(upper);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        try {
            return apidesignMapper.updateApiDesign(t);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("项目内已存在相同 method+path 的接口", 701);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteApiDesignByIds(Long[] ids) {
        return apidesignMapper.deleteApiDesignByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiDesign aiGenerate(Long apidesignId) {
        ApiDesign a = apidesignMapper.selectApiDesignById(apidesignId);
        if (a == null) {
            throw new ServiceException("接口设计不存在", 404);
        }
        AiChatRequest aiReq = AiChatRequest.builder("")
            .system("你是 PLM 资深 API 架构师,擅长 OpenAPI 3.0 与 mock 响应设计")
            .user("请生成 [" + a.getTitle() + "] " + a.getHttpMethod() + " " + a.getPath() + " 的 OpenAPI 定义")
            .callerTag("apidesign#" + apidesignId).build();
        String spec = "openapi: 3.0.3\n"
            + "info:\n  title: " + a.getTitle() + "\n  version: '1.0'\n"
            + "paths:\n  " + a.getPath() + ":\n"
            + "    " + a.getHttpMethod().toLowerCase() + ":\n"
            + "      summary: " + (a.getDescription() == null ? a.getTitle() : a.getDescription()) + "\n"
            + "      responses:\n"
            + "        '200':\n"
            + "          description: success\n"
            + "          content:\n"
            + "            application/json:\n"
            + "              schema:\n"
            + "                type: object\n"
            + "                properties:\n"
            + "                  code: { type: integer, example: 200 }\n"
            + "                  msg:  { type: string,  example: '操作成功' }\n"
            + "                  data: { type: object }\n";
        String reqSchema = "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
        String respSchema = "{\"type\":\"object\",\"properties\":{\"code\":{\"type\":\"integer\"},\"msg\":{\"type\":\"string\"},\"data\":{}}}";
        String mock = "{\"code\":200,\"msg\":\"操作成功\",\"data\":null}";
        a.setOpenapiSpec(AiTexts.generate(aiService,aiReq, () -> spec));
        a.setRequestSchema(reqSchema);
        a.setResponseSchema(respSchema);
        a.setMockResponse(mock);
        a.setAiGenerated("Y");
        a.setAiGeneratedAt(new Date());
        a.setUpdateBy(SecurityUtils.getUsername());
        apidesignMapper.updateApiDesign(a);
        return a;
    }

    private String generateApiDesignNo() {
        int year = LocalDate.now().getYear();
        String prefix = "APID-" + year + "-";
        Integer maxSeq = apidesignMapper.selectMaxSeqOfYear(prefix);
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
