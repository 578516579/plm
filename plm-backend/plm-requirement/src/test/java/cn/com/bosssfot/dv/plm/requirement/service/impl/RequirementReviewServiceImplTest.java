package cn.com.bosssfot.dv.plm.requirement.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.domain.RequirementReview;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementReviewMapper;

/**
 * RequirementReviewServiceImpl 单元测试
 *
 * 覆盖范围（PRD §F2.4 需求评审管理 / 2026-05-25 新增）:
 *   - submitReview: 参数校验 / FK 校验 / 打回意见必填 / 评审人兜底 / 默认时间回填
 *   - hasPassedReview: 0 通过 → false / ≥1 通过 → true
 *   - selectByRequirementId: 历史查询
 *   - deleteRequirementReviewByIds: 空数组防御 / 批量逻辑删除
 */
@ExtendWith(MockitoExtension.class)
class RequirementReviewServiceImplTest {

    @Mock
    private RequirementReviewMapper requirementReviewMapper;

    @Mock
    private RequirementMapper requirementMapper;

    @InjectMocks
    private RequirementReviewServiceImpl service;

    private Requirement existingReq;
    private RequirementReview passReview;
    private RequirementReview rejectReview;

    @BeforeEach
    void setUp() {
        existingReq = new Requirement();
        existingReq.setRequirementId(10L);
        existingReq.setTitle("测试需求");
        existingReq.setStatus("00");

        passReview = new RequirementReview();
        passReview.setReviewResult("00");
        passReview.setReviewerUserId(5L);
        passReview.setReviewComment("功能定义清晰");

        rejectReview = new RequirementReview();
        rejectReview.setReviewResult("01");
        rejectReview.setReviewerUserId(5L);
        rejectReview.setReviewComment("需求范围模糊,需细化");
    }

    // ─────────────────────────────────────────────────────────────────────
    // submitReview: 参数 / FK / 业务规则
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("submitReview 参数与 FK 校验")
    class SubmitReviewValidation {

        @Test
        @DisplayName("requirementId 为 null 抛 602")
        void nullRequirementId() {
            assertThatThrownBy(() -> service.submitReview(null, passReview))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("需求 ID");
        }

        @Test
        @DisplayName("review 对象为 null 抛 602")
        void nullReview() {
            assertThatThrownBy(() -> service.submitReview(10L, null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("评审对象");
        }

        @Test
        @DisplayName("需求不存在抛 704")
        void requirementNotFound() {
            when(requirementMapper.selectRequirementById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.submitReview(99L, passReview))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("关联需求不存在");
        }

        @Test
        @DisplayName("reviewResult 为空抛 602")
        void emptyReviewResult() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);
            RequirementReview r = new RequirementReview();
            r.setReviewerUserId(5L);
            assertThatThrownBy(() -> service.submitReview(10L, r))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("评审结果");
        }

        @Test
        @DisplayName("reviewResult 非白名单值抛 604")
        void invalidReviewResult() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);
            RequirementReview r = new RequirementReview();
            r.setReviewResult("99");
            r.setReviewerUserId(5L);
            assertThatThrownBy(() -> service.submitReview(10L, r))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("评审结果非法");
        }

        @Test
        @DisplayName("打回(01)未填意见抛 604")
        void rejectWithoutComment() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);
            RequirementReview r = new RequirementReview();
            r.setReviewResult("01");
            r.setReviewComment("   ");  // 空白也算空
            r.setReviewerUserId(5L);
            assertThatThrownBy(() -> service.submitReview(10L, r))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("打回评审");
        }

        @Test
        @DisplayName("通过(00)允许不填意见")
        void passAllowEmptyComment() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);
            when(requirementReviewMapper.insertRequirementReview(any())).thenReturn(1);

            RequirementReview r = new RequirementReview();
            r.setReviewResult("00");
            r.setReviewerUserId(5L);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.submitReview(10L, r);
                assertThat(rows).isEqualTo(1);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // submitReview: 默认值与回填
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("submitReview 默认值与回填")
    class SubmitReviewDefaults {

        @Test
        @DisplayName("reviewAt 未填时自动回填当前时间")
        void autoFillReviewAt() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);
            when(requirementReviewMapper.insertRequirementReview(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(passReview.getReviewAt()).isNull();
                service.submitReview(10L, passReview);
                assertThat(passReview.getReviewAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("requirementId 自动用 path 参数覆盖,createBy 用当前用户")
        void autoFillRequirementIdAndCreateBy() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);
            when(requirementReviewMapper.insertRequirementReview(any())).thenReturn(1);

            passReview.setRequirementId(999L);  // 故意填错
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("evaluator");
                service.submitReview(10L, passReview);
            }
            assertThat(passReview.getRequirementId()).isEqualTo(10L);  // 被覆盖
            assertThat(passReview.getCreateBy()).isEqualTo("evaluator");
            assertThat(passReview.getDelFlag()).isEqualTo("0");
        }

        @Test
        @DisplayName("reviewerUserId 未填时兜底为当前登录用户")
        void fallbackReviewerToCurrentUser() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);
            when(requirementReviewMapper.insertRequirementReview(any())).thenReturn(1);

            RequirementReview r = new RequirementReview();
            r.setReviewResult("00");
            // 不填 reviewerUserId

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                mocked.when(SecurityUtils::getUserId).thenReturn(7L);
                service.submitReview(10L, r);
            }
            assertThat(r.getReviewerUserId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("reviewerUserId 兜底也失败(无登录)抛 602")
        void noLoginNoReviewer() {
            when(requirementMapper.selectRequirementById(10L)).thenReturn(existingReq);

            RequirementReview r = new RequirementReview();
            r.setReviewResult("00");

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUserId).thenReturn(null);
                assertThatThrownBy(() -> service.submitReview(10L, r))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("评审人不能为空");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // hasPassedReview (状态机 00→01 前置)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hasPassedReview 状态机前置检查")
    class HasPassedReviewTests {

        @Test
        @DisplayName("无通过评审返 false")
        void noPassedReview() {
            when(requirementReviewMapper.countPassedReviewsByRequirementId(10L)).thenReturn(0);
            assertThat(service.hasPassedReview(10L)).isFalse();
        }

        @Test
        @DisplayName("1 条通过评审返 true")
        void onePassedReview() {
            when(requirementReviewMapper.countPassedReviewsByRequirementId(10L)).thenReturn(1);
            assertThat(service.hasPassedReview(10L)).isTrue();
        }

        @Test
        @DisplayName("多条通过评审也返 true")
        void multiplePassedReviews() {
            when(requirementReviewMapper.countPassedReviewsByRequirementId(10L)).thenReturn(5);
            assertThat(service.hasPassedReview(10L)).isTrue();
        }

        @Test
        @DisplayName("requirementId 为 null 直接返 false 不查 DB")
        void nullIdNoQuery() {
            assertThat(service.hasPassedReview(null)).isFalse();
            verify(requirementReviewMapper, never()).countPassedReviewsByRequirementId(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // selectByRequirementId
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("selectByRequirementId 历史查询")
    class SelectByRequirementIdTests {

        @Test
        @DisplayName("返回该需求全部评审记录")
        void returnsList() {
            List<RequirementReview> stub = Arrays.asList(passReview, rejectReview);
            when(requirementReviewMapper.selectByRequirementId(10L)).thenReturn(stub);

            List<RequirementReview> result = service.selectByRequirementId(10L);
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewResult()).isEqualTo("00");
        }

        @Test
        @DisplayName("无评审记录返回空 list")
        void emptyList() {
            when(requirementReviewMapper.selectByRequirementId(10L)).thenReturn(Collections.emptyList());
            assertThat(service.selectByRequirementId(10L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // deleteRequirementReviewByIds (撤回评审)
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteRequirementReviewByIds 撤回评审")
    class DeleteReviewTests {

        @Test
        @DisplayName("空数组直接返 0,不查 DB")
        void emptyArrayNoQuery() {
            assertThat(service.deleteRequirementReviewByIds(new Long[]{})).isZero();
            verify(requirementReviewMapper, never()).deleteRequirementReviewByIds(any());
        }

        @Test
        @DisplayName("null 数组直接返 0")
        void nullArrayNoQuery() {
            assertThat(service.deleteRequirementReviewByIds(null)).isZero();
        }

        @Test
        @DisplayName("批量删除调用 Mapper 且返回行数")
        void batchDelete() {
            Long[] ids = {1L, 2L, 3L};
            when(requirementReviewMapper.deleteRequirementReviewByIds(ids)).thenReturn(3);
            assertThat(service.deleteRequirementReviewByIds(ids)).isEqualTo(3);
        }
    }
}
