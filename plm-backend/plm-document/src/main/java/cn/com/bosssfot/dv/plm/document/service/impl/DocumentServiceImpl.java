package cn.com.bosssfot.dv.plm.document.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.document.domain.Document;
import cn.com.bosssfot.dv.plm.document.mapper.DocumentMapper;
import cn.com.bosssfot.dv.plm.document.service.IDocumentService;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 文档 Service 实现
 *
 * 落地:
 * - ADR-0007: DOC-<TYPE>-YYYY-NNNN 编号 (按 type 分别累加)
 * - PRD §4: 4×4 状态机含反向边 01→00 + 02→01 (重审)
 * - 进入 02 必填 reviewer_user_id → 707
 * - doc_type 字典内 → 604
 * - FK projectId 存在 → 702
 */
@Service
public class DocumentServiceImpl implements IDocumentService
{
    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    /** 4×4 状态机含反向边 */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),         // 草稿 → 待评审
        "01", Set.of("00", "02"),   // 待评审 → 草稿(反向打回) / 已发布
        "02", Set.of("01", "03"),   // 已发布 → 待评审(反向重审) / 已归档
        "03", Set.of()              // 已归档 终态
    );

    /** 合法的 doc_type 集合 */
    private static final Set<String> VALID_DOC_TYPES = Set.of(
        "prd", "arch", "db_design", "api_design", "proposal",
        "ued", "test_plan", "test_report", "api_doc",
        "manual_product", "manual_impl", "manual_ops"
    );

    @Autowired private DocumentMapper documentMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override public List<Document> selectDocumentList(Document d) { return documentMapper.selectDocumentList(d); }
    @Override public Document selectDocumentById(Long id) { return documentMapper.selectDocumentById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDocument(Document d) {
        if (StringUtils.isBlank(d.getTitle())) throw new ServiceException("文档标题不能为空", 602);
        if (StringUtils.isBlank(d.getDocType())) throw new ServiceException("doc_type 不能为空", 602);
        if (d.getProjectId() == null) throw new ServiceException("关联项目不能为空", 602);
        if (!VALID_DOC_TYPES.contains(d.getDocType())) {
            throw new ServiceException("doc_type 不合法: " + d.getDocType(), 604);
        }
        if (projectMapper.selectProjectById(d.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // 默认值
        if (StringUtils.isBlank(d.getVersion())) d.setVersion("v1.0");
        if (d.getAuthorUserId() == null) d.setAuthorUserId(SecurityUtils.getUserId());

        // 新建状态必须 00
        if (StringUtils.isBlank(d.getStatus())) d.setStatus("00");
        else if (!"00".equals(d.getStatus())) throw new ServiceException("新建文档状态必须为「草稿」", 601);

        if (StringUtils.isBlank(d.getDocumentNo())) d.setDocumentNo(generateDocumentNo(d.getDocType()));
        d.setCreateBy(SecurityUtils.getUsername());

        try {
            return documentMapper.insertDocument(d);
        } catch (DuplicateKeyException e) {
            log.warn("document_no 重号,重试: {}", d.getDocumentNo());
            d.setDocumentNo(generateDocumentNo(d.getDocType()));
            return documentMapper.insertDocument(d);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDocument(Document d) {
        Document old = documentMapper.selectDocumentById(d.getDocumentId());
        if (old == null) throw new ServiceException("文档不存在", 404);

        if (StringUtils.isNotBlank(d.getStatus()) && !d.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(d.getStatus())) {
                throw new ServiceException(
                    "文档状态 " + statusLabel(old.getStatus()) + " 不能转到 " + statusLabel(d.getStatus()), 601);
            }
            // 进入 02 已发布 必填 reviewer
            if ("02".equals(d.getStatus())) {
                Long rev = d.getReviewerUserId() != null ? d.getReviewerUserId() : old.getReviewerUserId();
                if (rev == null) throw new ServiceException("进入「已发布」必须指定审核人", 707);
            }
        }

        if (StringUtils.isNotBlank(d.getDocType()) && !VALID_DOC_TYPES.contains(d.getDocType())) {
            throw new ServiceException("doc_type 不合法: " + d.getDocType(), 604);
        }
        if (d.getProjectId() != null && !d.getProjectId().equals(old.getProjectId())
                && projectMapper.selectProjectById(d.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        d.setUpdateBy(SecurityUtils.getUsername());
        return documentMapper.updateDocument(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDocumentByIds(Long[] ids) {
        return documentMapper.deleteDocumentByIds(ids);
    }

    /** ADR-0007: DOC-<TYPE_UPPER>-YYYY-NNNN */
    private String generateDocumentNo(String docType) {
        int year = LocalDate.now().getYear();
        String typeUpper = docType.toUpperCase().replace("_", "");
        String prefix = "DOC-" + typeUpper + "-" + year + "-";
        Integer maxSeq = documentMapper.selectMaxSeqOfYearByType(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String s) {
        return switch (s) {
            case "00" -> "草稿";
            case "01" -> "待评审";
            case "02" -> "已发布";
            case "03" -> "已归档";
            default   -> "未知(" + s + ")";
        };
    }
}
