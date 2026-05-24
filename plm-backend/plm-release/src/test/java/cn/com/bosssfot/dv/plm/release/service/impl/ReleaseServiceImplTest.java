package cn.com.bosssfot.dv.plm.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.release.domain.Release;
import cn.com.bosssfot.dv.plm.release.mapper.ReleaseMapper;

/**
 * ReleaseServiceImpl 单元测试
 *
 * 覆盖范围:
 *   - ADR: generateReleaseNo REL-YYYY-NNNN
 *   - 策略白名单: blue_green / canary / rolling → 604
 *   - 5 状态机: 00→{01,04} / 01→{02,03} / 02→{03,04} / 03→{04} / 04→{} (终态)
 *   - 回滚必须有 rollbackReason → 602
 *   - FK: projectId 不存在 → 702
 */
@ExtendWith(MockitoExtension.class)
class ReleaseServiceImplTest {

    @Mock
    private ReleaseMapper releaseMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ReleaseServiceImpl service;

    private Release sample;

    @BeforeEach
    void setUp() {
        sample = new Release();
        sample.setVersion("v2.3.0");
        sample.setProjectId(1L);
        sample.setReleasedByUserId(10L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateReleaseNo
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateReleaseNo (REL-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无发布单，编号为 REL-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(releaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(releaseMapper.insertRelease(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRelease(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getReleaseNo()).isEqualTo(String.format("REL-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 3 个发布单，下一个为 0004")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(releaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(3);
            when(releaseMapper.insertRelease(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRelease(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getReleaseNo()).isEqualTo(String.format("REL-%d-0004", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后成功")
        void duplicateKeyRetry() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(releaseMapper.selectMaxSeqOfYear(anyString()))
                .thenReturn(null).thenReturn(1);
            when(releaseMapper.insertRelease(any()))
                .thenThrow(new DuplicateKeyException("dup"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRelease(sample);
            }

            verify(releaseMapper, Mockito.times(2)).insertRelease(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertRelease — 字段校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertRelease — 字段校验")
    class InsertValidationTests {

        @Test
        @DisplayName("版本号为空 → 602")
        void versionBlank() {
            sample.setVersion(null);
            assertThatThrownBy(() -> service.insertRelease(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("版本号");
        }

        @Test
        @DisplayName("发布人为空 → 602")
        void releasedByUserIdNull() {
            sample.setReleasedByUserId(null);
            assertThatThrownBy(() -> service.insertRelease(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("发布人");
        }

        @Test
        @DisplayName("策略非法 → 604")
        void invalidStrategy() {
            sample.setStrategy("hotfix");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertRelease(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("策略");
        }

        @Test
        @DisplayName("合法策略 canary 插入成功")
        void validStrategyCanary() {
            sample.setStrategy("canary");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(releaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(releaseMapper.insertRelease(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRelease(sample);
            }

            assertThat(sample.getStrategy()).isEqualTo("canary");
        }

        @Test
        @DisplayName("默认策略 rolling 被填充")
        void defaultStrategyRolling() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(releaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(releaseMapper.insertRelease(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertRelease(sample);
            }

            assertThat(sample.getStrategy()).isEqualTo("rolling");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertRelease(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态非 00 → 601")
        void initialStatusNotPlan() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertRelease(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("计划中");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (5 状态)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (5 状态)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 成功")
        void legal_00_to_01() {
            Release old = existingRelease("00");
            when(releaseMapper.selectReleaseById(1L)).thenReturn(old);
            when(releaseMapper.updateRelease(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateRelease(updateRelease(1L, "01"));
            }
            verify(releaseMapper).updateRelease(any());
        }

        @Test
        @DisplayName("进入 03 (已回滚) 无 rollbackReason → 602")
        void rollbackNoReason() {
            Release old = existingRelease("01");
            old.setRollbackReason(null);
            when(releaseMapper.selectReleaseById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateRelease(updateRelease(1L, "03")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("回滚原因");
            verify(releaseMapper, never()).updateRelease(any());
        }

        @Test
        @DisplayName("进入 03 (已回滚) 带 rollbackReason 成功")
        void rollbackWithReason() {
            Release old = existingRelease("01");
            when(releaseMapper.selectReleaseById(1L)).thenReturn(old);
            when(releaseMapper.updateRelease(any())).thenReturn(1);

            Release upd = updateRelease(1L, "03");
            upd.setRollbackReason("数据库连接异常");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateRelease(upd);
            }
            verify(releaseMapper).updateRelease(any());
        }

        @Test
        @DisplayName("终态 04→任意 → 601")
        void terminal_04_immutable() {
            Release old = existingRelease("04");
            when(releaseMapper.selectReleaseById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01", "02", "03"}) {
                assertThatThrownBy(() -> service.updateRelease(updateRelease(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已废弃");
            }
        }

        @Test
        @DisplayName("非法跳级 00→02 → 601")
        void illegal_00_to_02() {
            Release old = existingRelease("00");
            when(releaseMapper.selectReleaseById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateRelease(updateRelease(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("计划中");
        }

        @Test
        @DisplayName("发布单不存在 → 404")
        void notFound() {
            when(releaseMapper.selectReleaseById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateRelease(updateRelease(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("发布单不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 辅助方法
    // ─────────────────────────────────────────────────────────────────────

    private Project existingProject() {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("测试项目");
        return p;
    }

    private Release existingRelease(String status) {
        Release r = new Release();
        r.setReleaseId(1L);
        r.setVersion("v1.0.0");
        r.setStatus(status);
        r.setProjectId(1L);
        return r;
    }

    private Release updateRelease(Long id, String newStatus) {
        Release r = new Release();
        r.setReleaseId(id);
        r.setStatus(newStatus);
        return r;
    }
}
