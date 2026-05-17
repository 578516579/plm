package cn.com.bosssfot.dv.plm.common.dify;

import java.util.Map;
import cn.com.bosssfot.dv.plm.common.dify.dto.DifyWorkflowResult;

/**
 * Dify 集成统一门面 — 业务层(各 plm-* 业务模块)只依赖此接口。
 *
 * <p>实现策略:</p>
 * <ul>
 *   <li>{@code plm.dify.enabled=true} 且 api-key 有效 → 装配 {@code DifyServiceHttpImpl},真调 Dify Service API。</li>
 *   <li>否则 → 装配 {@code DifyServiceMockImpl},返回固定占位输出,保证本地启动零依赖、E2E 不抖动。</li>
 * </ul>
 *
 * <p>路由约定:每个 {@link cn.com.bosssfot.dv.plm.common.dify.dto.DifyWorkflowResult} 调用必须给定 workflow_id。
 * 推荐链路:</p>
 * <ol>
 *   <li>业务表(如 tb_ai_agent)持有 dify_workflow_id —— 直接取 → {@link #runWorkflow(String, Map)}。</li>
 *   <li>业务表没有 → 走 {@link #runWorkflowByType(String, Map)},用 agent_type 在 {@link DifyProperties#getWorkflows()} 兜底。</li>
 * </ol>
 *
 * <p>所有方法保证 <b>不抛受检异常</b>:HTTP 失败/超时/Dify 返回 4xx 5xx 时返回 {@code success=false} 的 Result。
 * 业务层根据 {@link DifyWorkflowResult#isSuccess()} 决定是否回退/降级,或抛 {@code ServiceException(708, msg)}。</p>
 *
 * @author plm
 */
public interface DifyService {

    /**
     * 调用指定 workflow,阻塞等待结果(response_mode=blocking)。
     *
     * @param workflowId Dify workflow ID (如 wf-xxxxxxxx),非空
     * @param inputs     workflow 输入参数 (与 Dify 流程节点的 inputs 对齐),允许空
     * @return 永不为 null;失败时 success=false 且 errorMessage 非空
     */
    DifyWorkflowResult runWorkflow(String workflowId, Map<String, Object> inputs);

    /**
     * 按 agent_type 路由(配合 {@link DifyProperties#getWorkflows()} 兜底)。
     *
     * @param agentType 如 "requirement"/"prd"/"code"/"test"/"release"/"ops"
     * @param inputs    workflow 输入参数
     */
    DifyWorkflowResult runWorkflowByType(String agentType, Map<String, Object> inputs);

    /**
     * 是否当前装配的是真实 HTTP 实现 (false → Mock)。供 healthcheck/管理端展示。
     */
    boolean isLive();
}
