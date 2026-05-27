package cn.com.bosssfot.dv.plm.integration.ztf;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import cn.com.bosssfot.dv.plm.integration.adapter.ztf.ZtfFieldMapper;

/**
 * 测试 {@link ZtfFieldMapper} 纯静态结果映射(设计 §4)。
 *
 * <p>覆盖范围:
 * <ul>
 *   <li>failed = fail + error + blocked(非通过合并计失败)</li>
 *   <li>pass_rate 服务端重算:total=0 → 0;48/45 → 93.75(DECIMAL(5,2) 四舍五入)</li>
 *   <li>rootCause 去 HTML + 实体解码 + 截断(LONGTEXT 兜底 4000)</li>
 *   <li>缺字段兜底(null → 0 / null)</li>
 * </ul>
 */
class ZtfFieldMapperTest {

    @Test
    @DisplayName("failed 合并 fail+error+blocked")
    void failedMergesFailErrorBlocked() {
        JSONObject z = JSON.parseObject("{\"fail\":2, \"error\":3, \"blocked\":1}");
        // 2 + 3 + 1 = 6
        assertThat(ZtfFieldMapper.failed(z)).isEqualTo(6);
    }

    @Test
    @DisplayName("failed 缺任一子项时只算存在的(缺字段当 0)")
    void failedTreatsMissingAsZero() {
        // 只有 fail,error/blocked 缺失 → 当 0
        assertThat(ZtfFieldMapper.failed(JSON.parseObject("{\"fail\":4}"))).isEqualTo(4);
        // 只有 blocked
        assertThat(ZtfFieldMapper.failed(JSON.parseObject("{\"blocked\":2}"))).isEqualTo(2);
        // 全缺 → 0
        assertThat(ZtfFieldMapper.failed(JSON.parseObject("{}"))).isZero();
    }

    @Test
    @DisplayName("total / passed / duration 缺字段兜底 0")
    void scalarFieldsFallbackToZero() {
        JSONObject empty = JSON.parseObject("{}");
        assertThat(ZtfFieldMapper.total(empty)).isZero();
        assertThat(ZtfFieldMapper.passed(empty)).isZero();
        assertThat(ZtfFieldMapper.durationSec(empty)).isZero();

        JSONObject full = JSON.parseObject("{\"total\":48, \"pass\":45, \"duration\":123}");
        assertThat(ZtfFieldMapper.total(full)).isEqualTo(48);
        assertThat(ZtfFieldMapper.passed(full)).isEqualTo(45);
        assertThat(ZtfFieldMapper.durationSec(full)).isEqualTo(123);
    }

    @Test
    @DisplayName("passRate total=0 → 0.00(避免除零)")
    void passRateTotalZeroReturnsZero() {
        BigDecimal r = ZtfFieldMapper.passRate(JSON.parseObject("{\"total\":0, \"pass\":0}"));
        assertThat(r).isEqualByComparingTo("0");
        // DECIMAL(5,2) 精度:scale=2
        assertThat(r.scale()).isEqualTo(2);

        // 缺 total 字段(兜底 0)同样不除零
        assertThat(ZtfFieldMapper.passRate(JSON.parseObject("{}"))).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("passRate 48/45 → 93.75(服务端重算,四舍五入)")
    void passRateRecomputed() {
        // 45 * 100 / 48 = 93.75
        BigDecimal r = ZtfFieldMapper.passRate(JSON.parseObject("{\"total\":48, \"pass\":45}"));
        assertThat(r).isEqualByComparingTo("93.75");

        // 1/3 → 33.33(HALF_UP)
        BigDecimal third = ZtfFieldMapper.passRate(JSON.parseObject("{\"total\":3, \"pass\":1}"));
        assertThat(third).isEqualByComparingTo("33.33");

        // 全通过 → 100.00
        assertThat(ZtfFieldMapper.passRate(JSON.parseObject("{\"total\":10, \"pass\":10}")))
            .isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("passRate 不信任外部传入(忽略 payload 里的 passRate 字段)")
    void passRateIgnoresExternalValue() {
        // payload 谎报 passRate=100,但真实 5/10 应重算成 50.00
        JSONObject lying = JSON.parseObject("{\"total\":10, \"pass\":5, \"passRate\":100}");
        assertThat(ZtfFieldMapper.passRate(lying)).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("rootCause 去 HTML + 实体解码 + <p>/<br> 转换行")
    void rootCauseStripsHtmlAndDecodesEntities() {
        JSONObject z = JSON.parseObject(
            "{\"failureSummary\":\"<p>断言失败</p><br/>a&nbsp;b&amp;c&lt;d&gt;e\"}");
        String rc = ZtfFieldMapper.rootCause(z);
        assertThat(rc).contains("断言失败");
        assertThat(rc).contains("a b&c<d>e");
        assertThat(rc).doesNotContain("<p>");
        assertThat(rc).doesNotContain("<br");
        assertThat(rc).doesNotContain("&nbsp;");
    }

    @Test
    @DisplayName("rootCause 缺字段/纯标签清空后 → null")
    void rootCauseNullAndBlankFallback() {
        // 缺 failureSummary → null
        assertThat(ZtfFieldMapper.rootCause(JSON.parseObject("{}"))).isNull();
        // 纯 HTML 标签 trim 后为空 → null(而非空串)
        assertThat(ZtfFieldMapper.rootCause(JSON.parseObject("{\"failureSummary\":\"<p></p>\"}"))).isNull();
    }

    @Test
    @DisplayName("rootCause 超 4000 截断")
    void rootCauseTruncatesAt4000() {
        // 构造 5000 个无标签字符
        String big = "x".repeat(5000);
        JSONObject z = new JSONObject();
        z.put("failureSummary", big);
        String rc = ZtfFieldMapper.rootCause(z);
        assertThat(rc).hasSize(4000);
    }
}
