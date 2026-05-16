package cn.com.bosssfot.dv.plm.document.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.document.domain.Document;
import cn.com.bosssfot.dv.plm.document.service.IDocumentService;

@RestController
@RequestMapping("/business/document")
public class DocumentController extends BaseController
{
    @Autowired
    private IDocumentService documentService;

    @PreAuthorize("@ss.hasPermi('business:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(Document document) {
        startPage();
        return getDataTable(documentService.selectDocumentList(document));
    }

    @PreAuthorize("@ss.hasPermi('business:document:export')")
    @Log(title = "文档", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Document document) {
        List<Document> list = documentService.selectDocumentList(document);
        ExcelUtil<Document> util = new ExcelUtil<>(Document.class);
        util.exportExcel(response, list, "文档数据");
    }

    @PreAuthorize("@ss.hasPermi('business:document:query')")
    @GetMapping("/{documentId}")
    public AjaxResult getInfo(@PathVariable Long documentId) {
        return success(documentService.selectDocumentById(documentId));
    }

    @PreAuthorize("@ss.hasPermi('business:document:add')")
    @Log(title = "文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Document document) {
        return toAjax(documentService.insertDocument(document));
    }

    @PreAuthorize("@ss.hasPermi('business:document:edit')")
    @Log(title = "文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Document document) {
        return toAjax(documentService.updateDocument(document));
    }

    @PreAuthorize("@ss.hasPermi('business:document:remove')")
    @Log(title = "文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{documentIds}")
    public AjaxResult remove(@PathVariable Long[] documentIds) {
        return toAjax(documentService.deleteDocumentByIds(documentIds));
    }
}
