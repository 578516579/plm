package cn.com.bosssfot.dv.plm.manualops.service.impl;

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
import cn.com.bosssfot.dv.plm.manualops.domain.ManualOps;
import cn.com.bosssfot.dv.plm.manualops.mapper.ManualOpsMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * ManualOpsServiceImpl 单元测试 — PRD §F5.3 + 原型 opsmanual.html
 *
 * 覆盖范围 (Phase 03 Gate B.4 关键路径 + §M.2 9 项 DoD):
 *   - generateManualopsNo: 格式 OM-YYYY-NNNN / 流水续号 / 撞号重试 / 用户传入保留
 *   - 字段校验: title / projectId / authorUserId 必填 / 关联项目存在性 / 新建非草稿拒
 *   - ENUM 白名单: monitoringPlan (单选) + alertChannels / iotDeviceTypes (CSV 逐项, 604)
 *   - 默认值: outputFormats='pdf' / aiGenerated='N' / status='00'
 *   - 4 状态机 (含反向边 02→00): 00→01 / 01→02 / 02→{00,03} / 03 终态 / 跳级非法 / 进 02 填 generatedAt
 *   - aiGenerate: content + status=02 + aiGenerated=Y + generatedAt + AiService.chat 一次审计
 *   - 删除: 转发 mapper
 */
@ExtendWith(MockitoExtension.class)
class ManualOpsServiceImplTest {

    @Mock
    private ManualOpsMapper manualopsMapper;

    @Mock
    private AiService aiService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ManualOpsServiceImpl service;

    private ManualOps sample;

    @BeforeEach
    void setUp() {
        sample = new ManualOps();
        sample.setTitle("AgriPLM 运维监控手册");
        sample.setProjectId(1L);
        sample.setAuthorUserId(10L);
        sample.setMonitoringPlan("prometheus_grafana");
        sample.setAlertChannels("dingtalk,email");
        sample.setIotDeviceTypes("soil_sensor,weather_station");
    }

    private Project existingProject() {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("农业病虫害智能识别系统");
        return p;
    }

    private ManualOps existing(String status) {
        ManualOps m = new ManualOps();
        m.setManualopsId(1L);
        m.setTitle("旧运维手册");
        m.setStatus(status);
        m.setProjectId(1L);
        return m;
    }

    private ManualOps updateTo(Long id, String newStatus) {
        ManualOps m = new ManualOps();
        m.setManualopsId(id);
        m.setStatus(newStatus);
        return m;
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateManualopsNo (OM-YYYY-NNNN)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateManualopsNo (OM-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无手册,编号为 OM-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualOps(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getManualopsNo()).isEqualTo(String.format("OM-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 4 个手册,下一个为 0005")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(4);
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualOps(sample);
            }
            int year = LocalDate.now().getYear();
            assertThat(sample.getManualopsNo()).isEqualTo(String.format("OM-%d-0005", year));
        }

        @Test
        @DisplayName("撞号重试: DuplicateKeyException 后用新编号成功")
        void retryOnDuplicate() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null, 1);
            when(manualopsMapper.insertManualOps(any()))
                .thenThrow(new DuplicateKeyException("uk_manualops_no"))
                .thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertManualOps(sample);
                assertThat(rows).isEqualTo(1);
                verify(manualopsMapper, times(2)).insertManualOps(any());
            }
        }

        @Test
        @DisplayName("用户显式传入 manualopsNo 时不自动生成")
        void userProvidedNoIsKept() {
            sample.setManualopsNo("OM-CUSTOM-2099");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualOps(sample);
            }
            assertThat(sample.getManualopsNo()).isEqualTo("OM-CUSTOM-2099");
            verify(manualopsMapper, never()).selectMaxSeqOfYear(anyString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertManualOps — 必填校验 + 默认值
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertManualOps — 必填校验 + 默认值")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("标题");
        }

        @Test
        @DisplayName("projectId 为空 → 602")
        void projectIdNull() {
            sample.setProjectId(null);
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("作者");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态 00 合法")
        void initialStatus00Valid() {
            sample.setStatus("00");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualOps(sample);
            }
            verify(manualopsMapper).insertManualOps(any());
        }

        @Test
        @DisplayName("新建状态 01 (生成中) 也合法")
        void initialStatus01Valid() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualOps(sample);
            }
            verify(manualopsMapper).insertManualOps(any());
        }

        @Test
        @DisplayName("新建状态 02 非法 → 601")
        void initialStatus02Invalid() {
            sample.setStatus("02");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("默认 outputFormats='pdf' / aiGenerated='N' / status='00' 被填充")
        void defaultsApplied() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualOps(sample);
            }
            assertThat(sample.getOutputFormats()).isEqualTo("pdf");
            assertThat(sample.getAiGenerated()).isEqualTo("N");
            assertThat(sample.getStatus()).isEqualTo("00");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ENUM 白名单 (monitoringPlan 单选 + alertChannels / iotDeviceTypes CSV)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ENUM 白名单 (monitoringPlan / alertChannels CSV / iotDeviceTypes CSV)")
    class EnumValidationTests {

        @Test
        @DisplayName("monitoringPlan 非白名单 → 604")
        void monitoringPlanOutOfWhitelist() {
            sample.setMonitoringPlan("splunk");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("监控方案");
        }

        @Test
        @DisplayName("alertChannels CSV 含非白名单项 → 604")
        void alertChannelsCsvInvalid() {
            sample.setAlertChannels("dingtalk,sms_unsupported");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("告警渠道");
        }

        @Test
        @DisplayName("iotDeviceTypes CSV 含非白名单项 → 604")
        void iotDeviceTypesCsvInvalid() {
            sample.setIotDeviceTypes("soil_sensor,satellite");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualOps(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("IoT 设备类型");
        }

        @Test
        @DisplayName("CSV 多项全合法时通过")
        void csvAllValidPasses() {
            sample.setAlertChannels("dingtalk,feishu,wework,email");
            sample.setIotDeviceTypes("soil_sensor,weather_station,drone,irrigation_controller");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertManualOps(sample);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("3 个 ENUM 字段为空时不校验 (可选字段)")
        void enumsCanBeNull() {
            sample.setMonitoringPlan(null);
            sample.setAlertChannels(null);
            sample.setIotDeviceTypes(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualopsMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualopsMapper.insertManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertManualOps(sample);
                assertThat(rows).isEqualTo(1);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4 状态机 (含反向边 02→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 4 态含反向边 02→00 (PRD §F5.3)")
    class StateMachineTests {

        @Test
        @DisplayName("00 草稿 → 01 生成中 合法")
        void legal_00_to_01() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("00"));
            when(manualopsMapper.updateManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualOps(updateTo(1L, "01"));
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("01 生成中 → 02 已生成,自动填 generatedAt")
        void enter02FillsGeneratedAt() {
            ManualOps old = existing("01");
            old.setGeneratedAt(null);
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(old);
            when(manualopsMapper.updateManualOps(any())).thenReturn(1);
            ManualOps upd = updateTo(1L, "02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateManualOps(upd);
            }
            assertThat(upd.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("反向边 02 已生成 → 00 草稿 (重做) 合法")
        void reverse_02_to_00() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("02"));
            when(manualopsMapper.updateManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualOps(updateTo(1L, "00"));
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("02 已生成 → 03 已发布 合法")
        void legal_02_to_03() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("02"));
            when(manualopsMapper.updateManualOps(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualOps(updateTo(1L, "03"));
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("终态 03 已发布 → 任意 非法")
        void terminal_03_immutable() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("03"));
            for (String to : new String[] { "00", "01", "02" }) {
                assertThatThrownBy(() -> service.updateManualOps(updateTo(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("不能直接转");
            }
        }

        @Test
        @DisplayName("非法跳级 00 → 02")
        void illegal_00_to_02() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("00"));
            assertThatThrownBy(() -> service.updateManualOps(updateTo(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能直接转");
        }

        @Test
        @DisplayName("status 不变时不校验状态机,只走字段更新")
        void noStatusChangeBypassesStateCheck() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("02"));
            when(manualopsMapper.updateManualOps(any())).thenReturn(1);
            ManualOps upd = updateTo(1L, "02");
            upd.setRemark("补充备注");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.updateManualOps(upd);
                assertThat(rows).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("update 时运维手册不存在 → 404")
        void updateNotFound() {
            when(manualopsMapper.selectManualOpsById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateManualOps(updateTo(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("运维手册不存在");
        }

        @Test
        @DisplayName("update 时改 projectId,新 projectId 不存在 → 702")
        void updateProjectIdNotFound() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("00"));
            when(projectMapper.selectProjectById(999L)).thenReturn(null);
            ManualOps upd = new ManualOps();
            upd.setManualopsId(1L);
            upd.setProjectId(999L);
            assertThatThrownBy(() -> service.updateManualOps(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("update 时改 monitoringPlan 非白名单 → 604")
        void updateMonitoringPlanInvalid() {
            when(manualopsMapper.selectManualOpsById(1L)).thenReturn(existing("00"));
            ManualOps upd = new ManualOps();
            upd.setManualopsId(1L);
            upd.setMonitoringPlan("invalid_monitor");
            assertThatThrownBy(() -> service.updateManualOps(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("监控方案");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // aiGenerate
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("aiGenerate (PRD §F5.3 AI 一键生成)")
    class AiGenerateTests {

        @Test
        @DisplayName("正常生成 → content 非空 + status=02 + aiGenerated=Y + generatedAt")
        void normalAiGenerate() {
            ManualOps a = new ManualOps();
            a.setManualopsId(50L);
            a.setTitle("AgriPLM 运维监控手册");
            a.setMonitoringPlan("zabbix");
            a.setAlertChannels("feishu");
            a.setIotDeviceTypes("drone");
            when(manualopsMapper.selectManualOpsById(50L)).thenReturn(a);
            when(manualopsMapper.updateManualOps(any())).thenReturn(1);
            when(aiService.chat(any(AiChatRequest.class)))
                .thenReturn(AiChatResult.ok("mock", "mock-1", "ok"));

            ManualOps result = service.aiGenerate(50L);
            assertThat(result.getContent())
                .isNotBlank()
                .contains("AgriPLM 运维监控手册")
                .contains("监控方案");
            assertThat(result.getStatus()).isEqualTo("02");
            assertThat(result.getAiGenerated()).isEqualTo("Y");
            assertThat(result.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("aiGenerate 时运维手册不存在 → 404")
        void aiGenerateNotFound() {
            when(manualopsMapper.selectManualOpsById(404L)).thenReturn(null);
            assertThatThrownBy(() -> service.aiGenerate(404L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("运维手册不存在");
        }

        @Test
        @DisplayName("aiGenerate 调用 AiService.chat 一次 (审计联动)")
        void aiServiceCalledOnce() {
            ManualOps a = new ManualOps();
            a.setManualopsId(60L);
            a.setTitle("测试运维手册");
            when(manualopsMapper.selectManualOpsById(60L)).thenReturn(a);
            when(manualopsMapper.updateManualOps(any())).thenReturn(1);
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
            when(manualopsMapper.deleteManualOpsByIds(any())).thenReturn(3);
            int rows = service.deleteManualOpsByIds(new Long[] { 1L, 2L, 3L });
            assertThat(rows).isEqualTo(3);
        }
    }
}
