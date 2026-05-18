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
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.manualops.domain.ManualOps;
import cn.com.bosssfot.dv.plm.manualops.mapper.ManualOpsMapper;
import cn.com.bosssfot.dv.plm.manualops.service.IManualOpsService;

/**
 * 运维手册 Service — PRD §F5.3
 *
 * 落地:
 * - ADR: generateManualOpsNo() — MOP-YYYY-NNNN
 * - 5 状态机: 00 草稿 → 01 生成中 → 02 已生成 → 03 已审核 → 04 已发布
 * - ENUM 白名单: monitoringPlan → 604
 * - aiGenerate() mock: 生成示例运维手册正文
 */
@Service
public class ManualOpsServiceImpl implements IManualOpsService
{
    private static final Logger log = LoggerFactory.getLogger(ManualOpsServiceImpl.class);

    private static final Set<String> ALLOWED_MONITORING = Set.of("prometheus_grafana", "aliyun", "zabbix");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of("04"));
        STATUS_TRANSITIONS.put("04", Set.of());
    }

    @Autowired private ManualOpsMapper manualOpsMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<ManualOps> selectManualOpsList(ManualOps t) {
        return manualOpsMapper.selectManualOpsList(t);
    }

    @Override
    public ManualOps selectManualOpsById(Long id) {
        return manualOpsMapper.selectManualOpsById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertManualOps(ManualOps t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("手册标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (StringUtils.isNotBlank(t.getMonitoringPlan()) && !ALLOWED_MONITORING.contains(t.getMonitoringPlan())) {
            throw new ServiceException("监控方案值非法 (允许: prometheus_grafana/aliyun/zabbix)", 604);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建运维手册状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getManualOpsNo())) {
            t.setManualOpsNo(generateManualOpsNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return manualOpsMapper.insertManualOps(t);
        } catch (DuplicateKeyException e) {
            log.warn("manual_ops_no 重号,重试一次: {}", t.getManualOpsNo());
            t.setManualOpsNo(generateManualOpsNo());
            return manualOpsMapper.insertManualOps(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateManualOps(ManualOps t) {
        ManualOps old = manualOpsMapper.selectManualOpsById(t.getManualOpsId());
        if (old == null) {
            throw new ServiceException("运维手册不存在", 404);
        }
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "运维手册状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(t.getMonitoringPlan()) && !ALLOWED_MONITORING.contains(t.getMonitoringPlan())) {
            throw new ServiceException("监控方案值非法", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return manualOpsMapper.updateManualOps(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteManualOpsByIds(Long[] ids) {
        return manualOpsMapper.deleteManualOpsByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManualOps aiGenerate(Long manualOpsId) {
        ManualOps mo = manualOpsMapper.selectManualOpsById(manualOpsId);
        if (mo == null) {
            throw new ServiceException("运维手册不存在", 404);
        }
        String mockContent = "# AgriPLM 运维手册\n\n"
            + "## 日常运维流程\n\n"
            + "### 1. 监控巡检\n\n"
            + "- 每日检查 Prometheus/Grafana 面板，确认各服务指标正常\n"
            + "- 关注 CPU 使用率（阈值 80%）、内存使用率（阈值 85%）、磁盘空间（阈值 90%）\n\n"
            + "### 2. 告警处理\n\n"
            + "- 收到告警后 5 分钟内响应\n"
            + "- 按优先级 P0/P1/P2 分级处理\n\n"
            + "### 3. IoT 设备管理\n\n"
            + "- 土壤传感器：每日校验数据上报频率（1次/小时）\n"
            + "- 气象站：每周检查设备电量和网络连接\n"
            + "- 无人机：每次作业后检查飞行日志\n"
            + "- 灌溉控制器：每月维护一次\n\n"
            + "### 4. 数据备份\n\n"
            + "```bash\n"
            + "# 每日凌晨 2 点自动备份\n"
            + "mysqldump -u plm -p plm > /backup/plm_$(date +%Y%m%d).sql\n"
            + "```\n\n"
            + "### 5. 故障恢复\n\n"
            + "按照 SLA 要求，RTO ≤ 4 小时，RPO ≤ 1 小时。\n";
        mo.setAiGenerated("Y");
        mo.setAiGeneratedAt(new Date());
        mo.setStatus("02");
        mo.setContent(mockContent);
        mo.setUpdateBy(SecurityUtils.getUsername());
        manualOpsMapper.updateManualOps(mo);
        return mo;
    }

    private String generateManualOpsNo() {
        int year = LocalDate.now().getYear();
        String prefix = "MOP-" + year + "-";
        Integer maxSeq = manualOpsMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "生成中";
            case "02": return "已生成";
            case "03": return "已审核";
            case "04": return "已发布";
            default:   return "未知(" + status + ")";
        }
    }
}
