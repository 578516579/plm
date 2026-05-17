package cn.com.bosssfot.dv.plm.aiagent.invocationlog.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * AI 调用审计日志 tb_ai_invocation_log
 *
 * <p>每次 AiService.chat() 写一条,可按 caller_tag/provider/model/success 聚合查询。</p>
 *
 * @author plm
 */
public class AiInvocationLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long logId;
    @Excel(name = "调用方") private String callerTag;
    @Excel(name = "Provider") private String provider;
    @Excel(name = "模型") private String model;
    @Excel(name = "成功") private Integer success;     // 0/1
    private String finishReason;
    @Excel(name = "输入token") private Long promptTokens;
    @Excel(name = "输出token") private Long completionTokens;
    @Excel(name = "总token") private Long totalTokens;
    @Excel(name = "耗时ms") private Long elapsedMs;
    private String requestId;
    private String errorMsg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "调用时间") private Date invokedAt;

    public Long getLogId() { return logId; }
    public void setLogId(Long v) { this.logId = v; }
    public String getCallerTag() { return callerTag; }
    public void setCallerTag(String v) { this.callerTag = v; }
    public String getProvider() { return provider; }
    public void setProvider(String v) { this.provider = v; }
    public String getModel() { return model; }
    public void setModel(String v) { this.model = v; }
    public Integer getSuccess() { return success; }
    public void setSuccess(Integer v) { this.success = v; }
    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String v) { this.finishReason = v; }
    public Long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Long v) { this.promptTokens = v; }
    public Long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Long v) { this.completionTokens = v; }
    public Long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Long v) { this.totalTokens = v; }
    public Long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Long v) { this.elapsedMs = v; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String v) { this.requestId = v; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String v) { this.errorMsg = v; }
    public Date getInvokedAt() { return invokedAt; }
    public void setInvokedAt(Date v) { this.invokedAt = v; }
}
