package cn.com.bosssfot.dv.plm.testcase.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;
import cn.com.bosssfot.dv.plm.testcase.mapper.TestCaseMapper;

/**
 * TestCaseServiceImpl 单元测试
 *
 * 覆盖范围（Phase 04 Gate B.0 强制项）：
 *   - ADR-0006 generateTestCaseNo: TC-YYYY-NNNN 格式 / 序号递增 / 撞号重试
 *   - PRD §2.3 状态机: 5×5 矩阵关键转换 / 反向边 03/04→01（重测）
 *   - is_automated='Y' 必填 automation_script_path → 706
 *   - 字段校验: title / steps / expectedResult 必填 → 602
 *   - FK 校验: project 必填 / requirement 可空 → 702
 *   - execute 端点: 仅 status=02 时可执行 / execution_count 递增
 */
@ExtendWith(MockitoExtension.class)
class TestCaseServiceImplTest {

    @Mock
    private TestCaseMapper testcaseMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private RequirementMapper requirementMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TestCaseServiceImpl service;

    private TestCase sample;

    @BeforeEach
    void setUp() {
        sample = new TestCase();
        sample.setTitle("验证登录功能");
        sample.setSteps("1. 打开登录页\n2. 输入账号密码\n3. 点击登录");
        sample.setExpectedResult("登录成功，跳转到首页");
        sample.setProjectId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateTestCaseNo (ADR-0006)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateTestCaseNo (ADR-0006)")
    class GenerateTestCaseNoTests {

        @Test
        @DisplayName("当年无用例时，编号为 TC-YYYY-0001")
        void firstTestCaseOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testcaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testcaseMapper.insertTestCase(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestCase(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTestcaseNo()).isEqualTo(String.format("TC-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 7 个用例时，下一个编号为 0008")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testcaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(7);
            when(testcaseMapper.insertTestCase(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestCase(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTestcaseNo()).isEqualTo(String.format("TC-%d-0008", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testcaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(testcaseMapper.insertTestCase(any()))
                .thenThrow(new DuplicateKeyException("uk_testcase_no"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertTestCase(sample);
                assertThat(rows).isEqualTo(1);
                verify(testcaseMapper, times(2)).insertTestCase(any());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 (API §2)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §2)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填，空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("用例标题不能为空");
        }

        @Test
        @DisplayName("steps 必填，空抛 602")
        void stepsRequired() {
            sample.setSteps(null);
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试步骤不能为空");
        }

        @Test
        @DisplayName("expectedResult 必填，空抛 602")
        void expectedResultRequired() {
            sample.setExpectedResult(null);
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("期望结果不能为空");
        }

        @Test
        @DisplayName("projectId 必填，null 抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不能为空");
        }

        @Test
        @DisplayName("projectId FK 不存在抛 702")
        void projectFkNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("可选 requirementId FK 不存在抛 702")
        void requirementFkNotFound() {
            sample.setRequirementId(99L);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(requirementMapper.selectRequirementById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联需求不存在");
        }

        @Test
        @DisplayName("is_automated='Y' 但 script_path 为空时抛 706")
        void automatedRequiresScriptPath() {
            sample.setIsAutomated("Y");
            sample.setAutomationScriptPath(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("脚本路径");
        }

        @Test
        @DisplayName("is_automated='Y' 且有 script_path 正常通过")
        void automatedWithScriptPath() {
            sample.setIsAutomated("Y");
            sample.setAutomationScriptPath("tests/login_test.py");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testcaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testcaseMapper.insertTestCase(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertTestCase(sample);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("新建状态非 00 抛 601")
        void initialStatusMustBe00() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertTestCase(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (PRD §2.3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机转换 (PRD §2.3)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01（草稿→待执行）")
        void legal_00_to_01() {
            TestCase old = existingTestCase("00");
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);
            when(testcaseMapper.updateTestCase(any())).thenReturn(1);

            TestCase upd = updateTestCase(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestCase(upd);
            }
            verify(testcaseMapper).updateTestCase(any());
        }

        @Test
        @DisplayName("合法转换 01→02（待执行→执行中）")
        void legal_01_to_02() {
            TestCase old = existingTestCase("01");
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);
            when(testcaseMapper.updateTestCase(any())).thenReturn(1);

            TestCase upd = updateTestCase(1L, "02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestCase(upd);
            }
            verify(testcaseMapper).updateTestCase(any());
        }

        @Test
        @DisplayName("反向边 03→01（已通过→待执行 重测）")
        void legal_03_to_01_retest() {
            TestCase old = existingTestCase("03");
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);
            when(testcaseMapper.updateTestCase(any())).thenReturn(1);

            TestCase upd = updateTestCase(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestCase(upd);
            }
            verify(testcaseMapper).updateTestCase(any());
        }

        @Test
        @DisplayName("反向边 04→01（已失败→待执行 重测）")
        void legal_04_to_01_retest() {
            TestCase old = existingTestCase("04");
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);
            when(testcaseMapper.updateTestCase(any())).thenReturn(1);

            TestCase upd = updateTestCase(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestCase(upd);
            }
            verify(testcaseMapper).updateTestCase(any());
        }

        @Test
        @DisplayName("非法跳级 00→02 抛 601")
        void illegal_00_to_02() {
            TestCase old = existingTestCase("00");
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);

            TestCase upd = updateTestCase(1L, "02");
            assertThatThrownBy(() -> service.updateTestCase(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿")
                .hasMessageContaining("执行中");
            verify(testcaseMapper, never()).updateTestCase(any());
        }

        @Test
        @DisplayName("用例不存在抛 404")
        void notFound() {
            when(testcaseMapper.selectTestCaseById(99L)).thenReturn(null);
            TestCase upd = updateTestCase(99L, "01");
            assertThatThrownBy(() -> service.updateTestCase(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("用例不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // execute 端点 (PRD §2.3)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("execute 端点 (PRD §2.3)")
    class ExecuteTests {

        @Test
        @DisplayName("status 非 02 时调用 execute 抛 601")
        void executeRequiresStatusIs02() {
            TestCase old = existingTestCase("01");
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.executeTestCase(1L, "03", "通过"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("执行中");
        }

        @Test
        @DisplayName("execute newStatus 只能传 03 或 04，否则抛 604")
        void executeInvalidNewStatus() {
            TestCase old = existingTestCase("02");
            old.setExecutionCount(0);
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.executeTestCase(1L, "01", ""))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("03");
        }

        @Test
        @DisplayName("execute 成功时 execution_count 递增 +1")
        void executeIncrementsCount() {
            TestCase old = existingTestCase("02");
            old.setExecutionCount(2);
            when(testcaseMapper.selectTestCaseById(1L)).thenReturn(old);
            when(testcaseMapper.updateTestCase(any())).thenAnswer(invocation -> {
                TestCase arg = invocation.getArgument(0);
                assertThat(arg.getExecutionCount()).isEqualTo(3);
                return 1;
            });

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.executeTestCase(1L, "03", "页面渲染正常");
            }
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

    private TestCase existingTestCase(String status) {
        TestCase t = new TestCase();
        t.setTestcaseId(1L);
        t.setTitle("旧用例");
        t.setStatus(status);
        t.setProjectId(1L);
        t.setExecutionCount(0);
        return t;
    }

    private TestCase updateTestCase(Long id, String newStatus) {
        TestCase t = new TestCase();
        t.setTestcaseId(id);
        t.setStatus(newStatus);
        return t;
    }
}
