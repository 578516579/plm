package cn.com.bosssfot.dv.plm.integration.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * Webhook 事件审计 - 列表 + 详情 + 重试（仅失败状态允许）
 */
@RestController
@RequestMapping("/business/integration/webhook")
public class IntegrationWebhookEventController extends BaseController
{
    @Autowired
    private IIntegrationWebhookEventService eventService;

    @PreAuthorize("@ss.hasPermi('business:integration:webhook:list')")
    @GetMapping("/list")
    public TableDataInfo list(IntegrationWebhookEvent event) {
        startPage();
        return getDataTable(eventService.selectEventList(event));
    }

    @PreAuthorize("@ss.hasPermi('business:integration:webhook:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(eventService.selectEventById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:integration:webhook:retry')")
    @Log(title = "Webhook 事件重试", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/retry")
    public AjaxResult retry(@PathVariable("id") Long id) {
        return toAjax(eventService.retry(id));
    }

    @PreAuthorize("@ss.hasPermi('business:integration:webhook:export')")
    @Log(title = "Webhook 事件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IntegrationWebhookEvent event) {
        List<IntegrationWebhookEvent> list = eventService.selectEventList(event);
        ExcelUtil<IntegrationWebhookEvent> util = new ExcelUtil<>(IntegrationWebhookEvent.class);
        util.exportExcel(response, list, "Webhook 事件");
    }
}
