package cn.com.bosssfot.dv.plm.integration.adapter;

import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;

/**
 * 外部系统适配器接口。
 *
 * <p>每个 connector_type（feishu/gitlab/...）实现一个 Bean，
 *    {@link ConnectorAdapterRegistry} 启动期按 {@link #type()} 索引。
 *
 * <p>本期只暴露最小操作：
 * <ul>
 *   <li>{@link #ping(IntegrationConnector)} - 连通性测试</li>
 *   <li>{@link #verifyWebhookSignature} - 入站 webhook 验签</li>
 * </ul>
 *
 * <p>类型特定的业务调用（发飞书消息 / 拉 GitLab MR）走各 adapter 自己的 public 方法，
 *    业务模块直接依赖 {@code FeishuConnectorAdapter} / {@code GitLabConnectorAdapter}。
 */
public interface ConnectorAdapter {

    /** 适配的 connector_type（同字典 biz_integration_type） */
    String type();

    /**
     * 连通性 + 鉴权测试，返回人类可读的状态描述。
     * @throws Exception 任何异常 → 测试失败
     */
    String ping(IntegrationConnector connector) throws Exception;

    /**
     * 入站 webhook 验签。
     *
     * @param connector 连接器配置（含 webhook_secret）
     * @param signature webhook 头部传入的签名
     * @param timestamp 时间戳（若类型需要，否则忽略）
     * @param rawBody   原始 payload 字节
     * @return true 验签通过
     */
    boolean verifyWebhookSignature(IntegrationConnector connector, String signature, String timestamp, byte[] rawBody);
}
