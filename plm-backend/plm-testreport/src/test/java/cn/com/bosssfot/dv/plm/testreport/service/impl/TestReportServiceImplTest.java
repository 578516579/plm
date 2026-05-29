package cn.com.bosssfot.dv.plm.testreport.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.defect.domain.Defect;
import cn.com.bosssfot.dv.plm.defect.mapper.DefectMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;
import cn.com.bosssfot.dv.plm.testcase.mapper.TestCaseMapper;
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;
import cn.com.bosssfot.dv.plm.testreport.mapper.TestReportMapper;

/**
 * TestReportServiceImpl 单元测试
 *
 * 覆盖范围:
 *   - ADR: generateTestreportNo TR-YYYY-NNNN
 *   - riskLevel 字典白名单: green/yellow/red → 604
 *   - 3 状态机: 00→{01} / 01→{00,02} / 02→{} (终态)
 *   - 反向边 01→00 (退回审核)
 *   - aiGenerated='Y' 时自动填 generatedAt
 *   - FK: projectId 不存在 → 702
 */
@ExtendWith(MockitoExtension.class)
class TestReportServiceImplTest {

    @Mock
    private TestReportMapper testreportMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TestCaseMapper testcaseMapper;

    @Mock
    private DefectMapper defectMapper;

    @InjectMocks
    private TestReportServiceImpl service;

    private TestReport sample;

    @BeforeEach
    void setUp() {
        sample = new TestReport();
        sample.setTitle("v2.3.0 测试报告");
        sample.setProjectId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateTestreportNo
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateTestreportNo (TR-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无报告，编号为 TR-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testreportMapper.insertTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTestreportNo()).isEqualTo(String.format("TR-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 9 个报告，下一个为 0010")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString())).thenReturn(9);
            when(testreportMapper.insertTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTestreportNo()).isEqualTo(String.format("TR-%d-0010", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后成功")
        void duplicateKeyRetry() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString()))
                .thenReturn(null).thenReturn(1);
            when(testreportMapper.insertTestReport(any()))
                .thenThrow(new DuplicateKeyException("dup"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            verify(testreportMapper, Mockito.times(2)).insertTestReport(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // riskLevel 字典白名单
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("riskLevel 字典校验")
    class RiskLevelTests {

        @Test
        @DisplayName("riskLevel=green 合法")
        void riskLevelGreen() {
            sample.setRiskLevel("green");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testreportMapper.insertTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            assertThat(sample.getRiskLevel()).isEqualTo("green");
        }

        @Test
        @DisplayName("riskLevel=red 合法")
        void riskLevelRed() {
            sample.setRiskLevel("red");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testreportMapper.insertTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            assertThat(sample.getRiskLevel()).isEqualTo("red");
        }

        @Test
        @DisplayName("riskLevel=critical 非法 → 604")
        void riskLevelInvalid() {
            sample.setRiskLevel("critical");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertTestReport(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("风险级别");
        }

        @Test
        @DisplayName("默认 riskLevel=green 被填充")
        void defaultRiskLevelGreen() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testreportMapper.insertTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            assertThat(sample.getRiskLevel()).isEqualTo("green");
        }

        @Test
        @DisplayName("update 时传入非法 riskLevel → 604")
        void updateInvalidRiskLevel() {
            TestReport old = existingTestReport("00");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(old);

            TestReport upd = new TestReport();
            upd.setTestreportId(1L);
            upd.setRiskLevel("orange");
            assertThatThrownBy(() -> service.updateTestReport(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("风险级别");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // AI 生成自动填 generatedAt
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerated='Y' 自动填 generatedAt")
    class AiGeneratedTests {

        @Test
        @DisplayName("aiGenerated='Y' 且 generatedAt 为 null → 自动填充")
        void aiGeneratedFillsTimestamp() {
            sample.setAiGenerated("Y");
            sample.setGeneratedAt(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testreportMapper.insertTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            assertThat(sample.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("aiGenerated='N' → generatedAt 不填充")
        void notAiGeneratedNoTimestamp() {
            sample.setAiGenerated("N");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testreportMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testreportMapper.insertTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestReport(sample);
            }

            assertThat(sample.getGeneratedAt()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (3 状态含反向边 01→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (3 状态含反向边)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 成功")
        void legal_00_to_01() {
            TestReport old = existingTestReport("00");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(old);
            when(testreportMapper.updateTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestReport(updateTestReport(1L, "01"));
            }
            verify(testreportMapper).updateTestReport(any());
        }

        @Test
        @DisplayName("反向边 01→00 (退回) 合法")
        void reverse_01_to_00() {
            TestReport old = existingTestReport("01");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(old);
            when(testreportMapper.updateTestReport(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestReport(updateTestReport(1L, "00"));
            }
            verify(testreportMapper).updateTestReport(any());
        }

        @Test
        @DisplayName("终态 02→任意 → 601（已发布不可逆）")
        void terminal_02_immutable() {
            TestReport old = existingTestReport("02");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01"}) {
                assertThatThrownBy(() -> service.updateTestReport(updateTestReport(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已发布");
            }
        }

        @Test
        @DisplayName("非法跳级 00→02 → 601")
        void illegal_00_to_02() {
            TestReport old = existingTestReport("00");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateTestReport(updateTestReport(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("测试报告不存在 → 404")
        void notFound() {
            when(testreportMapper.selectTestReportById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateTestReport(updateTestReport(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试报告不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertTestReport — 必填校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertTestReport — 必填校验")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertTestReport(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("标题");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertTestReport(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态非 00 → 601")
        void initialStatusNotDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertTestReport(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Proposal 0028 P0-3A — aggregateFromTestplan 真聚合
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aggregateFromTestplan (P0028 P0-3A)")
    class AggregationTests {

        @Test
        @DisplayName("正常计算 totalCases/passed/failed/p0/p1/coverage")
        void testAggregateOk() {
            TestReport report = existingTestReport("00");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(report);
            // 4 testcase: 2 已通过 03, 1 已失败 04, 1 草稿 00 → total=4 / passed=2 / failed=1 / coverage=75.00
            List<TestCase> cases = Arrays.asList(
                tc("03"), tc("03"), tc("04"), tc("00")
            );
            when(testcaseMapper.selectTestCaseList(any(TestCase.class))).thenReturn(cases);
            // 3 defect: 1 P0(00), 2 P1(01)
            List<Defect> defects = Arrays.asList(
                def("00"), def("01"), def("01")
            );
            when(defectMapper.selectDefectList(any(Defect.class))).thenReturn(defects);
            when(testreportMapper.updateTestReport(any())).thenReturn(1);

            TestReport result;
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                result = service.aggregateFromTestplan(1L);
            }

            assertThat(result.getTotalCases()).isEqualTo(4);
            assertThat(result.getPassedCases()).isEqualTo(2);
            assertThat(result.getFailedCases()).isEqualTo(1);
            assertThat(result.getCoverageRate()).isEqualByComparingTo(new BigDecimal("75.00"));
            assertThat(result.getP0Defects()).isEqualTo(1);
            assertThat(result.getP1Defects()).isEqualTo(2);
            assertThat(result.getIsAggregated()).isEqualTo("Y");
            verify(testreportMapper).updateTestReport(any());
        }

        @Test
        @DisplayName("无 testcase 时 coverage=0.00 不挂")
        void testAggregateZeroCases() {
            TestReport report = existingTestReport("00");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(report);
            when(testcaseMapper.selectTestCaseList(any(TestCase.class))).thenReturn(Collections.emptyList());
            when(defectMapper.selectDefectList(any(Defect.class))).thenReturn(Collections.emptyList());
            when(testreportMapper.updateTestReport(any())).thenReturn(1);

            TestReport result;
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                result = service.aggregateFromTestplan(1L);
            }

            assertThat(result.getTotalCases()).isZero();
            assertThat(result.getPassedCases()).isZero();
            assertThat(result.getFailedCases()).isZero();
            assertThat(result.getCoverageRate()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getP0Defects()).isZero();
            assertThat(result.getP1Defects()).isZero();
        }

        @Test
        @DisplayName("isManualOverride='Y' 跳过聚合,不写库")
        void testAggregateManualOverride() {
            TestReport report = existingTestReport("00");
            report.setIsManualOverride("Y");
            report.setTotalCases(99);  // 保留人工录入值
            report.setPassedCases(50);
            when(testreportMapper.selectTestReportById(1L)).thenReturn(report);

            TestReport result = service.aggregateFromTestplan(1L);

            // 不应写库
            verify(testreportMapper, never()).updateTestReport(any());
            // 不应查 testcase / defect
            verify(testcaseMapper, never()).selectTestCaseList(any());
            verify(defectMapper, never()).selectDefectList(any());
            // 原值保留
            assertThat(result.getTotalCases()).isEqualTo(99);
            assertThat(result.getPassedCases()).isEqualTo(50);
        }

        @Test
        @DisplayName("report 不存在 → 702")
        void testAggregateReportNotFound() {
            when(testreportMapper.selectTestReportById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.aggregateFromTestplan(99L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试报告不存在");
        }

        @Test
        @DisplayName("aggregatedAt 被设为 now()")
        void testAggregateSetsTimestamp() {
            TestReport report = existingTestReport("00");
            when(testreportMapper.selectTestReportById(1L)).thenReturn(report);
            when(testcaseMapper.selectTestCaseList(any(TestCase.class))).thenReturn(Collections.emptyList());
            when(defectMapper.selectDefectList(any(Defect.class))).thenReturn(Collections.emptyList());
            when(testreportMapper.updateTestReport(any())).thenReturn(1);

            long beforeMs = System.currentTimeMillis();
            TestReport result;
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                result = service.aggregateFromTestplan(1L);
            }
            long afterMs = System.currentTimeMillis();

            assertThat(result.getAggregatedAt()).isNotNull();
            long ts = result.getAggregatedAt().getTime();
            assertThat(ts).isBetween(beforeMs, afterMs);
        }

        private TestCase tc(String status) {
            TestCase c = new TestCase();
            c.setStatus(status);
            return c;
        }

        private Defect def(String severity) {
            Defect d = new Defect();
            d.setSeverity(severity);
            return d;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 辅助方法
    // ─────────────────────────────────────────────────────────────────────

    private Project existingProject() {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("测试项目");
        return p;
    }

    private TestReport existingTestReport(String status) {
        TestReport r = new TestReport();
        r.setTestreportId(1L);
        r.setTitle("旧报告");
        r.setStatus(status);
        r.setProjectId(1L);
        return r;
    }

    private TestReport updateTestReport(Long id, String newStatus) {
        TestReport r = new TestReport();
        r.setTestreportId(id);
        r.setStatus(newStatus);
        return r;
    }
}
