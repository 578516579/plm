package cn.com.bosssfot.dv.plm.testdata.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.testdata.domain.TestData;
import cn.com.bosssfot.dv.plm.testdata.mapper.TestDataMapper;

/**
 * TestDataServiceImpl 单元测试 — PRD §F4.3 + 原型 testdata.html
 *
 * 覆盖范围 (Phase 03 Gate B.4 关键路径 + §M.2 DoD;补 P0 双缺口之单测层):
 *   - generateTestDataNo: 格式 TD-YYYY-NNNN / 流水续号 / 撞号重试 / 用户传入不覆盖
 *   - 字段校验: title / projectId / targetTable / authorUserId 必填 (602)
 *   - ENUM 白名单: targetTable (5 农业表) / outputFormat (json/sql/csv) → 604
 *   - 关联项目存在性 → 702
 *   - 默认值填充: outputFormat=json / generateCount=1000 / 4 规则开关 /
 *                 aiGenerated=N / status=00 / 新建非草稿拒绝 (601)
 *   - 3 状态机 (无反向边): 00→01 / 01→02 / 02 终态 / 跳级非法 / 反向非法 (601)
 *   - generate (AI mock): fieldSemantics + generatedContent (中国坐标语义) +
 *                         status=01 + aiGenerated=Y + generatedAt
 *   - delete / list / get 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class TestDataServiceImplTest {

    @Mock
    private TestDataMapper testdataMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private AiService aiService;  // P0-1b: 默认未 stub,chat() 返回 null → AiTexts 走 fallback,断言保持原样

    @InjectMocks
    private TestDataServiceImpl service;

    private TestData sample;

    @BeforeEach
    void setUp() {
        // 仅填必填项,可选项 (outputFormat/generateCount/rules/status/testdataNo) 留 null 以验默认值
        sample = new TestData();
        sample.setTitle("土壤传感器测试数据集");
        sample.setProjectId(10L);
        sample.setTargetTable("soil_sensor");
        sample.setAuthorUserId(1L);
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    /** 断言抛 ServiceException 且 code + 文案均匹配 (比模板仅校验文案更严) */
    private void assertBizError(ThrowingCallable call, int expectedCode, String msgPart) {
        Throwable thrown = catchThrowable(call);
        assertThat(thrown).as("应抛 ServiceException").isInstanceOf(ServiceException.class);
        assertThat(((ServiceException) thrown).getCode()).as("错误码").isEqualTo(expectedCode);
        assertThat(thrown.getMessage()).as("错误文案").contains(msgPart);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateTestDataNo (ADR TD-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateTestDataNo (TD-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无数据集时,编号为 TD-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(testdataMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testdataMapper.insertTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestData(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getTestdataNo()).isEqualTo(String.format("TD-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 12 个时,下一个编号为 0013")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(testdataMapper.selectMaxSeqOfYear(anyString())).thenReturn(12);
            when(testdataMapper.insertTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestData(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getTestdataNo()).isEqualTo(String.format("TD-%d-0013", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功,insert 调两次")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(testdataMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(testdataMapper.insertTestData(any()))
                .thenThrow(new DuplicateKeyException("uk_testdata_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertTestData(sample);
                assertThat(rows).isEqualTo(1);
                verify(testdataMapper, times(2)).insertTestData(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 testdataNo 时不自动生成,不查流水号")
        void userProvidedNoIsKept() {
            sample.setTestdataNo("TD-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(testdataMapper.insertTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestData(sample);
            }
            assertThat(sample.getTestdataNo()).isEqualTo("TD-CUSTOM-2099");
            verify(testdataMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + ENUM 白名单 + FK (insertTestData)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 + ENUM 白名单 + FK (insertTestData)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertBizError(() -> service.insertTestData(sample), 602, "任务标题");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertBizError(() -> service.insertTestData(sample), 602, "关联项目不能为空");
        }

        @Test
        @DisplayName("targetTable 必填,空抛 602")
        void targetTableRequired() {
            sample.setTargetTable(null);
            assertBizError(() -> service.insertTestData(sample), 602, "目标表不能为空");
        }

        @Test
        @DisplayName("targetTable 非白名单 → 604 (校验先于项目存在性,不查 project)")
        void targetTableOutOfWhitelist() {
            sample.setTargetTable("t_unknown");
            assertBizError(() -> service.insertTestData(sample), 604, "目标表值非法");
            verify(projectMapper, never()).selectProjectById(any());
        }

        @Test
        @DisplayName("authorUserId 必填,空抛 602")
        void authorRequired() {
            sample.setAuthorUserId(null);
            assertBizError(() -> service.insertTestData(sample), 602, "创建人不能为空");
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(10L)).thenReturn(null);
            assertBizError(() -> service.insertTestData(sample), 702, "关联项目不存在");
        }

        @Test
        @DisplayName("outputFormat 非白名单 → 604 (校验晚于项目存在性)")
        void outputFormatOutOfWhitelist() {
            sample.setOutputFormat("xml");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertBizError(() -> service.insertTestData(sample), 604, "输出格式值非法");
        }

        @Test
        @DisplayName("新建数据集 status 非 00 时拒绝 → 601")
        void newTestDataMustBeDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertBizError(() -> service.insertTestData(sample), 601, "草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值填充 (insertTestData)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertTestData)")
    class DefaultsTests {

        @Test
        @DisplayName("可选项全空时填默认: json/1000/规则 YYYN/aiGenerated=N/status=00")
        void fillsAllDefaults() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(testdataMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testdataMapper.insertTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestData(sample);
            }
            assertThat(sample.getOutputFormat()).isEqualTo("json");
            assertThat(sample.getGenerateCount()).isEqualTo(1000);
            assertThat(sample.getRuleChinaCoord()).isEqualTo("Y");
            assertThat(sample.getRuleTimeContinuity()).isEqualTo("Y");
            assertThat(sample.getRuleSensorRange()).isEqualTo("Y");
            assertThat(sample.getRuleIncludeOutliers()).isEqualTo("N");
            assertThat(sample.getAiGenerated()).isEqualTo("N");
            assertThat(sample.getStatus()).isEqualTo("00");
            assertThat(sample.getCreateBy()).isEqualTo("admin");
        }

        @Test
        @DisplayName("用户显式传入的可选项不被默认值覆盖")
        void providedValuesNotOverwritten() {
            sample.setOutputFormat("csv");
            sample.setGenerateCount(500);
            sample.setRuleIncludeOutliers("Y");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(testdataMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testdataMapper.insertTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertTestData(sample);
            }
            assertThat(sample.getOutputFormat()).isEqualTo("csv");
            assertThat(sample.getGenerateCount()).isEqualTo(500);
            assertThat(sample.getRuleIncludeOutliers()).isEqualTo("Y");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3 状态机 (无反向边): 00 草稿 → 01 已生成 → 02 已归档 (终态)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 3 态无反向边 (PRD §F4.3)")
    class StateMachineTests {

        private TestData withStatus(String s) {
            TestData t = new TestData();
            t.setTestdataId(99L);
            t.setStatus(s);
            t.setProjectId(10L);
            return t;
        }

        private TestData updateTo(String status) {
            TestData upd = new TestData();
            upd.setTestdataId(99L);
            upd.setStatus(status);
            return upd;
        }

        @Test
        @DisplayName("00 草稿 → 01 已生成 合法")
        void draftToGenerated() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("00"));
            when(testdataMapper.updateTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateTestData(updateTo("01"))).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 已生成 → 02 已归档 合法")
        void generatedToArchived() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("01"));
            when(testdataMapper.updateTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateTestData(updateTo("02"))).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("00 草稿 → 02 已归档 跳级非法 → 601")
        void draftCannotJumpToArchived() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("00"));
            assertBizError(() -> service.updateTestData(updateTo("02")), 601, "不能直接转");
        }

        @Test
        @DisplayName("01 已生成 → 00 草稿 反向非法 → 601")
        void generatedCannotReverseToDraft() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("01"));
            assertBizError(() -> service.updateTestData(updateTo("00")), 601, "不能直接转");
        }

        @Test
        @DisplayName("02 已归档 → 01 任意状态 非法 (终态保护) → 601")
        void archivedIsTerminal() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("02"));
            assertBizError(() -> service.updateTestData(updateTo("01")), 601, "不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("02"));
            when(testdataMapper.updateTestData(any())).thenReturn(1);
            TestData upd = updateTo("02");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateTestData(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时数据集不存在抛 404")
        void updateNotFound() {
            when(testdataMapper.selectTestDataById(404L)).thenReturn(null);
            TestData upd = new TestData();
            upd.setTestdataId(404L);
            upd.setStatus("01");
            assertBizError(() -> service.updateTestData(upd), 404, "测试数据集不存在");
        }

        @Test
        @DisplayName("update 改 projectId,新项目不存在抛 702")
        void updateProjectIdNotFound() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("00"));
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            TestData upd = new TestData();
            upd.setTestdataId(99L);
            upd.setProjectId(999L);
            assertBizError(() -> service.updateTestData(upd), 702, "关联项目不存在");
        }

        @Test
        @DisplayName("update 改 targetTable 非白名单抛 604")
        void updateTargetTableOutOfWhitelist() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("00"));
            TestData upd = new TestData();
            upd.setTestdataId(99L);
            upd.setTargetTable("t_bad");
            assertBizError(() -> service.updateTestData(upd), 604, "目标表值非法");
        }

        @Test
        @DisplayName("update 改 outputFormat 非白名单抛 604")
        void updateOutputFormatOutOfWhitelist() {
            when(testdataMapper.selectTestDataById(99L)).thenReturn(withStatus("00"));
            TestData upd = new TestData();
            upd.setTestdataId(99L);
            upd.setOutputFormat("xml");
            assertBizError(() -> service.updateTestData(upd), 604, "输出格式值非法");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // generate (AI mock — data-gen-flow 本期 mock)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generate (AI 生成 mock)")
    class GenerateTests {

        @Test
        @DisplayName("正常生成 → fieldSemantics(中国坐标语义) + generatedContent + status=01 + aiGenerated=Y + generatedAt")
        void normalGenerate() {
            TestData td = new TestData();
            td.setTestdataId(50L);
            td.setStatus("00");
            td.setTitle("土壤传感器测试数据集");
            when(testdataMapper.selectTestDataById(50L)).thenReturn(td);
            when(testdataMapper.updateTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                TestData result = service.generate(50L);
                assertThat(result.getStatus()).isEqualTo("01");
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getGeneratedAt()).isNotNull();
                assertThat(result.getFieldSemantics())
                    .isNotBlank()
                    .contains("latitude")
                    .contains("中国范围");
                assertThat(result.getGeneratedContent())
                    .isNotBlank()
                    .contains("soil_moisture");
            }
        }

        @Test
        @DisplayName("generate 持久化经 updateTestData 一次")
        void generatePersistsOnce() {
            TestData td = new TestData();
            td.setTestdataId(60L);
            td.setStatus("00");
            when(testdataMapper.selectTestDataById(60L)).thenReturn(td);
            when(testdataMapper.updateTestData(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.generate(60L);
            }
            verify(testdataMapper, times(1)).updateTestData(any());
        }

        @Test
        @DisplayName("generate 时数据集不存在抛 404")
        void generateNotFound() {
            when(testdataMapper.selectTestDataById(404L)).thenReturn(null);
            assertBizError(() -> service.generate(404L), 404, "测试数据集不存在");
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
            when(testdataMapper.deleteTestDataByIds(any())).thenReturn(2);
            assertThat(service.deleteTestDataByIds(new Long[] { 1L, 2L })).isEqualTo(2);
        }

        @Test
        @DisplayName("列表查询转发到 mapper")
        void selectList() {
            when(testdataMapper.selectTestDataList(any())).thenReturn(List.of(sample));
            assertThat(service.selectTestDataList(new TestData())).hasSize(1);
        }

        @Test
        @DisplayName("按 id 查询转发到 mapper")
        void selectById() {
            when(testdataMapper.selectTestDataById(7L)).thenReturn(sample);
            assertThat(service.selectTestDataById(7L)).isSameAs(sample);
        }
    }
}
