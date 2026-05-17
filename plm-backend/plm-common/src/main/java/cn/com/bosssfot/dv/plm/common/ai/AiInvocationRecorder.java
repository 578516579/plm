package cn.com.bosssfot.dv.plm.common.ai;

import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * AI 调用审计 SPI — 由业务模块(plm-ai-agent)提供实现,plm-common 只声明接口。
 *
 * <p>{@link AiService} 通过可选注入持有此接口,每次 chat() 调用后写一条审计记录。
 * 若未注入(无实现),则跳过审计;不影响主链路。</p>
 *
 * <p>设计原则:</p>
 * <ul>
 *   <li>审计是横切关注点,不在业务关键路径上(异常/慢都不影响业务)</li>
 *   <li>实现层在 plm-ai-agent 中(InvocationLogServiceImpl implements AiInvocationRecorder)</li>
 *   <li>审计写库失败必须吃掉异常,绝不抛出</li>
 * </ul>
 *
 * @author plm
 */
public interface AiInvocationRecorder {

    /**
     * 记录一次 AI 调用。实现必须保证不抛异常(吃掉 DB/网络错误)。
     */
    void record(AiChatRequest request, AiChatResult result);
}
