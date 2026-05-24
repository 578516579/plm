package cn.com.bosssfot.dv.plm.testreport.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

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
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
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
