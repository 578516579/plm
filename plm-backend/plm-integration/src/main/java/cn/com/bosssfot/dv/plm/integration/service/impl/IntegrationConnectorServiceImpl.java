package cn.com.bosssfot.dv.plm.integration.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.integration.adapter.ConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.adapter.ConnectorAdapterRegistry;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.mapper.IntegrationConnectorMapper;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;

@Service
public class IntegrationConnectorServiceImpl implements IIntegrationConnectorService
{
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("0", Set.of("1", "2"));
        STATUS_TRANSITIONS.put("1", Set.of("0"));
        STATUS_TRANSITIONS.put("2", Set.of("0", "1"));
    }

    @Autowired
    private IntegrationConnectorMapper connectorMapper;

    @Autowired
    private ConnectorAdapterRegistry adapterRegistry;

    @Override
    public List<IntegrationConnector> selectConnectorList(IntegrationConnector connector) {
        return connectorMapper.selectConnectorList(connector);
    }

    @Override
    public IntegrationConnector selectConnectorById(Long id) {
        return connectorMapper.selectConnectorById(id);
    }

    @Override
    public IntegrationConnector selectConnectorByCode(String code) {
        return connectorMapper.selectConnectorByCode(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertConnector(IntegrationConnector connector) {
        if (StringUtils.isBlank(connector.getConnectorCode())) {
            throw new ServiceException("连接器编码不能为空", 601);
        }
        if (StringUtils.isBlank(connector.getConnectorType())) {
            throw new ServiceException("连接器类型不能为空", 601);
        }
        if (!adapterRegistry.supports(connector.getConnectorType())) {
            throw new ServiceException(
                "connector_type = " + connector.getConnectorType() + " 暂不支持，仅支持: "
                + String.join(", ", adapterRegistry.supportedTypes()), 810);
        }
        if (StringUtils.isBlank(connector.getStatus())) {
            connector.setStatus("0");
        }
        connector.setCreateBy(SecurityUtils.getUsername());
        return connectorMapper.insertConnector(connector);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateConnector(IntegrationConnector connector) {
        if (StringUtils.isNotBlank(connector.getStatus())) {
            IntegrationConnector old = connectorMapper.selectConnectorById(connector.getId());
            if (old == null) {
                throw new ServiceException("Connector 不存在", 805);
            }
            String oldS = old.getStatus(), newS = connector.getStatus();
            if (!oldS.equals(newS)) {
                Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldS, Set.of());
                if (!allowed.contains(newS)) {
                    throw new ServiceException(
                        "状态 " + statusLabel(oldS) + " 不能直接转到 " + statusLabel(newS), 701);
                }
            }
        }
        connector.setUpdateBy(SecurityUtils.getUsername());
        return connectorMapper.updateConnector(connector);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConnectorByIds(Long[] ids) {
        return connectorMapper.deleteConnectorByIds(ids);
    }

    @Override
    public TestResult testConnection(Long connectorId) {
        IntegrationConnector c = connectorMapper.selectConnectorById(connectorId);
        if (c == null) {
            throw new ServiceException("Connector [" + connectorId + "] 未找到", 805);
        }
        ConnectorAdapter adapter = adapterRegistry.get(c.getConnectorType());
        if (adapter == null) {
            return TestResult.fail("不支持的 connector_type = " + c.getConnectorType(), 0L);
        }
        long t0 = System.nanoTime();
        try {
            String detail = adapter.ping(c);
            long latency = (System.nanoTime() - t0) / 1_000_000;
            return TestResult.ok(detail, latency);
        } catch (Exception e) {
            long latency = (System.nanoTime() - t0) / 1_000_000;
            return TestResult.fail(e.getMessage(), latency);
        }
    }

    private static String statusLabel(String s) {
        switch (s) {
            case "0": return "启用";
            case "1": return "停用";
            case "2": return "异常";
            default:  return "未知(" + s + ")";
        }
    }
}
