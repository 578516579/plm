package cn.com.bosssfot.dv.plm.manualimpl.service.impl;

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
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.manualimpl.domain.ManualImpl;
import cn.com.bosssfot.dv.plm.manualimpl.mapper.ManualImplMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * ManualImplServiceImpl 单元测试 — PRD §F5.2 + 原型 implmanual.html
 *
 * 覆盖范围 (Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generateManualimplNo: 格式 IM-YYYY-NNNN / 流水续号 / 撞号重试 / 用户传入保留
 *   - 字段校验: title 必填 / projectId 必填 / authorUserId 必填 / 关联项目存在性 / 新建非草稿拒
 *   - ENUM 白名单: deployMode / osType / dbType (604) + 可空
 *   - 默认值: outputFormats='pdf' / aiGenerated='N' / status='00'
 *   - 4 状态机 (含反向边 02→00): 00→01 / 01→02 / 02→{00,03} / 03 终态 / 跳级非法 / 进 02 填 generatedAt
 *   - aiGenerate: 生成 content + status=02 + aiGenerated=Y + generatedAt + AiService.chat 一次审计调用
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class ManualImplServiceImplTest {

    @Mock
    private ManualImplMapper manualimplMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ManualImplServiceImpl service;

    private ManualImpl sample;

    @BeforeEach
    void setUp() {
        sample = new ManualImpl();
        sample.setTitle("AgriPLM 实施部署手册");
        sample.setProjectId(1L);
        sample.setAuthorUserId(10L);
        sample.setDeployMode("docker_compose");
        sample.setOsType("centos7");
        sample.setDbType("postgresql14");
    }

    private Project existingProject() {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    private ManualImpl existing(String status) {
        ManualImpl m = new ManualImpl();
        m.setManualimplId(1L);
        m.setTitle("旧实施手册");
        m.setStatus(status);
        m.setProjectId(1L);
        return m;
    }

    private ManualImpl updateTo(Long id, String newStatus) {
        ManualImpl m = new ManualImpl();
        m.setManualimplId(id);
        m.setStatus(newStatus);
        return m;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateManualimplNo (IM-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateManualimplNo (IM-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无手册,编号为 IM-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualimplMapper.insertManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualImpl(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getManualimplNo()).isEqualTo(String.format("IM-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 4 个手册,下一个为 0005")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.selectMaxSeqOfYear(anyString())).thenReturn(4);
            when(manualimplMapper.insertManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualImpl(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getManualimplNo()).isEqualTo(String.format("IM-%d-0005", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.selectMaxSeqOfYear(anyString())).thenReturn(null, 1);
            when(manualimplMapper.insertManualImpl(any()))
                .thenThrow(new DuplicateKeyException("uk_manualimpl_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertManualImpl(sample);
                assertThat(rows).isEqualTo(1);
                verify(manualimplMapper, times(2)).insertManualImpl(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 manualimplNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setManualimplNo("IM-CUSTOM-2099");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.insertManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualImpl(sample);
            }
            assertThat(sample.getManualimplNo()).isEqualTo("IM-CUSTOM-2099");
            verify(manualimplMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertManualImpl — 必填校验 + 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertManualImpl — 必填校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("标题");
        }

        @Test
        @DisplayName("projectId 为空 → 602")
        void projectIdNull() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("作者");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态 00 合法")
        void initialStatus00Valid() {
            sample.setStatus("00");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualimplMapper.insertManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualImpl(sample);
            }
            verify(manualimplMapper).insertManualImpl(any());
        }

        @Test
        @DisplayName("新建状态 01 (生成中) 也合法")
        void initialStatus01Valid() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualimplMapper.insertManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualImpl(sample);
            }
            verify(manualimplMapper).insertManualImpl(any());
        }

        @Test
        @DisplayName("新建状态 02 非法 → 601")
        void initialStatus02Invalid() {
            sample.setStatus("02");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("默认 outputFormats='pdf' / aiGenerated='N' 被填充")
        void defaultsApplied() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualimplMapper.insertManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualImpl(sample);
            }
            assertThat(sample.getOutputFormats()).isEqualTo("pdf");
            assertThat(sample.getAiGenerated()).isEqualTo("N");
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ENUM 白名单 (deployMode / osType / dbType → 604)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ENUM 白名单 (deployMode / osType / dbType)")
    class EnumValidationTests {

        @Test
        @DisplayName("deployMode 非白名单 → 604")
        void deployModeOutOfWhitelist() {
            sample.setDeployMode("nomad_swarm");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("部署模式");
        }

        @Test
        @DisplayName("osType 非白名单 → 604")
        void osTypeOutOfWhitelist() {
            sample.setOsType("windows2019");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("操作系统");
        }

        @Test
        @DisplayName("dbType 非白名单 → 604")
        void dbTypeOutOfWhitelist() {
            sample.setDbType("oracle19c");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualImpl(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("数据库");
        }

        @Test
        @DisplayName("3 个 ENUM 字段为空时不校验 (可选字段)")
        void enumsCanBeNull() {
            sample.setDeployMode(null);
            sample.setOsType(null);
            sample.setDbType(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualimplMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualimplMapper.insertManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertManualImpl(sample);
                assertThat(rows).isEqualTo(1);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4 状态机 (含反向边 02→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 4 态含反向边 02→00 (PRD §F5.2)")
    class StateMachineTests {

        @Test
        @DisplayName("00 草稿 → 01 生成中 合法")
        void legal_00_to_01() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("00"));
            when(manualimplMapper.updateManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualImpl(updateTo(1L, "01"));
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 生成中 → 02 已生成,自动填 generatedAt")
        void enter02FillsGeneratedAt() {
            ManualImpl old = existing("01");
            old.setGeneratedAt(null);
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(old);
            when(manualimplMapper.updateManualImpl(any())).thenReturn(1);
            ManualImpl upd = updateTo(1L, "02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateManualImpl(upd);
            }
            assertThat(upd.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("反向边 02 已生成 → 00 草稿 (重做) 合法")
        void reverse_02_to_00() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("02"));
            when(manualimplMapper.updateManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualImpl(updateTo(1L, "00"));
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已生成 → 03 已发布 合法")
        void legal_02_to_03() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("02"));
            when(manualimplMapper.updateManualImpl(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualImpl(updateTo(1L, "03"));
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("终态 03 已发布 → 任意 非法")
        void terminal_03_immutable() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("03"));
            for (String to : new String[] { "00", "01", "02" }) {
                assertThatThrownBy(() -> service.updateManualImpl(updateTo(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("不能直接转");
            }
        }

        @Test
        @DisplayName("非法跳级 00 → 02")
        void illegal_00_to_02() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("00"));
            assertThatThrownBy(() -> service.updateManualImpl(updateTo(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("02"));
            when(manualimplMapper.updateManualImpl(any())).thenReturn(1);
            ManualImpl upd = updateTo(1L, "02");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualImpl(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时实施手册不存在 → 404")
        void updateNotFound() {
            when(manualimplMapper.selectManualImplById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateManualImpl(updateTo(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("实施手册不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在 → 702")
        void updateProjectIdNotFound() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("00"));
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            ManualImpl upd = new ManualImpl();
            upd.setManualimplId(1L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updateManualImpl(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("update 时改 deployMode 非白名单 → 604")
        void updateDeployModeInvalid() {
            when(manualimplMapper.selectManualImplById(1L)).thenReturn(existing("00"));
            ManualImpl upd = new ManualImpl();
            upd.setManualimplId(1L);
            upd.setDeployMode("invalid_mode");
            assertThatThrownBy(() -> service.updateManualImpl(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("部署模式");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate (生成 content + status 02 + AiService 审计)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F5.2 AI 一键生成)")
    class AiGenerateTests {

        @Test
        @DisplayName("正常生成 → content 非空 + status=02 + aiGenerated=Y + generatedAt")
        void normalAiGenerate() {
            ManualImpl a = new ManualImpl();
            a.setManualimplId(50L);
            a.setTitle("AgriPLM 实施部署手册");
            a.setDeployMode("kubernetes");
            a.setOsType("kylin");
            a.setDbType("kdb");
            when(manualimplMapper.selectManualImplById(50L)).thenReturn(a);
            when(manualimplMapper.updateManualImpl(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            ManualImpl result = service.aiGenerate(50L);
            assertThat(result.getContent())
                .isNotBlank()
                .contains("AgriPLM 实施部署手册")
                .contains("部署步骤");
            assertThat(result.getStatus()).isEqualTo("02");
            assertThat(result.getAiGenerated()).isEqualTo("Y");
            assertThat(result.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("aiGenerate 时实施手册不存在 → 404")
        void aiGenerateNotFound() {
            when(manualimplMapper.selectManualImplById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("实施手册不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            ManualImpl a = new ManualImpl();
            a.setManualimplId(60L);
            a.setTitle("测试实施手册");
            when(manualimplMapper.selectManualImplById(60L)).thenReturn(a);
            when(manualimplMapper.updateManualImpl(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            service.aiGenerate(60L);
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
            when(manualimplMapper.deleteManualImplByIds(any())).thenReturn(3);
            int rows = service.deleteManualImplByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
        }
    }
}
