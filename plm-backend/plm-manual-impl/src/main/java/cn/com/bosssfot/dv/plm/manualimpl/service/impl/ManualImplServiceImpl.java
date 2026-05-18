package cn.com.bosssfot.dv.plm.manualimpl.service.impl;

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
import cn.com.bosssfot.dv.plm.manualimpl.domain.ManualImpl;
import cn.com.bosssfot.dv.plm.manualimpl.mapper.ManualImplMapper;
import cn.com.bosssfot.dv.plm.manualimpl.service.IManualImplService;

/**
 * 实施手册 Service — PRD §F5.2
 *
 * 落地:
 * - ADR: generateManualImplNo() — MIM-YYYY-NNNN
 * - 5 状态机: 00 草稿 → 01 生成中 → 02 已生成 → 03 已审核 → 04 已发布
 * - ENUM 白名单: deploymentMode / os / database → 604
 * - aiGenerate() mock: 生成示例实施手册正文
 */
@Service
public class ManualImplServiceImpl implements IManualImplService
{
    private static final Logger log = LoggerFactory.getLogger(ManualImplServiceImpl.class);

    private static final Set<String> ALLOWED_DEPLOYMENT = Set.of("docker", "k8s", "bare_metal");
    private static final Set<String> ALLOWED_OS = Set.of("centos7", "ubuntu20", "kylin");
    private static final Set<String> ALLOWED_DATABASE = Set.of("mysql8", "pg14", "kingbase");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of("04"));
        STATUS_TRANSITIONS.put("04", Set.of());
    }

    @Autowired private ManualImplMapper manualImplMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<ManualImpl> selectManualImplList(ManualImpl t) {
        return manualImplMapper.selectManualImplList(t);
    }

    @Override
    public ManualImpl selectManualImplById(Long id) {
        return manualImplMapper.selectManualImplById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertManualImpl(ManualImpl t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("手册标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("作者不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (StringUtils.isNotBlank(t.getDeploymentMode()) && !ALLOWED_DEPLOYMENT.contains(t.getDeploymentMode())) {
            throw new ServiceException("部署模式值非法 (允许: docker/k8s/bare_metal)", 604);
        }
        if (StringUtils.isNotBlank(t.getOs()) && !ALLOWED_OS.contains(t.getOs())) {
            throw new ServiceException("操作系统值非法 (允许: centos7/ubuntu20/kylin)", 604);
        }
        if (StringUtils.isNotBlank(t.getDatabase()) && !ALLOWED_DATABASE.contains(t.getDatabase())) {
            throw new ServiceException("数据库值非法 (允许: mysql8/pg14/kingbase)", 604);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建实施手册状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getManualImplNo())) {
            t.setManualImplNo(generateManualImplNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return manualImplMapper.insertManualImpl(t);
        } catch (DuplicateKeyException e) {
            log.warn("manual_impl_no 重号,重试一次: {}", t.getManualImplNo());
            t.setManualImplNo(generateManualImplNo());
            return manualImplMapper.insertManualImpl(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateManualImpl(ManualImpl t) {
        ManualImpl old = manualImplMapper.selectManualImplById(t.getManualImplId());
        if (old == null) {
            throw new ServiceException("实施手册不存在", 404);
        }
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "实施手册状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(t.getDeploymentMode()) && !ALLOWED_DEPLOYMENT.contains(t.getDeploymentMode())) {
            throw new ServiceException("部署模式值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getOs()) && !ALLOWED_OS.contains(t.getOs())) {
            throw new ServiceException("操作系统值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getDatabase()) && !ALLOWED_DATABASE.contains(t.getDatabase())) {
            throw new ServiceException("数据库值非法", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return manualImplMapper.updateManualImpl(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteManualImplByIds(Long[] ids) {
        return manualImplMapper.deleteManualImplByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManualImpl aiGenerate(Long manualImplId) {
        ManualImpl mi = manualImplMapper.selectManualImplById(manualImplId);
        if (mi == null) {
            throw new ServiceException("实施手册不存在", 404);
        }
        String mockContent = "# AgriPLM 实施手册\n\n"
            + "## 部署步骤\n\n"
            + "### 1. 环境准备\n\n"
            + "- 确认服务器满足最低配置要求（4 核 8G 内存）\n"
            + "- 安装必要依赖：JDK 17、MySQL 8.x、Redis 7.x\n\n"
            + "### 2. 数据库初始化\n\n"
            + "```sql\n"
            + "CREATE DATABASE plm DEFAULT CHARACTER SET utf8mb4;\n"
            + "```\n\n"
            + "### 3. 应用部署\n\n"
            + "```bash\n"
            + "java -jar plm-admin.jar --spring.profiles.active=prod\n"
            + "```\n\n"
            + "### 4. 验证\n\n"
            + "访问 http://<server>:8080 确认系统正常运行。\n";
        mi.setAiGenerated("Y");
        mi.setAiGeneratedAt(new Date());
        mi.setStatus("02");
        mi.setContent(mockContent);
        mi.setUpdateBy(SecurityUtils.getUsername());
        manualImplMapper.updateManualImpl(mi);
        return mi;
    }

    private String generateManualImplNo() {
        int year = LocalDate.now().getYear();
        String prefix = "MIM-" + year + "-";
        Integer maxSeq = manualImplMapper.selectMaxSeqOfYear(prefix);
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
