package cn.com.bosssfot.dv.plm.integration.sync;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.common.core.event.DefectChangedEvent;
import cn.com.bosssfot.dv.plm.common.core.event.EntityChangedEvent;
import cn.com.bosssfot.dv.plm.common.core.event.RequirementChangedEvent;
import cn.com.bosssfot.dv.plm.common.core.event.TaskChangedEvent;
import cn.com.bosssfot.dv.plm.common.core.event.TestCaseChangedEvent;
import cn.com.bosssfot.dv.plm.integration.adapter.zentao.ZentaoConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.adapter.zentao.ZentaoFieldMapper;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationUserMappingService;

/**
 * PLM → 禅道 出站同步 Service。
 *
 * <p>监听 4 个业务模块发出的 {@link EntityChangedEvent} 子类型(在事务提交后),
 * 把变更反向推送到禅道:
 * <ul>
 *   <li>{@code DefectChangedEvent} → PUT /api/v1/bugs/{external_id}</li>
 *   <li>{@code RequirementChangedEvent} → PUT /api/v1/stories/{external_id}</li>
 *   <li>{@code TaskChangedEvent} → PUT /api/v1/tasks/{external_id}</li>
 *   <li>{@code TestCaseChangedEvent} → PUT /api/v1/cases/{external_id}</li>
 * </ul>
 *
 * <p>关键决策:
 * <ol>
 *   <li>{@link SyncContext#isInbound()} 检测:入站触发的本地写入不反推(防循环)</li>
 *   <li>实体 {@code external_source='zentao' && external_id} 非空 → 反推送 update;否则跳过(本期不创建,除非 connector.config_json.outboundCreateOnNew=true)</li>
 *   <li>last-write-wins:先 GET 一次拿禅道 lastEditedDate,若 PLM update_time ≤ 禅道,跳过</li>
 *   <li>60s 防抖:{@link #recentSyncCache} 抑制同一对象的高频重复反推</li>
 *   <li>本期只支持单个 zentao connector(status=0);多个时取第一个并 WARN</li>
 * </ol>
 *
 * <p>注:本服务**不**在原事务内执行(用 {@link TransactionPhase#AFTER_COMMIT}),
 *     因此外部 HTTP 调用失败不会回滚业务事务,只记 ERROR 日志。
 *
 * @see <a href="../../../99-跨阶段/proposals/0014-zentao-bidirectional-sync.md">Proposal 0014</a>
 */
@Service
public class ZentaoOutboundSyncService {

    private static final Logger log = LoggerFactory.getLogger(ZentaoOutboundSyncService.class);
    private static final String SOURCE = "zentao";
    private static final long DEDUPE_TTL_MS = 60 * 1000L;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private IIntegrationUserMappingService userMapService;

    @Autowired
    private ZentaoConnectorAdapter zentaoAdapter;

    /** key = "{type}-{entityId}",value=最近一次同步的 epochMs;60s 内重复跳过 */
    private final Map<String, Long> recentSyncCache = new ConcurrentHashMap<>();

    // ───────── 4 个 EventListener ────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDefectChanged(DefectChangedEvent ev) {
        if (SyncContext.isInbound()) return;
        if (ev.getAction() == EntityChangedEvent.Action.DELETE) {
            log.debug("[plm-integration/zentao-outbound] defect DELETE 不反推(本期不删禅道),id={}", ev.getEntityId());
            return;
        }
        syncEntityToZentao("defect", "tb_defect", "defect_id", ev.getEntityId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRequirementChanged(RequirementChangedEvent ev) {
        if (SyncContext.isInbound()) return;
        if (ev.getAction() == EntityChangedEvent.Action.DELETE) return;
        syncEntityToZentao("requirement", "tb_requirement", "requirement_id", ev.getEntityId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskChanged(TaskChangedEvent ev) {
        if (SyncContext.isInbound()) return;
        if (ev.getAction() == EntityChangedEvent.Action.DELETE) return;
        syncEntityToZentao("task", "tb_task", "task_id", ev.getEntityId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTestCaseChanged(TestCaseChangedEvent ev) {
        if (SyncContext.isInbound()) return;
        if (ev.getAction() == EntityChangedEvent.Action.DELETE) return;
        syncEntityToZentao("testcase", "tb_testcase", "testcase_id", ev.getEntityId());
    }

    // ───────── 核心同步逻辑 ────────────────────────────────────────────

    private void syncEntityToZentao(String type, String table, String pkColumn, Long entityId) {
        if (entityId == null) return;
        // 防抖
        String key = type + "-" + entityId;
        Long last = recentSyncCache.get(key);
        long now = System.currentTimeMillis();
        if (last != null && now - last < DEDUPE_TTL_MS) {
            log.debug("[plm-integration/zentao-outbound] 60s 防抖跳过 {} id={}", type, entityId);
            return;
        }

        IntegrationConnector connector = findActiveZentaoConnector();
        if (connector == null) {
            log.debug("[plm-integration/zentao-outbound] 无启用的 zentao connector,跳过 {} id={}", type, entityId);
            return;
        }

        // 读 PLM 当前行 — 包括 external_* 字段
        Map<String, Object> row = readRow(table, pkColumn, entityId);
        if (row == null) {
            log.warn("[plm-integration/zentao-outbound] PLM {} id={} 不存在(可能已被硬删)", type, entityId);
            return;
        }

        String externalSource = (String) row.get("external_source");
        String externalId = (String) row.get("external_id");
        Timestamp plmUpdate = (Timestamp) row.get("update_time");

        if (externalId == null || externalId.isEmpty() || !SOURCE.equals(externalSource)) {
            // 本期不创建,除非 connector.config_json.outboundCreateOnNew=true
            if (shouldCreateOnNew(connector)) {
                createInZentao(connector, type, row);
                recentSyncCache.put(key, now);
            } else {
                log.debug("[plm-integration/zentao-outbound] {} id={} 无 external_id,跳过出站(outboundCreateOnNew=false)", type, entityId);
            }
            return;
        }

        try {
            updateInZentao(connector, type, externalId, plmUpdate, row);
            recentSyncCache.put(key, now);
            cleanupCacheIfBig();
        } catch (Exception e) {
            log.error("[plm-integration/zentao-outbound] 出站同步失败 {} id={} external_id={}", type, entityId, externalId, e);
        }
    }

    private void updateInZentao(IntegrationConnector connector, String type, String externalId, Timestamp plmUpdate, Map<String, Object> row) throws Exception {
        // last-write-wins:先 GET 拿禅道 lastEditedDate
        JSONObject remote = fetchRemote(connector, type, externalId);
        if (remote != null && plmUpdate != null) {
            String remoteEditedStr = remote.getString("lastEditedDate");
            if (remoteEditedStr == null || remoteEditedStr.isEmpty()) {
                remoteEditedStr = remote.getString("openedDate");
            }
            Date remoteEdited = parseDate(remoteEditedStr);
            if (remoteEdited != null && !plmUpdate.after(new Timestamp(remoteEdited.getTime()))) {
                log.info("[plm-integration/zentao-outbound] 远端更新于本地,跳过:plm={} zentao={}", plmUpdate, remoteEdited);
                return;
            }
        }

        JSONObject body = buildUpdateBody(connector, type, row);
        switch (type) {
            case "defect"      -> zentaoAdapter.updateBug(connector, externalId, body);
            case "requirement" -> zentaoAdapter.updateStory(connector, externalId, body);
            case "task"        -> zentaoAdapter.updateTask(connector, externalId, body);
            case "testcase"    -> zentaoAdapter.updateCase(connector, externalId, body);
            default -> log.warn("不支持的 outbound type={}", type);
        }
        log.info("[plm-integration/zentao-outbound] 反推 {} external_id={} 成功", type, externalId);
    }

    private void createInZentao(IntegrationConnector connector, String type, Map<String, Object> row) {
        log.info("[plm-integration/zentao-outbound] outboundCreateOnNew 已开启,但本期未实现自动创建(留待 v0.6+):type={}", type);
        // 留 TODO:需要 product/execution 反向映射 + 创建后回写 external_source/external_id
    }

    private JSONObject fetchRemote(IntegrationConnector connector, String type, String externalId) {
        try {
            return switch (type) {
                case "defect"      -> zentaoAdapter.getBug(connector, externalId);
                case "requirement" -> zentaoAdapter.getStory(connector, externalId);
                case "task"        -> zentaoAdapter.getTask(connector, externalId);
                case "testcase"    -> zentaoAdapter.getCase(connector, externalId);
                default -> null;
            };
        } catch (Exception e) {
            log.warn("[plm-integration/zentao-outbound] fetch remote {} id={} 失败,跳过 last-write-wins 直接 update: {}", type, externalId, e.getMessage());
            return null;
        }
    }

    /** 构造出站 PUT body — 只发 connector.config_json.outboundFields 白名单字段 */
    private JSONObject buildUpdateBody(IntegrationConnector connector, String type, Map<String, Object> row) {
        JSONObject cfg = parseConfig(connector);
        List<String> whitelist = cfg == null ? null : cfg.getList("outboundFields", String.class);
        JSONObject body = new JSONObject();
        switch (type) {
            case "defect" -> {
                putIfWhitelisted(body, whitelist, "title", row.get("title"));
                String plmStatus = (String) row.get("status");
                String zStatus = ZentaoFieldMapper.defectStatusToBug(plmStatus);
                if (zStatus != null) putIfWhitelisted(body, whitelist, "status", zStatus);
                Integer zSev = ZentaoFieldMapper.defectSeverityToBug((String) row.get("severity"));
                if (zSev != null) putIfWhitelisted(body, whitelist, "severity", zSev);
                Object asn = row.get("assignee_user_id");
                if (asn instanceof Long uid) {
                    String acct = userMapService.resolveExternalAccountByUserId(connector.getId(), uid);
                    if (acct != null) putIfWhitelisted(body, whitelist, "assignedTo", acct);
                }
                putIfWhitelisted(body, whitelist, "steps", row.get("reproduce_steps"));
            }
            case "requirement" -> {
                putIfWhitelisted(body, whitelist, "title", row.get("title"));
                putIfWhitelisted(body, whitelist, "spec", row.get("description"));
                putIfWhitelisted(body, whitelist, "pri", row.get("priority"));
                String stage = ZentaoFieldMapper.reqStatusToStoryStage((String) row.get("status"));
                if (stage != null) putIfWhitelisted(body, whitelist, "stage", stage);
                Object asn = row.get("assignee_user_id");
                if (asn instanceof Long uid) {
                    String acct = userMapService.resolveExternalAccountByUserId(connector.getId(), uid);
                    if (acct != null) putIfWhitelisted(body, whitelist, "assignedTo", acct);
                }
            }
            case "task" -> {
                putIfWhitelisted(body, whitelist, "name", row.get("title"));
                putIfWhitelisted(body, whitelist, "desc", row.get("description"));
                putIfWhitelisted(body, whitelist, "pri", row.get("priority"));
                String zStatus = ZentaoFieldMapper.taskStatusToZentao((String) row.get("status"));
                if (zStatus != null) putIfWhitelisted(body, whitelist, "status", zStatus);
                Object asn = row.get("assignee_user_id");
                if (asn instanceof Long uid) {
                    String acct = userMapService.resolveExternalAccountByUserId(connector.getId(), uid);
                    if (acct != null) putIfWhitelisted(body, whitelist, "assignedTo", acct);
                }
            }
            case "testcase" -> {
                putIfWhitelisted(body, whitelist, "title", row.get("title"));
                putIfWhitelisted(body, whitelist, "precondition", row.get("preconditions"));
                putIfWhitelisted(body, whitelist, "steps", row.get("steps"));
                putIfWhitelisted(body, whitelist, "pri", row.get("priority"));
                putIfWhitelisted(body, whitelist, "type", row.get("category"));
                String zStatus = ZentaoFieldMapper.caseStatusToZentao((String) row.get("status"));
                if (zStatus != null) putIfWhitelisted(body, whitelist, "status", zStatus);
            }
            default -> {}
        }
        return body;
    }

    private static void putIfWhitelisted(JSONObject body, List<String> whitelist, String key, Object value) {
        if (value == null) return;
        if (whitelist == null || whitelist.isEmpty() || whitelist.contains(key)) {
            body.put(key, value);
        }
    }

    // ───────── 数据访问 ────────────────────────────────────────────────

    private Map<String, Object> readRow(String table, String pkColumn, Long pk) {
        try {
            return jdbc.queryForMap("SELECT * FROM " + table + " WHERE " + pkColumn + "=?", pk);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** 找一个启用中的 zentao connector;多个时取第一个并 WARN */
    private IntegrationConnector findActiveZentaoConnector() {
        IntegrationConnector q = new IntegrationConnector();
        q.setConnectorType(SOURCE);
        q.setStatus("0");
        List<IntegrationConnector> list = connectorService.selectConnectorList(q);
        if (list == null || list.isEmpty()) return null;
        if (list.size() > 1) {
            log.warn("[plm-integration/zentao-outbound] 存在多个启用的 zentao connector(共 {} 个),取首个 id={}",
                list.size(), list.get(0).getId());
        }
        return list.get(0);
    }

    private boolean shouldCreateOnNew(IntegrationConnector connector) {
        JSONObject cfg = parseConfig(connector);
        return cfg != null && Boolean.TRUE.equals(cfg.getBoolean("outboundCreateOnNew"));
    }

    private static JSONObject parseConfig(IntegrationConnector connector) {
        String cfg = connector.getConfigJson();
        if (cfg == null || cfg.isEmpty()) return null;
        try {
            return com.alibaba.fastjson2.JSON.parseObject(cfg);
        } catch (Exception e) {
            return null;
        }
    }

    private static Date parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s); } catch (Exception e) { return null; }
    }

    private void cleanupCacheIfBig() {
        if (recentSyncCache.size() < 1000) return;
        long cutoff = System.currentTimeMillis() - DEDUPE_TTL_MS;
        recentSyncCache.entrySet().removeIf(e -> e.getValue() < cutoff);
    }

    /** 测试用:暴露内部 cache size */
    int _cacheSize() { return recentSyncCache.size(); }

    /** 测试用:清空 cache */
    void _clearCache() { recentSyncCache.clear(); }
}
