package cn.com.bosssfot.dv.plm.autotest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.autotest.domain.AutoTest;
import cn.com.bosssfot.dv.plm.autotest.mapper.AutoTestMapper;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * AutoTestServiceImpl 单元测试 — PRD §F4.5 + 原型 autotest.html
 *
 * 覆盖范围 (闭合最后一个后端单测缺口 30→31/31;§M.2 DoD):
 *   - generateAutotestNo: AT-YYYY-NNNN / 流水续号 / 撞号重试 / 用户传入不覆盖
 *   - 字段校验: title/projectId/testSuiteType/framework/authorUserId 必填 (602)
 *   - ENUM 白名单: testSuiteType (ui/api/perf/regression) / framework
 *                  (playwright/selenium/jmeter/cypress) → 604
 *   - 关联项目存在性 → 702
 *   - 默认值填充 + 新建状态 00/01 均可、02 拒绝 (601)
 *   - 3 状态机含反向边: 00→01 / 01→02 / 02→01 (已禁用→已激活,非终态) /
 *                       跳级/反向非法 (601)
 *   - passRate 自动计算: total+passed / 取 old 兜底 / HALF_UP 2 位 / total=0 跳过
 *   - aiGenerate: scriptContent 按框架生成骨架 + aiGenerated=Y + AiService.chat 一次
 *   - runAutoTest: 仅 01 可执行 (否则 601) + mock 统计区间 + failed>0 触发 RCA + AiService.chat
 *   - delete/list/get 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class AutoTestServiceImplTest {

    @Mock
    private AutoTestMapper autotestMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private AutoTestServiceImpl service;

    private AutoTest sample;

    @BeforeEach
    void setUp() {
        // 仅填必填项,可选项留 null 以验默认值
        sample = new AutoTest();
        sample.setTitle("灌溉决策平台回归套件");
        sample.setProjectId(10L);
        sample.setTestSuiteType("ui");
        sample.setFramework("playwright");
        sample.setAuthorUserId(1L);
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    /** 断言抛 ServiceException 且 code + 文案均匹配 */
    private void assertBizError(ThrowingCallable call, int expectedCode, String msgPart) {
        Throwable thrown = catchThrowable(call);
        assertThat(thrown).as("应抛 ServiceException").isInstanceOf(ServiceException.class);
        assertThat(((ServiceException) thrown).getCode()).as("错误码").isEqualTo(expectedCode);
        assertThat(thrown.getMessage()).as("错误文案").contains(msgPart);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateAutotestNo (ADR AT-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateAutotestNo (AT-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无套件时,编号为 AT-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(autotestMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(autotestMapper.insertAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAutoTest(sample);
            }
            assertThat(sample.getAutotestNo()).isEqualTo(String.format("AT-%d-0001", LocalDate.now().getYear()));
        }

        @Test
        @DisplayName("当年已有 7 个时,下一个编号为 0008")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(autotestMapper.selectMaxSeqOfYear(anyString())).thenReturn(7);
            when(autotestMapper.insertAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAutoTest(sample);
            }
            assertThat(sample.getAutotestNo()).isEqualTo(String.format("AT-%d-0008", LocalDate.now().getYear()));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功,insert 调两次")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(autotestMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(autotestMapper.insertAutoTest(any()))
                .thenThrow(new DuplicateKeyException("uk_autotest_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertAutoTest(sample);
                assertThat(rows).isEqualTo(1);
                verify(autotestMapper, times(2)).insertAutoTest(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 autotestNo 时不自动生成,不查流水号")
        void userProvidedNoIsKept() {
            sample.setAutotestNo("AT-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(autotestMapper.insertAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAutoTest(sample);
            }
            assertThat(sample.getAutotestNo()).isEqualTo("AT-CUSTOM-2099");
            verify(autotestMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + ENUM 白名单 + FK (insertAutoTest)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 + ENUM 白名单 + FK (insertAutoTest)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertBizError(() -> service.insertAutoTest(sample), 602, "套件名称");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertBizError(() -> service.insertAutoTest(sample), 602, "关联项目不能为空");
        }

        @Test
        @DisplayName("testSuiteType 必填,空抛 602")
        void suiteTypeRequired() {
            sample.setTestSuiteType(null);
            assertBizError(() -> service.insertAutoTest(sample), 602, "套件类型不能为空");
        }

        @Test
        @DisplayName("framework 必填,空抛 602")
        void frameworkRequired() {
            sample.setFramework(null);
            assertBizError(() -> service.insertAutoTest(sample), 602, "测试框架不能为空");
        }

        @Test
        @DisplayName("authorUserId 必填,空抛 602")
        void authorRequired() {
            sample.setAuthorUserId(null);
            assertBizError(() -> service.insertAutoTest(sample), 602, "创建人不能为空");
        }

        @Test
        @DisplayName("testSuiteType 非白名单 → 604 (校验先于项目存在性,不查 project)")
        void suiteTypeOutOfWhitelist() {
            sample.setTestSuiteType("load");
            assertBizError(() -> service.insertAutoTest(sample), 604, "套件类型非法");
            verify(projectMapper, never()).selectProjectById(any());
        }

        @Test
        @DisplayName("framework 非白名单 → 604")
        void frameworkOutOfWhitelist() {
            sample.setFramework("postman");
            assertBizError(() -> service.insertAutoTest(sample), 604, "测试框架非法");
            verify(projectMapper, never()).selectProjectById(any());
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(10L)).thenReturn(null);
            assertBizError(() -> service.insertAutoTest(sample), 702, "关联项目不存在");
        }

        @Test
        @DisplayName("新建套件 status=02 已禁用 → 601 (仅允许 00/01)")
        void newSuiteStatusMustBeDraftOrActive() {
            sample.setStatus("02");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertBizError(() -> service.insertAutoTest(sample), 601, "草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值填充 + 新建状态
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertAutoTest)")
    class DefaultsTests {

        @Test
        @DisplayName("可选项全空时填默认: schedule=N/ai=N/统计 0/passRate=0/status=00")
        void fillsAllDefaults() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(autotestMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(autotestMapper.insertAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertAutoTest(sample);
            }
            assertThat(sample.getScheduleEnabled()).isEqualTo("N");
            assertThat(sample.getAiGenerated()).isEqualTo("N");
            assertThat(sample.getTotalCases()).isZero();
            assertThat(sample.getPassedCases()).isZero();
            assertThat(sample.getFailedCases()).isZero();
            assertThat(sample.getPassRate()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(sample.getExecutionDurationSec()).isZero();
            assertThat(sample.getStatus()).isEqualTo("00");
            assertThat(sample.getCreateBy()).isEqualTo("admin");
        }

        @Test
        @DisplayName("新建套件可直接为 01 已激活 (区别于多数模块仅允许草稿)")
        void statusActiveAllowedAtCreation() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(autotestMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(autotestMapper.insertAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertAutoTest(sample);
                assertThat(rows).isEqualTo(1);
            }
            assertThat(sample.getStatus()).isEqualTo("01");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3 状态机含反向边: 00→01→02→01 (已禁用→已激活,02 非终态)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 3 态含反向边 02→01 (PRD §F4.5)")
    class StateMachineTests {

        private AutoTest withStatus(String s) {
            AutoTest t = new AutoTest();
            t.setAutotestId(99L);
            t.setStatus(s);
            t.setProjectId(10L);
            return t;
        }

        private AutoTest updateTo(String status) {
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setStatus(status);
            return upd;
        }

        @Test
        @DisplayName("00 草稿 → 01 已激活 合法")
        void draftToActive() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("00"));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAutoTest(updateTo("01"))).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 已激活 → 02 已禁用 合法")
        void activeToDisabled() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("01"));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAutoTest(updateTo("02"))).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已禁用 → 01 已激活 反向边合法 (重新启用,02 非终态)")
        void disabledToActiveReverse() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("02"));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAutoTest(updateTo("01"))).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("00 草稿 → 02 已禁用 跳级非法 → 601")
        void draftCannotJumpToDisabled() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("00"));
            assertBizError(() -> service.updateAutoTest(updateTo("02")), 601, "不能直接转");
        }

        @Test
        @DisplayName("01 已激活 → 00 草稿 反向非法 → 601")
        void activeCannotReverseToDraft() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("01"));
            assertBizError(() -> service.updateAutoTest(updateTo("00")), 601, "不能直接转");
        }

        @Test
        @DisplayName("02 已禁用 → 00 草稿 非法 → 601")
        void disabledCannotGoToDraft() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("02"));
            assertBizError(() -> service.updateAutoTest(updateTo("00")), 601, "不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("02"));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            AutoTest upd = updateTo("02");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateAutoTest(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时套件不存在抛 404")
        void updateNotFound() {
            when(autotestMapper.selectAutoTestById(404L)).thenReturn(null);
            AutoTest upd = new AutoTest();
            upd.setAutotestId(404L);
            upd.setStatus("01");
            assertBizError(() -> service.updateAutoTest(upd), 404, "自动化套件不存在");
        }

        @Test
        @DisplayName("update 改 projectId,新项目不存在抛 702")
        void updateProjectIdNotFound() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("00"));
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setProjectId(999L);
            assertBizError(() -> service.updateAutoTest(upd), 702, "关联项目不存在");
        }

        @Test
        @DisplayName("update 改 testSuiteType 非白名单抛 604")
        void updateSuiteTypeOutOfWhitelist() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("00"));
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setTestSuiteType("load");
            assertBizError(() -> service.updateAutoTest(upd), 604, "套件类型非法");
        }

        @Test
        @DisplayName("update 改 framework 非白名单抛 604")
        void updateFrameworkOutOfWhitelist() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(withStatus("00"));
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setFramework("postman");
            assertBizError(() -> service.updateAutoTest(upd), 604, "测试框架非法");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // passRate 自动计算 (updateAutoTest)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("passRate 自动计算 (updateAutoTest)")
    class PassRateTests {

        private AutoTest oldWith(Integer total, Integer passed) {
            AutoTest t = new AutoTest();
            t.setAutotestId(99L);
            t.setStatus("01");
            t.setTotalCases(total);
            t.setPassedCases(passed);
            return t;
        }

        @Test
        @DisplayName("传入 total=50 passed=45 → passRate=90.00")
        void computesFromTotalAndPassed() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(oldWith(null, null));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setTotalCases(50);
            upd.setPassedCases(45);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateAutoTest(upd);
            }
            assertThat(upd.getPassRate()).isEqualByComparingTo("90.00");
        }

        @Test
        @DisplayName("未传 total/passed → 取 old 兜底计算 (old 20/10 → 50.00)")
        void usesOldWhenNotProvided() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(oldWith(20, 10));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setRemark("仅改备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateAutoTest(upd);
            }
            assertThat(upd.getPassRate()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("HALF_UP 2 位小数 (1/3 → 33.33)")
        void roundsHalfUp() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(oldWith(null, null));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setTotalCases(3);
            upd.setPassedCases(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateAutoTest(upd);
            }
            assertThat(upd.getPassRate()).isEqualByComparingTo("33.33");
        }

        @Test
        @DisplayName("total=0 时跳过计算 (passRate 保持不变)")
        void zeroTotalSkipsCalc() {
            when(autotestMapper.selectAutoTestById(99L)).thenReturn(oldWith(0, 0));
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            AutoTest upd = new AutoTest();
            upd.setAutotestId(99L);
            upd.setTotalCases(0);
            upd.setPassedCases(5);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateAutoTest(upd);
            }
            assertThat(upd.getPassRate()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate (按框架生成脚本骨架 + 审计联动)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (AI 生成脚本骨架)")
    class AiGenerateTests {

        @Test
        @DisplayName("playwright → scriptContent 含 @playwright/test + aiGenerated=Y + chat 一次")
        void normalAiGeneratePlaywright() {
            AutoTest t = new AutoTest();
            t.setAutotestId(50L);
            t.setFramework("playwright");
            t.setTargetUrl("http://farm.example.com");
            when(autotestMapper.selectAutoTestById(50L)).thenReturn(t);
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                AutoTest result = service.aiGenerate(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getAiGeneratedAt()).isNotNull();
                assertThat(result.getScriptContent())
                    .isNotBlank()
                    .contains("@playwright/test")
                    .contains("http://farm.example.com");
            }
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
        }

        @Test
        @DisplayName("jmeter → scriptContent 含 JMeter ThreadGroup 骨架")
        void aiGenerateJmeterSkeleton() {
            AutoTest t = new AutoTest();
            t.setAutotestId(51L);
            t.setFramework("jmeter");
            when(autotestMapper.selectAutoTestById(51L)).thenReturn(t);
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                AutoTest result = service.aiGenerate(51L);
                assertThat(result.getScriptContent())
                    .contains("JMeter")
                    .contains("ThreadGroup");
            }
        }

        @Test
        @DisplayName("aiGenerate 时套件不存在抛 404 (不调 AiService)")
        void aiGenerateNotFound() {
            when(autotestMapper.selectAutoTestById(404L)).thenReturn(null);
            assertBizError(() -> service.aiGenerate(404L), 404, "自动化套件不存在");
            verify(aiService, never()).chat(any(AiChatRequest.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // runAutoTest (mock 执行 + 失败根因分析)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("runAutoTest (立即执行 + RCA)")
    class RunTests {

        @Test
        @DisplayName("已激活套件执行 → 统计区间合理 + 必有失败触发 RCA + chat 一次 + 状态不变")
        void normalRun() {
            AutoTest t = new AutoTest();
            t.setAutotestId(70L);
            t.setStatus("01");
            t.setTitle("回归套件");
            t.setFramework("playwright");
            when(autotestMapper.selectAutoTestById(70L)).thenReturn(t);
            when(autotestMapper.updateAutoTest(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                AutoTest result = service.runAutoTest(70L);
                assertThat(result.getTotalCases()).isBetween(30, 50);
                assertThat(result.getPassedCases()).isLessThan(result.getTotalCases());
                assertThat(result.getFailedCases()).isGreaterThanOrEqualTo(1);
                assertThat(result.getPassedCases() + result.getFailedCases())
                    .as("passed + failed = total").isEqualTo(result.getTotalCases());
                assertThat(result.getExecutionDurationSec()).isBetween(60, 300);
                assertThat(result.getPassRate()).isGreaterThan(BigDecimal.ZERO);
                assertThat(result.getLastExecutedAt()).isNotNull();
                assertThat(result.getLastRootCauseAnalysis()).contains("根因");
                assertThat(result.getStatus()).as("执行不改变套件状态").isEqualTo("01");
            }
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
        }

        @Test
        @DisplayName("非「已激活」套件执行 → 601 (草稿不可执行,不调 AiService)")
        void runRequiresActive() {
            AutoTest t = new AutoTest();
            t.setAutotestId(71L);
            t.setStatus("00");
            when(autotestMapper.selectAutoTestById(71L)).thenReturn(t);
            assertBizError(() -> service.runAutoTest(71L), 601, "已激活");
            verify(aiService, never()).chat(any(AiChatRequest.class));
        }

        @Test
        @DisplayName("runAutoTest 时套件不存在抛 404")
        void runNotFound() {
            when(autotestMapper.selectAutoTestById(404L)).thenReturn(null);
            assertBizError(() -> service.runAutoTest(404L), 404, "自动化套件不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // delete / list / get 转发
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete / list / get 转发 mapper")
    class PassthroughTests {

        @Test
        @DisplayName("批量删除转发到 mapper")
        void deleteByIds() {
            when(autotestMapper.deleteAutoTestByIds(any())).thenReturn(2);
            assertThat(service.deleteAutoTestByIds(new Long[] { 1L, 2L })).isEqualTo(2);
        }

        @Test
        @DisplayName("列表查询转发到 mapper")
        void selectList() {
            when(autotestMapper.selectAutoTestList(any())).thenReturn(List.of(sample));
            assertThat(service.selectAutoTestList(new AutoTest())).hasSize(1);
        }

        @Test
        @DisplayName("按 id 查询转发到 mapper")
        void selectById() {
            when(autotestMapper.selectAutoTestById(7L)).thenReturn(sample);
            assertThat(service.selectAutoTestById(7L)).isSameAs(sample);
        }
    }
}
