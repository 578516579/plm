package cn.com.bosssfot.dv.plm.competitive.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.competitive.domain.Competitive;
import cn.com.bosssfot.dv.plm.competitive.mapper.CompetitiveMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * CompetitiveServiceImpl 单元测试 — PRD §F1.3 + 原型 competitive.html
 *
 * 覆盖范围(Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generateCompetitiveNo: 格式 COMP-YYYY-NNNN / 流水续号 / 撞号重试
 *   - 字段校验: competitorName 必填 / projectId 必填 / authorUserId 必填 / 关联项目存在性
 *   - ENUM 白名单: pricingTier ∈ {free, midrange, enterprise}(604)
 *   - 默认值填充: aiGenerated=N / monitorEnabled=N / status='00' / 新建非草稿拒绝
 *   - 3 状态机(无反向边): 00→01 / 01→02 / 02 终态 / 跳级 / 反向边非法
 *   - aiAnalyze: SWOT 四象限 + 综合报告 + AiService 调用 / 不存在抛 404
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class CompetitiveServiceImplTest {

    @Mock
    private CompetitiveMapper competitiveMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private CompetitiveServiceImpl service;

    private Competitive sample;

    @BeforeEach
    void setUp() {
        sample = new Competitive();
        sample.setCompetitorName("Atlassian Jira");
        sample.setProjectId(10L);
        sample.setAuthorUserId(1L);
        sample.setPricingTier("enterprise");
        sample.setVendor("Atlassian");
        sample.setWebsite("https://www.atlassian.com/software/jira");
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateCompetitiveNo (ADR COMP-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateCompetitiveNo (COMP-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无竞品时,编号为 COMP-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertCompetitive(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getCompetitiveNo()).isEqualTo(String.format("COMP-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 12 个时,下一个编号为 0013")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(12);
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertCompetitive(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getCompetitiveNo()).isEqualTo(String.format("COMP-%d-0013", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(competitiveMapper.insertCompetitive(any()))
                .thenThrow(new DuplicateKeyException("uk_competitive_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertCompetitive(sample);
                assertThat(rows).isEqualTo(1);
                verify(competitiveMapper, times(2)).insertCompetitive(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 competitiveNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setCompetitiveNo("COMP-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertCompetitive(sample);
            }
            assertThat(sample.getCompetitiveNo()).isEqualTo("COMP-CUSTOM-2099");
            verify(competitiveMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + 关联项目存在性
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §F1.3)")
    class ValidationTests {

        @Test
        @DisplayName("competitorName 必填,空抛 602")
        void competitorNameRequired() {
            sample.setCompetitorName(null);
            assertThatThrownBy(() -> service.insertCompetitive(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("竞品名称");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertCompetitive(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("authorUserId 必填,空抛 602")
        void authorRequired() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertCompetitive(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("创建人");
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(anyLong())).thenReturn(null);
            assertThatThrownBy(() -> service.insertCompetitive(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("pricingTier 非白名单 → 604")
        void pricingTierOutOfWhitelist() {
            sample.setPricingTier("premium_plus");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertCompetitive(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("价格档");
        }

        @Test
        @DisplayName("pricingTier 全 3 个合法值都接受")
        void allTiersAccepted() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            for (String tier : new String[] { "free", "midrange", "enterprise" }) {
                Competitive c = new Competitive();
                c.setCompetitorName("c-" + tier);
                c.setProjectId(10L);
                c.setAuthorUserId(1L);
                c.setPricingTier(tier);
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.insertCompetitive(c);
                }
                assertThat(c.getPricingTier()).isEqualTo(tier);
            }
        }

        @Test
        @DisplayName("pricingTier 空值可接受(可选字段)")
        void pricingTierEmptyOk() {
            sample.setPricingTier(null);
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertCompetitive(sample);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("新建竞品 status 非 00 时拒绝 → 601")
        void newCompetitiveMustBeDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertCompetitive(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertCompetitive)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 aiGenerated 默认 N")
        void defaultAiGeneratedN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertCompetitive(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 monitorEnabled 默认 N")
        void defaultMonitorEnabledN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertCompetitive(sample);
            }
            assertThat(sample.getMonitorEnabled()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 status 默认 00 草稿")
        void defaultStatusDraft() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(competitiveMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(competitiveMapper.insertCompetitive(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertCompetitive(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3 状态机(无反向边)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 3 态(PRD §F1.3)")
    class StateMachineTests {

        private Competitive withStatus(String s) {
            Competitive c = new Competitive();
            c.setCompetitiveId(99L);
            c.setStatus(s);
            c.setProjectId(10L);
            return c;
        }

        @Test
        @DisplayName("00 草稿 → 01 已发布 合法")
        void draftToPublished() {
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(withStatus("00"));
            when(competitiveMapper.updateCompetitive(any())).thenReturn(1);
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateCompetitive(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 已发布 → 02 已归档 合法")
        void publishedToArchived() {
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(withStatus("01"));
            when(competitiveMapper.updateCompetitive(any())).thenReturn(1);
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateCompetitive(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已归档 → 任何状态 非法 (终态保护)")
        void archivedIsTerminal() {
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(withStatus("02"));
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateCompetitive(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("01 已发布 → 00 草稿 反向边非法")
        void publishedCannotReverseToDraft() {
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(withStatus("01"));
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setStatus("00");
            assertThatThrownBy(() -> service.updateCompetitive(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 02 已归档 跳级非法")
        void draftCannotJumpToArchived() {
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(withStatus("00"));
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateCompetitive(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(withStatus("02"));
            when(competitiveMapper.updateCompetitive(any())).thenReturn(1);
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setStatus("02");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateCompetitive(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时竞品不存在抛 404")
        void updateNotFound() {
            when(competitiveMapper.selectCompetitiveById(404L)).thenReturn(null);
            Competitive upd = new Competitive();
            upd.setCompetitiveId(404L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateCompetitive(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("竞品不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在抛 702")
        void updateProjectIdNotFound() {
            Competitive old = withStatus("00");
            old.setProjectId(10L);
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(old);
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updateCompetitive(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("update 时改 pricingTier 非白名单抛 604")
        void updatePricingTierOutOfWhitelist() {
            when(competitiveMapper.selectCompetitiveById(99L)).thenReturn(withStatus("00"));
            Competitive upd = new Competitive();
            upd.setCompetitiveId(99L);
            upd.setPricingTier("invalid");
            assertThatThrownBy(() -> service.updateCompetitive(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("价格档");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiAnalyze
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiAnalyze (PRD §F1.3 AI SWOT)")
    class AiAnalyzeTests {

        @Test
        @DisplayName("正常分析 → SWOT 四象限 + 综合报告 + aiGenerated=Y / 时间填充")
        void normalAiAnalyze() {
            Competitive comp = new Competitive();
            comp.setCompetitiveId(50L);
            comp.setCompetitorName("Atlassian Jira");
            when(competitiveMapper.selectCompetitiveById(50L)).thenReturn(comp);
            when(competitiveMapper.updateCompetitive(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Competitive result = service.aiAnalyze(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getStrengths()).isNotBlank();
                assertThat(result.getWeaknesses()).isNotBlank();
                assertThat(result.getOpportunities()).isNotBlank();
                assertThat(result.getThreats()).isNotBlank();
                assertThat(result.getAiAnalysisReport())
                    .contains("竞品分析报告")
                    .contains("Atlassian Jira")
                    .contains("SWOT 矩阵");
                assertThat(result.getAiGeneratedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("aiAnalyze 时竞品不存在抛 404")
        void aiAnalyzeNotFound() {
            when(competitiveMapper.selectCompetitiveById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiAnalyze(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("竞品不存在");
        }

        @Test
        @DisplayName("aiAnalyze 调用 AiService.chat 一次(审计联动)")
        void aiServiceCalledOnce() {
            Competitive comp = new Competitive();
            comp.setCompetitiveId(60L);
            comp.setCompetitorName("飞书项目");
            when(competitiveMapper.selectCompetitiveById(60L)).thenReturn(comp);
            when(competitiveMapper.updateCompetitive(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.aiAnalyze(60L);
            }
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
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
            when(competitiveMapper.deleteCompetitiveByIds(any())).thenReturn(3);
            int rows = service.deleteCompetitiveByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
        }
    }
}
