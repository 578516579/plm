package cn.com.bosssfot.dv.plm.common.dify.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dify workflow 执行结果 — 业务层只关心三件事:
 *   1) 是否成功 (success)
 *   2) 输出 (outputs map,Dify workflow 节点的 outputs)
 *   3) 失败时的错误信息 (errorMessage) 与原始响应 ID,便于审计追踪
 *
 * 不暴露 HTTP 细节,业务层与 HTTP/Mock 实现解耦。
 *
 * @author plm
 */
public class DifyWorkflowResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    /** Dify workflow_run_id,用于审计/追溯。Mock 模式给空串。 */
    private String workflowRunId;
    /** Dify task_id */
    private String taskId;
    /** workflow outputs(节点输出),业务层按 key 取 */
    private Map<String, Object> outputs;
    /** elapsed time (秒),失败时为 0 */
    private double elapsedSeconds;
    /** Token 消耗 */
    private long totalTokens;
    /** 失败原因 — success=true 时为 null */
    private String errorMessage;

    public static DifyWorkflowResult ok(String runId, String taskId, Map<String, Object> outputs,
                                         double elapsed, long tokens) {
        DifyWorkflowResult r = new DifyWorkflowResult();
        r.success = true;
        r.workflowRunId = runId;
        r.taskId = taskId;
        r.outputs = outputs == null ? Collections.emptyMap() : outputs;
        r.elapsedSeconds = elapsed;
        r.totalTokens = tokens;
        return r;
    }

    public static DifyWorkflowResult fail(String msg) {
        DifyWorkflowResult r = new DifyWorkflowResult();
        r.success = false;
        r.errorMessage = msg;
        r.outputs = new LinkedHashMap<>();
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean v) { this.success = v; }
    public String getWorkflowRunId() { return workflowRunId; }
    public void setWorkflowRunId(String v) { this.workflowRunId = v; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String v) { this.taskId = v; }
    public Map<String, Object> getOutputs() { return outputs; }
    public void setOutputs(Map<String, Object> v) { this.outputs = v; }
    public double getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(double v) { this.elapsedSeconds = v; }
    public long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(long v) { this.totalTokens = v; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String v) { this.errorMessage = v; }
}
