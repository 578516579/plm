package cn.com.bosssfot.dv.plm.integration.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;

public interface IntegrationConnectorMapper
{
    List<IntegrationConnector> selectConnectorList(IntegrationConnector connector);

    IntegrationConnector selectConnectorById(Long id);

    IntegrationConnector selectConnectorByCode(String connectorCode);

    int insertConnector(IntegrationConnector connector);

    int updateConnector(IntegrationConnector connector);

    int deleteConnectorByIds(Long[] ids);
}
