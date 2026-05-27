package cn.com.bosssfot.dv.plm.dbdesign.service.impl;

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
import cn.com.bosssfot.dv.plm.dbdesign.domain.DbDesign;
import cn.com.bosssfot.dv.plm.dbdesign.mapper.DbDesignMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * DbDesignServiceImpl 单元测试 — PRD §F3.2 + 原型 dbdesign.html
 *
 * 覆盖范围 (Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generateDbDesignNo: 格式 DB-YYYY-NNNN / 流水续号 / 撞号重试 / 用户传入不覆盖
 *   - 字段校验: title 必填 / projectId 必填 / authorUserId 必填 / 关联项目存在性 (702)
 *   - ENUM 白名单: dbEngine ∈ {mysql, postgresql, kingbase} (604)
 *   - 默认值: aiGenerated=N / status='00' / 新建非草稿拒绝 (601)
 *   - 4 状态机 (含反向边 01→00): 00→01 / 01→{00 反向, 02} / 02→03 / 03 终态
 *   - aiGenerate: 输出 ER 图 + 数据字典 + DDL + 规范检查 + AiService 调用一次
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class DbDesignServiceImplTest {

    @Mock
    private DbDesignMapper dbdesignMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private DbDesignServiceImpl service;

    private DbDesign sample;

    @BeforeEach
    void setUp() {
        sample = new DbDesign();
        sample.setTitle("PLM 业务库 ER 设计 v1");
        sample.setProjectId(10L);
        sample.setAuthorUserId(1L);
        sample.setDbEngine("mysql");
    }

    private Project mockProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateDbDesignNo (ADR DB-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateDbDesignNo (DB-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无 DB 设计时,编号为 DB-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dbdesignMapper.insertDbDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDbDesign(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getDbdesignNo()).isEqualTo(String.format("DB-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 7 条时,下一编号为 0008")
        void nextSequence() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(7);
            when(dbdesignMapper.insertDbDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDbDesign(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getDbdesignNo()).isEqualTo(String.format("DB-%d-0008", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(0, 1);
            when(dbdesignMapper.insertDbDesign(any()))
                .thenThrow(new DuplicateKeyException("uk_dbdesign_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertDbDesign(sample);
                assertThat(rows).isEqualTo(1);
                verify(dbdesignMapper, times(2)).insertDbDesign(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 dbdesignNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setDbdesignNo("DB-CUSTOM-2099");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.insertDbDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDbDesign(sample);
            }
            assertThat(sample.getDbdesignNo()).isEqualTo("DB-CUSTOM-2099");
            verify(dbdesignMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 字段校验 + 关联项目存在性
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("字段校验 (API §F3.2)")
    class ValidationTests {

        @Test
        @DisplayName("title 必填,空抛 602")
        void titleRequired() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertDbDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("设计标题");
        }

        @Test
        @DisplayName("projectId 必填,空抛 602")
        void projectIdRequired() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertDbDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("authorUserId 必填,空抛 602")
        void authorRequired() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertDbDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("DBA");
        }

        @Test
        @DisplayName("关联项目不存在 → 702")
        void projectNotFound() {
            when(projectMapper.selectProjectById(anyLong())).thenReturn(null);
            assertThatThrownBy(() -> service.insertDbDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("dbEngine 非白名单 → 604")
        void dbEngineOutOfWhitelist() {
            sample.setDbEngine("oracle");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertDbDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("DB 引擎");
        }

        @Test
        @DisplayName("dbEngine 全 3 个合法值都接受")
        void allEnginesAccepted() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dbdesignMapper.insertDbDesign(any())).thenReturn(1);
            for (String engine : new String[] { "mysql", "postgresql", "kingbase" }) {
                DbDesign d = new DbDesign();
                d.setTitle("t-" + engine);
                d.setProjectId(10L);
                d.setAuthorUserId(1L);
                d.setDbEngine(engine);
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.insertDbDesign(d);
                }
                assertThat(d.getDbEngine()).isEqualTo(engine);
            }
        }

        @Test
        @DisplayName("dbEngine 空值可接受(可选字段)")
        void dbEngineEmptyOk() {
            sample.setDbEngine(null);
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dbdesignMapper.insertDbDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertDbDesign(sample);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("新建 DB 设计 status 非 00 时拒绝 → 601")
        void newDbDesignMustBeDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            assertThatThrownBy(() -> service.insertDbDesign(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("默认值填充 (insertDbDesign)")
    class DefaultsTests {

        @Test
        @DisplayName("未指定 aiGenerated 默认 N")
        void defaultAiGeneratedN() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dbdesignMapper.insertDbDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDbDesign(sample);
            }
            assertThat(sample.getAiGenerated()).isEqualTo("N");
        }

        @Test
        @DisplayName("未指定 status 默认 00 草稿")
        void defaultStatusDraft() {
            when(projectMapper.selectProjectById(10L)).thenReturn(mockProject(10L));
            when(dbdesignMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(dbdesignMapper.insertDbDesign(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertDbDesign(sample);
            }
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4 状态机 (含反向边 01→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 4 态含反向边 (PRD §F3.2)")
    class StateMachineTests {

        private DbDesign withStatus(String s) {
            DbDesign d = new DbDesign();
            d.setDbdesignId(99L);
            d.setStatus(s);
            d.setProjectId(10L);
            return d;
        }

        @Test
        @DisplayName("00 草稿 → 01 评审中 合法")
        void draftToReview() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("00"));
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateDbDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 02 已确认 合法")
        void reviewToConfirmed() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("01"));
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateDbDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 评审中 → 00 草稿 反向打回合法 (dbdesign 关键反向边)")
        void reviewToDraftReverse() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("01"));
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateDbDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已确认 → 03 已废弃 合法")
        void confirmedToObsolete() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("02"));
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("03");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateDbDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("03 已废弃 → 任何状态 非法 (终态保护)")
        void obsoleteIsTerminal() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("03"));
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateDbDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 02 已确认 跳级非法")
        void draftCannotJumpToConfirmed() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("00"));
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("02");
            assertThatThrownBy(() -> service.updateDbDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("00 草稿 → 03 已废弃 跳级非法")
        void draftCannotJumpToObsolete() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("00"));
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("03");
            assertThatThrownBy(() -> service.updateDbDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("02 已确认 → 01 评审中 反向非法")
        void confirmedCannotReverseToReview() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("02"));
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateDbDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("03"));
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setStatus("03");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateDbDesign(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时 DB 设计不存在抛 404")
        void updateNotFound() {
            when(dbdesignMapper.selectDbDesignById(404L)).thenReturn(null);
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(404L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updateDbDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("DB 设计不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在抛 702")
        void updateProjectIdNotFound() {
            DbDesign old = withStatus("00");
            old.setProjectId(10L);
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(old);
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updateDbDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("update 时改 dbEngine 非白名单抛 604")
        void updateDbEngineOutOfWhitelist() {
            when(dbdesignMapper.selectDbDesignById(99L)).thenReturn(withStatus("00"));
            DbDesign upd = new DbDesign();
            upd.setDbdesignId(99L);
            upd.setDbEngine("oracle");
            assertThatThrownBy(() -> service.updateDbDesign(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("DB 引擎");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate (ER + 字典 + DDL + 规范检查)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F3.2 AI 自动生成 ER + DDL)")
    class AiGenerateTests {

        @Test
        @DisplayName("正常生成 → ER 图 + 数据字典 + DDL + 规范检查 + aiGenerated=Y / 时间填充")
        void normalAiGenerate() {
            DbDesign d = new DbDesign();
            d.setDbdesignId(50L);
            d.setTitle("PLM 业务库 ER 设计");
            d.setDbEngine("mysql");
            when(dbdesignMapper.selectDbDesignById(50L)).thenReturn(d);
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                DbDesign result = service.aiGenerate(50L);
                assertThat(result.getAiGenerated()).isEqualTo("Y");
                assertThat(result.getErDiagramContent())
                    .isNotBlank()
                    .contains("erDiagram")
                    .contains("PROJECT");
                assertThat(result.getDataDictionary())
                    .isNotBlank()
                    .contains("tb_project");
                assertThat(result.getDdlScript())
                    .isNotBlank()
                    .contains("CREATE TABLE")
                    .contains("mysql");
                assertThat(result.getNormalizationCheck())
                    .isNotBlank()
                    .contains("naming")
                    .contains("normalization");
                assertThat(result.getAiGeneratedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("aiGenerate 时 DB 设计不存在抛 404")
        void aiGenerateNotFound() {
            when(dbdesignMapper.selectDbDesignById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("DB 设计不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            DbDesign d = new DbDesign();
            d.setDbdesignId(60L);
            d.setTitle("OMS 库 ER");
            when(dbdesignMapper.selectDbDesignById(60L)).thenReturn(d);
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.aiGenerate(60L);
            }
            verify(aiService, times(1)).chat(any(AiChatRequest.class));
        }

        @Test
        @DisplayName("aiGenerate dbEngine 为空时回退 mysql")
        void aiGenerateDefaultEngineMysql() {
            DbDesign d = new DbDesign();
            d.setDbdesignId(70L);
            d.setTitle("默认引擎库");
            d.setDbEngine(null);
            when(dbdesignMapper.selectDbDesignById(70L)).thenReturn(d);
            when(dbdesignMapper.updateDbDesign(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                DbDesign result = service.aiGenerate(70L);
                assertThat(result.getDdlScript()).contains("-- mysql");
            }
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
            when(dbdesignMapper.deleteDbDesignByIds(any())).thenReturn(3);
            int rows = service.deleteDbDesignByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
        }
    }
}
