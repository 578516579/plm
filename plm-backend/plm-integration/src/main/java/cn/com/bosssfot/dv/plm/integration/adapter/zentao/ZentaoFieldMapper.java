package cn.com.bosssfot.dv.plm.integration.adapter.zentao;

import java.util.Map;
import com.alibaba.fastjson2.JSONObject;

/**
 * 禅道字段 ↔ PLM 字段映射工具类(纯静态,无状态)。
 *
 * <p>覆盖 4 类资源的双向映射:
 * <ul>
 *   <li>bug ↔ defect(severity/priority/status)</li>
 *   <li>story ↔ requirement(priority/stage→status)</li>
 *   <li>task ↔ task(priority/status)</li>
 *   <li>case ↔ testcase(priority/status)</li>
 * </ul>
 *
 * <p>未识别的外部状态值 → 落 PLM 兜底值 {@code "99"}(已经在字典 [biz_*_status] 加 99=外部同步)。
 */
public final class ZentaoFieldMapper {

    private ZentaoFieldMapper() {}

    // ─────────── Bug ↔ Defect 状态 ───────────
    public static String bugStatusToDefect(String zStatus) {
        if (zStatus == null) return "99";
        return switch (zStatus) {
            case "active"   -> "1"; // 新建
            case "resolved" -> "2"; // 已解决
            case "closed"   -> "3"; // 已关闭
            default -> "99";
        };
    }

    public static String defectStatusToBug(String plmStatus) {
        if (plmStatus == null) return null;
        return switch (plmStatus) {
            case "1" -> "active";
            case "2" -> "resolved";
            case "3" -> "closed";
            default -> null; // 不识别就不推
        };
    }

    // ─────────── Bug severity 1-4 → defect severity ───────────
    // 禅道 severity: 1=Blocker 2=Critical 3=Major 4=Minor
    // PLM biz_defect_severity 字典(参 plm-defect SQL): 0=Blocker 1=Critical 2=Major 3=Minor
    public static String bugSeverityToDefect(Integer zSeverity) {
        if (zSeverity == null) return null;
        return switch (zSeverity) {
            case 1 -> "0"; // Blocker
            case 2 -> "1"; // Critical
            case 3 -> "2"; // Major
            case 4 -> "3"; // Minor
            default -> null;
        };
    }

    public static Integer defectSeverityToBug(String plmSeverity) {
        if (plmSeverity == null) return null;
        return switch (plmSeverity) {
            case "0" -> 1;
            case "1" -> 2;
            case "2" -> 3;
            case "3" -> 4;
            default -> null;
        };
    }

    // ─────────── Story stage → Requirement status ───────────
    public static String storyStageToReqStatus(String stage) {
        if (stage == null) return "99";
        return switch (stage) {
            case "wait", "planned" -> "00";       // 待评审
            case "projected", "developing" -> "01"; // 开发中
            case "released", "closed" -> "02";    // 已完成
            default -> "99";
        };
    }

    public static String reqStatusToStoryStage(String plmStatus) {
        if (plmStatus == null) return null;
        return switch (plmStatus) {
            case "00" -> "wait";
            case "01" -> "developing";
            case "02" -> "released";
            case "03" -> "closed"; // 已取消视为 close
            default -> null;
        };
    }

    // ─────────── Task status ───────────
    public static String taskStatusToPlm(String zStatus) {
        if (zStatus == null) return "99";
        return switch (zStatus) {
            case "wait"   -> "0";
            case "doing"  -> "1";
            case "done"   -> "2";
            case "pause"  -> "3";
            case "cancel" -> "4";
            case "closed" -> "5";
            default -> "99";
        };
    }

    public static String taskStatusToZentao(String plmStatus) {
        if (plmStatus == null) return null;
        return switch (plmStatus) {
            case "0" -> "wait";
            case "1" -> "doing";
            case "2" -> "done";
            case "3" -> "pause";
            case "4" -> "cancel";
            case "5" -> "closed";
            default -> null;
        };
    }

    // ─────────── Case status ───────────
    public static String caseStatusToPlm(String zStatus) {
        if (zStatus == null) return "99";
        return switch (zStatus) {
            case "normal"  -> "0";
            case "blocked" -> "1";
            default -> "99";
        };
    }

    public static String caseStatusToZentao(String plmStatus) {
        if (plmStatus == null) return null;
        return switch (plmStatus) {
            case "0" -> "normal";
            case "1" -> "blocked";
            default -> null;
        };
    }

    // ─────────── Bug payload → Defect (字段提取) ───────────

    /** 从禅道 bug payload(扁平 data 对象)提取要写入 tb_defect 的字段映射(不含 reporter/assignee user_id,需要单独通过 user-map 解析) */
    public static Map<String, Object> bugPayloadToDefectFields(JSONObject z) {
        return Map.ofEntries(
            Map.entry("title",            nonNullString(z.getString("title"))),
            Map.entry("description",      nonNullString(stripHtml(z.getString("steps")))),
            Map.entry("severity",         orEmpty(bugSeverityToDefect(z.getInteger("severity")))),
            Map.entry("status",           bugStatusToDefect(z.getString("status"))),
            Map.entry("reproduceSteps",   nonNullString(stripHtml(z.getString("steps"))))
        );
    }

    /** 从禅道 story payload 提取要写入 tb_requirement 的字段 */
    public static Map<String, Object> storyPayloadToReqFields(JSONObject z) {
        return Map.of(
            "title",       nonNullString(z.getString("title")),
            "description", nonNullString(z.getString("spec")),
            "priority",    nonNullString(z.getString("pri")),
            "status",      storyStageToReqStatus(z.getString("stage"))
        );
    }

    /** 从禅道 task payload 提取要写入 tb_task 的字段(key 对齐 DB 列名约定) */
    public static Map<String, Object> taskPayloadToTaskFields(JSONObject z) {
        return Map.of(
            "title",          nonNullString(z.getString("name")),
            "description",    nonNullString(z.getString("desc")),
            "priority",       nonNullString(z.getString("pri")),
            "status",         taskStatusToPlm(z.getString("status")),
            "estimatedHours", orEmpty(z.get("estimate")),
            "actualHours",    orEmpty(z.get("consumed"))
        );
    }

    /** 从禅道 case payload 提取要写入 tb_testcase 的字段 */
    public static Map<String, Object> casePayloadToTestcaseFields(JSONObject z) {
        return Map.of(
            "title",         nonNullString(z.getString("title")),
            "preconditions", nonNullString(z.getString("precondition")),
            "steps",         nonNullString(z.containsKey("steps") ? z.get("steps").toString() : null),
            "expectedResult",nonNullString(z.getString("expect")),
            "priority",      nonNullString(z.getString("pri")),
            "category",      nonNullString(z.getString("type")),
            "status",        caseStatusToPlm(z.getString("status"))
        );
    }

    // ─────────── 工具 ───────────

    private static String nonNullString(String s) { return s == null ? "" : s; }
    private static Object orEmpty(Object v) { return v == null ? "" : v; }

    private static String stripHtml(String s) {
        if (s == null) return "";
        // 简单粗暴的 HTML 标签清理(禅道 steps 是 HTML);保留段落断行
        String cleaned = s.replaceAll("</p>", "\n")
                          .replaceAll("<br[^>]*>", "\n")
                          .replaceAll("<[^>]+>", "")
                          .replaceAll("&nbsp;", " ")
                          .replaceAll("&amp;",  "&")
                          .replaceAll("&lt;",   "<")
                          .replaceAll("&gt;",   ">")
                          .trim();
        // 长度兜底 4000(VARCHAR 上限)
        return cleaned.length() > 4000 ? cleaned.substring(0, 4000) : cleaned;
    }
}
