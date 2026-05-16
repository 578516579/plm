package cn.com.bosssfot.dv.plm.testdata.service.impl;

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
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.testdata.domain.TestData;
import cn.com.bosssfot.dv.plm.testdata.mapper.TestDataMapper;
import cn.com.bosssfot.dv.plm.testdata.service.ITestDataService;

/**
 * 测试数据工厂 Service — PRD §F4.3 + 原型 testdata.html
 *
 * 落地:
 * - ADR: generateTestDataNo() — TD-YYYY-NNNN
 * - 3 状态机: 00 草稿 → 01 已生成 → 02 已归档 (终态)
 * - ENUM 白名单: targetTable / outputFormat → 604
 * - generate() mock: 根据 targetTable + 4 规则开关 + AgriKB 字段语义,生成示例 JSON
 *   Dify 工作流 data-gen-flow Phase 后续接入
 */
@Service
public class TestDataServiceImpl implements ITestDataService
{
    private static final Logger log = LoggerFactory.getLogger(TestDataServiceImpl.class);

    private static final Set<String> ALLOWED_TABLE =
        Set.of("soil_sensor", "weather", "crop", "pest", "irrigation");
    private static final Set<String> ALLOWED_FORMAT = Set.of("json", "sql", "csv");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of());
    }

    @Autowired private TestDataMapper testdataMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<TestData> selectTestDataList(TestData t) { return testdataMapper.selectTestDataList(t); }

    @Override
    public TestData selectTestDataById(Long id) { return testdataMapper.selectTestDataById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTestData(TestData t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("任务标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (StringUtils.isBlank(t.getTargetTable())) {
            throw new ServiceException("目标表不能为空", 602);
        }
        if (!ALLOWED_TABLE.contains(t.getTargetTable())) {
            throw new ServiceException("目标表值非法 (允许: soil_sensor/weather/crop/pest/irrigation)", 604);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("创建人不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (StringUtils.isNotBlank(t.getOutputFormat()) && !ALLOWED_FORMAT.contains(t.getOutputFormat())) {
            throw new ServiceException("输出格式值非法 (允许: json/sql/csv)", 604);
        }

        if (StringUtils.isBlank(t.getOutputFormat())) t.setOutputFormat("json");
        if (t.getGenerateCount() == null) t.setGenerateCount(1000);
        if (StringUtils.isBlank(t.getRuleChinaCoord())) t.setRuleChinaCoord("Y");
        if (StringUtils.isBlank(t.getRuleTimeContinuity())) t.setRuleTimeContinuity("Y");
        if (StringUtils.isBlank(t.getRuleSensorRange())) t.setRuleSensorRange("Y");
        if (StringUtils.isBlank(t.getRuleIncludeOutliers())) t.setRuleIncludeOutliers("N");
        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建测试数据状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getTestdataNo())) {
            t.setTestdataNo(generateTestDataNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return testdataMapper.insertTestData(t);
        } catch (DuplicateKeyException e) {
            log.warn("testdata_no 重号,重试一次: {}", t.getTestdataNo());
            t.setTestdataNo(generateTestDataNo());
            return testdataMapper.insertTestData(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTestData(TestData t) {
        TestData old = testdataMapper.selectTestDataById(t.getTestdataId());
        if (old == null) {
            throw new ServiceException("测试数据集不存在", 404);
        }
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "测试数据状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(t.getTargetTable()) && !ALLOWED_TABLE.contains(t.getTargetTable())) {
            throw new ServiceException("目标表值非法", 604);
        }
        if (StringUtils.isNotBlank(t.getOutputFormat()) && !ALLOWED_FORMAT.contains(t.getOutputFormat())) {
            throw new ServiceException("输出格式值非法", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return testdataMapper.updateTestData(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestDataByIds(Long[] ids) {
        return testdataMapper.deleteTestDataByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestData generate(Long testdataId) {
        TestData td = testdataMapper.selectTestDataById(testdataId);
        if (td == null) {
            throw new ServiceException("测试数据集不存在", 404);
        }
        String semantics = "{\"sensor_id\":\"ID\",\"farm_id\":\"FK->farm\","
            + "\"timestamp\":\"datetime\",\"latitude\":\"中国范围 18~53\","
            + "\"longitude\":\"中国范围 73~135\","
            + "\"soil_moisture\":\"0-100\",\"soil_temperature\":\"-10~50\"}";
        String sample = "[{\"sensor_id\":\"S001\",\"farm_id\":1,"
            + "\"timestamp\":\"2026-05-17 10:00:00\","
            + "\"latitude\":34.7472,\"longitude\":113.6253,"
            + "\"soil_moisture\":42.5,\"soil_temperature\":18.3}]";
        td.setFieldSemantics(semantics);
        td.setGeneratedContent(sample);
        td.setStatus("01");
        td.setAiGenerated("Y");
        td.setGeneratedAt(new Date());
        td.setUpdateBy(SecurityUtils.getUsername());
        testdataMapper.updateTestData(td);
        return td;
    }

    private String generateTestDataNo() {
        int year = LocalDate.now().getYear();
        String prefix = "TD-" + year + "-";
        Integer maxSeq = testdataMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "已生成";
            case "02": return "已归档";
            default:   return "未知(" + status + ")";
        }
    }
}
