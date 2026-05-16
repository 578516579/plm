package cn.com.bosssfot.dv.plm.document.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.document.domain.Document;

public interface DocumentMapper {
    List<Document> selectDocumentList(Document document);
    Document selectDocumentById(Long documentId);
    int insertDocument(Document document);
    int updateDocument(Document document);
    int deleteDocumentByIds(Long[] documentIds);
    /** ADR-0007: DOC-<TYPE>-YYYY-NNNN 流水号按 type 分别累加 */
    Integer selectMaxSeqOfYearByType(@Param("prefix") String prefix);
}
