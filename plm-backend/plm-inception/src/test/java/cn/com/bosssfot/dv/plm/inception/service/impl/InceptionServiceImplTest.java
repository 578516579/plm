package cn.com.bosssfot.dv.plm.inception.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Date;

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
import cn.com.bosssfot.dv.plm.inception.domain.Inception;
import cn.com.bosssfot.dv.plm.inception.mapper.InceptionMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.service.IProjectService;

/**
 * InceptionServiceImpl 单元测试 — PRD §F1.1 + 原型 inception.html
 *
 * 覆盖范围(Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generateInceptionNo: 格式 INC-YYYY-NNNN / 流水续号 / 撞号重试
 *   - 字段校验: projectName 必填 / submitterUserId 必填 / 业务线白名单 / 类型白名单
 *   - 默认值填充: aiGenerated=N / status='00' / 新建非草稿拒绝
 *   - 5×5 状态机 (含反向边 04→00): 各状态合法 / 非法 / 边界转换
 *   - 业务规则: →04 必填 rejectReason / 02→03 自动填 approvedAt / 不存在抛 404
 *   - aiGenerate: AI 字段填充 / AiService 调用 / 不存在抛 404
 */
@ExtendWith(MockitoExtension.class)
class InceptionServiceImplTest {

    @Mock
    private InceptionMapper inceptionMapper;

    @Mock
    private AiService aiService;

    @Mock
    private IProjectService projectService;

    @InjectMocks
    private InceptionServiceImpl service;

    private Inception sample;

    @BeforeEach
    void setUp() {
        sample = new Inception();
        sample.setProjectName("农业病虫害智能识别系统");
        sample.setSubmitterUserId(1L);
        sample.setBusinessLine("plant_protection");
        sample.setInceptionType("new_product");
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateInceptionNo (ADR INC-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateInceptionNo (INC-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无立项时,编号为 INC-YYYY-0001")
        void firstOfYear() {
            when(inceptionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(inceptionMapper.insertInception(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertInception(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getInceptionNo()).isEqualTo(String.format("INC-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 7 个时,下一个编号为 0008")
        void nextSequence() {
            when(inceptionMapper.selectMaxSeqOfYear(anyString())).thenReturn(7);
            when(inceptionMapper.insertInception(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertInception(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getInceptionNo()).isEqualTo(String.format("INC-%d-0008", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(inceptionMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(inceptionMapper.insertInception(any()))
                .thenThrow(new DuplicateKeyException("uk_inception_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertInception(sample);
                assertThat(rows).isEqualTo(1);
                verify(inceptionMapper, times(2)).insertInception(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 inceptionNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setInceptionNo("INC-CUSTOM-2099");
            when(inceptionMapper.insertInception(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertInception(sample);
            }
            assertThat(sample.getInceptionNo()).isEqualTo("INC-CUSTOM-2099");
            verify(inceptionMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §F1.1)")
    class ValidationTests {

        @Test
        @DisplayName("projectName 必填,空抛 602")
        void projectNameRequired() {
            sample.setProjectName(null);
            assertThatThrownBy(() -> service.insertInception(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("项目名称");
        }

        @Test
        @DisplayName("submitterUserId 必填,空抛 602")
        void submitterRequired() {
            sample.setSubmitterUserId(null);
            assertThatThrownBy(() -> service.insertInception(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("提交人");
        }

        @Test
        @DisplayName("businessLine 非白名单 → 604")
        void businessLineOutOfWhitelist() {
            sample.setBusinessLine("invalid_line");
            assertThatThrownBy(() -> service.insertInception(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("业务线");
        }

        @Test
        @DisplayName("inceptionType 非白名单 → 604")
        void inceptionTypeOutOfWhitelist() {
            sample.setInceptionType("invalid_type");
            assertThatThrownBy(() -> service.insertInception(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("项目类型");
        }

        @Test
        @DisplayName("新建立项 status 非 00 时拒绝 → 601")
        void newInceptionMustBeDraft() {
            sample.setStatus("01");
            assertThatThrownBy(() -> service.insertInception(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("businessLine 全 4 个合法值都接受")
        void allBusinessLinesAccepted() {
            for (String biz : new String[] { "plant_protection", "precision_farming", "agri_supply", "traceability" }) {
                Inception inc = new Inception();
                inc.setProjectName("p-" + biz);
                inc.setSubmitterUserId(1L);
                inc.setBusinessLine(biz);
                inc.setInceptionType("new_product");
                when(inceptionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
                when(inceptionMapper.insertInception(any())).thenReturn(1);
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.insertInception(inc);
                }
                assertThat(inc.getInceptionId()).isNull();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertInception)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 aiGenerated 默认 N")
        void defaultAiGeneratedN() {
            when(inceptionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(inceptionMapper.insertInception(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertInception(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 status 默认 00 草稿")
        void defaultStatusDraft() {
            when(inceptionMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(inceptionMapper.insertInception(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertInception(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5×5 状态机 (含反向边 04→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 5×5 (PRD §F1.1)")
    class StateMachineTests {

        private Inception withStatus(String s) {
            Inception inc = new Inception();
            inc.setInceptionId(99L);
            inc.setStatus(s);
            return inc;
        }

        @Test
        @DisplayName("00 草稿 → 01 已提交 合法")
        void draftToSubmitted() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("00"));
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateInception(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 已提交 → 02 审批中 合法")
        void submittedToReviewing() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("01"));
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateInception(upd);
            }
        }

        @Test
        @DisplayName("02 审批中 → 03 已批准 合法 且自动填 approvedAt")
        void reviewingToApprovedFillsApprovedAt() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("02"));
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateInception(upd);
            }
            assertThat(upd.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("02 → 03 已批准时,如已有 approvedAt 不再覆盖")
        void approvedAtNotOverwritten() {
            Inception old = withStatus("02");
            Date earlier = new Date(System.currentTimeMillis() - 100_000);
            old.setApprovedAt(earlier);
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(old);
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateInception(upd);
            }
            // 没有触发覆盖逻辑,upd.approvedAt 仍为 null(old 的不动)
            assertThat(upd.getApprovedAt()).isNull();
        }

        @Test
        @DisplayName("02 审批中 → 04 已驳回 合法 (且必须填 rejectReason)")
        void reviewingToRejectedRequiresReason() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("02"));
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("04");
            upd.setRejectReason("预算不足");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateInception(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("→ 04 已驳回 缺 rejectReason → 602")
        void rejectedWithoutReasonFails() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("02"));
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("04");
            assertThatThrownBy(() -> service.updateInception(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("驳回");
        }

        @Test
        @DisplayName("04 已驳回 → 00 草稿 合法 (反向边,打回重写)")
        void rejectedReverseToDraft() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("04"));
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateInception(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("03 已批准 → 任何状态 非法 (终态保护)")
        void approvedIsTerminal() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("03"));
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateInception(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 03 已批准 非法 (必经审批流)")
        void draftCannotJumpToApproved() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("00"));
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("03");
            assertThatThrownBy(() -> service.updateInception(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("01 已提交 → 03 已批准 非法 (必经审批中)")
        void submittedCannotJumpToApproved() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("01"));
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("03");
            assertThatThrownBy(() -> service.updateInception(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(inceptionMapper.selectInceptionById(99L)).thenReturn(withStatus("03"));
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            Inception upd = new Inception();
            upd.setInceptionId(99L);
            upd.setStatus("03");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateInception(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时立项不存在抛 404")
        void updateNotFound() {
            when(inceptionMapper.selectInceptionById(404L)).thenReturn(null);
            Inception upd = new Inception();
            upd.setInceptionId(404L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateInception(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("立项单不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F1.1 AI 立项)")
    class AiGenerateTests {

        @Test
        @DisplayName("正常生成 → aiGenerated=Y / 内容 / 风险 / 时间填充")
        void normalAiGenerate() {
            Inception inc = new Inception();
            inc.setInceptionId(50L);
            inc.setProjectName("精准灌溉项目");
            inc.setBusinessLine("precision_farming");
            inc.setInceptionType("new_product");
            inc.setEstimatedDurationMonths(8);
            inc.setEstimatedTeam("产品×1 后端×2");
            when(inceptionMapper.selectInceptionById(50L)).thenReturn(inc);
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            lenient().when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Inception result = service.aiGenerate(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getAiProposalContent()).contains("立项建议书").contains("精准灌溉项目");
                assertThat(result.getAiRisks()).isNotBlank();
                assertThat(result.getAiGeneratedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("aiGenerate 时立项不存在抛 404")
        void aiGenerateNotFound() {
            when(inceptionMapper.selectInceptionById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("立项单不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            Inception inc = new Inception();
            inc.setInceptionId(60L);
            inc.setProjectName("p");
            inc.setBusinessLine("plant_protection");
            inc.setInceptionType("iteration");
            when(inceptionMapper.selectInceptionById(60L)).thenReturn(inc);
            when(inceptionMapper.updateInception(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.aiGenerate(60L);
            }
            verify(aiService).chat(any(AiChatRequest.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // promoteToProject (Proposal 0028 P0-2 立项 → 项目主线贯通)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("promoteToProject (Proposal 0028 P0-2)")
    class PromoteToProject {

        private Inception approvedInception(Long projectIdField) {
            Inception inc = new Inception();
            inc.setInceptionId(50L);
            inc.setStatus("03");                            // 已批准
            inc.setProjectName("精准灌溉系统");
            inc.setBackground("基于土壤湿度的智能灌溉");
            inc.setSubmitterUserId(7L);
            inc.setInceptionType("new_product");
            inc.setProjectId(projectIdField);
            return inc;
        }

        @Test
        @DisplayName("testPromoteOk_审批通过的立项成功建项目 + 回填 projectId")
        void testPromoteOk() {
            when(inceptionMapper.selectInceptionById(50L)).thenReturn(approvedInception(null));
            when(projectService.insertProject(any())).thenAnswer(invocation -> {
                Project p = invocation.getArgument(0);
                p.setId(8888L);  // 模拟自动生成 id
                return 1;
            });
            when(inceptionMapper.updateInception(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Long newId = service.promoteToProject(50L);
                assertThat(newId).isEqualTo(8888L);
            }
            // 验证调用 insertProject 一次,字段映射正确
            verify(projectService).insertProject(any(Project.class));
            // 验证回填 inception.projectId
            verify(inceptionMapper).updateInception(any(Inception.class));
        }

        @Test
        @DisplayName("testPromoteIdempotent_已晋升过 → 直接返回旧 projectId 不重建")
        void testPromoteIdempotent() {
            Inception inc = approvedInception(7777L);
            when(inceptionMapper.selectInceptionById(50L)).thenReturn(inc);
            Project existing = new Project();
            existing.setId(7777L);
            when(projectService.selectProjectById(7777L)).thenReturn(existing);

            Long result = service.promoteToProject(50L);
            assertThat(result).isEqualTo(7777L);
            verify(projectService, never()).insertProject(any());
            verify(inceptionMapper, never()).updateInception(any());
        }

        @Test
        @DisplayName("testPromoteNotApproved_状态非已批准 → 601")
        void testPromoteNotApproved() {
            Inception inc = approvedInception(null);
            inc.setStatus("02");  // 审批中
            when(inceptionMapper.selectInceptionById(50L)).thenReturn(inc);

            assertThatThrownBy(() -> service.promoteToProject(50L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已批准");
            verify(projectService, never()).insertProject(any());
        }

        @Test
        @DisplayName("testPromoteNotFound_inception 不存在 → 404")
        void testPromoteNotFound() {
            when(inceptionMapper.selectInceptionById(404L)).thenReturn(null);

            assertThatThrownBy(() -> service.promoteToProject(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("立项单不存在");
        }

        @Test
        @DisplayName("testPromoteOldProjectGone_旧 projectId 指向已删项目 → 重新建")
        void testPromoteOldProjectGone() {
            // inception.projectId 不为空但 projectService 查不到 → 重新建
            Inception inc = approvedInception(6666L);
            when(inceptionMapper.selectInceptionById(50L)).thenReturn(inc);
            when(projectService.selectProjectById(6666L)).thenReturn(null);
            when(projectService.insertProject(any())).thenAnswer(invocation -> {
                Project p = invocation.getArgument(0);
                p.setId(9999L);
                return 1;
            });
            when(inceptionMapper.updateInception(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Long newId = service.promoteToProject(50L);
                assertThat(newId).isEqualTo(9999L);
            }
            verify(projectService).insertProject(any(Project.class));
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
            when(inceptionMapper.deleteInceptionByIds(any())).thenReturn(2);
            int rows = service.deleteInceptionByIds(new Long[] { 1L, 2L });
            assertThat(rows).isEqualTo(2);
        }
    }
}
