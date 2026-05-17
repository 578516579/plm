package cn.com.bosssfot.dv.plm.aiagent.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;
import cn.com.bosssfot.dv.plm.aiagent.service.IAiAgentService;

/**
 * AI Agent 控制器
 *
 * REST 路径：/business/ai-agent
 * 权限串前缀：business:ai-agent
 */
@RestController
@RequestMapping("/business/ai-agent")
public class AiAgentController extends BaseController
{
    @Autowired
    private IAiAgentService aiAgentService;

    /** 列表（分页 + 搜索） */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiAgent aiAgent)
    {
        startPage();
        List<AiAgent> list = aiAgentService.selectAiAgentList(aiAgent);
        return getDataTable(list);
    }

    /** 导出 */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:export')")
    @Log(title = "AI Agent", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiAgent aiAgent)
    {
        List<AiAgent> list = aiAgentService.selectAiAgentList(aiAgent);
        ExcelUtil<AiAgent> util = new ExcelUtil<AiAgent>(AiAgent.class);
        util.exportExcel(response, list, "AI Agent数据");
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(aiAgentService.selectAiAgentById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:add')")
    @Log(title = "AI Agent", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AiAgent aiAgent)
    {
        return toAjax(aiAgentService.insertAiAgent(aiAgent));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:edit')")
    @Log(title = "AI Agent", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AiAgent aiAgent)
    {
        return toAjax(aiAgentService.updateAiAgent(aiAgent));
    }

    /** 删除（逻辑） */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:remove')")
    @Log(title = "AI Agent", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(aiAgentService.deleteAiAgentByIds(ids));
    }

    /**
     * 切换 Agent 状态（启动/暂停）
     *
     * 请求体：{ "status": "0" }
     */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:edit')")
    @Log(title = "AI Agent 状态切换", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public AjaxResult changeStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> body)
    {
        String newStatus = body.get("status");
        if (newStatus == null) {
            return error("status 参数不能为空");
        }
        return toAjax(aiAgentService.changeAgentStatus(id, newStatus));
    }
}
