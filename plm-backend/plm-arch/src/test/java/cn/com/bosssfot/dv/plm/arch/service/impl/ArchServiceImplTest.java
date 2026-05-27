package cn.com.bosssfot.dv.plm.arch.service.impl;

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

import cn.com.bosssfot.dv.plm.arch.domain.Arch;
import cn.com.bosssfot.dv.plm.arch.mapper.ArchMapper;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * ArchServiceImpl 单元测试 — PRD §F3.1 + 原型 archdesign.html
 *
 * 覆盖范围 (Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generateArchNo: 格式 ARCH-YYYY-NNNN / 流水续号 / 撞号重试
 *   - 字段校验: title 必填 / projectId 必填 / authorUserId 必填 / 关联项目存在性
 *   - ENUM 白名单 6 个: archMode / primaryStack / databaseChoice
 *                        aiOrchestration / deploymentType / iotProtocol (604)
 *   - 默认值填充: aiGenerated=N / status='00' / 新建非草稿拒绝
 *   - 4 状态机 (含反向边 01→00): 00→01 / 01→00 反向 / 01→02 / 02→03 /
 *                                03 终态 / 跳级非法 / 02→00 反向非法 / 02→01 反向非法
 *   - aiGenerate: 生成 designContent / c4DiagramContent (Mermaid C4) / nfrMapping
 *                 + aiGenerated=Y + AiService.chat 一次审计调用
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class ArchServiceImplTest {

    @Mock
    private ArchMapper archMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ArchServiceImpl service;

    private Arch sample;

    @BeforeEach
    void setUp() {
        sample = new Arch();
        sample.setTitle("AI 灌溉决策平台架构");
        sample.setProjectId(10L);
        sample.setAuthorUserId(1L);
        sample.setArchMode("microservice");
        sample.setPrimaryStack("java_sb3");
        sample.setDatabaseChoice("pg_redis");
        sample.setAiOrchestration("dify_deepseek");
        sample.setDeploymentType("k8s");
        sample.setIotProtocol("mqtt");
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateArchNo (ADR ARCH-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateArchNo (ARCH-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无架构方案时,编号为 ARCH-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(archMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(archMapper.insertArch(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertArch(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getArchNo()).isEqualTo(String.format("ARCH-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 7 个时,下一个编号为 0008")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(archMapper.selectMaxSeqOfYear(anyString())).thenReturn(7);
            when(archMapper.insertArch(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertArch(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getArchNo()).isEqualTo(String.format("ARCH-%d-0008", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(archMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(archMapper.insertArch(any()))
                .thenThrow(new DuplicateKeyException("uk_arch_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertArch(sample);
                assertThat(rows).isEqualTo(1);
                verify(archMapper, times(2)).insertArch(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 archNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setArchNo("ARCH-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(archMapper.insertArch(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertArch(sample);
            }
            assertThat(sample.getArchNo()).isEqualTo("ARCH-CUSTOM-2099");
            verify(archMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + 关联项目存在性 + ENUM 白名单
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 + ENUM 白名单 (API §F3.1)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("架构方案标题");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("authorUserId 必填,空抛 602")
        void authorRequired() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("架构师");
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(anyLong())).thenReturn(null);
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("archMode 非白名单 → 604")
        void archModeOutOfWhitelist() {
            sample.setArchMode("mesh_xyz");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("架构模式");
        }

        @Test
        @DisplayName("primaryStack 非白名单 → 604")
        void primaryStackOutOfWhitelist() {
            sample.setPrimaryStack("rust_actix");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("技术栈");
        }

        @Test
        @DisplayName("databaseChoice 非白名单 → 604")
        void databaseChoiceOutOfWhitelist() {
            sample.setDatabaseChoice("oracle_19c");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("数据库选型");
        }

        @Test
        @DisplayName("aiOrchestration 非白名单 → 604")
        void aiOrchestrationOutOfWhitelist() {
            sample.setAiOrchestration("openai_assistants");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("AI 编排");
        }

        @Test
        @DisplayName("deploymentType 非白名单 → 604")
        void deploymentTypeOutOfWhitelist() {
            sample.setDeploymentType("nomad");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("部署方式");
        }

        @Test
        @DisplayName("iotProtocol 非白名单 → 604")
        void iotProtocolOutOfWhitelist() {
            sample.setIotProtocol("coap");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("IoT 协议");
        }

        @Test
        @DisplayName("6 ENUM 字段为空时不校验 (可选字段)")
        void allEnumsCanBeEmpty() {
            sample.setArchMode(null);
            sample.setPrimaryStack(null);
            sample.setDatabaseChoice(null);
            sample.setAiOrchestration(null);
            sample.setDeploymentType(null);
            sample.setIotProtocol(null);
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(archMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(archMapper.insertArch(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertArch(sample);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("新建架构 status 非 00 时拒绝 → 601")
        void newArchMustBeDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertArch(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertArch)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 aiGenerated 默认 N")
        void defaultAiGeneratedN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(archMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(archMapper.insertArch(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertArch(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 status 默认 00 草稿")
        void defaultStatusDraft() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(archMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(archMapper.insertArch(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertArch(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4 状态机 (含反向边 01→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 4 态含反向边 (PRD §F3.1)")
    class StateMachineTests {

        private Arch withStatus(String s) {
            Arch a = new Arch();
            a.setArchId(99L);
            a.setStatus(s);
            a.setProjectId(10L);
            return a;
        }

        @Test
        @DisplayName("00 草稿 → 01 评审中 合法")
        void draftToReview() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("00"));
            when(archMapper.updateArch(any())).thenReturn(1);
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateArch(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 00 草稿 反向边合法 (打回)")
        void reviewToDraftReverseAllowed() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("01"));
            when(archMapper.updateArch(any())).thenReturn(1);
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateArch(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 02 已确认 合法")
        void reviewToConfirmed() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("01"));
            when(archMapper.updateArch(any())).thenReturn(1);
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateArch(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已确认 → 03 已废弃 合法")
        void confirmedToDeprecated() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("02"));
            when(archMapper.updateArch(any())).thenReturn(1);
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateArch(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("03 已废弃 → 任何状态 非法 (终态保护)")
        void deprecatedIsTerminal() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("03"));
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 02 已确认 跳级非法")
        void draftCannotJumpToConfirmed() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("00"));
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 03 已废弃 跳级非法")
        void draftCannotJumpToDeprecated() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("00"));
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("03");
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("02 已确认 → 00 草稿 反向非法 (不可回退到草稿)")
        void confirmedCannotReverseToDraft() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("02"));
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("00");
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("02 已确认 → 01 评审中 反向非法 (不可回退到评审)")
        void confirmedCannotReverseToReview() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("02"));
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("03"));
            when(archMapper.updateArch(any())).thenReturn(1);
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setStatus("03");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateArch(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时架构方案不存在抛 404")
        void updateNotFound() {
            when(archMapper.selectArchById(404L)).thenReturn(null);
            Arch upd = new Arch();
            upd.setArchId(404L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("架构方案不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在抛 702")
        void updateProjectIdNotFound() {
            Arch old = withStatus("00");
            old.setProjectId(10L);
            when(archMapper.selectArchById(99L)).thenReturn(old);
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("update 时改 archMode 非白名单抛 604")
        void updateArchModeOutOfWhitelist() {
            when(archMapper.selectArchById(99L)).thenReturn(withStatus("00"));
            Arch upd = new Arch();
            upd.setArchId(99L);
            upd.setArchMode("invalid");
            assertThatThrownBy(() -> service.updateArch(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("架构模式");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate (生成 design / C4 Mermaid / NFR)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F3.1 AI HLD)")
    class AiGenerateTests {

        @Test
        @DisplayName("正常生成 → designContent / c4DiagramContent (Mermaid C4) / nfrMapping + aiGenerated=Y")
        void normalAiGenerate() {
            Arch a = new Arch();
            a.setArchId(50L);
            a.setTitle("AI 灌溉决策平台架构");
            a.setArchMode("microservice");
            a.setPrimaryStack("java_sb3");
            a.setDatabaseChoice("pg_redis");
            a.setAiOrchestration("dify_deepseek");
            a.setDeploymentType("k8s");
            a.setIotProtocol("mqtt");
            when(archMapper.selectArchById(50L)).thenReturn(a);
            when(archMapper.updateArch(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                Arch result = service.aiGenerate(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getDesignContent())
                    .isNotBlank()
                    .contains("AI 灌溉决策平台架构")
                    .contains("架构模式")
                    .contains("技术选型");
                assertThat(result.getC4DiagramContent())
                    .isNotBlank()
                    .contains("C4Container")
                    .contains("System_Boundary(plm")
                    .contains("Rel(user, web");
                assertThat(result.getNfrMapping())
                    .isNotBlank()
                    .contains("性能")
                    .contains("可靠性")
                    .contains("安全");
                assertThat(result.getAiGeneratedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("aiGenerate 时架构方案不存在抛 404")
        void aiGenerateNotFound() {
            when(archMapper.selectArchById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("架构方案不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            Arch a = new Arch();
            a.setArchId(60L);
            a.setTitle("测试架构");
            when(archMapper.selectArchById(60L)).thenReturn(a);
            when(archMapper.updateArch(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.aiGenerate(60L);
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
            when(archMapper.deleteArchByIds(any())).thenReturn(3);
            int rows = service.deleteArchByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
        }
    }
}
