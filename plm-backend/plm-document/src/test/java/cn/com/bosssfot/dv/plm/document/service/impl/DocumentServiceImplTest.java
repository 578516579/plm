package cn.com.bosssfot.dv.plm.document.service.impl;

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
import cn.com.bosssfot.dv.plm.document.domain.Document;
import cn.com.bosssfot.dv.plm.document.mapper.DocumentMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * DocumentServiceImpl 单元测试
 *
 * 覆盖范围:
 *   - ADR-0007 generateDocumentNo: DOC-<TYPE_UPPER>-YYYY-NNNN 格式 / 序号递增
 *   - PRD §4 状态机: 4×4 矩阵含反向边 01→00 / 02→01 / 终态 03
 *   - doc_type 字典白名单: 604
 *   - 进入 02 必须 reviewer: 707
 *   - FK 校验: projectId 不存在 → 702
 *   - 新建状态保护: 非 00 → 601
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private DocumentServiceImpl service;

    private Document sample;

    @BeforeEach
    void setUp() {
        sample = new Document();
        sample.setTitle("架构设计文档");
        sample.setProjectId(1L);
        sample.setDocType("arch");
    }

    // ─────────────────────────────────────────────────────────────────────
    // generateDocumentNo (ADR-0007)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateDocumentNo (ADR-0007)")
    class GenerateDocumentNoTests {

        @Test
        @DisplayName("当年无 arch 文档时，编号为 DOC-ARCH-YYYY-0001")
        void firstDocumentOfYear() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(documentMapper.selectMaxSeqOfYearByType(anyString())).thenReturn(null);
            when(documentMapper.insertDocument(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDocument(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getDocumentNo()).isEqualTo(String.format("DOC-ARCH-%d-0001", year));
        }

        @Test
        @DisplayName("当年已有 5 个 arch 文档时，下一个编号为 0006")
        void nextSequence() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(documentMapper.selectMaxSeqOfYearByType(anyString())).thenReturn(5);
            when(documentMapper.insertDocument(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDocument(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getDocumentNo()).isEqualTo(String.format("DOC-ARCH-%d-0006", year));
        }

        @Test
        @DisplayName("下划线 doc_type (db_design) → 编号中 _ 被去除: DOC-DBDESIGN-YYYY-0001")
        void underscoreTypeNormalized() {
            sample.setDocType("db_design");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(documentMapper.selectMaxSeqOfYearByType(anyString())).thenReturn(null);
            when(documentMapper.insertDocument(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDocument(sample);
            }

            int year = LocalDate.now().getYear();
            assertThat(sample.getDocumentNo()).isEqualTo(String.format("DOC-DBDESIGN-%d-0001", year));
        }

        @Test
        @DisplayName("用户自定义了 documentNo，不自动生成")
        void userProvidedNoIsKept() {
            sample.setDocumentNo("DOC-CUSTOM-001");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(documentMapper.insertDocument(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDocument(sample);
            }

            assertThat(sample.getDocumentNo()).isEqualTo("DOC-CUSTOM-001");
            verify(documentMapper, never()).selectMaxSeqOfYearByType(anyString());
        }

        @Test
        @DisplayName("撞号重试：DuplicateKeyException 后用新编号成功")
        void duplicateKeyRetry() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(documentMapper.selectMaxSeqOfYearByType(anyString()))
                .thenReturn(null)
                .thenReturn(1);
            when(documentMapper.insertDocument(any()))
                .thenThrow(new DuplicateKeyException("dup"))
                .thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDocument(sample);
            }

            verify(documentMapper, Mockito.times(2)).insertDocument(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // insertDocument — 字段校验 + FK
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertDocument — 字段校验")
    class InsertValidationTests {

        @Test
        @DisplayName("标题为空 → 602")
        void titleBlank() {
            sample.setTitle("");
            assertThatThrownBy(() -> service.insertDocument(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("标题");
        }

        @Test
        @DisplayName("doc_type 为空 → 602")
        void docTypeBlank() {
            sample.setDocType(null);
            assertThatThrownBy(() -> service.insertDocument(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("doc_type");
        }

        @Test
        @DisplayName("doc_type 不在白名单 → 604")
        void invalidDocType() {
            sample.setDocType("excel_sheet");
            // 注: insertDocument 先校验 docType 合法性(在 FK 之前),无需 stub projectMapper
            assertThatThrownBy(() -> service.insertDocument(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("doc_type");
        }

        @Test
        @DisplayName("FK projectId 不存在 → 702")
        void fkProjectNotFound() {
            when(projectMapper.selectProjectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.insertDocument(sample))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联项目不存在");
        }

        @Test
        @DisplayName("新建时初始状态非 00 → 601")
        void initialStatusNotDraft() {
            sample.setStatus("01");
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            // authorUserId 为 null 会调 SecurityUtils.getUserId，需 mock
            assertThatThrownBy(() -> {
                try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                    mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                    mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                    service.insertDocument(sample);
                }
            }).isInstanceOf(ServiceException.class)
              .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("默认版本 v1.0 被自动填充")
        void defaultVersionFilled() {
            when(projectMapper.selectProjectById(1L)).thenReturn(existingProject());
            when(documentMapper.selectMaxSeqOfYearByType(anyString())).thenReturn(null);
            when(documentMapper.insertDocument(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(1L);
                service.insertDocument(sample);
            }

            assertThat(sample.getVersion()).isEqualTo("v1.0");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 状态机 (PRD §4)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("状态机 (PRD §4 4×4 含反向边)")
    class StatusMachineTests {

        @Test
        @DisplayName("合法转换 00→01 成功")
        void legal_00_to_01() {
            Document old = existingDoc("00");
            when(documentMapper.selectDocumentById(1L)).thenReturn(old);
            when(documentMapper.updateDocument(any())).thenReturn(1);

            Document upd = updateDoc(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDocument(upd);
            }
            verify(documentMapper).updateDocument(any());
        }

        @Test
        @DisplayName("反向边 01→00 (打回) 合法")
        void reverse_01_to_00() {
            Document old = existingDoc("01");
            when(documentMapper.selectDocumentById(1L)).thenReturn(old);
            when(documentMapper.updateDocument(any())).thenReturn(1);

            Document upd = updateDoc(1L, "00");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDocument(upd);
            }
            verify(documentMapper).updateDocument(any());
        }

        @Test
        @DisplayName("反向边 02→01 (重审) 合法")
        void reverse_02_to_01() {
            Document old = existingDoc("02");
            when(documentMapper.selectDocumentById(1L)).thenReturn(old);
            when(documentMapper.updateDocument(any())).thenReturn(1);

            Document upd = updateDoc(1L, "01");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDocument(upd);
            }
            verify(documentMapper).updateDocument(any());
        }

        @Test
        @DisplayName("进入 02 (已发布) 必须有 reviewerUserId → 707")
        void enterPublishedRequiresReviewer() {
            Document old = existingDoc("01");
            old.setReviewerUserId(null);
            when(documentMapper.selectDocumentById(1L)).thenReturn(old);

            Document upd = updateDoc(1L, "02");
            // reviewerUserId 未设
            assertThatThrownBy(() -> service.updateDocument(upd))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("审核人");
            verify(documentMapper, never()).updateDocument(any());
        }

        @Test
        @DisplayName("进入 02 (已发布) 带 reviewerUserId 成功")
        void enterPublishedWithReviewerOk() {
            Document old = existingDoc("01");
            when(documentMapper.selectDocumentById(1L)).thenReturn(old);
            when(documentMapper.updateDocument(any())).thenReturn(1);

            Document upd = updateDoc(1L, "02");
            upd.setReviewerUserId(10L);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                service.updateDocument(upd);
            }
            verify(documentMapper).updateDocument(any());
        }

        @Test
        @DisplayName("非法跳级 00→02 → 601")
        void illegal_00_to_02() {
            Document old = existingDoc("00");
            when(documentMapper.selectDocumentById(1L)).thenReturn(old);

            assertThatThrownBy(() -> service.updateDocument(updateDoc(1L, "02")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("草稿");
        }

        @Test
        @DisplayName("终态 03→任意 → 601（已归档不可逆）")
        void terminal_03_immutable() {
            Document old = existingDoc("03");
            when(documentMapper.selectDocumentById(1L)).thenReturn(old);

            for (String to : new String[]{"00", "01", "02"}) {
                assertThatThrownBy(() -> service.updateDocument(updateDoc(1L, to)))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("已归档");
            }
        }

        @Test
        @DisplayName("文档不存在 → 404")
        void notFound() {
            when(documentMapper.selectDocumentById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.updateDocument(updateDoc(99L, "01")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("文档不存在");
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

    private Document existingDoc(String status) {
        Document d = new Document();
        d.setDocumentId(1L);
        d.setTitle("旧文档");
        d.setStatus(status);
        d.setProjectId(1L);
        d.setDocType("arch");
        return d;
    }

    private Document updateDoc(Long id, String newStatus) {
        Document d = new Document();
        d.setDocumentId(id);
        d.setStatus(newStatus);
        return d;
    }
}
