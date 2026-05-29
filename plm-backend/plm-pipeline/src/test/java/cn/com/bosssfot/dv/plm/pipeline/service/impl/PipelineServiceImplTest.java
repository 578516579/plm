package cn.com.bosssfot.dv.plm.pipeline.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.spi.ProjectScopedLookup;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;
import cn.com.bosssfot.dv.plm.pipeline.mapper.PipelineMapper;

/**
 * PipelineServiceImpl 单元测试 — DevOps 扩展 + 原型 pipeline.html
 *
 * 覆盖范围:
 *   - generatePipelineNo: PIPE-YYYY-NNNN / 续号 / 用户传入保留 / 撞号重试
 *   - 校验: pipelineName / repoName / authorUserId 必填 + cron 触发需 cronExpr
 *   - ENUM 白名单: cicdTool / triggerType / lastRunStatus (604)
 *   - 默认值: repoBranch=main / totalRuns=0 / successCount=0 / status=00
 *   - 2 状态机: 00↔01 (启用↔停用)
 *   - trigger: 不存在 404 / 停用 601 / 正常累加 totalRuns + 计算 successRate
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class PipelineServiceImplTest {

    @Mock
    private PipelineMapper pipelineMapper;

    /**
     * proposal 0028 P0-2A: SPI Map 由 @InjectMocks 按字段类型注入到 service。
     * 用 @Spy + new HashMap 保留真实 Map.get() 行为,测试内手动 put 期望的 lookup mock。
     */
    @Spy
    private Map<String, ProjectScopedLookup> projectScopedLookups = new HashMap<>();

    @InjectMocks
    private PipelineServiceImpl service;

    private Pipeline sample;

    @BeforeEach
    void setUp() {
        sample = new Pipeline();
        sample.setPipelineName("主干 CI 流水线");
        sample.setRepoName("plm-backend");
        sample.setAuthorUserId(10L);
        sample.setCicdTool("jenkins");
        sample.setTriggerType("push");
    }

    private Pipeline existing(String status) {
        Pipeline p = new Pipeline();
        p.setPipelineId(1L);
        p.setPipelineName("旧流水线");
        p.setStatus(status);
        p.setTotalRuns(5);
        p.setSuccessCount(4);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generatePipelineNo (PIPE-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generatePipelineNo (PIPE-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无流水线,编号为 PIPE-YYYY-0001")
        void firstOfYear() {
            when(pipelineMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(pipelineMapper.insertPipeline(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPipeline(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getPipelineNo()).isEqualTo(String.format("PIPE-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 11 个,下一个为 0012")
        void nextSequence() {
            when(pipelineMapper.selectMaxSeqOfYear(anyString())).thenReturn(11);
            when(pipelineMapper.insertPipeline(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPipeline(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getPipelineNo()).isEqualTo(String.format("PIPE-%d-0012", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(pipelineMapper.selectMaxSeqOfYear(anyString())).thenReturn(null, 1);
            when(pipelineMapper.insertPipeline(any()))
                .thenThrow(new DuplicateKeyException("uk_pipeline_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertPipeline(sample);
                assertThat(rows).isEqualTo(1);
                verify(pipelineMapper, times(2)).insertPipeline(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 pipelineNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setPipelineNo("PIPE-CUSTOM-2099");
            when(pipelineMapper.insertPipeline(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPipeline(sample);
            }
            assertThat(sample.getPipelineNo()).isEqualTo("PIPE-CUSTOM-2099");
            verify(pipelineMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 必填校验 + 默认值 + cron
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertPipeline — 校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("pipelineName 为空 → 602")
        void nameBlank() {
            sample.setPipelineName(null);
            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("流水线名称");
        }

        @Test
        @DisplayName("repoName 为空 → 602")
        void repoBlank() {
            sample.setRepoName("");
            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("代码仓库");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("创建者");
        }

        @Test
        @DisplayName("cron 触发但缺 cronExpr → 602")
        void cronWithoutExpr() {
            sample.setTriggerType("cron");
            sample.setCronExpr(null);
            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Cron");
        }

        @Test
        @DisplayName("默认 repoBranch=main / totalRuns=0 / successCount=0 / status=00")
        void defaultsApplied() {
            when(pipelineMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(pipelineMapper.insertPipeline(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertPipeline(sample);
            }
            assertThat(sample.getRepoBranch()).isEqualTo("main");
            assertThat(sample.getTotalRuns()).isZero();
            assertThat(sample.getSuccessCount()).isZero();
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ENUM 白名单
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ENUM 白名单 (cicdTool / triggerType / lastRunStatus)")
    class EnumValidationTests {

        @Test
        @DisplayName("cicdTool 非白名单 → 604")
        void cicdToolInvalid() {
            sample.setCicdTool("travis");
            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("CICD 工具");
        }

        @Test
        @DisplayName("triggerType 非白名单 → 604")
        void triggerTypeInvalid() {
            sample.setTriggerType("webhook");
            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("触发方式");
        }

        @Test
        @DisplayName("lastRunStatus 非白名单 → 604")
        void lastRunStatusInvalid() {
            sample.setLastRunStatus("aborted");
            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("执行结果");
        }

        @Test
        @DisplayName("ENUM 字段为空时不校验 (可选)")
        void enumsCanBeNull() {
            sample.setCicdTool(null);
            sample.setTriggerType(null);
            sample.setLastRunStatus(null);
            when(pipelineMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(pipelineMapper.insertPipeline(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.insertPipeline(sample)).isEqualTo(1);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2 状态机 (00↔01)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 2 态 (00 启用 ↔ 01 停用)")
    class StateMachineTests {

        @Test
        @DisplayName("00 启用 → 01 停用 合法")
        void legal_00_to_01() {
            when(pipelineMapper.selectPipelineById(1L)).thenReturn(existing("00"));
            when(pipelineMapper.updatePipeline(any())).thenReturn(1);
            Pipeline upd = new Pipeline();
            upd.setPipelineId(1L);
            upd.setStatus("01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updatePipeline(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 停用 → 00 启用 反向合法")
        void legal_01_to_00() {
            when(pipelineMapper.selectPipelineById(1L)).thenReturn(existing("01"));
            when(pipelineMapper.updatePipeline(any())).thenReturn(1);
            Pipeline upd = new Pipeline();
            upd.setPipelineId(1L);
            upd.setStatus("00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updatePipeline(upd)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(pipelineMapper.selectPipelineById(99L)).thenReturn(null);
            Pipeline upd = new Pipeline();
            upd.setPipelineId(99L);
            upd.setStatus("01");
            assertThatThrownBy(() -> service.updatePipeline(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("流水线不存在");
        }

        @Test
        @DisplayName("改 cicdTool 非白名单 → 604")
        void updateCicdToolInvalid() {
            when(pipelineMapper.selectPipelineById(1L)).thenReturn(existing("00"));
            Pipeline upd = new Pipeline();
            upd.setPipelineId(1L);
            upd.setCicdTool("circleci");
            assertThatThrownBy(() -> service.updatePipeline(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("CICD 工具");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // trigger (模拟执行)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("trigger (模拟执行 + 统计累加)")
    class TriggerTests {

        @Test
        @DisplayName("不存在 → 404")
        void notFound() {
            when(pipelineMapper.selectPipelineById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.trigger(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("流水线不存在");
        }

        @Test
        @DisplayName("已停用 (status=01) 触发 → 601")
        void stoppedCannotTrigger() {
            when(pipelineMapper.selectPipelineById(1L)).thenReturn(existing("01"));
            assertThatThrownBy(() -> service.trigger(1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已停用");
        }

        @Test
        @DisplayName("正常触发: totalRuns 累加 + lastRunStatus 落 + successRate 计算")
        void normalTriggerAccumulates() {
            Pipeline p = existing("00"); // totalRuns=5, successCount=4
            when(pipelineMapper.selectPipelineById(1L)).thenReturn(p);
            when(pipelineMapper.updatePipeline(any())).thenReturn(1);

            Pipeline result = service.trigger(1L);
            assertThat(result.getTotalRuns()).isEqualTo(6);
            assertThat(result.getLastRunStatus()).isIn("success", "failed");
            assertThat(result.getLastRunAt()).isNotNull();
            assertThat(result.getSuccessRate()).isNotNull();
            assertThat(result.getSuccessRate().doubleValue()).isBetween(0.0, 100.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // proposal 0028 P0-2A: 跨模块 FK 校验 (pipeline.releaseId 同项目)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("FkValidation — pipeline.releaseId 同项目校验")
    class FkValidation {

        @Test
        @DisplayName("testFkOk_目标存在且同 projectId 通过")
        void testFkOk() {
            sample.setProjectId(1L);
            sample.setReleaseId(55L);
            ProjectScopedLookup releaseLookup = Mockito.mock(ProjectScopedLookup.class);
            when(releaseLookup.resolveProjectId(55L)).thenReturn(1L);
            projectScopedLookups.put("release", releaseLookup);

            when(pipelineMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(pipelineMapper.insertPipeline(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.insertPipeline(sample)).isEqualTo(1);
            }
            verify(releaseLookup).resolveProjectId(55L);
        }

        @Test
        @DisplayName("testFkNullOk_releaseId 为 null 跳过校验")
        void testFkNullOk() {
            sample.setProjectId(1L);
            sample.setReleaseId(null);
            when(pipelineMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(pipelineMapper.insertPipeline(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.insertPipeline(sample)).isEqualTo(1);
            }
            assertThat(projectScopedLookups).doesNotContainKey("release");
        }

        @Test
        @DisplayName("testFkNotFound_lookup 返回 null → 702")
        void testFkNotFound() {
            sample.setProjectId(1L);
            sample.setReleaseId(999L);
            ProjectScopedLookup releaseLookup = Mockito.mock(ProjectScopedLookup.class);
            when(releaseLookup.resolveProjectId(999L)).thenReturn(null);
            projectScopedLookups.put("release", releaseLookup);

            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("发布单不存在");
            verify(pipelineMapper, never()).insertPipeline(any());
        }

        @Test
        @DisplayName("testFkDifferentProject_lookup 返回别的 projectId → 702")
        void testFkDifferentProject() {
            sample.setProjectId(1L);
            sample.setReleaseId(55L);
            ProjectScopedLookup releaseLookup = Mockito.mock(ProjectScopedLookup.class);
            when(releaseLookup.resolveProjectId(55L)).thenReturn(2L); // 不同项目
            projectScopedLookups.put("release", releaseLookup);

            assertThatThrownBy(() -> service.insertPipeline(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("同一项目");
            verify(pipelineMapper, never()).insertPipeline(any());
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
            when(pipelineMapper.deletePipelineByIds(any())).thenReturn(2);
            int rows = service.deletePipelineByIds(new Long[] { 1L, 2L });
            assertThat(rows).isEqualTo(2);
        }
    }
}
