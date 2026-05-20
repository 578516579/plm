package cn.com.bosssfot.dv.plm.integration.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * Webhook 入站事件流水 tb_integration_webhook_event
 *
 * <p>不可逻辑删除（审计字段）。
 */
public class IntegrationWebhookEvent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    /** 来源 connector */
    private Long connectorId;

    /** 事件类型（如 feishu.im.message.receive_v1 / gitlab.merge_request） */
    @Excel(name = "事件")
    private String eventType;

    /** 外部 event id（幂等键） */
    private String externalEventId;

    /** 原始 payload JSON */
    private String payloadJson;

    /** 签名头 */
    private String signature;

    /** 验签是否通过：0 失败 / 1 通过 */
    @Excel(name = "验签", dictType = "biz_yes_no")
    private String signatureVerified;

    /** 处理状态（字典 biz_webhook_status：0=待 1=中 2=成 3=败 4=略） */
    @Excel(name = "状态", dictType = "biz_webhook_status")
    private String processStatus;

    /** 失败原因 */
    private String processError;

    /** 重试次数 */
    @Excel(name = "重试")
    private Integer retryCount;

    /** 调用方 IP */
    private String sourceIp;

    /** 处理完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date processTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConnectorId() { return connectorId; }
    public void setConnectorId(Long connectorId) { this.connectorId = connectorId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getExternalEventId() { return externalEventId; }
    public void setExternalEventId(String externalEventId) { this.externalEventId = externalEventId; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getSignatureVerified() { return signatureVerified; }
    public void setSignatureVerified(String signatureVerified) { this.signatureVerified = signatureVerified; }

    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }

    public String getProcessError() { return processError; }
    public void setProcessError(String processError) { this.processError = processError; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public Date getProcessTime() { return processTime; }
    public void setProcessTime(Date processTime) { this.processTime = processTime; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("connectorId", getConnectorId())
            .append("eventType", getEventType())
            .append("externalEventId", getExternalEventId())
            .append("signatureVerified", getSignatureVerified())
            .append("processStatus", getProcessStatus())
            .append("retryCount", getRetryCount())
            .append("sourceIp", getSourceIp())
            .append("createTime", getCreateTime())
            .append("processTime", getProcessTime())
            .toString();
    }
}
