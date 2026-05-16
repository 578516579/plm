package cn.com.bosssfot.dv.plm.defect.service.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.defect.domain.Defect;
import cn.com.bosssfot.dv.plm.defect.mapper.DefectMapper;
import cn.com.bosssfot.dv.plm.defect.service.IDefectService;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.sprint.mapper.SprintMapper;
import cn.com.bosssfot.dv.plm.task.mapper.TaskMapper;

/**
 * 缺陷 Service 实现
 *
 * 落地:
 * - ADR-0005: generateDefectNo() DEFECT-YYYY-NNNN
 * - PRD §3.4 / API §2: 5×5 状态机含反向边 03→01 (回归打回)
 * - 进入 03 已解决 必填 resolution → 705
 * - 3 FK 校验 (projectId 必,sprintId/taskId 可空)
 * - reporter_user_id 默认 = 当前 user
 */
@Service
public class DefectServiceImpl implements IDefectService
{
    private static final Logger log = LoggerFactory.getLogger(DefectServiceImpl.class);

    /**
     * PRD §3.4 5×5 状态机转换矩阵
     *
     *            00 新建  01 已确认  02 处理中  03 已解决  04 已关闭
     * 00 新建      —       ✅        ❌        ❌        ❌
     * 01 已确认   ❌        —        ✅        ❌        ✅ (重复/无效)
     * 02 处理中   ❌       ✅        —         ✅        ❌
     * 03 已解决   ❌       ✅ (反向) ❌        —         ✅
     * 04 已关闭   ❌       ❌        ❌        ❌        — (终态)
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));                  // 新建 → 已确认
        STATUS_TRANSITIONS.put("01", Set.of("02", "04"));            // 已确认 → 处理中 / 已关闭
        STATUS_TRANSITIONS.put("02", Set.of("01", "03"));            // 处理中 → 重新分析 / 已解决
        STATUS_TRANSITIONS.put("03", Set.of("01", "04"));            // 已解决 → 反向打回 / 已关闭
        STATUS_TRANSITIONS.put("04", Set.of());                      // 已关闭 (终态)
    }

    @Autowired private DefectMapper defectMapper;
    @Autowired private ProjectMapper projectMapper;
    @Autowired private SprintMapper sprintMapper;
    @Autowired private TaskMapper taskMapper;

    @Override
    public List<Defect> selectDefectList(Defect defect) { return defectMapper.selectDefectList(defect); }

    @Override
    public Defect selectDefectById(Long defectId) { return defectMapper.selectDefectById(defectId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDefect(Defect defect)
    {
        if (StringUtils.isBlank(defect.getTitle())) throw new ServiceException("缺陷标题不能为空", 602);
        if (defect.getProjectId() == null) throw new ServiceException("关联项目不能为空", 602);
        if (StringUtils.isBlank(defect.getSeverity())) defect.setSeverity("02");
        if (StringUtils.isBlank(defect.getCategory())) defect.setCategory("01");

        // FK 校验
        if (projectMapper.selectProjectById(defect.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (defect.getSprintId() != null && sprintMapper.selectSprintById(defect.getSprintId()) == null) {
            throw new ServiceException("关联迭代不存在", 702);
        }
        if (defect.getTaskId() != null && taskMapper.selectTaskById(defect.getTaskId()) == null) {
            throw new ServiceException("关联任务不存在", 702);
        }

        // 新建状态必须 00
        if (StringUtils.isBlank(defect.getStatus())) defect.setStatus("00");
        else if (!"00".equals(defect.getStatus())) {
            throw new ServiceException("新建缺陷状态必须为「新建」", 601);
        }

        // reporter 默认 = 当前用户
        if (defect.getReporterUserId() == null) defect.setReporterUserId(SecurityUtils.getUserId());

        if (StringUtils.isBlank(defect.getDefectNo())) defect.setDefectNo(generateDefectNo());
        defect.setCreateBy(SecurityUtils.getUsername());

        try {
            return defectMapper.insertDefect(defect);
        } catch (DuplicateKeyException e) {
            log.warn("defect_no 重号,重试: {}", defect.getDefectNo());
            defect.setDefectNo(generateDefectNo());
            return defectMapper.insertDefect(defect);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDefect(Defect defect)
    {
        Defect old = defectMapper.selectDefectById(defect.getDefectId());
        if (old == null) throw new ServiceException("缺陷不存在", 404);

        // 状态机
        if (StringUtils.isNotBlank(defect.getStatus())
                && !defect.getStatus().equals(old.getStatus())) {
            String os = old.getStatus(), ns = defect.getStatus();
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(os, Set.of());
            if (!allowed.contains(ns)) {
                throw new ServiceException(
                    "缺陷状态 " + statusLabel(os) + " 不能转到 " + statusLabel(ns), 601);
            }
            // 进入 03 必填 resolution
            if ("03".equals(ns)) {
                String res = defect.getResolution() != null ? defect.getResolution() : old.getResolution();
                if (StringUtils.isBlank(res)) {
                    throw new ServiceException("进入「已解决」必须填解决说明", 705);
                }
            }
        }

        // 改 FK 时校验
        if (defect.getProjectId() != null && !defect.getProjectId().equals(old.getProjectId())
                && projectMapper.selectProjectById(defect.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (defect.getSprintId() != null && !defect.getSprintId().equals(old.getSprintId())
                && sprintMapper.selectSprintById(defect.getSprintId()) == null) {
            throw new ServiceException("关联迭代不存在", 702);
        }
        if (defect.getTaskId() != null && !defect.getTaskId().equals(old.getTaskId())
                && taskMapper.selectTaskById(defect.getTaskId()) == null) {
            throw new ServiceException("关联任务不存在", 702);
        }

        defect.setUpdateBy(SecurityUtils.getUsername());
        return defectMapper.updateDefect(defect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDefectByIds(Long[] defectIds) {
        return defectMapper.deleteDefectByIds(defectIds);
    }

    // ───────── 私有 ─────────

    private String generateDefectNo() {
        int year = LocalDate.now().getYear();
        String prefix = "DEFECT-" + year + "-";
        Integer maxSeq = defectMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "新建";
            case "01": return "已确认";
            case "02": return "处理中";
            case "03": return "已解决";
            case "04": return "已关闭";
            default:   return "未知(" + status + ")";
        }
    }
}
