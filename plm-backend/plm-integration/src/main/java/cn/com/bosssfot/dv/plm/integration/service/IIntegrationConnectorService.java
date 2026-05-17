package cn.com.bosssfot.dv.plm.integration.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;

public interface IIntegrationConnectorService
{
    List<IntegrationConnector> selectConnectorList(IntegrationConnector connector);

    IntegrationConnector selectConnectorById(Long id);

    IntegrationConnector selectConnectorByCode(String connectorCode);

    int insertConnector(IntegrationConnector connector);

    int updateConnector(IntegrationConnector connector);

    int deleteConnectorByIds(Long[] ids);

    /** 连通性测试：调对应 adapter.ping() */
    TestResult testConnection(Long connectorId);

    /** 测试结果 DTO */
    class TestResult {
        public boolean ok;
        public String detail;
        public long latencyMs;

        public static TestResult ok(String detail, long latency) {
            TestResult r = new TestResult(); r.ok = true; r.detail = detail; r.latencyMs = latency; return r;
        }
        public static TestResult fail(String detail, long latency) {
            TestResult r = new TestResult(); r.ok = false; r.detail = detail; r.latencyMs = latency; return r;
        }
    }
}
