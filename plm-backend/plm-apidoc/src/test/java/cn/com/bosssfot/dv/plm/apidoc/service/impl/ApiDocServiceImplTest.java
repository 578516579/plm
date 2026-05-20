package cn.com.bosssfot.dv.plm.apidoc.service.impl;

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

import cn.com.bosssfot.dv.plm.apidoc.domain.ApiDoc;
import cn.com.bosssfot.dv.plm.apidoc.mapper.ApiDocMapper;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * ApiDocServiceImpl 单元测试
 *
 * 覆盖范围:
 *   - ADR: generateApidocNo API-YYYY-NNNN
 *   - HTTP 方法白名单: GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS → 604
 *   - HTTP 方法自动大写
 *   - UK (method+path+version) 冲突 → 701
 *   - autoExtracted='Y' 时自动填 lastSyncedAt
 *   - 3 状态机: 00→{01} / 01→{02} / 02→{} (终态)
 *   - FK: projectId 不存在 → 702
 */
@ExtendWith(MockitoExtension.class)
class ApiDocServiceImplTest {

    @Mock
    private ApiDocMapper apidocMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ApiDocServiceImpl service;

    private ApiDoc sample;

    @BeforeEach
    void setUp() {
        sample = new ApiDoc();
        sample.setTitle("用户登录接口");
        sample.setProjectId(1L);
        sample.setHttpMethod("POST");
        sample.setPath("/api/v1/auth/login");
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateApidocNo
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateApidocNo (API-YYYY-NNNN)")
    class GenerateNoTests {

        @Test
        @DisplayName("当年无 API 文档，编号为 API-YYYY-0001")
        void firstOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidocMapper.insertApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getApidocNo()).isEqualTo(String.format("API-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 20 个 API 文档，下一个为 0021")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(20);
            when(apidocMapper.insertApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getApidocNo()).isEqualTo(String.format("API-%d-0021", year));
        }

        @Test
        @DisplayName("apidoc_no 重号重试：DuplicateKeyException(非 uk_apidoc) 后成功")
        void noRetryOnUkViolation() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString()))
                .thenReturn(null).thenReturn(1);
            // 第一次抛不含 uk_apidoc_method_path 的 DuplicateKeyException (号码冲突)
            when(apidocMapper.insertApiDoc(any()))
                .thenThrow(new DuplicateKeyException("Duplicate entry for apidoc_no"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            verify(apidocMapper, Mockito.times(2)).insertApiDoc(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HTTP 方法白名单
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("HTTP 方法白名单校验")
    class HttpMethodTests {

        @Test
        @DisplayName("非法 method CONNECT → 604")
        void invalidMethod() {
            sample.setHttpMethod("CONNECT");
            // 注: method 合法性校验在 FK 校验之前，无需 stub projectMapper
            assertThatThrownBy(() -> service.insertApiDoc(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("HTTP 方法");
        }

        @Test
        @DisplayName("小写 post 自动转大写 POST")
        void lowerCaseMethodNormalized() {
            sample.setHttpMethod("post");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidocMapper.insertApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            assertThat(sample.getHttpMethod()).isEqualTo("POST");
        }

        @Test
        @DisplayName("DELETE 方法合法")
        void deleteMethodValid() {
            sample.setHttpMethod("DELETE");
            sample.setPath("/api/v1/users/1");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidocMapper.insertApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            verify(apidocMapper).insertApiDoc(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UK 冲突 → 701
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UK (method+path+version) 冲突 → 701")
    class UniqueKeyTests {

        @Test
        @DisplayName("insert 时 UK 冲突 → 701")
        void insertUkConflict() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidocMapper.insertApiDoc(any()))
                .thenThrow(new DuplicateKeyException("Duplicate entry for uk_apidoc_method_path"));

            assertThatThrownBy(() -> {
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.insertApiDoc(sample);
                }
            }).isInstanceOf(ServiceException.class)
              .hasMessageContaining("method+path+version");
        }

        @Test
        @DisplayName("update 时 UK 冲突 → 701")
        void updateUkConflict() {
            ApiDoc old = existingApiDoc("00");
            when(apidocMapper.selectApiDocById(1L)).thenReturn(old);
            when(apidocMapper.updateApiDoc(any()))
                .thenThrow(new DuplicateKeyException("Duplicate entry for uk_apidoc_method_path"));

            ApiDoc upd = new ApiDoc();
            upd.setApidocId(1L);
            upd.setPath("/api/v1/new-path");
            assertThatThrownBy(() -> {
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.updateApiDoc(upd);
                }
            }).isInstanceOf(ServiceException.class)
              .hasMessageContaining("method+path+version");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // autoExtracted 自动填 lastSyncedAt
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("autoExtracted='Y' 自动填 lastSyncedAt")
    class AutoExtractedTests {

        @Test
        @DisplayName("autoExtracted='Y' 且 lastSyncedAt 为 null → 自动填充")
        void autoExtractedFillsTimestamp() {
            sample.setAutoExtracted("Y");
            sample.setLastSyncedAt(null);
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidocMapper.insertApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            assertThat(sample.getLastSyncedAt()).isNotNull();
        }

        @Test
        @DisplayName("autoExtracted='N' → lastSyncedAt 不填充")
        void notAutoExtractedNoTimestamp() {
            sample.setAutoExtracted("N");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidocMapper.insertApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            assertThat(sample.getLastSyncedAt()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (3 状态)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (3 状态)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 成功")
        void legal_00_to_01() {
            ApiDoc old = existingApiDoc("00");
            when(apidocMapper.selectApiDocById(1L)).thenReturn(old);
            when(apidocMapper.updateApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateApiDoc(updateApiDoc(1L, "01"));
            }
            verify(apidocMapper).updateApiDoc(any());
        }

        @Test
        @DisplayName("终态 02→任意 → 601（已废弃不可逆）")
        void terminal_02_immutable() {
            ApiDoc old = existingApiDoc("02");
            when(apidocMapper.selectApiDocById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01"}) {
                assertThatThrownBy(() -> service.updateApiDoc(updateApiDoc(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已废弃");
            }
        }

        @Test
        @DisplayName("非法跳级 00→02 → 601")
        void illegal_00_to_02() {
            ApiDoc old = existingApiDoc("00");
            when(apidocMapper.selectApiDocById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateApiDoc(updateApiDoc(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("API 文档不存在 → 404")
        void notFound() {
            when(apidocMapper.selectApiDocById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateApiDoc(updateApiDoc(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("API 文档不存在");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertApiDoc — 必填校验
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertApiDoc — 必填校验")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle(null);
            assertThatThrownBy(() -> service.insertApiDoc(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("接口标题");
        }

        @Test
        @DisplayName("HTTP 方法为空 → 602")
        void httpMethodBlank() {
            sample.setHttpMethod(null);
            assertThatThrownBy(() -> service.insertApiDoc(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("HTTP 方法");
        }

        @Test
        @DisplayName("接口路径为空 → 602")
        void pathBlank() {
            sample.setPath(null);
            assertThatThrownBy(() -> service.insertApiDoc(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("接口路径");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertApiDoc(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("默认版本 v1.0 被填充")
        void defaultVersionFilled() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(apidocMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(apidocMapper.insertApiDoc(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.insertApiDoc(sample);
            }

            assertThat(sample.getVersion()).isEqualTo("v1.0");
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

    private ApiDoc existingApiDoc(String status) {
        ApiDoc a = new ApiDoc();
        a.setApidocId(1L);
        a.setTitle("旧接口");
        a.setStatus(status);
        a.setProjectId(1L);
        a.setHttpMethod("GET");
        a.setPath("/api/v1/users");
        return a;
    }

    private ApiDoc updateApiDoc(Long id, String newStatus) {
        ApiDoc a = new ApiDoc();
        a.setApidocId(id);
        a.setStatus(newStatus);
        return a;
    }
}
