package cn.com.bosssfot.dv.plm.prd.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.prd.domain.Prd;
import cn.com.bosssfot.dv.plm.prd.mapper.PrdMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * PrdServiceImpl 单元测试 — PRD §F2.2 + 原型 prd.html
 *
 * 覆盖范围(Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generatePrdNo: 格式 PRD-YYYY-NNNN / 流水续号 / 撞号重试
 *   - 字段校验: title 必填 / projectId 必填 / authorUserId 必填 / 项目存在性 702
 *   - ENUM 白名单: sceneTemplate ∈ {irrigation/agri_sales/pest_control/traceability} (604)
 *                  targetUser   ∈ {farmer/agronomist/admin} (604)
 *   - 默认值: aiGenerated=N / version=v1.0 / status='00' / 新建非草稿拒(601)
 *   - 4 状态机(含反向边 01→00): 5 合法路径 + N 非法路径
 *   - aiGenerate: 正常 / 不存在 404 / AiService.chat 审计 / chat 失败 fallback
 *   - 完整度真实计算 computeCompleteness: 7 段全 ≥ 95% / 部分缺失 < 80% / 空白 0%
 */
@ExtendWith(MockitoExtension.class)
class PrdServiceImplTest {

    @Mock
    private PrdMapper prdMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private PrdServiceImpl service;

    private Prd sample;

    @BeforeEach
    void setUp() {
        sample = new Prd();
        sample.setTitle("AI 灌溉推荐引擎");
        sample.setProjectId(10L);
        sample.setAuthorUserId(1L);
        sample.setDescription("基于土壤墒情传感器与作物模型,自动推荐灌溉时段与水量。");
        sample.setSceneTemplate("irrigation");
        sample.setTargetUser("farmer");
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generatePrdNo (ADR PRD-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generatePrdNo (PRD-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无 PRD 时,编号为 PRD-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(prdMapper.insertPrd(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPrd(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getPrdNo()).isEqualTo(String.format("PRD-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 9 个时,下一个编号为 0010")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.selectMaxSeqOfYear(anyString())).thenReturn(9);
            when(prdMapper.insertPrd(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPrd(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getPrdNo()).isEqualTo(String.format("PRD-%d-0010", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(prdMapper.insertPrd(any()))
                .thenThrow(new DuplicateKeyException("uk_prd_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertPrd(sample);
                assertThat(rows).isEqualTo(1);
                verify(prdMapper, times(2)).insertPrd(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 prdNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setPrdNo("PRD-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.insertPrd(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPrd(sample);
            }
            assertThat(sample.getPrdNo()).isEqualTo("PRD-CUSTOM-2099");
            verify(prdMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + 关联项目存在性 + ENUM 白名单
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (PRD §F2.2)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertPrd(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("功能名称");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertPrd(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("authorUserId 必填,空抛 602")
        void authorRequired() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertPrd(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("作者");
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(anyLong())).thenReturn(null);
            assertThatThrownBy(() -> service.insertPrd(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("sceneTemplate 非白名单 → 604")
        void sceneTemplateOutOfWhitelist() {
            sample.setSceneTemplate("organic_farming");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertPrd(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("业务场景");
        }

        @Test
        @DisplayName("sceneTemplate 全 4 个合法值都接受")
        void allScenesAccepted() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(prdMapper.insertPrd(any())).thenReturn(1);
            for (String s : new String[] { "irrigation", "agri_sales", "pest_control", "traceability" }) {
                Prd p = new Prd();
                p.setTitle("p-" + s);
                p.setProjectId(10L);
                p.setAuthorUserId(1L);
                p.setSceneTemplate(s);
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.insertPrd(p);
                }
                assertThat(p.getSceneTemplate()).isEqualTo(s);
            }
        }

        @Test
        @DisplayName("targetUser 非白名单 → 604")
        void targetUserOutOfWhitelist() {
            sample.setTargetUser("guest");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertPrd(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("目标用户");
        }

        @Test
        @DisplayName("新建 PRD status 非 00 时拒绝 → 601")
        void newPrdMustBeDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertPrd(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertPrd)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 aiGenerated 默认 N")
        void defaultAiGeneratedN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(prdMapper.insertPrd(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPrd(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 version 默认 v1.0")
        void defaultVersion() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(prdMapper.insertPrd(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPrd(sample);
            }
            assertThat(sample.getVersion()).isEqualTo("v1.0");
        }

        @Test
        @DisplayName("未指定 status 默认 00 草稿")
        void defaultStatusDraft() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(prdMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(prdMapper.insertPrd(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPrd(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4 状态机(含反向边 01→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 4 态(PRD §F2.2 含反向边)")
    class StateMachineTests {

        private Prd withStatus(String s) {
            Prd p = new Prd();
            p.setPrdId(99L);
            p.setStatus(s);
            p.setProjectId(10L);
            return p;
        }

        @Test
        @DisplayName("00 草稿 → 01 评审中 合法")
        void draftToReview() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("00"));
            when(prdMapper.updatePrd(any())).thenReturn(1);
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updatePrd(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 00 草稿 反向打回合法(反向边)")
        void reviewBackToDraft() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("01"));
            when(prdMapper.updatePrd(any())).thenReturn(1);
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updatePrd(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 02 已确认 合法")
        void reviewToConfirmed() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("01"));
            when(prdMapper.updatePrd(any())).thenReturn(1);
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updatePrd(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已确认 → 03 已废弃 合法")
        void confirmedToDeprecated() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("02"));
            when(prdMapper.updatePrd(any())).thenReturn(1);
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updatePrd(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("03 已废弃 → 任意状态 非法(终态保护)")
        void deprecatedIsTerminal() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("03"));
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("00");
            assertThatThrownBy(() -> service.updatePrd(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 02 已确认 跳级非法")
        void draftCannotJumpToConfirmed() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("00"));
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updatePrd(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("02 已确认 → 00 草稿 反向边非法(只允许 01→00)")
        void confirmedCannotReverseToDraft() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("02"));
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("00");
            assertThatThrownBy(() -> service.updatePrd(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(prdMapper.selectPrdById(99L)).thenReturn(withStatus("02"));
            when(prdMapper.updatePrd(any())).thenReturn(1);
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setStatus("02");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updatePrd(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时 PRD 不存在抛 404")
        void updateNotFound() {
            when(prdMapper.selectPrdById(404L)).thenReturn(null);
            Prd upd = new Prd();
            upd.setPrdId(404L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updatePrd(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("PRD 不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在抛 702")
        void updateProjectIdNotFound() {
            Prd old = withStatus("00");
            old.setProjectId(10L);
            when(prdMapper.selectPrdById(99L)).thenReturn(old);
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            Prd upd = new Prd();
            upd.setPrdId(99L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updatePrd(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate (PRD §F2.2 AI PRD 生成)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F2.2 — 7 段 + 完整度 ≥ 80%)")
    class AiGenerateTests {

        @Test
        @DisplayName("正常生成 → 7 段 Markdown + aiGenerated=Y + completeness ≥ 80%")
        void normalAiGenerate() {
            Prd prd = new Prd();
            prd.setPrdId(50L);
            prd.setTitle("AI 灌溉推荐引擎");
            prd.setSceneTemplate("irrigation");
            prd.setTargetUser("farmer");
            prd.setVersion("v1.0");
            prd.setDescription("基于土壤墒情自动推荐灌溉时段。");
            when(prdMapper.selectPrdById(50L)).thenReturn(prd);
            when(prdMapper.updatePrd(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Prd result = service.aiGenerate(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getContent()).isNotBlank();
                assertThat(result.getContent())
                    .contains("背景与目标")
                    .contains("用户故事")
                    .contains("功能描述")
                    .contains("非功能")
                    .contains("验收")
                    .contains("原型")
                    .contains("版本");
                assertThat(result.getCompletenessScore()).isNotNull();
                assertThat(result.getCompletenessScore())
                    .as("PRD §F2.2 验收红线 ≥ 80%")
                    .isGreaterThanOrEqualTo(new BigDecimal("80.00"));
                assertThat(result.getAiGeneratedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("aiGenerate 不存在抛 404")
        void aiGenerateNotFound() {
            when(prdMapper.selectPrdById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("PRD 不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次(审计联动)")
        void aiServiceCalledOnce() {
            Prd prd = new Prd();
            prd.setPrdId(60L);
            prd.setTitle("农产品溯源 H5");
            prd.setSceneTemplate("traceability");
            prd.setTargetUser("admin");
            when(prdMapper.selectPrdById(60L)).thenReturn(prd);
            when(prdMapper.updatePrd(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.aiGenerate(60L);
            }
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
        }

        @Test
        @DisplayName("AiService.chat 失败 → fallback 走场景化 mock,不阻塞(业务连续)")
        void aiServiceFailFallback() {
            Prd prd = new Prd();
            prd.setPrdId(70L);
            prd.setTitle("病虫害识别");
            prd.setSceneTemplate("pest_control");
            prd.setTargetUser("agronomist");
            when(prdMapper.selectPrdById(70L)).thenReturn(prd);
            when(prdMapper.updatePrd(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.fail("openai", "rate_limit_exceeded"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Prd result = service.aiGenerate(70L);
                // 业务连续:即使 chat 失败,仍然生成 7 段 mock + 完整度 + aiGenerated=Y
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getContent()).contains("病虫害");
                assertThat(result.getCompletenessScore())
                    .isGreaterThanOrEqualTo(new BigDecimal("80.00"));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 完整度真实计算 computeCompleteness
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("completenessScore 真实计算 (PRD §F2.2 红线 ≥ 80%)")
    class CompletenessTests {

        @Test
        @DisplayName("7 段全 + 字数 ≥ 700 → 100%")
        void fullContentScore100() {
            String content = "## 背景与目标\n" + "x".repeat(120) + "\n"
                + "## 用户故事\n" + "x".repeat(120) + "\n"
                + "## 功能描述\n" + "x".repeat(120) + "\n"
                + "## 非功能需求\n" + "x".repeat(120) + "\n"
                + "## 验收标准\n" + "x".repeat(120) + "\n"
                + "## 原型说明\n" + "x".repeat(120) + "\n"
                + "## 版本\n" + "x".repeat(120);
            BigDecimal score = service.computeCompleteness(content);
            assertThat(score).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("仅 5 段命中 → 64.29% + 字数加权 < 80%")
        void partialContent5SectionsBelowThreshold() {
            String content = "## 背景\n短.\n## 用户故事\n短.\n## 功能\n短.\n## 非功能\n短.\n## 验收\n短.\n";
            BigDecimal score = service.computeCompleteness(content);
            assertThat(score).isLessThan(new BigDecimal("80.00"));
        }

        @Test
        @DisplayName("空白内容 → 0%")
        void emptyContentScore0() {
            assertThat(service.computeCompleteness(null)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(service.computeCompleteness("")).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(service.computeCompleteness("   ")).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 删除
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("批量删除转发到 mapper")
        void deleteByIds() {
            when(prdMapper.deletePrdByIds(any())).thenReturn(3);
            int rows = service.deletePrdByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
        }
    }
}
