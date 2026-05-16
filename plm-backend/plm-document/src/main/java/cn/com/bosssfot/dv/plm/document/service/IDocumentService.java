package cn.com.bosssfot.dv.plm.document.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.document.domain.Document;

public interface IDocumentService {
    List<Document> selectDocumentList(Document document);
    Document selectDocumentById(Long documentId);
    int insertDocument(Document document);
    int updateDocument(Document document);
    int deleteDocumentByIds(Long[] documentIds);
}
