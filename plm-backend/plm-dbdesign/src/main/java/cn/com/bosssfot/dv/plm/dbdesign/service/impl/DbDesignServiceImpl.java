package cn.com.bosssfot.dv.plm.dbdesign.service.impl;

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
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.dbdesign.domain.DbDesign;
import cn.com.bosssfot.dv.plm.dbdesign.mapper.DbDesignMapper;
import cn.com.bosssfot.dv.plm.dbdesign.service.IDbDesignService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 数据库设计 Service — PRD §F3.2 + 原型 dbdesign.html
 *
 * 落地:
 * - ADR: generateDbDesignNo() — DB-YYYY-NNNN
 * - 4 状态机 (含反向边 01→00): 00→01→{00,02}→03 终态
 * - ENUM 白名单 dbEngine ∈ {mysql, postgresql, kingbase} → 604
 * - aiGenerate() mock: 返回 Mermaid ER 图 + 数据字典 Markdown + DDL 模板 + 规范检查 JSON
 */
@Service
public class DbDesignServiceImpl implements IDbDesignService
{
    private static final Logger log = LoggerFactory.getLogger(DbDesignServiceImpl.class);

    private static final Set<String> ALLOWED_ENGINE = Set.of("mysql", "postgresql", "kingbase");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00", "02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired private DbDesignMapper dbdesignMapper;
    @Autowired private AiService aiService;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<DbDesign> selectDbDesignList(DbDesign t) { return dbdesignMapper.selectDbDesignList(t); }

    @Override
    public DbDesign selectDbDesignById(Long id) { return dbdesignMapper.selectDbDesignById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDbDesign(DbDesign t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("设计标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("DBA 不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (StringUtils.isNotBlank(t.getDbEngine()) && !ALLOWED_ENGINE.contains(t.getDbEngine())) {
            throw new ServiceException("DB 引擎值非法 (允许: mysql/postgresql/kingbase)", 604);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建 DB 设计状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getDbdesignNo())) {
            t.setDbdesignNo(generateDbDesignNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return dbdesignMapper.insertDbDesign(t);
        } catch (DuplicateKeyException e) {
            log.warn("dbdesign_no 重号,重试一次: {}", t.getDbdesignNo());
            t.setDbdesignNo(generateDbDesignNo());
            return dbdesignMapper.insertDbDesign(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDbDesign(DbDesign t) {
        DbDesign old = dbdesignMapper.selectDbDesignById(t.getDbdesignId());
        if (old == null) {
            throw new ServiceException("DB 设计不存在", 404);
        }
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "DB 设计状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(t.getDbEngine()) && !ALLOWED_ENGINE.contains(t.getDbEngine())) {
            throw new ServiceException("DB 引擎值非法", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return dbdesignMapper.updateDbDesign(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDbDesignByIds(Long[] ids) {
        return dbdesignMapper.deleteDbDesignByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DbDesign aiGenerate(Long dbdesignId) {
        DbDesign d = dbdesignMapper.selectDbDesignById(dbdesignId);
        if (d == null) {
            throw new ServiceException("DB 设计不存在", 404);
        }
        aiService.chat(AiChatRequest.builder("")
            .system("你是 PLM 资深数据库架构师,擅长 ER 设计与规范化")
            .user("请生成 [" + d.getTitle() + "] 的 ER 图与 DDL")
            .callerTag("dbdesign#" + dbdesignId).build());
        String engine = d.getDbEngine() == null ? "mysql" : d.getDbEngine();
        String er = "erDiagram\n"
            + "  PROJECT ||--o{ REQUIREMENT : has\n"
            + "  PROJECT ||--o{ SPRINT : has\n"
            + "  SPRINT ||--o{ TASK : contains\n"
            + "  REQUIREMENT ||--o{ TASK : drives\n"
            + "  PROJECT { bigint id PK\n    string name\n    string status }\n"
            + "  REQUIREMENT { bigint id PK\n    bigint project_id FK\n    string title }\n"
            + "  TASK { bigint id PK\n    bigint sprint_id FK\n    string title }\n";
        String dict = "| 表名 | 字段 | 类型 | 说明 |\n|---|---|---|---|\n"
            + "| tb_project | project_id | BIGINT | 主键 |\n"
            + "| tb_project | project_name | VARCHAR(200) | 项目名称 |\n"
            + "| tb_requirement | requirement_no | VARCHAR(32) | REQ-YYYY-NNNN |\n";
        String ddl = "-- " + engine + "\n"
            + "CREATE TABLE tb_project (\n"
            + "  project_id BIGINT(20) NOT NULL AUTO_INCREMENT,\n"
            + "  project_name VARCHAR(200) NOT NULL,\n"
            + "  status VARCHAR(2) DEFAULT '0',\n"
            + "  PRIMARY KEY (project_id)\n"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
        String check = "{\"naming\":\"pass (tb_/sys_ prefix)\","
            + "\"index\":\"warn (建议为外键加索引)\","
            + "\"normalization\":\"pass (3NF)\"}";
        d.setErDiagramContent(er);
        d.setDataDictionary(dict);
        d.setDdlScript(ddl);
        d.setNormalizationCheck(check);
        d.setAiGenerated("Y");
        d.setAiGeneratedAt(new Date());
        d.setUpdateBy(SecurityUtils.getUsername());
        dbdesignMapper.updateDbDesign(d);
        return d;
    }

    private String generateDbDesignNo() {
        int year = LocalDate.now().getYear();
        String prefix = "DB-" + year + "-";
        Integer maxSeq = dbdesignMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "评审中";
            case "02": return "已确认";
            case "03": return "已废弃";
            default:   return "未知(" + status + ")";
        }
    }
}
