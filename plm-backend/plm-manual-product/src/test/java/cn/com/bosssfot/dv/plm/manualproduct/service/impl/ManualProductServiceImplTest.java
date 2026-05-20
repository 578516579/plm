package cn.com.bosssfot.dv.plm.manualproduct.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import cn.com.bosssfot.dv.plm.manualproduct.domain.ManualProduct;
import cn.com.bosssfot.dv.plm.manualproduct.mapper.ManualProductMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * ManualProductServiceImpl 单元测试
 *
 * 覆盖范围:
 *   - ADR: generateManualproductNo PM-YYYY-NNNN
 *   - 必填: title / productVersion / includeModules / authorUserId / projectId
 *   - 4 状态机: 00→{01} / 01→{02} / 02→{00,03} / 03→{} (终态)
 *   - 反向边 02→00 (回退到草稿)
 *   - 进入 02 (已生成) 自动填 generatedAt
 *   - 新建允许状态 00 或 01
 *   - FK: projectId 不存在 → 702
 */
@ExtendWith(MockitoExtension.class)
class ManualProductServiceImplTest {

    @Mock
    private ManualProductMapper manualproductMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ManualProductServiceImpl service;

    private ManualProduct sample;

    @BeforeEach
    void setUp() {
        sample = new ManualProduct();
        sample.setTitle("AgriPLM 产品使用手册");
        sample.setProjectId(1L);
        sample.setProductVersion("v2.3.0");
        sample.setIncludeModules("项目管理,需求管理,测试管理");
        sample.setAuthorUserId(10L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateManualproductNo
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateManualproductNo (PM-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无手册，编号为 PM-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualproductMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualproductMapper.insertManualProduct(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualProduct(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getManualproductNo()).isEqualTo(String.format("PM-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 4 个手册，下一个为 0005")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualproductMapper.selectMaxSeqOfYear(anyString())).thenReturn(4);
            when(manualproductMapper.insertManualProduct(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualProduct(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getManualproductNo()).isEqualTo(String.format("PM-%d-0005", year));
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后成功")
        void duplicateKeyRetry() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualproductMapper.selectMaxSeqOfYear(anyString()))
                .thenReturn(null).thenReturn(1);
            when(manualproductMapper.insertManualProduct(any()))
                .thenThrow(new DuplicateKeyException("dup"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualProduct(sample);
            }

            verify(manualproductMapper, Mockito.times(2)).insertManualProduct(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertManualProduct — 必填校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertManualProduct — 必填校验")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertManualProduct(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("标题");
        }

        @Test
        @DisplayName("productVersion 为空 → 602")
        void productVersionBlank() {
            sample.setProductVersion(null);
            assertThatThrownBy(() -> service.insertManualProduct(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("产品版本");
        }

        @Test
        @DisplayName("includeModules 为空 → 602")
        void includeModulesBlank() {
            sample.setIncludeModules("");
            assertThatThrownBy(() -> service.insertManualProduct(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("包含模块");
        }

        @Test
        @DisplayName("authorUserId 为空 → 602")
        void authorNull() {
            sample.setAuthorUserId(null);
            assertThatThrownBy(() -> service.insertManualProduct(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("作者");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertManualProduct(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建状态 00 合法")
        void initialStatus00Valid() {
            sample.setStatus("00");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualproductMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualproductMapper.insertManualProduct(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualProduct(sample);
            }

            verify(manualproductMapper).insertManualProduct(any());
        }

        @Test
        @DisplayName("新建状态 01 (生成中) 也合法")
        void initialStatus01Valid() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualproductMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualproductMapper.insertManualProduct(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualProduct(sample);
            }

            verify(manualproductMapper).insertManualProduct(any());
        }

        @Test
        @DisplayName("新建状态 02 非法 → 601")
        void initialStatus02Invalid() {
            sample.setStatus("02");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            assertThatThrownBy(() -> service.insertManualProduct(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("默认 outputFormats='pdf' 被填充")
        void defaultOutputFormatsPdf() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(manualproductMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(manualproductMapper.insertManualProduct(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertManualProduct(sample);
            }

            assertThat(sample.getOutputFormats()).isEqualTo("pdf");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (4 状态含反向边 02→00)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (4 状态含反向边 02→00)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 成功")
        void legal_00_to_01() {
            ManualProduct old = existingManualProduct("00");
            when(manualproductMapper.selectManualProductById(1L)).thenReturn(old);
            when(manualproductMapper.updateManualProduct(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateManualProduct(updateManualProduct(1L, "01"));
            }
            verify(manualproductMapper).updateManualProduct(any());
        }

        @Test
        @DisplayName("进入 02 (已生成) 自动填 generatedAt")
        void enter02FillsGeneratedAt() {
            ManualProduct old = existingManualProduct("01");
            old.setGeneratedAt(null);
            when(manualproductMapper.selectManualProductById(1L)).thenReturn(old);
            when(manualproductMapper.updateManualProduct(any())).thenReturn(1);

            ManualProduct upd = updateManualProduct(1L, "02");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateManualProduct(upd);
            }

            assertThat(upd.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("反向边 02→00 (重新生成) 合法")
        void reverse_02_to_00() {
            ManualProduct old = existingManualProduct("02");
            when(manualproductMapper.selectManualProductById(1L)).thenReturn(old);
            when(manualproductMapper.updateManualProduct(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateManualProduct(updateManualProduct(1L, "00"));
            }
            verify(manualproductMapper).updateManualProduct(any());
        }

        @Test
        @DisplayName("终态 03→任意 → 601（已发布不可逆）")
        void terminal_03_immutable() {
            ManualProduct old = existingManualProduct("03");
            when(manualproductMapper.selectManualProductById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01", "02"}) {
                assertThatThrownBy(() -> service.updateManualProduct(updateManualProduct(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已发布");
            }
        }

        @Test
        @DisplayName("非法跳级 00→02 → 601")
        void illegal_00_to_02() {
            ManualProduct old = existingManualProduct("00");
            when(manualproductMapper.selectManualProductById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateManualProduct(updateManualProduct(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("产品手册不存在 → 404")
        void notFound() {
            when(manualproductMapper.selectManualProductById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateManualProduct(updateManualProduct(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("产品手册不存在");
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

    private ManualProduct existingManualProduct(String status) {
        ManualProduct m = new ManualProduct();
        m.setManualproductId(1L);
        m.setTitle("旧手册");
        m.setStatus(status);
        m.setProjectId(1L);
        m.setProductVersion("v1.0.0");
        return m;
    }

    private ManualProduct updateManualProduct(Long id, String newStatus) {
        ManualProduct m = new ManualProduct();
        m.setManualproductId(id);
        m.setStatus(newStatus);
        return m;
    }
}
