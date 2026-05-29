package cn.com.bosssfot.dv.plm.submission.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import cn.com.bosssfot.dv.plm.submission.domain.Submission;
import cn.com.bosssfot.dv.plm.submission.mapper.SubmissionMapper;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;
import cn.com.bosssfot.dv.plm.testplan.mapper.TestPlanMapper;

/**
 * SubmissionServiceImpl 单元测试
 *
 * 覆盖范围:
 *   - ADR: generateSubmissionNo SUB-YYYY-NNNN
 *   - PRD §F4.4: AI 质量门禁 4 项全 Y → qualityGatePassed='Y'
 *   - 5×5 状态机含反向边 04→00
 *   - 进入 03 (已通过) 必须门禁通过 → 708
 *   - 进入 04 (已退回) 必须有 rejectReason → 602
 *   - 00→01 自动填 submittedAt
 *   - FK: projectId 不存在 → 702
 */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock
    private SubmissionMapper submissionMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TestPlanMapper testPlanMapper;

    @InjectMocks
    private SubmissionServiceImpl service;

    private Submission sample;

    @BeforeEach
    void setUp() {
        sample = new Submission();
        sample.setTitle("v2.3.0 提测单");
        sample.setProjectId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateSubmissionNo
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateSubmissionNo (SUB-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无提测单时，编号为 SUB-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSubmission(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getSubmissionNo()).isEqualTo(String.format("SUB-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 7 个提测单，下一个为 0008")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(7);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSubmission(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getSubmissionNo()).isEqualTo(String.format("SUB-%d-0008", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后成功")
        void duplicateKeyRetry() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString()))
                .thenReturn(null).thenReturn(1);
            when(submissionMapper.insertSubmission(any()))
                .thenThrow(new DuplicateKeyException("dup"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSubmission(sample);
            }

            verify(submissionMapper, Mockito.times(2)).insertSubmission(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // AI 质量门禁 computeQualityGate (PRD §F4.4)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("AI 质量门禁 (PRD §F4.4)")
    class QualityGateTests {

        @Test
        @DisplayName("4 项全 Y + 覆盖率 ≥60% → qualityGatePassed='Y'")
        void allPassedGateY() {
            fillGateFields(sample, "75.00", "Y", "Y", "Y");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSubmission(sample);
            }

            assertThat(sample.getQualityGatePassed()).isEqualTo("Y");
        }

        @Test
        @DisplayName("覆盖率 <60% → qualityGatePassed='N'")
        void lowCoverageGateN() {
            fillGateFields(sample, "55.00", "Y", "Y", "Y");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSubmission(sample);
            }

            assertThat(sample.getQualityGatePassed()).isEqualTo("N");
        }

        @Test
        @DisplayName("代码扫描未通过 → qualityGatePassed='N'")
        void scanFailedGateN() {
            fillGateFields(sample, "80.00", "N", "Y", "Y");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSubmission(sample);
            }

            assertThat(sample.getQualityGatePassed()).isEqualTo("N");
        }

        @Test
        @DisplayName("4 项均未设 → qualityGatePassed='N'")
        void allNullGateN() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertSubmission(sample);
            }

            assertThat(sample.getQualityGatePassed()).isEqualTo("N");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (5×5 含反向边 04→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (5×5 含反向边)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 自动填 submittedAt")
        void legal_00_to_01_fillsSubmittedAt() {
            Submission old = existingSub("00");
            old.setSubmittedAt(null);
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(old);
            when(submissionMapper.updateSubmission(any())).thenReturn(1);

            Submission upd = updateSub(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateSubmission(upd);
            }

            assertThat(upd.getSubmittedAt()).isNotNull();
        }

        @Test
        @DisplayName("进入 03 (已通过) 门禁 N → 708")
        void enter03GateNotPassed() {
            Submission old = existingSub("02");
            old.setQualityGatePassed("N");
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(old);

            Submission upd = updateSub(1L, "03");
            assertThatThrownBy(() -> service.updateSubmission(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("质量门禁");
            verify(submissionMapper, never()).updateSubmission(any());
        }

        @Test
        @DisplayName("进入 03 (已通过) 门禁 Y → 成功并填 approvedAt")
        void enter03GatePassed() {
            Submission old = existingSub("02");
            old.setQualityGatePassed("Y");
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(old);
            when(submissionMapper.updateSubmission(any())).thenReturn(1);

            Submission upd = updateSub(1L, "03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateSubmission(upd);
            }

            assertThat(upd.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("进入 04 (已退回) 无 rejectReason → 602")
        void enter04NoReason() {
            Submission old = existingSub("02");
            old.setRejectReason(null);
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(old);

            Submission upd = updateSub(1L, "04");
            assertThatThrownBy(() -> service.updateSubmission(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("退回原因");
        }

        @Test
        @DisplayName("反向边 04→00 (打回到草稿) 合法")
        void reverse_04_to_00() {
            Submission old = existingSub("04");
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(old);
            when(submissionMapper.updateSubmission(any())).thenReturn(1);

            Submission upd = updateSub(1L, "00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateSubmission(upd);
            }
            verify(submissionMapper).updateSubmission(any());
        }

        @Test
        @DisplayName("终态 03→01 → 601")
        void terminal_03_immutable() {
            Submission old = existingSub("03");
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateSubmission(updateSub(1L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已通过");
        }

        @Test
        @DisplayName("非法跳级 00→03 → 601")
        void illegal_00_to_03() {
            Submission old = existingSub("00");
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateSubmission(updateSub(1L, "03")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("提测单不存在 → 404")
        void notFound() {
            when(submissionMapper.selectSubmissionById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateSubmission(updateSub(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("提测单不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertSubmission — 字段校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertSubmission — 字段校验")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertSubmission(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("标题");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertSubmission(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态非 00 → 601")
        void initialStatusNotDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertSubmission(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 跨模块 FK testplanId — Proposal 0028 P0-1 (同 projectId 强约束)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("跨模块 FK testplanId (Proposal 0028 P0-1)")
    class TestplanFkTests {

        @Test
        @DisplayName("testFkOk_当目标存在且同 projectId 时插入成功")
        void testFkOk() {
            sample.setTestplanId(100L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testPlanMapper.selectTestPlanById(100L)).thenReturn(testPlanWithProject(100L, 1L));
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertSubmission(sample);
                assertThat(rows).isEqualTo(1);
            }
            assertThat(sample.getTestplanId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("testFkNullOk_当 testplanId 为 null 时跳过校验")
        void testFkNullOk() {
            sample.setTestplanId(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(submissionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(submissionMapper.insertSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertSubmission(sample);
                assertThat(rows).isEqualTo(1);
            }
            verify(testPlanMapper, never()).selectTestPlanById(any());
        }

        @Test
        @DisplayName("testFkNotFound_当目标 TestPlan 不存在 → 702")
        void testFkNotFound() {
            sample.setTestplanId(999L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testPlanMapper.selectTestPlanById(999L)).thenReturn(null);

            assertThatThrownBy(() -> service.insertSubmission(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试方案不存在");
        }

        @Test
        @DisplayName("testFkDifferentProject_当目标 projectId 不同 → 702")
        void testFkDifferentProject() {
            sample.setTestplanId(100L);  // submission.projectId = 1L
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testPlanMapper.selectTestPlanById(100L)).thenReturn(testPlanWithProject(100L, 2L)); // 不同项目

            assertThatThrownBy(() -> service.insertSubmission(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("同一项目");
        }

        private TestPlan testPlanWithProject(Long testplanId, Long projectId) {
            TestPlan tp = new TestPlan();
            tp.setTestplanId(testplanId);
            tp.setProjectId(projectId);
            return tp;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // attachTestplan (Proposal 0028 P0-2 研发 → 测试主线贯通)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("attachTestplan (Proposal 0028 P0-2)")
    class AttachTestplan {

        @Test
        @DisplayName("testAttachOk_合法 testplanId 同 projectId 时写入成功")
        void testAttachOk() {
            // submission 已存在(projectId=1)
            Submission existing = existingSub("01");
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(existing);
            // 目标 testplan(projectId=1,同项目)
            TestPlan tp = new TestPlan();
            tp.setTestplanId(100L);
            tp.setProjectId(1L);
            when(testPlanMapper.selectTestPlanById(100L)).thenReturn(tp);
            when(submissionMapper.updateSubmission(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.attachTestplan(1L, 100L);
            }
            // 校验真正落到 update
            verify(submissionMapper).updateSubmission(any(Submission.class));
        }

        @Test
        @DisplayName("testAttachSubmissionNotFound_提测单不存在 → 404")
        void testAttachSubmissionNotFound() {
            when(submissionMapper.selectSubmissionById(404L)).thenReturn(null);

            assertThatThrownBy(() -> service.attachTestplan(404L, 100L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("提测单不存在");
            verify(submissionMapper, never()).updateSubmission(any());
        }

        @Test
        @DisplayName("testAttachFkViolation_testplan 不属同项目 → 702 (复用 P0-1 校验)")
        void testAttachFkViolation() {
            // submission.projectId=1,testplan.projectId=2 → 跨项目
            Submission existing = existingSub("01");
            // updateSubmission 内部:第一次 selectById 取 old(projectId=1)
            when(submissionMapper.selectSubmissionById(1L)).thenReturn(existing);
            TestPlan tp = new TestPlan();
            tp.setTestplanId(100L);
            tp.setProjectId(2L);
            when(testPlanMapper.selectTestPlanById(100L)).thenReturn(tp);

            assertThatThrownBy(() -> service.attachTestplan(1L, 100L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("同一项目");
            verify(submissionMapper, never()).updateSubmission(any());
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

    private Submission existingSub(String status) {
        Submission s = new Submission();
        s.setSubmissionId(1L);
        s.setTitle("旧提测单");
        s.setStatus(status);
        s.setProjectId(1L);
        return s;
    }

    private Submission updateSub(Long id, String newStatus) {
        Submission s = new Submission();
        s.setSubmissionId(id);
        s.setStatus(newStatus);
        return s;
    }

    private void fillGateFields(Submission s, String coverage, String scan, String prd, String api) {
        s.setUnitTestCoverage(new BigDecimal(coverage));
        s.setCodeScanPassed(scan);
        s.setPrdCompleted(prd);
        s.setApiDocUpdated(api);
    }
}
