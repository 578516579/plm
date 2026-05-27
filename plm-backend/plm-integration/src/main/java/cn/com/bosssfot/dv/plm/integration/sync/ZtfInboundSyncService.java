package cn.com.bosssfot.dv.plm.integration.sync;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.integration.adapter.ztf.ZtfFieldMapper;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;
import cn.com.bosssfot.dv.plm.integration.service.impl.IntegrationWebhookEventServiceImpl.WebhookReceived;

/**
 * ZTF → PLM 入站同步 Service(<b>单向 Inbound</b>)。
 *
 * <p>消费 {@link WebhookReceived} 事件(由 {@link IIntegrationWebhookEventService#receive} 异步发布,
 * 仅 {@code ztf.} 前缀),把 ZTF run 完成结果写入 {@code tb_autotest} 的执行结果字段
 * (total_cases / passed_cases / failed_cases / pass_rate / execution_duration_sec /
 *  last_executed_at / last_root_cause_analysis),<b>不改 status 生命周期</b>。
 *
 * <p>关键决策(设计 §0 / §6):
 * <ul>
 *   <li><b>无 SyncContext / 无防回环</b>:单向 Inbound 不存在 A→B→A 回环(PLM 不回写 ZTF)。</li>
 *   <li>匹配:先按 {@code (external_source='ztf', external_id=taskId)} 查;查不到按 autotest_no 查并回填 external_id;
 *       都查不到 → markProcessed(4, errorCode=821 无匹配套件),本期不自动建套件。</li>
 *   <li>入站 last-write-wins:{@code SELECT ... FOR UPDATE} 锁行 + finishedAt 比 last_executed_at,
 *       旧 run 重发跳过(errorCode 819 stale)。</li>
 *   <li>pass_rate 服务端重算(不信任外部传入)。</li>
 * </ul>
 *
 * @see <a href="../../../../../../../../02-设计/ZTF-集成-设计.md">ZTF 集成设计</a>
 */
@Service
public class ZtfInboundSyncService {

    private static final Logger log = LoggerFactory.getLogger(ZtfInboundSyncService.class);
    private static final String SOURCE = "ztf";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private IIntegrationWebhookEventService eventService;

    @EventListener
    @Async
    public void onWebhookReceived(WebhookReceived ev) {
        IntegrationWebhookEvent event = ev.getEvent();
        if (event == null || event.getEventType() == null || !event.getEventType().startsWith("ztf.")) {
            return; // 非 ZTF 事件,忽略
        }
        IntegrationConnector connector = connectorService.selectConnectorById(event.getConnectorId());
        if (connector == null || !SOURCE.equals(connector.getConnectorType())) {
            eventService.markProcessed(event.getId(), "4", "connector 不匹配或被删除");
            return;
        }
        try {
            dispatch(connector, event);
        } catch (StaleException stale) {
            eventService.markProcessed(event.getId(), "4", "[errorCode=819] stale: ZTF run 旧于 last_executed_at");
        } catch (NoMatchException nm) {
            eventService.markProcessed(event.getId(), "4", "[errorCode=821] " + nm.getMessage());
        } catch (Exception e) {
            log.error("[plm-integration/ztf] 入站同步失败 eventId={}", event.getId(), e);
            eventService.markProcessed(event.getId(), "3", e.getMessage());
        }
    }

    private void dispatch(IntegrationConnector connector, IntegrationWebhookEvent event) {
        JSONObject payload = JSON.parseObject(event.getPayloadJson());
        if (payload == null) {
            eventService.markProcessed(event.getId(), "4", "payload 解析失败");
            return;
        }
        String taskId = payload.getString("taskId");
        if (taskId == null) taskId = payload.getString("runId");
        String autotestNo = payload.getString("autotestNo");
        if ((taskId == null || taskId.isEmpty()) && (autotestNo == null || autotestNo.isEmpty())) {
            eventService.markProcessed(event.getId(), "4", "payload 缺 taskId 与 autotestNo");
            return;
        }
        syncRun(payload, taskId, autotestNo);
        eventService.markProcessed(event.getId(), "2", null);
    }

    /**
     * 把 ZTF run 结果写入匹配的 tb_autotest 行。
     *
     * <p><b>裸 JDBC 旁路 AutoTestService 是有意决策(集成层直写结果字段,不触发业务状态机校验),见 ADR-0009。</b>
     *
     * @throws NoMatchException taskId/autotestNo 都查不到(errorCode 821)
     * @throws StaleException   finishedAt ≤ 现有 last_executed_at(errorCode 819,旧 run 重发)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void syncRun(JSONObject payload, String taskId, String autotestNo) {
        // 1. 匹配:先按 external_id,查不到按 autotest_no(并回填 external_id)
        AutotestRef ref = findAutotest(taskId, autotestNo);
        if (ref == null) {
            throw new NoMatchException("无匹配 autotest 套件: taskId=" + taskId + " autotestNo=" + autotestNo);
        }

        // 2. 入站 last-write-wins:SELECT ... FOR UPDATE 锁行,比对 finishedAt vs last_executed_at
        Date finishedAt = parseDate(payload.getString("finishedAt"));
        // 旁路 AutoTestService 是有意决策(集成层直写结果字段),见 ADR-0009
        Timestamp plmTs = jdbc.queryForObject(
            "SELECT last_executed_at FROM tb_autotest WHERE autotest_id=? FOR UPDATE",
            Timestamp.class, ref.autotestId);
        if (finishedAt != null && plmTs != null && !plmTs.before(new Timestamp(finishedAt.getTime()))) {
            throw new StaleException("PLM last_executed_at=" + plmTs + " >= ZTF finishedAt=" + finishedAt);
        }

        // 3. 服务端重算结果字段
        int total = ZtfFieldMapper.total(payload);
        int passed = ZtfFieldMapper.passed(payload);
        int failed = ZtfFieldMapper.failed(payload);
        BigDecimal passRate = ZtfFieldMapper.passRate(payload);
        int durationSec = ZtfFieldMapper.durationSec(payload);
        String rootCause = ZtfFieldMapper.rootCause(payload);
        String runUrl = payload.getString("runUrl");

        // 4. 裸 JDBC UPDATE 结果字段(含首次回填 external_source/external_id/external_url)
        //    旁路 AutoTestService 是有意决策(集成层直写结果字段),见 ADR-0009
        jdbc.update("""
            UPDATE tb_autotest
               SET total_cases=?, passed_cases=?, failed_cases=?, pass_rate=?,
                   execution_duration_sec=?, last_executed_at=?, last_root_cause_analysis=?,
                   external_source=?, external_id=?,
                   external_url=COALESCE(?, external_url),
                   update_by=?, update_time=sysdate()
             WHERE autotest_id=?
            """,
            total, passed, failed, passRate,
            durationSec,
            finishedAt == null ? null : new Timestamp(finishedAt.getTime()),
            rootCause,
            SOURCE, ref.externalId,
            runUrl,
            "ztf-sync",
            ref.autotestId);

        log.info("[plm-integration/ztf] 入站回写 autotest_id={} external_id={} total={} pass={} fail={} passRate={}",
            ref.autotestId, ref.externalId, total, passed, failed, passRate);
    }

    /**
     * 匹配 tb_autotest:先按 (external_source='ztf', external_id=taskId),查不到按 autotest_no 查并回填 external_id。
     *
     * @return 命中行引用(含确定要写回的 external_id);都查不到返回 null
     */
    private AutotestRef findAutotest(String taskId, String autotestNo) {
        // (1) 按 external_id 精确匹配
        if (taskId != null && !taskId.isEmpty()) {
            Long id = queryAutotestIdByExternalId(taskId);
            if (id != null) {
                return new AutotestRef(id, taskId);
            }
        }
        // (2) 按 autotest_no 匹配(首次绑定,回填 external_id=taskId)
        if (autotestNo != null && !autotestNo.isEmpty()) {
            Long id = queryAutotestIdByNo(autotestNo);
            if (id != null) {
                // taskId 为空时 external_id 写 null(后续仍可按 autotest_no 命中);否则首次绑定 taskId
                return new AutotestRef(id, (taskId == null || taskId.isEmpty()) ? null : taskId);
            }
        }
        return null;
    }

    private Long queryAutotestIdByExternalId(String taskId) {
        try {
            return jdbc.queryForObject(
                "SELECT autotest_id FROM tb_autotest WHERE external_source=? AND external_id=? AND del_flag='0' LIMIT 1",
                Long.class, SOURCE, taskId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Long queryAutotestIdByNo(String autotestNo) {
        try {
            return jdbc.queryForObject(
                "SELECT autotest_id FROM tb_autotest WHERE autotest_no=? AND del_flag='0' LIMIT 1",
                Long.class, autotestNo);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static Date parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    /** 命中的 tb_autotest 行引用 + 本次要写回的 external_id */
    private static class AutotestRef {
        final Long autotestId;
        final String externalId;
        AutotestRef(Long autotestId, String externalId) {
            this.autotestId = autotestId;
            this.externalId = externalId;
        }
    }

    /** 内部异常:ZTF run 旧于 PLM last_executed_at,跳过此次同步(errorCode 819) */
    static class StaleException extends RuntimeException {
        StaleException(String msg) { super(msg); }
    }

    /** 内部异常:taskId/autotestNo 都查不到匹配套件(errorCode 821) */
    static class NoMatchException extends RuntimeException {
        NoMatchException(String msg) { super(msg); }
    }
}
