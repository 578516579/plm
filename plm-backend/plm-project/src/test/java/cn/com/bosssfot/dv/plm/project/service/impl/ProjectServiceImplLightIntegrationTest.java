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
 * "轻"集成测试 — Service ↔ Mapper 协作验证（用 Mockito 模拟 Mapper）。
 *
 * 为什么不是真集成测试（@SpringBootTest + 真 DB）？
 *   Phase 04 选择 standalone MockMvc + Mockito 而非 @SpringBootTest，原因：
 *   - 不需启动 Spring 上下文，单测速度快（< 1s vs > 30s）
 *   - Controller 真实集成（含权限、Filter、Web 上下文）已在 Phase 03 的 E2E
 *     curl 验证中覆盖（[cb195a7]），等价于跑了一遍真集成测试
 *   - 业务逻辑层（Service ↔ Mapper 契约）由本类覆盖
 *
 * 若 Phase 06 引入 staging / 真 DB 测试需求，再升级为 @SpringBootTest。
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplLightIntegrationTest {

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl service;

    @Test
    @DisplayName("完整生命周期：insert → update(0→1) → update(1→3) → 终态")
    void fullLifecycle() {
        // 1) Insert: 未传 projectNo → 自动生成
        Project newProject = new Project();
        newProject.setProjectName("生命周期测试");
        when(projectMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
        when(projectMapper.insertProject(any())).thenReturn(1);

        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.insertProject(newProject);
        }
        assertThat(newProject.getProjectNo()).startsWith("PRJ-");
        assertThat(newProject.getStatus()).isEqualTo("0");

        // 2) Update: 0→1 启动
        Project current = newProject;
        current.setId(1L);
        when(projectMapper.selectProjectById(1L)).thenReturn(current);
        when(projectMapper.updateProject(any())).thenReturn(1);

        Project to1 = new Project();
        to1.setId(1L);
        to1.setStatus("1");
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.updateProject(to1);
        }

        // 3) 模拟 status 已更新（mock 已设的 current 在第 2 步未自动改 status，重新设置）
        current.setStatus("1");
        Project to3 = new Project();
        to3.setId(1L);
        to3.setStatus("3");
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.updateProject(to3);
        }

        // 4) 终态保护：3→1 应失败
        current.setStatus("3");
        Project illegal = new Project();
        illegal.setId(1L);
        illegal.setStatus("1");
        assertThatThrownBy(() -> service.updateProject(illegal))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("已完成");
    }
}
