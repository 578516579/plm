package cn.com.bosssfot.dv.plm.testplan.service.impl;

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

import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;
import cn.com.bosssfot.dv.plm.testplan.mapper.TestPlanMapper;

/**
 * TestPlanServiceImpl 单元测试
 *
 * 覆盖范围:
 *   - ADR: generateTestplanNo TP-YYYY-NNNN
 *   - 必填: title / testTypes / authorUserId / projectId
 *   - 4 状态机: 00→{01} / 01→{00,02} / 02→{03} / 03→{} (终态)
 *   - 反向边 01→00 (取消确认)
 *   - FK: projectId 不存在 → 702
 */
@ExtendWith(MockitoExtension.class)
class TestPlanServiceImplTest {

    @Mock
    private TestPlanMapper testplanMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private AiService aiService;

    @InjectMocks
    private TestPlanServiceImpl service;

    private TestPlan sample;

    @BeforeEach
    void setUp() {
        sample = new TestPlan();
        sample.setTitle("v2.3 集成测试方案");
        sample.setProjectId(1L);
        sample.setTestTypes("integration,regression");
        sample.setAuthorUserId(10L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateTestplanNo
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateTestplanNo (TP-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无测试方案，编号为 TP-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testplanMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testplanMapper.insertTestPlan(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestPlan(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTestplanNo()).isEqualTo(String.format("TP-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 12 个测试方案，下一个为 0013")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testplanMapper.selectMaxSeqOfYear(anyString())).thenReturn(12);
            when(testplanMapper.insertTestPlan(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestPlan(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getTestplanNo()).isEqualTo(String.format("TP-%d-0013", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后成功")
        void duplicateKeyRetry() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testplanMapper.selectMaxSeqOfYear(anyString()))
                .thenReturn(null).thenReturn(1);
            when(testplanMapper.insertTestPlan(any()))
                .thenThrow(new DuplicateKeyException("dup"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestPlan(sample);
            }

            verify(testplanMapper, Mockito.times(2)).insertTestPlan(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertTestPlan — 必填校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertTestPlan — 必填校验")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle("");
            assertThatThrownBy(() -> service.insertTestPlan(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("标题");
        }

        @Test
        @DisplayName("testTypes 为空 → 602")
        void testTypesBlank() {
            sample.setTestTypes(null);
            assertThatThrownBy(() -> service.insertTestPlan(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试类型");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertTestPlan(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("撰写人");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertTestPlan(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态非 00 → 601")
        void initialStatusNotDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertTestPlan(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("默认 testCycleDays=10 被填充")
        void defaultTestCycleDays() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testplanMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testplanMapper.insertTestPlan(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestPlan(sample);
            }

            assertThat(sample.getTestCycleDays()).isEqualTo(10);
        }

        @Test
        @DisplayName("默认 aiGenerated='N' 被填充")
        void defaultAiGeneratedN() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(testplanMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testplanMapper.insertTestPlan(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestPlan(sample);
            }

            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (4 状态含反向边 01→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (4 状态含反向边)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 成功")
        void legal_00_to_01() {
            TestPlan old = existingTestPlan("00");
            when(testplanMapper.selectTestPlanById(1L)).thenReturn(old);
            when(testplanMapper.updateTestPlan(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestPlan(updateTestPlan(1L, "01"));
            }
            verify(testplanMapper).updateTestPlan(any());
        }

        @Test
        @DisplayName("反向边 01→00 (取消确认) 合法")
        void reverse_01_to_00() {
            TestPlan old = existingTestPlan("01");
            when(testplanMapper.selectTestPlanById(1L)).thenReturn(old);
            when(testplanMapper.updateTestPlan(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateTestPlan(updateTestPlan(1L, "00"));
            }
            verify(testplanMapper).updateTestPlan(any());
        }

        @Test
        @DisplayName("非法转换 00→02 → 601")
        void illegal_00_to_02() {
            TestPlan old = existingTestPlan("00");
            when(testplanMapper.selectTestPlanById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateTestPlan(updateTestPlan(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("终态 03→任意 → 601（已完成不可逆）")
        void terminal_03_immutable() {
            TestPlan old = existingTestPlan("03");
            when(testplanMapper.selectTestPlanById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01", "02"}) {
                assertThatThrownBy(() -> service.updateTestPlan(updateTestPlan(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已完成");
            }
        }

        @Test
        @DisplayName("测试方案不存在 → 404")
        void notFound() {
            when(testplanMapper.selectTestPlanById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateTestPlan(updateTestPlan(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试方案不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate (PRD §F4.1 AI 生成测试方案 — test-plan-flow)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (AI 生成测试方案)")
    class AiGenerateTests {

        @Test
        @DisplayName("生成成功：aiGenerated=Y / 策略含中文类型标签 / 工具含 playwright / 回写 + 审计")
        void generateSuccess() {
            TestPlan t = existingTestPlan("01");
            t.setTestTypes("functional,api,automation");
            t.setTestCycleDays(7);
            when(testplanMapper.selectTestPlanById(1L)).thenReturn(t);
            when(testplanMapper.updateTestPlan(any())).thenReturn(1);

            TestPlan r;
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("tester");
                r = service.aiGenerate(1L);
            }

            assertThat(r.getAiGenerated()).isEqualTo("Y");
            assertThat(r.getStrategy()).contains("功能测试").contains("接口测试").contains("自动化测试");
            assertThat(r.getScope()).isNotBlank();
            assertThat(r.getToolsRecommended()).contains("playwright");
            assertThat(r.getResourcesPlan()).contains("7 天");
            assertThat(r.getRiskAssessment()).isNotBlank();
            verify(aiService).chat(any());            // 审计:必走一次 AiService 产生 invocation log
            verify(testplanMapper).updateTestPlan(any());
        }

        @Test
        @DisplayName("testTypes 为空 → 类型标签回退为「功能测试」")
        void blankTestTypesFallback() {
            TestPlan t = existingTestPlan("01");
            t.setTestTypes(null);
            when(testplanMapper.selectTestPlanById(1L)).thenReturn(t);
            when(testplanMapper.updateTestPlan(any())).thenReturn(1);

            TestPlan r;
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("tester");
                r = service.aiGenerate(1L);
            }
            assertThat(r.getStrategy()).contains("功能测试");
        }

        @Test
        @DisplayName("testCycleDays 为空 → 资源计划默认周期 10 天")
        void defaultCycleDays() {
            TestPlan t = existingTestPlan("01");
            t.setTestTypes("functional");
            t.setTestCycleDays(null);
            when(testplanMapper.selectTestPlanById(1L)).thenReturn(t);
            when(testplanMapper.updateTestPlan(any())).thenReturn(1);

            TestPlan r;
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("tester");
                r = service.aiGenerate(1L);
            }
            assertThat(r.getResourcesPlan()).contains("10 天");
        }

        @Test
        @DisplayName("测试方案不存在 → 404")
        void notFound() {
            when(testplanMapper.selectTestPlanById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(99L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("测试方案不存在");
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

    private TestPlan existingTestPlan(String status) {
        TestPlan t = new TestPlan();
        t.setTestplanId(1L);
        t.setTitle("旧测试方案");
        t.setStatus(status);
        t.setProjectId(1L);
        return t;
    }

    private TestPlan updateTestPlan(Long id, String newStatus) {
        TestPlan t = new TestPlan();
        t.setTestplanId(id);
        t.setStatus(newStatus);
        return t;
    }
}
