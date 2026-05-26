package cn.com.bosssfot.dv.plm.integration.sync;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.integration.adapter.zentao.ZentaoConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.adapter.zentao.ZentaoFieldMapper;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationUserMappingService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;
import cn.com.bosssfot.dv.plm.integration.service.impl.IntegrationWebhookEventServiceImpl.WebhookReceived;

/**
 * 禅道 → PLM 入站同步 Service。
 *
 * <p>消费 {@link WebhookReceived} 事件(由 {@link IIntegrationWebhookEventService#receive} 异步发布),
 * 用 {@link JdbcTemplate} 直接读写 4 张业务表(tb_defect / tb_requirement / tb_task / tb_testcase),
 * 不走业务 Service(规避业务状态机校验,但写入前自己做 last-write-wins 比对)。
 *
 * <p>关键决策:
 * <ul>
 *   <li>Webhook payload 走 {@code data} 对象(禅道 v18 webhook 标准格式)</li>
 *   <li>状态机映射走 {@link ZentaoFieldMapper};未识别值落 {@code 99}(字典已加 "外部同步")</li>
 *   <li>用户映射走 {@link IIntegrationUserMappingService},缺映射时容忍(reporter/assignee 留 null)</li>
 *   <li>SyncContext.inbound 在 try-finally 内设置,防止后续业务 ApplicationEvent 触发出站循环</li>
 * </ul>
 *
 * <p>错误处理:
 * <ul>
 *   <li>productProjectMap 缺映射 → 标记 webhook event {@code process_status=3} 失败,{@code errorCode=816}</li>
 *   <li>stale(PLM 更新 ≥ 禅道) → 标记 {@code process_status=4} 已忽略,{@code errorCode=819}</li>
 *   <li>未识别 objectType → 标记 {@code process_status=4}</li>
 * </ul>
 *
 * @see <a href="../../../99-跨阶段/proposals/0014-zentao-bidirectional-sync.md">Proposal 0014</a>
 */
@Service
public class ZentaoInboundSyncService {

    private static final Logger log = LoggerFactory.getLogger(ZentaoInboundSyncService.class);
    private static final String SOURCE = "zentao";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private IIntegrationWebhookEventService eventService;

    @Autowired
    private IIntegrationUserMappingService userMapService;

    @Autowired
    private ZentaoConnectorAdapter zentaoAdapter;

    @EventListener
    @Async
    public void onWebhookReceived(WebhookReceived ev) {
        IntegrationWebhookEvent event = ev.getEvent();
        if (event == null || event.getEventType() == null || !event.getEventType().startsWith("zentao.")) {
            return; // 非禅道事件,忽略
        }
        IntegrationConnector connector = connectorService.selectConnectorById(event.getConnectorId());
        if (connector == null || !SOURCE.equals(connector.getConnectorType())) {
            eventService.markProcessed(event.getId(), "4", "connector 不匹配或被删除");
            return;
        }
        try {
            dispatch(connector, event);
        } catch (StaleException stale) {
            eventService.markProcessed(event.getId(), "4", "[errorCode=819] stale: PLM newer");
        } catch (UnmappedException um) {
            eventService.markProcessed(event.getId(), "3", "[errorCode=" + um.errorCode + "] " + um.getMessage());
        } catch (Exception e) {
            log.error("[plm-integration/zentao] 入站同步失败 eventId={}", event.getId(), e);
            eventService.markProcessed(event.getId(), "3", e.getMessage());
        }
    }

    private void dispatch(IntegrationConnector connector, IntegrationWebhookEvent event) throws Exception {
        JSONObject payload = JSON.parseObject(event.getPayloadJson());
        if (payload == null) {
            eventService.markProcessed(event.getId(), "4", "payload 解析失败");
            return;
        }
        String objectType = payload.getString("objectType");
        JSONObject data = payload.getJSONObject("data");
        if (data == null) {
            eventService.markProcessed(event.getId(), "4", "payload.data 缺失");
            return;
        }
        if (objectType == null) {
            eventService.markProcessed(event.getId(), "4", "objectType 缺失");
            return;
        }
        SyncContext.setInbound(true);
        try {
            switch (objectType) {
                case "bug"   -> syncBug(connector, data, payload);
                case "story" -> syncStory(connector, data, payload);
                case "task"  -> syncTask(connector, data, payload);
                case "case"  -> syncCase(connector, data, payload);
                default -> {
                    eventService.markProcessed(event.getId(), "4", "未识别 objectType=" + objectType);
                    return;
                }
            }
            eventService.markProcessed(event.getId(), "2", null);
        } finally {
            SyncContext.clear();
        }
    }

    // ───────── Bug ───────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncBug(IntegrationConnector connector, JSONObject data, JSONObject payload) {
        String externalId = data.getString("id");
        Date lastEdited = parseDate(data.getString("lastEditedDate"), data.getString("openedDate"));

        Long projectId = mapProductToProject(connector, data.getInteger("product"), "bug");
        Long reporterId = userMapService.resolveUserIdByExternalAccount(connector.getId(), data.getString("openedBy"));
        Long assigneeId = userMapService.resolveUserIdByExternalAccount(connector.getId(), data.getString("assignedTo"));

        Map<String, Object> fields = ZentaoFieldMapper.bugPayloadToDefectFields(data);

        Long existingId = findExistingId("tb_defect", "defect_id", externalId);
        final String externalUrl = resolveExternalUrl(payload, connector, "bug", externalId);

        if (existingId == null) {
            String defectNo = buildExtDefectNo(externalId);
            jdbc.update("""
                INSERT INTO tb_defect
                    (defect_no, project_id, title, description, severity, status, reproduce_steps,
                     assignee_user_id, reporter_user_id,
                     create_by, create_time, update_by, update_time,
                     external_source, external_id, external_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate(), ?, sysdate(), ?, ?, ?)
                """,
                defectNo, projectId,
                fields.get("title"), fields.get("description"),
                emptyToNull((String) fields.get("severity")),
                fields.get("status"),
                fields.get("reproduceSteps"),
                assigneeId, reporterId,
                "zentao-sync", "zentao-sync",
                SOURCE, externalId, externalUrl);
            log.info("[plm-integration/zentao] 入站新建 defect external_id={} project={}", externalId, projectId);
        } else {
            checkStaleAndUpdate("tb_defect", "defect_id", existingId, lastEdited, () ->
                jdbc.update("""
                    UPDATE tb_defect SET title=?, description=?, severity=?, status=?, reproduce_steps=?,
                                         assignee_user_id=?, reporter_user_id=?, update_by=?,
                                         update_time=sysdate(), external_url=?
                    WHERE defect_id=?
                    """,
                    fields.get("title"), fields.get("description"),
                    emptyToNull((String) fields.get("severity")),
                    fields.get("status"),
                    fields.get("reproduceSteps"),
                    assigneeId, reporterId,
                    "zentao-sync",
                    externalUrl,
                    existingId)
            );
        }
    }

    // ───────── Story ─────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncStory(IntegrationConnector connector, JSONObject data, JSONObject payload) {
        String externalId = data.getString("id");
        Date lastEdited = parseDate(data.getString("lastEditedDate"), data.getString("openedDate"));

        Long projectId = mapProductToProject(connector, data.getInteger("product"), "story");
        Long assigneeId = userMapService.resolveUserIdByExternalAccount(connector.getId(), data.getString("assignedTo"));

        Map<String, Object> fields = ZentaoFieldMapper.storyPayloadToReqFields(data);

        Long existingId = findExistingId("tb_requirement", "requirement_id", externalId);
        final String externalUrl = resolveExternalUrl(payload, connector, "story", externalId);

        if (existingId == null) {
            String reqNo = buildExtReqNo(externalId);
            jdbc.update("""
                INSERT INTO tb_requirement
                    (requirement_no, project_id, title, description, priority, status,
                     assignee_user_id, create_by, create_time, update_by, update_time,
                     external_source, external_id, external_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, sysdate(), ?, sysdate(), ?, ?, ?)
                """,
                reqNo, projectId,
                fields.get("title"), fields.get("description"),
                emptyToNull((String) fields.get("priority")),
                fields.get("status"),
                assigneeId,
                "zentao-sync", "zentao-sync",
                SOURCE, externalId, externalUrl);
        } else {
            checkStaleAndUpdate("tb_requirement", "requirement_id", existingId, lastEdited, () ->
                jdbc.update("""
                    UPDATE tb_requirement SET title=?, description=?, priority=?, status=?,
                                              assignee_user_id=?, update_by=?, update_time=sysdate(),
                                              external_url=?
                    WHERE requirement_id=?
                    """,
                    fields.get("title"), fields.get("description"),
                    emptyToNull((String) fields.get("priority")),
                    fields.get("status"),
                    assigneeId,
                    "zentao-sync",
                    externalUrl,
                    existingId)
            );
        }
    }

    // ───────── Task ──────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncTask(IntegrationConnector connector, JSONObject data, JSONObject payload) {
        String externalId = data.getString("id");
        Date lastEdited = parseDate(data.getString("lastEditedDate"), data.getString("openedDate"));

        Long assigneeId = userMapService.resolveUserIdByExternalAccount(connector.getId(), data.getString("assignedTo"));
        Long sprintId = mapExecutionToSprint(connector, data.getInteger("execution"));
        Long projectId = sprintId == null ? null : findProjectIdBySprint(sprintId);

        Map<String, Object> fields = ZentaoFieldMapper.taskPayloadToTaskFields(data);

        Long existingId = findExistingId("tb_task", "task_id", externalId);
        final String externalUrl = resolveExternalUrl(payload, connector, "task", externalId);

        if (existingId == null) {
            String taskNo = buildExtTaskNo(externalId);
            jdbc.update("""
                INSERT INTO tb_task
                    (task_no, project_id, sprint_id, title, description, priority, status,
                     assignee_user_id, estimated_hours, actual_hours,
                     create_by, create_time, update_by, update_time,
                     external_source, external_id, external_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate(), ?, sysdate(), ?, ?, ?)
                """,
                taskNo, projectId, sprintId,
                fields.get("title"), fields.get("description"),
                emptyToNull((String) fields.get("priority")),
                fields.get("status"),
                assigneeId,
                emptyToNull(fields.get("estimatedHours")), emptyToNull(fields.get("actualHours")),
                "zentao-sync", "zentao-sync",
                SOURCE, externalId, externalUrl);
        } else {
            checkStaleAndUpdate("tb_task", "task_id", existingId, lastEdited, () ->
                jdbc.update("""
                    UPDATE tb_task SET title=?, description=?, priority=?, status=?,
                                       assignee_user_id=?, estimated_hours=?, actual_hours=?,
                                       update_by=?, update_time=sysdate(), external_url=?
                    WHERE task_id=?
                    """,
                    fields.get("title"), fields.get("description"),
                    emptyToNull((String) fields.get("priority")),
                    fields.get("status"),
                    assigneeId,
                    emptyToNull(fields.get("estimatedHours")), emptyToNull(fields.get("actualHours")),
                    "zentao-sync",
                    externalUrl,
                    existingId)
            );
        }
    }

    // ───────── Case ──────────────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncCase(IntegrationConnector connector, JSONObject data, JSONObject payload) {
        String externalId = data.getString("id");
        Date lastEdited = parseDate(data.getString("lastEditedDate"), data.getString("openedDate"));

        Long projectId = mapProductToProject(connector, data.getInteger("product"), "case");

        Map<String, Object> fields = ZentaoFieldMapper.casePayloadToTestcaseFields(data);

        Long existingId = findExistingId("tb_testcase", "testcase_id", externalId);
        final String externalUrl = resolveExternalUrl(payload, connector, "case", externalId);

        if (existingId == null) {
            String tcNo = buildExtTcNo(externalId);
            jdbc.update("""
                INSERT INTO tb_testcase
                    (testcase_no, project_id, title, preconditions, steps, expected_result,
                     priority, category, status,
                     create_by, create_time, update_by, update_time,
                     external_source, external_id, external_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate(), ?, sysdate(), ?, ?, ?)
                """,
                tcNo, projectId,
                fields.get("title"), fields.get("preconditions"), fields.get("steps"),
                fields.get("expectedResult"),
                emptyToNull((String) fields.get("priority")),
                emptyToNull((String) fields.get("category")),
                fields.get("status"),
                "zentao-sync", "zentao-sync",
                SOURCE, externalId, externalUrl);
        } else {
            checkStaleAndUpdate("tb_testcase", "testcase_id", existingId, lastEdited, () ->
                jdbc.update("""
                    UPDATE tb_testcase SET title=?, preconditions=?, steps=?, priority=?, category=?, status=?,
                                           update_by=?, update_time=sysdate(), external_url=?
                    WHERE testcase_id=?
                    """,
                    fields.get("title"), fields.get("preconditions"), fields.get("steps"),
                    emptyToNull((String) fields.get("priority")),
                    emptyToNull((String) fields.get("category")),
                    fields.get("status"),
                    "zentao-sync",
                    externalUrl,
                    existingId)
            );
        }
    }

    // ───────── 公共逻辑 ─────────────────────────────────────────────────

    /** 按 (external_source=zentao, external_id=...) 查 PLM 行 id;返回 null 表示未同步过 */
    private Long findExistingId(String table, String pkColumn, String externalId) {
        try {
            return jdbc.queryForObject(
                "SELECT " + pkColumn + " FROM " + table + " WHERE external_source=? AND external_id=? LIMIT 1",
                Long.class, SOURCE, externalId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** 比对 lastEditedDate vs PLM update_time,若 PLM 更新 → 抛 StaleException;否则 SELECT FOR UPDATE 锁行,执行 update */
    private void checkStaleAndUpdate(String table, String pkColumn, Long pk, Date lastEdited, Runnable update) {
        if (lastEdited != null) {
            Timestamp plmTs = jdbc.queryForObject(
                "SELECT update_time FROM " + table + " WHERE " + pkColumn + "=? FOR UPDATE",
                Timestamp.class, pk);
            if (plmTs != null && !plmTs.before(new Timestamp(lastEdited.getTime()))) {
                throw new StaleException("PLM update_time=" + plmTs + " >= zentao lastEditedDate=" + lastEdited);
            }
        }
        update.run();
    }

    private Long mapProductToProject(IntegrationConnector connector, Integer productId, String objectType) {
        if (productId == null) return null;
        JSONObject cfg = parseConfig(connector);
        JSONObject map = cfg == null ? null : cfg.getJSONObject("productProjectMap");
        if (map == null || !map.containsKey(String.valueOf(productId))) {
            throw new UnmappedException(816,
                "禅道 " + objectType + " 关联 product=" + productId + " 未在 connector.config_json.productProjectMap 中配置");
        }
        return map.getLong(String.valueOf(productId));
    }

    private Long mapExecutionToSprint(IntegrationConnector connector, Integer executionId) {
        if (executionId == null) return null;
        JSONObject cfg = parseConfig(connector);
        JSONObject map = cfg == null ? null : cfg.getJSONObject("executionSprintMap");
        if (map == null || !map.containsKey(String.valueOf(executionId))) {
            // execution 未映射 → 容忍 null,任务可不挂 sprint
            return null;
        }
        return map.getLong(String.valueOf(executionId));
    }

    private Long findProjectIdBySprint(Long sprintId) {
        try {
            return jdbc.queryForObject(
                "SELECT project_id FROM tb_sprint WHERE sprint_id=?", Long.class, sprintId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    private JSONObject parseConfig(IntegrationConnector connector) {
        String cfg = connector.getConfigJson();
        if (cfg == null || cfg.isEmpty()) return null;
        try {
            return JSON.parseObject(cfg);
        } catch (Exception e) {
            log.warn("[plm-integration/zentao] connector {} config_json 解析失败", connector.getId(), e);
            return null;
        }
    }

    private static Date parseDate(String primary, String fallback) {
        String s = (primary != null && !primary.isEmpty()) ? primary : fallback;
        if (s == null || s.isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveExternalUrl(JSONObject payload, IntegrationConnector connector, String objectType, String externalId) {
        String url = payload.getString("url");
        if (url == null || url.isEmpty()) {
            url = zentaoAdapter.buildExternalUrl(connector, objectType, externalId);
        }
        return url;
    }

    private static String buildExtDefectNo(String externalId) { return "ZT-BUG-" + externalId; }
    private static String buildExtReqNo(String externalId)    { return "ZT-STORY-" + externalId; }
    private static String buildExtTaskNo(String externalId)   { return "ZT-TASK-" + externalId; }
    private static String buildExtTcNo(String externalId)     { return "ZT-CASE-" + externalId; }

    private static Object emptyToNull(Object v) {
        return (v == null || (v instanceof String s && s.isEmpty())) ? null : v;
    }

    /** 内部异常:PLM 侧数据更新于禅道,跳过此次同步 */
    static class StaleException extends RuntimeException {
        StaleException(String msg) { super(msg); }
    }

    /** 内部异常:productProjectMap 缺映射等,带 errorCode */
    static class UnmappedException extends RuntimeException {
        final int errorCode;
        UnmappedException(int errorCode, String msg) { super(msg); this.errorCode = errorCode; }
    }
}
