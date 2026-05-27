package cn.com.bosssfot.dv.plm.integration.adapter.zentao;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 {@link ZentaoFieldMapper} 的状态机映射 + payload 提取。
 *
 * <p>覆盖范围:
 * <ul>
 *   <li>4 个资源(bug/story/task/case)的状态机正反向映射,含未识别值落 99</li>
 *   <li>bug severity 1-4 → defect 0-3 互转</li>
 *   <li>4 个 payload → fields 提取,含 HTML 清洗</li>
 * </ul>
 */
class ZentaoFieldMapperTest {

    @Test
    void bugStatusToDefect_allKnown() {
        assertEquals("1",  ZentaoFieldMapper.bugStatusToDefect("active"));
        assertEquals("2",  ZentaoFieldMapper.bugStatusToDefect("resolved"));
        assertEquals("3",  ZentaoFieldMapper.bugStatusToDefect("closed"));
    }

    @Test
    void bugStatusToDefect_unknownFallsBackTo99() {
        assertEquals("99", ZentaoFieldMapper.bugStatusToDefect("invalid"));
        assertEquals("99", ZentaoFieldMapper.bugStatusToDefect(null));
        assertEquals("99", ZentaoFieldMapper.bugStatusToDefect(""));
    }

    @Test
    void defectStatusToBug_reverse() {
        assertEquals("active",   ZentaoFieldMapper.defectStatusToBug("1"));
        assertEquals("resolved", ZentaoFieldMapper.defectStatusToBug("2"));
        assertEquals("closed",   ZentaoFieldMapper.defectStatusToBug("3"));
        assertNull(ZentaoFieldMapper.defectStatusToBug("99"));   // 兜底值不反推
        assertNull(ZentaoFieldMapper.defectStatusToBug(null));
    }

    @Test
    void bugSeverity_bidirectional() {
        // 禅道 1-4 ↔ PLM 0-3
        assertEquals("0", ZentaoFieldMapper.bugSeverityToDefect(1));
        assertEquals("1", ZentaoFieldMapper.bugSeverityToDefect(2));
        assertEquals("2", ZentaoFieldMapper.bugSeverityToDefect(3));
        assertEquals("3", ZentaoFieldMapper.bugSeverityToDefect(4));
        assertNull(ZentaoFieldMapper.bugSeverityToDefect(null));
        assertNull(ZentaoFieldMapper.bugSeverityToDefect(99));

        assertEquals(1, ZentaoFieldMapper.defectSeverityToBug("0"));
        assertEquals(2, ZentaoFieldMapper.defectSeverityToBug("1"));
        assertEquals(3, ZentaoFieldMapper.defectSeverityToBug("2"));
        assertEquals(4, ZentaoFieldMapper.defectSeverityToBug("3"));
        assertNull(ZentaoFieldMapper.defectSeverityToBug(null));
    }

    @Test
    void storyStage_multipleStagesMapToSamePlmStatus() {
        // wait / planned 都映射到 00 待评审
        assertEquals("00", ZentaoFieldMapper.storyStageToReqStatus("wait"));
        assertEquals("00", ZentaoFieldMapper.storyStageToReqStatus("planned"));
        // projected / developing 都映射到 01 开发中
        assertEquals("01", ZentaoFieldMapper.storyStageToReqStatus("projected"));
        assertEquals("01", ZentaoFieldMapper.storyStageToReqStatus("developing"));
        // released / closed 都映射到 02 已完成
        assertEquals("02", ZentaoFieldMapper.storyStageToReqStatus("released"));
        assertEquals("02", ZentaoFieldMapper.storyStageToReqStatus("closed"));
        // 未知 → 99
        assertEquals("99", ZentaoFieldMapper.storyStageToReqStatus("unknown"));
        assertEquals("99", ZentaoFieldMapper.storyStageToReqStatus(null));
    }

    @Test
    void reqStatusToStoryStage_reverseTakesMostSpecific() {
        // PLM 01 反推 禅道 → "developing"(选最具体的,不是 projected)
        assertEquals("developing", ZentaoFieldMapper.reqStatusToStoryStage("01"));
        // PLM 02 反推 → "released"
        assertEquals("released",   ZentaoFieldMapper.reqStatusToStoryStage("02"));
        // PLM 03 已取消 → "closed"
        assertEquals("closed",     ZentaoFieldMapper.reqStatusToStoryStage("03"));
        assertNull(ZentaoFieldMapper.reqStatusToStoryStage("99"));
    }

    @Test
    void taskStatus_full6x6() {
        assertEquals("0", ZentaoFieldMapper.taskStatusToPlm("wait"));
        assertEquals("1", ZentaoFieldMapper.taskStatusToPlm("doing"));
        assertEquals("2", ZentaoFieldMapper.taskStatusToPlm("done"));
        assertEquals("3", ZentaoFieldMapper.taskStatusToPlm("pause"));
        assertEquals("4", ZentaoFieldMapper.taskStatusToPlm("cancel"));
        assertEquals("5", ZentaoFieldMapper.taskStatusToPlm("closed"));
        assertEquals("99", ZentaoFieldMapper.taskStatusToPlm("xxx"));

        assertEquals("wait",   ZentaoFieldMapper.taskStatusToZentao("0"));
        assertEquals("doing",  ZentaoFieldMapper.taskStatusToZentao("1"));
        assertEquals("done",   ZentaoFieldMapper.taskStatusToZentao("2"));
        assertEquals("pause",  ZentaoFieldMapper.taskStatusToZentao("3"));
        assertEquals("cancel", ZentaoFieldMapper.taskStatusToZentao("4"));
        assertEquals("closed", ZentaoFieldMapper.taskStatusToZentao("5"));
    }

    @Test
    void caseStatus_simple() {
        assertEquals("0", ZentaoFieldMapper.caseStatusToPlm("normal"));
        assertEquals("1", ZentaoFieldMapper.caseStatusToPlm("blocked"));
        assertEquals("99", ZentaoFieldMapper.caseStatusToPlm("anything"));
        assertEquals("normal",  ZentaoFieldMapper.caseStatusToZentao("0"));
        assertEquals("blocked", ZentaoFieldMapper.caseStatusToZentao("1"));
    }

    @Test
    void bugPayload_toDefectFields_full() {
        JSONObject z = JSON.parseObject("""
            {
              "id": 1234,
              "title": "登录接口返回 500",
              "steps": "<p>1. 进入登录页</p><p>2. 输入正确账号</p>",
              "severity": 2,
              "pri": 2,
              "status": "active",
              "openedBy": "wjl"
            }
            """);
        Map<String, Object> fields = ZentaoFieldMapper.bugPayloadToDefectFields(z);
        assertEquals("登录接口返回 500", fields.get("title"));
        // severity 2 → "1" (Critical)
        assertEquals("1", fields.get("severity"));
        // active → "1" (新建)
        assertEquals("1", fields.get("status"));
        // HTML 标签被剥离;<p> 转换成换行;期望以"1. 进入登录页"开头
        String desc = (String) fields.get("description");
        assertTrue(desc.contains("1. 进入登录页"), "description should contain stripped text, but got: " + desc);
        assertFalse(desc.contains("<p>"), "description should strip HTML tags");
    }

    @Test
    void bugPayload_emptyStringFieldsAreSafe() {
        JSONObject z = JSON.parseObject("{\"id\": 1, \"title\": \"\", \"status\": \"\"}");
        Map<String, Object> fields = ZentaoFieldMapper.bugPayloadToDefectFields(z);
        assertEquals("", fields.get("title"));
        // 空状态 → 99
        assertEquals("99", fields.get("status"));
    }

    @Test
    void htmlEntitiesAreDecoded() {
        JSONObject z = JSON.parseObject("{\"id\":1,\"steps\":\"a&nbsp;b&amp;c&lt;d&gt;e\"}");
        Map<String, Object> fields = ZentaoFieldMapper.bugPayloadToDefectFields(z);
        String desc = (String) fields.get("description");
        assertEquals("a b&c<d>e", desc);
    }

    @Test
    void taskPayload_keysAlignWithDbColumns() {
        // tb_task DB 列是 estimated_hours / actual_hours,不是 estimate_hours / consumed_hours
        JSONObject z = JSON.parseObject("""
            {"id":42, "name":"任务标题", "desc":"略", "pri":"2",
             "status":"doing", "estimate":4.5, "consumed":1.5}
            """);
        Map<String, Object> fields = ZentaoFieldMapper.taskPayloadToTaskFields(z);
        assertTrue(fields.containsKey("estimatedHours"), "key must align with DB column estimated_hours");
        assertTrue(fields.containsKey("actualHours"),    "key must align with DB column actual_hours");
        assertFalse(fields.containsKey("estimateHours"), "old key 'estimateHours' shouldn't exist");
        assertEquals("1", fields.get("status"));  // doing → 1
    }

    @Test
    void casePayload_keysAlignWithDbColumns() {
        // tb_testcase DB 列是 preconditions / category / expected_result(都不是单数 / case_type)
        JSONObject z = JSON.parseObject("""
            {"id":7, "title":"登录正常", "precondition":"用户已注册",
             "steps":"[{\\"step\\":\\"输入账号\\"}]", "expect":"登录成功",
             "pri":"1", "type":"functional", "status":"normal"}
            """);
        Map<String, Object> fields = ZentaoFieldMapper.casePayloadToTestcaseFields(z);
        assertTrue(fields.containsKey("preconditions"),  "key must align with DB column preconditions");
        assertTrue(fields.containsKey("category"),       "key must align with DB column category");
        assertTrue(fields.containsKey("expectedResult"), "key must align with DB column expected_result");
        assertFalse(fields.containsKey("precondition"),  "old key 'precondition' shouldn't exist");
        assertFalse(fields.containsKey("caseType"),      "old key 'caseType' shouldn't exist");
        assertEquals("用户已注册", fields.get("preconditions"));
        assertEquals("登录成功",   fields.get("expectedResult"));
        assertEquals("functional", fields.get("category"));
        assertEquals("0", fields.get("status"));  // normal → 0
    }

    @Test
    void storyPayload_basicFields() {
        JSONObject z = JSON.parseObject("""
            {
              "id": 10,
              "title": "新增 OAuth 登录",
              "spec": "支持飞书 / 钉钉",
              "pri": "1",
              "stage": "developing"
            }
            """);
        Map<String, Object> fields = ZentaoFieldMapper.storyPayloadToReqFields(z);
        assertEquals("新增 OAuth 登录", fields.get("title"));
        assertEquals("支持飞书 / 钉钉", fields.get("description"));
        assertEquals("1", fields.get("priority"));
        assertEquals("01", fields.get("status"));  // developing → 01
    }
}
