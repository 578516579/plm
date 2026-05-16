package cn.com.bosssfot.dv.plm.manualops.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.manualops.domain.ManualOps;
import cn.com.bosssfot.dv.plm.manualops.mapper.ManualOpsMapper;
import cn.com.bosssfot.dv.plm.manualops.service.IManualOpsService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 运维手册 Service — PRD §F5.3 + 原型 opsmanual.html
 *
 * 落地:
 * - generateManualopsNo() — OM-YYYY-NNNN
 * - PRD §F5.3: AI 一键生成 + 监控方案/告警渠道/IoT 多选 + 多格式导出
 * - 4 状态机: 00→{01}, 01→{02}, 02→{00,03}, 03→{} (终态)
 * - alertChannels/iotDeviceTypes 以 CSV 存储,校验时拆分逐项验
 */
@Service
public class ManualOpsServiceImpl implements IManualOpsService {
    private static final Logger log = LoggerFactory.getLogger(ManualOpsServiceImpl.class);

    private static final Set<String> ALLOWED_MONITORING = Set.of("prometheus_grafana", "aliyun_cms", "zabbix");
    private static final Set<String> ALLOWED_ALERT      = Set.of("dingtalk", "feishu", "wework", "email");
    private static final Set<String> ALLOWED_IOT        = Set.of("soil_sensor", "weather_station", "drone", "irrigation_controller");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of("00", "03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired private ManualOpsMapper manualopsMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<ManualOps> selectManualOpsList(ManualOps t) {
        return manualopsMapper.selectManualOpsList(t);
    }

    @Override
    public ManualOps selectManualOpsById(Long id) {
        return manualopsMapper.selectManualOpsById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertManualOps(ManualOps t) {
        if (StringUtils.isBlank(t.getTitle()))     throw new ServiceException("手册标题不能为空", 602);
        if (t.getProjectId() == null)              throw new ServiceException("关联项目不能为空", 602);
        if (t.getAuthorUserId() == null)           throw new ServiceException("作者不能为空", 602);
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null)                       throw new ServiceException("关联项目不存在", 702);
        validateEnums(t);

        if (StringUtils.isBlank(t.getOutputFormats())) t.setOutputFormats("pdf");
        if (StringUtils.isBlank(t.getAiGenerated()))   t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus()) && !"01".equals(t.getStatus())) {
            throw new ServiceException("新建手册状态必须为「草稿」或「生成中」", 601);
        }

        if (StringUtils.isBlank(t.getManualopsNo())) t.setManualopsNo(generateManualopsNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return manualopsMapper.insertManualOps(t);
        } catch (DuplicateKeyException e) {
            log.warn("manualops_no 重号,重试一次: {}", t.getManualopsNo());
            t.setManualopsNo(generateManualopsNo());
            return manualopsMapper.insertManualOps(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateManualOps(ManualOps t) {
        ManualOps old = manualopsMapper.selectManualOpsById(t.getManualopsId());
        if (old == null) throw new ServiceException("运维手册不存在", 404);

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "手册状态 " + statusLabel(old.getStatus()) + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601);
            }
            if ("02".equals(t.getStatus()) && t.getGeneratedAt() == null && old.getGeneratedAt() == null) {
                t.setGeneratedAt(new Date());
            }
        }
        validateEnums(t);
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) throw new ServiceException("关联项目不存在", 702);
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return manualopsMapper.updateManualOps(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteManualOpsByIds(Long[] ids) {
        return manualopsMapper.deleteManualOpsByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManualOps aiGenerate(Long id) {
        ManualOps t = manualopsMapper.selectManualOpsById(id);
        if (t == null) throw new ServiceException("运维手册不存在", 404);

        String content = buildAiContent(t);
        t.setContent(content);
        t.setStatus("02");
        t.setAiGenerated("Y");
        t.setGeneratedAt(new Date());
        t.setUpdateBy("ai-agent");
        manualopsMapper.updateManualOps(t);
        return manualopsMapper.selectManualOpsById(id);
    }

    private String buildAiContent(ManualOps t) {
        String monitoring = t.getMonitoringPlan() != null ? t.getMonitoringPlan() : "prometheus_grafana";
        String alerts     = t.getAlertChannels()  != null ? t.getAlertChannels()  : "dingtalk,email";
        String iot        = t.getIotDeviceTypes() != null ? t.getIotDeviceTypes() : "soil_sensor,weather_station";

        return "# " + t.getTitle() + "\n\n" +
               "> AI Generated by AgriPLM — PRD §F5.3 Ops Manual Flow\n\n" +
               "## 1. 监控方案\n" +
               "- **方案**: " + monitoring + "\n" +
               "- **关键指标**: CPU/Mem/Disk/JVM/接口 QPS / IoT 设备心跳\n\n" +
               "## 2. 告警渠道\n" +
               "- **启用渠道**: " + alerts + "\n" +
               "- **告警级别**: P0 → 钉钉@运维主管, P1 → 邮件值班组\n\n" +
               "## 3. IoT 设备运维 (农情专项)\n" +
               "- **接入设备**: " + iot + "\n" +
               "- **巡检周期**: 土壤传感器 7 日/次,气象站 14 日/次,无人机 飞行后必检\n" +
               "- **故障替换 SLA**: 4 小时到场\n\n" +
               "## 4. 备份策略\n" +
               "- 数据库 mysqldump 每日 02:00,保留 30 天\n" +
               "- Redis RDB 每 5 分钟 + AOF\n" +
               "- 文件 minio 双副本 + 异地\n\n" +
               "## 5. 应急预案\n" +
               "P0 故障 → 5 分钟响应,30 分钟恢复或回滚至上版本。\n";
    }

    private void validateEnums(ManualOps t) {
        if (t.getMonitoringPlan() != null && !ALLOWED_MONITORING.contains(t.getMonitoringPlan()))
            throw new ServiceException("无效的监控方案: " + t.getMonitoringPlan(), 604);
        validateCsv(t.getAlertChannels(),  ALLOWED_ALERT, "告警渠道");
        validateCsv(t.getIotDeviceTypes(), ALLOWED_IOT,   "IoT 设备类型");
    }

    private void validateCsv(String csv, Set<String> allowed, String label) {
        if (csv == null || csv.isBlank()) return;
        for (String item : csv.split(",")) {
            String v = item.trim();
            if (!v.isEmpty() && !allowed.contains(v))
                throw new ServiceException("无效的" + label + "值: " + v, 604);
        }
    }

    private String generateManualopsNo() {
        int year = LocalDate.now().getYear();
        String prefix = "OM-" + year + "-";
        Integer maxSeq = manualopsMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String s) {
        switch (s) {
            case "00": return "草稿";
            case "01": return "生成中";
            case "02": return "已生成";
            case "03": return "已发布";
            default:   return "未知(" + s + ")";
        }
    }
}
