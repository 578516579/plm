package cn.com.bosssfot.dv.plm.integration.adapter.ztf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.alibaba.fastjson2.JSONObject;

/**
 * ZTF run 结果字段 ↔ tb_autotest 结果列映射工具(纯静态,无状态)。
 *
 * <p>单向:只把 ZTF run-complete payload 映射成 tb_autotest 的执行结果字段
 *    (total_cases / passed_cases / failed_cases / pass_rate / execution_duration_sec /
 *     last_root_cause_analysis)。<b>不碰 status 生命周期</b>(00/01/02 是 PLM 管理态)。
 *
 * <p>关键规则(设计 §4):
 * <ul>
 *   <li>failed = fail + error + blocked(非通过合并计失败)</li>
 *   <li>pass_rate = total>0 ? passed*100/total : 0,<b>服务端重算</b>,不信任外部传入</li>
 *   <li>failureSummary 去 HTML + 截断(写 LONGTEXT,兜底 4000)</li>
 * </ul>
 */
public final class ZtfFieldMapper {

    private ZtfFieldMapper() {}

    /** 用例总数 */
    public static int total(JSONObject z) {
        return nz(z.getInteger("total"));
    }

    /** 通过数 */
    public static int passed(JSONObject z) {
        return nz(z.getInteger("pass"));
    }

    /** 失败数 = fail + error + blocked(非通过合并) */
    public static int failed(JSONObject z) {
        return nz(z.getInteger("fail")) + nz(z.getInteger("error")) + nz(z.getInteger("blocked"));
    }

    /**
     * 通过率(服务端重算,不信任外部)。
     *
     * <p>{@code total>0 ? passed*100/total : 0},DECIMAL(5,2) 精度,四舍五入。
     */
    public static BigDecimal passRate(JSONObject z) {
        int total = total(z);
        int passed = passed(z);
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(passed)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /** 执行时长(秒) */
    public static int durationSec(JSONObject z) {
        return nz(z.getInteger("duration"));
    }

    /** 失败根因摘要:去 HTML + 截断(LONGTEXT,兜底 4000)。可空 */
    public static String rootCause(JSONObject z) {
        return stripHtml(z.getString("failureSummary"));
    }

    // ─────────── 工具 ───────────

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private static String stripHtml(String s) {
        if (s == null) return null;
        String cleaned = s.replaceAll("</p>", "\n")
                          .replaceAll("<br[^>]*>", "\n")
                          .replaceAll("<[^>]+>", "")
                          .replaceAll("&nbsp;", " ")
                          .replaceAll("&amp;",  "&")
                          .replaceAll("&lt;",   "<")
                          .replaceAll("&gt;",   ">")
                          .trim();
        if (cleaned.isEmpty()) return null;
        return cleaned.length() > 4000 ? cleaned.substring(0, 4000) : cleaned;
    }
}
