package cn.com.bosssfot.dv.plm.apidoc.service.impl;

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
import cn.com.bosssfot.dv.plm.apidoc.domain.ApiDoc;
import cn.com.bosssfot.dv.plm.apidoc.mapper.ApiDocMapper;
import cn.com.bosssfot.dv.plm.apidoc.service.IApiDocService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * API 文档 Service — PRD §F5.4 + 原型 apidoc.html
 *
 * 落地:
 * - ADR: generateApiDocNo() — API-YYYY-NNNN
 * - 唯一键: (http_method, path, version) — 701
 * - 3 状态机: 00 草稿 → 01 已发布 → 02 已废弃
 *   - 00→{01}, 01→{02}, 02→{} (终态)
 * - autoExtracted='Y' 时自动填 lastSyncedAt
 */
@Service
public class ApiDocServiceImpl implements IApiDocService
{
    private static final Logger log = LoggerFactory.getLogger(ApiDocServiceImpl.class);

    private static final Set<String> ALLOWED_METHOD =
        Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of());
    }

    @Autowired private ApiDocMapper apidocMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<ApiDoc> selectApiDocList(ApiDoc t) {
        return apidocMapper.selectApiDocList(t);
    }

    @Override
    public ApiDoc selectApiDocById(Long id) {
        return apidocMapper.selectApiDocById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertApiDoc(ApiDoc t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("接口标题不能为空", 602);
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
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        t.setHttpMethod(t.getHttpMethod().toUpperCase());
        if (StringUtils.isBlank(t.getVersion())) t.setVersion("v1.0");
        if (StringUtils.isBlank(t.getAutoExtracted())) t.setAutoExtracted("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建 API 文档状态必须为「草稿」", 601);
        }
        if ("Y".equalsIgnoreCase(t.getAutoExtracted()) && t.getLastSyncedAt() == null) {
            t.setLastSyncedAt(new Date());
        }

        if (StringUtils.isBlank(t.getApidocNo())) {
            t.setApidocNo(generateApidocNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return apidocMapper.insertApiDoc(t);
        } catch (DuplicateKeyException e) {
            // 可能是 apidoc_no 重号,或 (method,path,version) 唯一键冲突
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("uk_apidoc_method_path")) {
                throw new ServiceException(
                    "已存在相同 method+path+version 的接口 ("
                        + t.getHttpMethod() + " " + t.getPath() + " " + t.getVersion() + ")",
                    701
                );
            }
            log.warn("apidoc_no 重号,重试一次: {}", t.getApidocNo());
            t.setApidocNo(generateApidocNo());
            return apidocMapper.insertApiDoc(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateApiDoc(ApiDoc t) {
        ApiDoc old = apidocMapper.selectApiDocById(t.getApidocId());
        if (old == null) {
            throw new ServiceException("API 文档不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "API 文档状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }

        if (StringUtils.isNotBlank(t.getHttpMethod())) {
            String upper = t.getHttpMethod().toUpperCase();
            if (!ALLOWED_METHOD.contains(upper)) {
                throw new ServiceException("HTTP 方法仅支持 GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS", 604);
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
            return apidocMapper.updateApiDoc(t);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(
                "已存在相同 method+path+version 的接口",
                701
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteApiDocByIds(Long[] ids) {
        return apidocMapper.deleteApiDocByIds(ids);
    }

    private String generateApidocNo() {
        int year = LocalDate.now().getYear();
        String prefix = "API-" + year + "-";
        Integer maxSeq = apidocMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "已发布";
            case "02": return "已废弃";
            default:   return "未知(" + status + ")";
        }
    }
}
