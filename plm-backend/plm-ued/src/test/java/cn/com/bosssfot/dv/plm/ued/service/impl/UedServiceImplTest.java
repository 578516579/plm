package cn.com.bosssfot.dv.plm.ued.service.impl;

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
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.ued.domain.Ued;
import cn.com.bosssfot.dv.plm.ued.mapper.UedMapper;

/**
 * UedServiceImpl 单元测试 — PRD §F2.3 + 原型 ued.html
 *
 * 覆盖范围(Phase 03 Gate B.4 关键路径 + PRD-MAPPING §M.2 9 项 DoD):
 *   - generateUedNo: 格式 UED-YYYY-NNNN / 流水续号 / 撞号重试 / 用户传 No 时不自动生成
 *   - 字段校验: title 必填 / projectId 必填 / designerUserId 必填 / 关联项目存在性 / 新建非草稿拒
 *   - 默认值填充: aiGenerated=N / status='00'
 *   - 4 状态机(含反向边 01→00): 00→01 / 01→{00,02} / 02→03 / 03 终态 / 跳级
 *   - aiReview: AI 报告 + complianceCheck + usabilityIssues + aiReviewScore=85 + aiGenerated=Y / AiService.chat 调用一次 / 不存在 404
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class UedServiceImplTest {

    @Mock
    private UedMapper uedMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private UedServiceImpl service;

    private Ued sample;

    @BeforeEach
    void setUp() {
        sample = new Ued();
        sample.setTitle("灌溉控制台 v2.1 设计稿");
        sample.setProjectId(10L);
        sample.setDesignerUserId(1L);
        sample.setVersionLabel("v2.1");
        sample.setFigmaUrl("https://www.figma.com/file/abc123/Irrigation-v2.1");
        sample.setFigmaFileKey("abc123");
        sample.setAgriComponentTags("农情大屏组件,IoT数据看板");
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateUedNo (ADR UED-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateUedNo (UED-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无设计稿时,编号为 UED-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(uedMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(uedMapper.insertUed(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertUed(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getUedNo()).isEqualTo(String.format("UED-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 8 个时,下一个编号为 0009")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(uedMapper.selectMaxSeqOfYear(anyString())).thenReturn(8);
            when(uedMapper.insertUed(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertUed(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getUedNo()).isEqualTo(String.format("UED-%d-0009", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(uedMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(uedMapper.insertUed(any()))
                .thenThrow(new DuplicateKeyException("uk_ued_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertUed(sample);
                assertThat(rows).isEqualTo(1);
                verify(uedMapper, times(2)).insertUed(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 uedNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setUedNo("UED-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(uedMapper.insertUed(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertUed(sample);
            }
            assertThat(sample.getUedNo()).isEqualTo("UED-CUSTOM-2099");
            verify(uedMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + 关联项目存在性
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §F2.3)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,null 抛 602")
        void titleNullRejected() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertUed(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("设计稿名称");
        }

        @Test
        @DisplayName("title 空白,空格也拒(StringUtils.isBlank)")
        void titleBlankRejected() {
            sample.setTitle("   ");
            assertThatThrownBy(() -> service.insertUed(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("设计稿名称");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertUed(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("designerUserId 必填,空抛 602")
        void designerRequired() {
            sample.setDesignerUserId(null);
            assertThatThrownBy(() -> service.insertUed(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("设计师");
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(anyLong())).thenReturn(null);
            assertThatThrownBy(() -> service.insertUed(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建 UED status 非 00 时拒绝 → 601")
        void newUedMustBeDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertUed(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("新建 UED 显式传 status='00' 接受")
        void newUedDraftAccepted() {
            sample.setStatus("00");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(uedMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(uedMapper.insertUed(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertUed(sample);
                assertThat(rows).isEqualTo(1);
                assertThat(sample.getStatus()).isEqualTo("00");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertUed)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 aiGenerated 默认 N")
        void defaultAiGeneratedN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(uedMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(uedMapper.insertUed(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertUed(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 status 默认 00 草稿")
        void defaultStatusDraft() {
            sample.setStatus(null);
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(uedMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(uedMapper.insertUed(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertUed(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4 状态机(含反向边 01→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 4 态含反向边 (PRD §F2.3)")
    class StateMachineTests {

        private Ued withStatus(String s) {
            Ued u = new Ued();
            u.setUedId(99L);
            u.setStatus(s);
            u.setProjectId(10L);
            return u;
        }

        @Test
        @DisplayName("00 草稿 → 01 评审中 合法")
        void draftToReview() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("00"));
            when(uedMapper.updateUed(any())).thenReturn(1);
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateUed(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 02 已确认 合法")
        void reviewToConfirmed() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("01"));
            when(uedMapper.updateUed(any())).thenReturn(1);
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateUed(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 00 草稿 反向打回 合法 (UED 特色反向边)")
        void reviewReverseToDraft() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("01"));
            when(uedMapper.updateUed(any())).thenReturn(1);
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateUed(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已确认 → 03 已废弃 合法")
        void confirmedToDiscarded() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("02"));
            when(uedMapper.updateUed(any())).thenReturn(1);
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateUed(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("03 已废弃 → 任何状态 非法 (终态保护)")
        void discardedIsTerminal() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("03"));
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateUed(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 02 已确认 跳级非法")
        void draftCannotJumpToConfirmed() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("00"));
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateUed(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("02 已确认 → 01 评审中 反向边非法 (已确认不可回评审)")
        void confirmedCannotReverseToReview() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("02"));
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateUed(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 03 已废弃 跳级非法")
        void draftCannotJumpToDiscarded() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("00"));
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("03");
            assertThatThrownBy(() -> service.updateUed(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(uedMapper.selectUedById(99L)).thenReturn(withStatus("03"));
            when(uedMapper.updateUed(any())).thenReturn(1);
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setStatus("03");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateUed(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时设计稿不存在抛 404")
        void updateNotFound() {
            when(uedMapper.selectUedById(404L)).thenReturn(null);
            Ued upd = new Ued();
            upd.setUedId(404L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateUed(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("UED 设计不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在抛 702")
        void updateProjectIdNotFound() {
            Ued old = withStatus("00");
            old.setProjectId(10L);
            when(uedMapper.selectUedById(99L)).thenReturn(old);
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            Ued upd = new Ued();
            upd.setUedId(99L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updateUed(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiReview
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiReview (PRD §F2.3 AI 设计规范检查)")
    class AiReviewTests {

        @Test
        @DisplayName("正常评审 → 报告 + complianceCheck + usabilityIssues + score=85 + aiGenerated=Y / 时间填充")
        void normalAiReview() {
            Ued u = new Ued();
            u.setUedId(50L);
            u.setTitle("灌溉控制台 v2.1 设计稿");
            when(uedMapper.selectUedById(50L)).thenReturn(u);
            when(uedMapper.updateUed(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Ued result = service.aiReview(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getAiReviewReport())
                    .contains("UED 设计评审报告")
                    .contains("灌溉控制台 v2.1 设计稿")
                    .contains("设计规范遵从度");
                assertThat(result.getComplianceCheck()).contains("layout").contains("accessibility");
                assertThat(result.getUsabilityIssues()).isNotBlank();
                assertThat(result.getAiReviewScore()).isEqualByComparingTo(new BigDecimal("85.00"));
                assertThat(result.getAiGeneratedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("aiReview 时设计稿不存在抛 404")
        void aiReviewNotFound() {
            when(uedMapper.selectUedById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiReview(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("UED 设计不存在");
        }

        @Test
        @DisplayName("aiReview 调用 AiService.chat 一次(审计联动)")
        void aiServiceCalledOnce() {
            Ued u = new Ued();
            u.setUedId(60L);
            u.setTitle("地块大屏 v1.0");
            when(uedMapper.selectUedById(60L)).thenReturn(u);
            when(uedMapper.updateUed(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.aiReview(60L);
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
            when(uedMapper.deleteUedByIds(any())).thenReturn(3);
            int rows = service.deleteUedByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
        }
    }
}
