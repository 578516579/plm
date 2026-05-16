package cn.com.bosssfot.dv.plm.testdata.service.impl;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.DateUtils;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.testdata.domain.Testdata;
import cn.com.bosssfot.dv.plm.testdata.mapper.TestdataMapper;
import cn.com.bosssfot.dv.plm.testdata.service.ITestdataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TestdataServiceImpl implements ITestdataService {

    private static final Set<String> ALLOWED_TARGET_TABLE = Set.of(
            "t_soil_sensor_data", "t_weather_record", "t_crop_info",
            "t_pest_record", "t_irrigation_plan");
    private static final Set<String> ALLOWED_FORMAT = Set.of("json", "sql_insert", "csv");
    private static final Set<String> ALLOWED_TARGET = Set.of("test", "dev");
    private static final Set<String> ALLOWED_WRITE_MODE = Set.of("append", "truncate", "upsert");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();

    static {
        STATUS_TRANSITIONS.put("00", Set.of("01", "02"));
        STATUS_TRANSITIONS.put("01", Set.of("02", "03"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired
    private TestdataMapper testdataMapper;

    @Override
    public List<Testdata> selectTestdataList(Testdata testdata) {
        return testdataMapper.selectTestdataList(testdata);
    }

    @Override
    public Testdata selectTestdataById(Long testdataId) {
        return testdataMapper.selectTestdataById(testdataId);
    }

    @Override
    public int insertTestdata(Testdata testdata) {
        if (testdata.getTitle() == null || testdata.getTitle().isBlank()) {
            throw new ServiceException("数据集名称不能为空", 602);
        }
        if (testdata.getProjectId() == null) {
            throw new ServiceException("项目ID不能为空", 602);
        }
        if (testdata.getAuthorUserId() == null) {
            throw new ServiceException("创建者用户ID不能为空", 602);
        }
        validateEnums(testdata);
        testdata.setTestdataNo(generateTestdataNo());
        if (testdata.getGenerateCount() == null) testdata.setGenerateCount(1000);
        if (testdata.getOutputFormat() == null) testdata.setOutputFormat("json");
        if (testdata.getRuleCoordinate() == null) testdata.setRuleCoordinate("Y");
        if (testdata.getRuleTimeSeries() == null) testdata.setRuleTimeSeries("Y");
        if (testdata.getRuleSensorRange() == null) testdata.setRuleSensorRange("Y");
        if (testdata.getRuleIncludeAbnormal() == null) testdata.setRuleIncludeAbnormal("N");
        testdata.setAiGenerated("N");
        testdata.setStatus("00");
        testdata.setCreateBy(SecurityUtils.getUsername());
        testdata.setCreateTime(DateUtils.getNowDate());
        testdata.setUpdateBy(SecurityUtils.getUsername());
        testdata.setUpdateTime(DateUtils.getNowDate());
        try {
            return testdataMapper.insertTestdata(testdata);
        } catch (DuplicateKeyException e) {
            testdata.setTestdataNo(generateTestdataNo());
            return testdataMapper.insertTestdata(testdata);
        }
    }

    @Override
    public int updateTestdata(Testdata testdata) {
        if (testdata.getStatus() != null) {
            Testdata existing = testdataMapper.selectTestdataById(testdata.getTestdataId());
            if (existing == null) throw new ServiceException("测试数据集不存在", 404);
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(existing.getStatus(), Set.of());
            if (!allowed.contains(testdata.getStatus())) {
                throw new ServiceException("状态不允许从 " + existing.getStatus() + " 流转到 " + testdata.getStatus(), 601);
            }
        }
        validateEnums(testdata);
        testdata.setUpdateBy(SecurityUtils.getUsername());
        testdata.setUpdateTime(DateUtils.getNowDate());
        return testdataMapper.updateTestdata(testdata);
    }

    @Override
    public int deleteTestdataByIds(Long[] testdataIds) {
        return testdataMapper.deleteTestdataByIds(testdataIds);
    }

    @Override
    public Testdata aiGenerate(Long testdataId) {
        Testdata testdata = testdataMapper.selectTestdataById(testdataId);
        if (testdata == null) throw new ServiceException("测试数据集不存在", 404);

        String table = testdata.getTargetTable() != null ? testdata.getTargetTable() : "t_soil_sensor_data";
        int count = testdata.getGenerateCount() != null ? testdata.getGenerateCount() : 1000;
        String format = testdata.getOutputFormat() != null ? testdata.getOutputFormat() : "json";

        String data = buildMockData(table, count, format, testdata);
        testdata.setGeneratedData(data);
        testdata.setStatus("02");
        testdata.setAiGenerated("Y");
        testdata.setAiGeneratedAt(DateUtils.getNowDate());
        testdata.setUpdateBy("ai-agent");
        testdata.setUpdateTime(DateUtils.getNowDate());
        testdataMapper.updateTestdata(testdata);
        return testdataMapper.selectTestdataById(testdataId);
    }

    private String buildMockData(String table, int count, String format, Testdata td) {
        String ruleCoord = "Y".equals(td.getRuleCoordinate()) ? "✅" : "❌";
        String ruleTs   = "Y".equals(td.getRuleTimeSeries()) ? "✅" : "❌";
        String ruleSens = "Y".equals(td.getRuleSensorRange()) ? "✅" : "❌";
        String ruleAbn  = "Y".equals(td.getRuleIncludeAbnormal()) ? "✅ 含异常值" : "❌";

        if ("json".equals(format)) {
            return "[\n  {\n" +
                   "    \"_generated_by\": \"AgriPLM AI Data Factory\",\n" +
                   "    \"_table\": \"" + table + "\",\n" +
                   "    \"_count\": " + count + ",\n" +
                   "    \"_rules\": {\"coordinate\": \"" + ruleCoord + "\", \"time_series\": \"" + ruleTs +
                   "\", \"sensor_range\": \"" + ruleSens + "\", \"include_abnormal\": \"" + ruleAbn + "\"},\n" +
                   "    \"field_code\": \"350200-FC-0342\",\n" +
                   "    \"soil_moisture\": 23.5,\n" +
                   "    \"soil_temp\": 18.2,\n" +
                   "    \"lat\": 28.2356,\n" +
                   "    \"lng\": 117.8942,\n" +
                   "    \"ts\": \"2026-05-16T08:00:00+08:00\"\n" +
                   "  },\n  ... (" + (count - 1) + " more records)\n]";
        } else if ("sql_insert".equals(format)) {
            return "-- Generated by AgriPLM AI Data Factory\n" +
                   "-- Table: " + table + ", Count: " + count + "\n" +
                   "INSERT INTO " + table + " (field_code, soil_moisture, soil_temp, lat, lng, ts) VALUES\n" +
                   "  ('350200-FC-0342', 23.5, 18.2, 28.2356, 117.8942, '2026-05-16 08:00:00'),\n" +
                   "  ... (" + (count - 1) + " more rows);";
        } else {
            return "field_code,soil_moisture,soil_temp,lat,lng,ts\n" +
                   "350200-FC-0342,23.5,18.2,28.2356,117.8942,2026-05-16T08:00:00\n" +
                   "... (" + (count - 1) + " more rows)";
        }
    }

    private void validateEnums(Testdata testdata) {
        if (testdata.getTargetTable() != null && !ALLOWED_TARGET_TABLE.contains(testdata.getTargetTable())) {
            throw new ServiceException("无效的目标数据表: " + testdata.getTargetTable(), 604);
        }
        if (testdata.getOutputFormat() != null && !ALLOWED_FORMAT.contains(testdata.getOutputFormat())) {
            throw new ServiceException("无效的输出格式: " + testdata.getOutputFormat(), 604);
        }
        if (testdata.getWriteTarget() != null && !ALLOWED_TARGET.contains(testdata.getWriteTarget())) {
            throw new ServiceException("无效的写入目标: " + testdata.getWriteTarget(), 604);
        }
        if (testdata.getWriteMode() != null && !ALLOWED_WRITE_MODE.contains(testdata.getWriteMode())) {
            throw new ServiceException("无效的写入模式: " + testdata.getWriteMode(), 604);
        }
    }

    private String generateTestdataNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "TD-" + year + "-";
        Integer maxSeq = testdataMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return prefix + String.format("%04d", next);
    }
}
