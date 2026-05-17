package cn.com.bosssfot.dv.plm.project.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * "轻"集成测试 — Service ↔ Mapper 协作验证 (Mockito 模拟 Mapper)
 *
 * v2 PRD-align:状态值改两位数,加 businessLine 必填,加 lifecyclePhase 状态机。
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplLightIntegrationTest {

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl service;

    @Test
    @DisplayName("完整生命周期:insert → status 00→01→00 → phase 00→01 → 完成 02")
    void fullLifecycle() {
        // 1) Insert:必填字段齐 + 自动 projectNo + 默认 status=00 / phase=00 / progress=0
        Project newProject = new Project();
        newProject.setProjectName("生命周期测试");
        newProject.setBusinessLine("precision_agri");
        when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
        when(projectMapper.insertProject(any())).thenReturn(1);

        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.insertProject(newProject);
        }
        assertThat(newProject.getProjectNo()).startsWith("PRJ-");
        assertThat(newProject.getStatus()).isEqualTo("00");
        assertThat(newProject.getLifecyclePhase()).isEqualTo("00");
        assertThat(newProject.getProgress()).isEqualTo(0);

        // 2) Update:status 00→01 (暂停)
        Project current = newProject;
        current.setId(1L);
        when(projectMapper.selectProjectById(1L)).thenReturn(current);
        when(projectMapper.updateProject(any())).thenReturn(1);

        Project to01 = new Project();
        to01.setId(1L);
        to01.setStatus("01");
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.updateProject(to01);
        }

        // 3) status 01→00 反向边恢复
        current.setStatus("01");
        Project to00 = new Project();
        to00.setId(1L);
        to00.setStatus("00");
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.updateProject(to00);
        }

        // 4) phase 00→01 推进
        current.setStatus("00");
        Project phaseTo01 = new Project();
        phaseTo01.setId(1L);
        phaseTo01.setLifecyclePhase("01");
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.updateProject(phaseTo01);
        }

        // 5) status 00→02 完成 (终态)
        Project toDone = new Project();
        toDone.setId(1L);
        toDone.setStatus("02");
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.updateProject(toDone);
        }

        // 6) 终态保护:02→00 应失败
        current.setStatus("02");
        Project illegal = new Project();
        illegal.setId(1L);
        illegal.setStatus("00");
        assertThatThrownBy(() -> service.updateProject(illegal))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("已完成");
    }
}
