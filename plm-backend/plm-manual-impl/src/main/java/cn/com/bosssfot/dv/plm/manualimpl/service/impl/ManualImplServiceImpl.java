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
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.AiTexts;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.manualimpl.domain.ManualImpl;
import cn.com.bosssfot.dv.plm.manualimpl.mapper.ManualImplMapper;
import cn.com.bosssfot.dv.plm.manualimpl.service.IManualImplService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 实施手册 Service — PRD §F5.2 + 原型 implmanual.html
 *
 * 落地:
 * - generateManualimplNo() — IM-YYYY-NNNN
 * - PRD §F5.2: AI 一键生成 + 部署模式/OS/DB 维度配置 + 多格式导出
 * - 4 状态机: 00→{01}, 01→{02}, 02→{00,03}, 03→{} (终态)
 * - 02 (已生成) 时自动填 generatedAt
 */
@Service
public class ManualImplServiceImpl implements IManualImplService {
    private static final Logger log = LoggerFactory.getLogger(ManualImplServiceImpl.class);

    private static final Set<String> ALLOWED_DEPLOY = Set.of("docker_compose", "kubernetes", "baremetal");
    private static final Set<String> ALLOWED_OS     = Set.of("centos7", "ubuntu20", "kylin");
    private static final Set<String> ALLOWED_DB     = Set.of("postgresql14", "mysql8", "kdb");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of("00", "03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired private ManualImplMapper manualimplMapper;
    @Autowired private AiService aiService;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<ManualImpl> selectManualImplList(ManualImpl t) {
        return manualimplMapper.selectManualImplList(t);
    }

    @Override
    public ManualImpl selectManualImplById(Long id) {
        return manualimplMapper.selectManualImplById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertManualImpl(ManualImpl t) {
        if (StringUtils.isBlank(t.getTitle()))            throw new ServiceException("手册标题不能为空", 602);
        if (t.getProjectId() == null)                     throw new ServiceException("关联项目不能为空", 602);
        if (t.getAuthorUserId() == null)                  throw new ServiceException("作者不能为空", 602);
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null)                              throw new ServiceException("关联项目不存在", 702);
        validateEnums(t);

        if (StringUtils.isBlank(t.getOutputFormats())) t.setOutputFormats("pdf");
        if (StringUtils.isBlank(t.getAiGenerated()))   t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus()) && !"01".equals(t.getStatus())) {
            throw new ServiceException("新建手册状态必须为「草稿」或「生成中」", 601);
        }

        if (StringUtils.isBlank(t.getManualimplNo())) t.setManualimplNo(generateManualimplNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return manualimplMapper.insertManualImpl(t);
        } catch (DuplicateKeyException e) {
            log.warn("manualimpl_no 重号,重试一次: {}", t.getManualimplNo());
            t.setManualimplNo(generateManualimplNo());
            return manualimplMapper.insertManualImpl(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateManualImpl(ManualImpl t) {
        ManualImpl old = manualimplMapper.selectManualImplById(t.getManualimplId());
        if (old == null) throw new ServiceException("实施手册不存在", 404);

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
        return manualimplMapper.updateManualImpl(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteManualImplByIds(Long[] ids) {
        return manualimplMapper.deleteManualImplByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManualImpl aiGenerate(Long id) {
        ManualImpl t = manualimplMapper.selectManualImplById(id);
        if (t == null) throw new ServiceException("实施手册不存在", 404);
        String content = AiTexts.generate(aiService,AiChatRequest.builder("")
            .system("你是 PLM 实施工程师,擅长部署步骤与环境配置文档撰写")
            .user("请生成 [" + t.getTitle() + "] 实施手册")
            .callerTag("manual-impl#" + id).build(),
            () -> buildAiContent(t));
        t.setContent(content);
        t.setStatus("02");
        t.setAiGenerated("Y");
        t.setGeneratedAt(new Date());
        t.setUpdateBy("ai-agent");
        manualimplMapper.updateManualImpl(t);
        return manualimplMapper.selectManualImplById(id);
    }

    private String buildAiContent(ManualImpl t) {
        String deploy = t.getDeployMode() != null ? t.getDeployMode() : "docker_compose";
        String os     = t.getOsType()     != null ? t.getOsType()     : "centos7";
        String db     = t.getDbType()     != null ? t.getDbType()     : "postgresql14";

        return "# " + t.getTitle() + "\n\n" +
               "> AI Generated by AgriPLM — PRD §F5.2 Impl Manual Flow\n\n" +
               "## 1. 部署环境\n" +
               "- **部署模式**: " + deploy + "\n" +
               "- **操作系统**: " + os + "\n" +
               "- **数据库**: " + db + "\n\n" +
               "## 2. 环境变量\n```json\n" + (t.getEnvConfig() != null ? t.getEnvConfig() : "{\"DB_HOST\":\"127.0.0.1\",\"REDIS_HOST\":\"127.0.0.1\"}") + "\n```\n\n" +
               "## 3. 部署步骤\n" +
               "1. 准备 " + os + " 环境,关闭防火墙/SELinux\n" +
               "2. 安装 " + db + " 并初始化 plm schema\n" +
               "3. 启动 " + deploy + " 编排 (docker-compose up -d / kubectl apply -f)\n" +
               "4. 验证 /actuator/health 返回 UP\n\n" +
               "## 4. 农情大屏接入\n" +
               "- 配置 IoT MQTT broker 地址\n" +
               "- 导入土壤/气象传感器初始化 dict\n" +
               "- 启用 captchaImage 验证码\n\n" +
               "## 5. 回滚预案\n" +
               "停服 < 5 分钟,数据库 binlog 回滚 + 镜像 rollback。\n";
    }

    private void validateEnums(ManualImpl t) {
        if (t.getDeployMode() != null && !ALLOWED_DEPLOY.contains(t.getDeployMode()))
            throw new ServiceException("无效的部署模式: " + t.getDeployMode(), 604);
        if (t.getOsType() != null && !ALLOWED_OS.contains(t.getOsType()))
            throw new ServiceException("无效的操作系统: " + t.getOsType(), 604);
        if (t.getDbType() != null && !ALLOWED_DB.contains(t.getDbType()))
            throw new ServiceException("无效的数据库: " + t.getDbType(), 604);
    }

    private String generateManualimplNo() {
        int year = LocalDate.now().getYear();
        String prefix = "IM-" + year + "-";
        Integer maxSeq = manualimplMapper.selectMaxSeqOfYear(prefix);
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
