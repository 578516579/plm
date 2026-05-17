package cn.com.bosssfot.dv.plm.testcase.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
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
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;
import cn.com.bosssfot.dv.plm.testcase.mapper.TestCaseMapper;

/**
 * TestCase Service 单元测试 — ADR-B Option B 后的 category 白名单
 *
 * 覆盖:
 *   - category 默认值 'functional'
 *   - 合法 8 值通过
 *   - 非法值抛 604
 *   - 旧 '01'~'07' 数字编码也被白名单拒绝(验证 break-change 成立)
 */
@ExtendWith(MockitoExtension.class)
class TestCaseServiceImplCategoryTest {

    @Mock private TestCaseMapper testcaseMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private RequirementMapper requirementMapper;
    @InjectMocks private TestCaseServiceImpl service;

    private TestCase sample;

    @BeforeEach
    void setUp() {
        sample = new TestCase();
        sample.setTitle("测试用例");
        sample.setProjectId(1L);
        sample.setSteps("步骤 1");
        sample.setExpectedResult("期望");
    }

    @Test
    @DisplayName("category 未传时默认 'functional'")
    void defaultCategoryFunctional() {
        when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
        when(testcaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
        when(testcaseMapper.insertTestCase(any())).thenReturn(1);

        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.insertTestCase(sample);
        }
        assertThat(sample.getCategory()).isEqualTo("functional");
    }

    @Test
    @DisplayName("category='agri' 农业专项合法")
    void legalAgri() {
        when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
        when(testcaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
        when(testcaseMapper.insertTestCase(any())).thenReturn(1);
        sample.setCategory("agri");
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            service.insertTestCase(sample);
        }
        assertThat(sample.getCategory()).isEqualTo("agri");
    }

    @Test
    @DisplayName("8 值字典全部合法")
    void all8ValuesLegal() {
        String[] values = {"functional", "boundary", "exception", "agri",
                           "api", "performance", "security", "compatibility"};
        for (String v : values) {
            TestCase t = new TestCase();
            t.setTitle("t-" + v);
            t.setProjectId(1L);
            t.setSteps("s");
            t.setExpectedResult("e");
            t.setCategory(v);
            when(projectMapper.selectProjectById(1L)).thenReturn(new Project());
            when(testcaseMapper.selectMaxSeqOfYear(anyString())).thenReturn(null);
            when(testcaseMapper.insertTestCase(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertTestCase(t);
                assertThat(rows).isEqualTo(1);
            }
        }
    }

    @Test
    @DisplayName("非法 category 'invalid' 抛 604")
    void illegalCategoryInvalid() {
        sample.setCategory("invalid");
        assertThatThrownBy(() -> service.insertTestCase(sample))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("category");
    }

    @Test
    @DisplayName("旧数字编码 '01' 已废弃,抛 604 (验证 break-change)")
    void oldNumericRejected() {
        sample.setCategory("01");
        assertThatThrownBy(() -> service.insertTestCase(sample))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("category");
    }

    @Test
    @DisplayName("旧 'E2E' / 'smoke' 已废弃,转入 tags 承载;category='e2e' 视为非法")
    void oldE2eRejected() {
        sample.setCategory("e2e");
        assertThatThrownBy(() -> service.insertTestCase(sample))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("category");
    }
}
