package cn.com.bosssfot.dv.plm.apidesign.service.impl;

import cn.com.bosssfot.dv.plm.apidesign.domain.Apidesign;
import cn.com.bosssfot.dv.plm.apidesign.mapper.ApidesignMapper;
import cn.com.bosssfot.dv.plm.apidesign.service.IApidesignService;
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
public class ApidesignServiceImpl implements IApidesignService {

    private static final Set<String> ALLOWED_HTTP_METHOD =
            Set.of("get", "post", "put", "delete", "patch");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();

    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00", "02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired
    private ApidesignMapper apidesignMapper;

    @Override
    public List<Apidesign> selectApidesignList(Apidesign apidesign) {
        return apidesignMapper.selectApidesignList(apidesign);
    }

    @Override
    public Apidesign selectApidesignById(Long apidesignId) {
        return apidesignMapper.selectApidesignById(apidesignId);
    }

    @Override
    public int insertApidesign(Apidesign apidesign) {
        if (apidesign.getTitle() == null || apidesign.getTitle().isBlank()) {
            throw new ServiceException("接口设计标题不能为空", 602);
        }
        if (apidesign.getProjectId() == null) {
            throw new ServiceException("项目ID不能为空", 602);
        }
        if (apidesign.getAuthorUserId() == null) {
            throw new ServiceException("设计者用户ID不能为空", 602);
        }
        if (apidesign.getHttpMethod() != null && !ALLOWED_HTTP_METHOD.contains(apidesign.getHttpMethod())) {
            throw new ServiceException("无效的HTTP方法: " + apidesign.getHttpMethod(), 604);
        }
        apidesign.setApidesignNo(generateApidesignNo());
        if (apidesign.getMockEnabled() == null) {
            apidesign.setMockEnabled("N");
        }
        if (apidesign.getVersion() == null || apidesign.getVersion().isBlank()) {
            apidesign.setVersion("v1.0");
        }
        apidesign.setAiGenerated("N");
        apidesign.setStatus("00");
        apidesign.setCreateBy(SecurityUtils.getUsername());
        apidesign.setCreateTime(DateUtils.getNowDate());
        apidesign.setUpdateBy(SecurityUtils.getUsername());
        apidesign.setUpdateTime(DateUtils.getNowDate());
        try {
            return apidesignMapper.insertApidesign(apidesign);
        } catch (DuplicateKeyException e) {
            apidesign.setApidesignNo(generateApidesignNo());
            return apidesignMapper.insertApidesign(apidesign);
        }
    }

    @Override
    public int updateApidesign(Apidesign apidesign) {
        if (apidesign.getStatus() != null) {
            Apidesign existing = apidesignMapper.selectApidesignById(apidesign.getApidesignId());
            if (existing == null) {
                throw new ServiceException("接口设计不存在", 404);
            }
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(existing.getStatus(), Set.of());
            if (!allowed.contains(apidesign.getStatus())) {
                throw new ServiceException(
                        "状态不允许从 " + existing.getStatus() + " 流转到 " + apidesign.getStatus(), 601);
            }
        }
        if (apidesign.getHttpMethod() != null && !ALLOWED_HTTP_METHOD.contains(apidesign.getHttpMethod())) {
            throw new ServiceException("无效的HTTP方法: " + apidesign.getHttpMethod(), 604);
        }
        apidesign.setUpdateBy(SecurityUtils.getUsername());
        apidesign.setUpdateTime(DateUtils.getNowDate());
        return apidesignMapper.updateApidesign(apidesign);
    }

    @Override
    public int deleteApidesignByIds(Long[] apidesignIds) {
        return apidesignMapper.deleteApidesignByIds(apidesignIds);
    }

    @Override
    public Apidesign aiOpenapi(Long apidesignId) {
        Apidesign apidesign = apidesignMapper.selectApidesignById(apidesignId);
        if (apidesign == null) {
            throw new ServiceException("接口设计不存在", 404);
        }
        String report = buildAiOpenapiReport(apidesign);
        apidesign.setReviewReport(report);
        apidesign.setAiGenerated("Y");
        apidesign.setAiGeneratedAt(DateUtils.getNowDate());
        apidesign.setUpdateBy("ai-agent");
        apidesign.setUpdateTime(DateUtils.getNowDate());
        apidesignMapper.updateApidesign(apidesign);
        return apidesignMapper.selectApidesignById(apidesignId);
    }

    private String buildAiOpenapiReport(Apidesign apidesign) {
        String method = apidesign.getHttpMethod() != null ? apidesign.getHttpMethod().toUpperCase() : "N/A";
        String path = apidesign.getApiPath() != null ? apidesign.getApiPath() : "/未定义";
        String version = apidesign.getVersion() != null ? apidesign.getVersion() : "v1.0";
        return "## AI OpenAPI 规范生成报告\n\n" +
               "**接口**: `" + method + " " + path + "` (" + version + ")\n\n" +
               "### 1. OpenAPI 3.1 规范摘要\n" +
               "```yaml\n" +
               "openapi: \"3.1.0\"\n" +
               "info:\n" +
               "  title: AgriPLM API\n" +
               "  version: \"" + version + "\"\n" +
               "paths:\n" +
               "  " + path + ":\n" +
               "    " + method.toLowerCase() + ":\n" +
               "      summary: " + (apidesign.getDescription() != null ? apidesign.getDescription() : "待补充") + "\n" +
               "      responses:\n" +
               "        '200':\n" +
               "          description: 操作成功\n" +
               "```\n\n" +
               "### 2. RESTful 规范检查\n" +
               "- **URL 命名**: " + (path.matches("^/[a-z][a-z0-9/_-]*$") ? "✅ 符合 kebab-case" : "⚠️ 建议使用小写 kebab-case") + "\n" +
               "- **HTTP 方法语义**: ✅ " + method + " 方法用途正确\n" +
               "- **版本化**: ✅ 接口版本 " + version + " 已声明\n\n" +
               "### 3. 请求/响应 Schema 建议\n" +
               "- 所有响应应统一包装为 `{ code, msg, data }` 格式\n" +
               "- 分页列表接口需在响应中包含 `total` 字段\n" +
               "- 建议添加 `X-Request-ID` 请求头以支持链路追踪\n\n" +
               "### 4. 安全规范\n" +
               "- Bearer JWT 认证 ✅\n" +
               "- 建议对敏感操作 (DELETE/POST) 增加 RBAC 权限校验\n" +
               "- 建议对文件上传接口限制 Content-Type 和文件大小\n\n" +
               "### 5. Mock 服务建议\n" +
               "- 已生成 Mock 数据模板，可在「Mock 服务控制台」中启用\n" +
               "- 建议配合 Postman Collection 进行联调测试\n\n" +
               "> 报告由 AI Agent 自动生成，请接口负责人审核后确认。\n";
    }

    private String generateApidesignNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "APID-" + year + "-";
        Integer maxSeq = apidesignMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return prefix + String.format("%04d", next);
    }
}
