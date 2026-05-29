package cn.com.bosssfot.dv.plm.task.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.com.bosssfot.dv.plm.task.mapper.TaskMapper;

/**
 * {@link TaskQueryServiceImpl} 单元测试 — SPI 反向依赖实现层。
 *
 * <p>覆盖盲点(jacoco 0% → 100%):TaskQueryServiceImpl 之前被 plm-sprint / plm-requirement
 * 通过 ITaskQueryService 接口反向调用,但本身没有任何 case 覆盖,导致 plm-task -Pcoverage
 * 被 jacoco 60% 门槛硬拦。本类补 3 case × 委托验证。
 *
 * <p>由本会话 task #4 jacoco 覆盖率扫描发现(2026-05-29)。详 03-开发/测试规范.md §5 B.0。
 */
@ExtendWith(MockitoExtension.class)
class TaskQueryServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskQueryServiceImpl service;

    @Test
    @DisplayName("countBySprintId 委托 mapper.countBySprintId(sprintId)")
    void countBySprintIdDelegates() {
        when(taskMapper.countBySprintId(100L)).thenReturn(7);

        int result = service.countBySprintId(100L);

        assertThat(result).isEqualTo(7);
        verify(taskMapper, times(1)).countBySprintId(100L);
    }

    @Test
    @DisplayName("countByStatusAndSprint 委托 mapper.countByStatusAndSprint(sprintId, status)")
    void countByStatusAndSprintDelegates() {
        when(taskMapper.countByStatusAndSprint(200L, "01")).thenReturn(3);

        int result = service.countByStatusAndSprint(200L, "01");

        assertThat(result).isEqualTo(3);
        verify(taskMapper, times(1)).countByStatusAndSprint(200L, "01");
    }

    @Test
    @DisplayName("countByRequirementId 委托 mapper.countByRequirementId(requirementId)")
    void countByRequirementIdDelegates() {
        when(taskMapper.countByRequirementId(300L)).thenReturn(5);

        int result = service.countByRequirementId(300L);

        assertThat(result).isEqualTo(5);
        verify(taskMapper, times(1)).countByRequirementId(300L);
    }
}
