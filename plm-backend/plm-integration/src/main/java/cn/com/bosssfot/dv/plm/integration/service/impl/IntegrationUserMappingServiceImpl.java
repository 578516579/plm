package cn.com.bosssfot.dv.plm.integration.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationUserMapping;
import cn.com.bosssfot.dv.plm.integration.mapper.IntegrationUserMappingMapper;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationUserMappingService;

@Service
public class IntegrationUserMappingServiceImpl implements IIntegrationUserMappingService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationUserMappingServiceImpl.class);

    @Autowired
    private IntegrationUserMappingMapper mapper;

    @Override
    public List<IntegrationUserMapping> selectList(IntegrationUserMapping query) {
        return mapper.selectList(query);
    }

    @Override
    public IntegrationUserMapping selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public Long resolveUserIdByExternalAccount(Long connectorId, String externalAccount) {
        if (connectorId == null || externalAccount == null || externalAccount.isEmpty()) return null;
        IntegrationUserMapping m = mapper.selectByConnectorAndAccount(connectorId, externalAccount);
        if (m == null) {
            log.debug("[plm-integration/user-map] 缺映射 connector={} account={}, 容忍 user_id=null", connectorId, externalAccount);
            return null;
        }
        // 异步刷新 last_used_at(单次 UPDATE,失败不影响主路径)
        try { mapper.touchLastUsed(m.getId()); } catch (Exception ignore) {}
        return m.getUserId();
    }

    @Override
    public String resolveExternalAccountByUserId(Long connectorId, Long userId) {
        if (connectorId == null || userId == null) return null;
        IntegrationUserMapping m = mapper.selectByConnectorAndUserId(connectorId, userId);
        if (m == null) {
            log.debug("[plm-integration/user-map] 缺反向映射 connector={} userId={}, 容忍 account=null", connectorId, userId);
            return null;
        }
        try { mapper.touchLastUsed(m.getId()); } catch (Exception ignore) {}
        return m.getExternalAccount();
    }

    @Override
    public int insert(IntegrationUserMapping mapping) {
        return mapper.insert(mapping);
    }

    @Override
    public int update(IntegrationUserMapping mapping) {
        return mapper.update(mapping);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        return mapper.deleteByIds(ids);
    }
}
