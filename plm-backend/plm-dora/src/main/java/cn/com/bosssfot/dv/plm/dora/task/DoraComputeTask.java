package cn.com.bosssfot.dv.plm.dora.task;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.bosssfot.dv.plm.common.utils.DateUtils;
import cn.com.bosssfot.dv.plm.dora.service.IDoraMetricService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * Quartz 任务 — DORA 全量项目指标聚合 (Proposal 0028 P0-3B)。
 *
 * 由 sys_job seed 触发,默认 cron = "0 0 3 * * ?" 每日凌晨 3 点跑。
 * invokeTarget = "doraComputeTask.computeAllProjects(30)"
 *
 * 不放在 plm-quartz 模块,避免 plm-quartz 反向依赖业务模块。
 * Spring 启动后 bean 名 "doraComputeTask" 进 ApplicationContext,
 * Quartz JobInvokeUtil 用 SpringUtils.getBean(beanName) 反射调用。
 */
@Component("doraComputeTask")
public class DoraComputeTask {

    private static final Logger log = LoggerFactory.getLogger(DoraComputeTask.class);

    @Autowired
    private IDoraMetricService doraService;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 全量项目 DORA 聚合 — Quartz 参数自动转 Integer (JobInvokeUtil 默认数值类型)。
     * 单项目失败不阻塞其他,仅记日志。
     *
     * @param periodDays 聚合窗口天数,缺省/非法时按 30 处理
     */
    public void computeAllProjects(Integer periodDays) {
        int days = (periodDays != null && periodDays > 0) ? periodDays : 30;
        Date now = new Date();
        Date start = DateUtils.addDays(now, -days);

        List<Project> projects = projectMapper.selectProjectList(new Project());
        log.info("DORA 全量聚合启动,项目数={} 窗口={}d 范围=[{}, {})",
            projects == null ? 0 : projects.size(), days, start, now);

        if (projects == null || projects.isEmpty()) return;

        int ok = 0, fail = 0;
        for (Project p : projects) {
            Long pid = p.getId();
            if (pid == null) continue;
            try {
                doraService.computeMetrics(pid, start, now);
                ok++;
            } catch (Exception e) {
                fail++;
                log.error("DORA compute failed for project {}: {}", pid, e.toString());
            }
        }
        log.info("DORA 全量聚合结束 ok={} fail={}", ok, fail);
    }
}
