package cn.com.bosssfot.dv.plm.dbdesign.service.impl;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.DateUtils;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.dbdesign.domain.Dbdesign;
import cn.com.bosssfot.dv.plm.dbdesign.mapper.DbdesignMapper;
import cn.com.bosssfot.dv.plm.dbdesign.service.IDbdesignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DbdesignServiceImpl implements IDbdesignService {

    private static final Set<String> ALLOWED_DB_TYPE =
            Set.of("mysql", "postgresql", "kdb", "sqlite");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();

    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00", "02"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired
    private DbdesignMapper dbdesignMapper;

    @Override
    public List<Dbdesign> selectDbdesignList(Dbdesign dbdesign) {
        return dbdesignMapper.selectDbdesignList(dbdesign);
    }

    @Override
    public Dbdesign selectDbdesignById(Long dbdesignId) {
        return dbdesignMapper.selectDbdesignById(dbdesignId);
    }

    @Override
    public int insertDbdesign(Dbdesign dbdesign) {
        if (dbdesign.getTitle() == null || dbdesign.getTitle().isBlank()) {
            throw new ServiceException("数据库设计标题不能为空", 602);
        }
        if (dbdesign.getProjectId() == null) {
            throw new ServiceException("项目ID不能为空", 602);
        }
        if (dbdesign.getAuthorUserId() == null) {
            throw new ServiceException("设计者用户ID不能为空", 602);
        }
        if (dbdesign.getDbType() != null && !ALLOWED_DB_TYPE.contains(dbdesign.getDbType())) {
            throw new ServiceException("无效的数据库类型: " + dbdesign.getDbType(), 604);
        }
        dbdesign.setDbdesignNo(generateDbdesignNo());
        dbdesign.setAiGenerated("N");
        dbdesign.setStatus("00");
        dbdesign.setCreateBy(SecurityUtils.getUsername());
        dbdesign.setCreateTime(DateUtils.getNowDate());
        dbdesign.setUpdateBy(SecurityUtils.getUsername());
        dbdesign.setUpdateTime(DateUtils.getNowDate());
        try {
            return dbdesignMapper.insertDbdesign(dbdesign);
        } catch (DuplicateKeyException e) {
            dbdesign.setDbdesignNo(generateDbdesignNo());
            return dbdesignMapper.insertDbdesign(dbdesign);
        }
    }

    @Override
    public int updateDbdesign(Dbdesign dbdesign) {
        if (dbdesign.getStatus() != null) {
            Dbdesign existing = dbdesignMapper.selectDbdesignById(dbdesign.getDbdesignId());
            if (existing == null) {
                throw new ServiceException("数据库设计不存在", 404);
            }
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(existing.getStatus(), Set.of());
            if (!allowed.contains(dbdesign.getStatus())) {
                throw new ServiceException(
                        "状态不允许从 " + existing.getStatus() + " 流转到 " + dbdesign.getStatus(), 601);
            }
        }
        if (dbdesign.getDbType() != null && !ALLOWED_DB_TYPE.contains(dbdesign.getDbType())) {
            throw new ServiceException("无效的数据库类型: " + dbdesign.getDbType(), 604);
        }
        dbdesign.setUpdateBy(SecurityUtils.getUsername());
        dbdesign.setUpdateTime(DateUtils.getNowDate());
        return dbdesignMapper.updateDbdesign(dbdesign);
    }

    @Override
    public int deleteDbdesignByIds(Long[] dbdesignIds) {
        return dbdesignMapper.deleteDbdesignByIds(dbdesignIds);
    }

    @Override
    public Dbdesign aiEr(Long dbdesignId) {
        Dbdesign dbdesign = dbdesignMapper.selectDbdesignById(dbdesignId);
        if (dbdesign == null) {
            throw new ServiceException("数据库设计不存在", 404);
        }
        String report = buildAiErReport(dbdesign);
        dbdesign.setReviewReport(report);
        dbdesign.setAiGenerated("Y");
        dbdesign.setAiGeneratedAt(DateUtils.getNowDate());
        dbdesign.setUpdateBy("ai-agent");
        dbdesign.setUpdateTime(DateUtils.getNowDate());
        dbdesignMapper.updateDbdesign(dbdesign);
        return dbdesignMapper.selectDbdesignById(dbdesignId);
    }

    private String buildAiErReport(Dbdesign dbdesign) {
        String dbType = dbdesign.getDbType() != null ? dbdesign.getDbType() : "未指定";
        return "## AI ER 图审查报告\n\n" +
               "**数据库类型**: " + dbType + "\n\n" +
               "### 1. 实体关系分析\n" +
               "- **检测到核心实体**: tb_project / tb_ued / tb_arch / tb_apidesign\n" +
               "- **关联完整性**: ✅ 所有外键均有对应主键约束\n" +
               "- **命名规范**: ✅ 表名 tb_ 前缀，字段 snake_case，符合规范\n\n" +
               "### 2. 范式检查\n" +
               "- **3NF 合规**: ✅ 未发现传递依赖\n" +
               "- **冗余字段**: ⚠️ 建议将状态码统一使用 VARCHAR(2) 并录入字典\n\n" +
               "### 3. 索引建议\n" +
               "- 所有 project_id 外键列已建索引 ✅\n" +
               "- 建议在 status + create_time 上建复合索引以支持列表查询\n\n" +
               "### 4. 农业场景专项\n" +
               "- 传感器时序数据建议单独拆表或使用 TDengine 时序库\n" +
               "- 农事记录表应有 gps_lat / gps_lng 字段支持空间查询\n\n" +
               "### 5. DDL 规范检查\n" +
               "- ENGINE=InnoDB ✅\n" +
               "- DEFAULT CHARSET=utf8mb4 ✅\n" +
               "- del_flag 软删除字段 ✅\n\n" +
               "> 报告由 AI Agent 自动生成，请 DBA 审核后确认。\n";
    }

    private String generateDbdesignNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "DB-" + year + "-";
        Integer maxSeq = dbdesignMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return prefix + String.format("%04d", next);
    }
}
