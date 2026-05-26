package cn.com.bosssfot.dv.plm.integration.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationUserMapping;

/**
 * 集成用户映射 Service
 */
public interface IIntegrationUserMappingService {

    List<IntegrationUserMapping> selectList(IntegrationUserMapping query);

    IntegrationUserMapping selectById(Long id);

    /**
     * 入站常用:按外部账号查 PLM user_id。
     * 缺映射 → 返回 null(容忍)。
     * 命中 → 异步刷新 last_used_at。
     */
    Long resolveUserIdByExternalAccount(Long connectorId, String externalAccount);

    /**
     * 出站常用:按 PLM user_id 查外部账号。
     * 缺映射 → 返回 null。
     */
    String resolveExternalAccountByUserId(Long connectorId, Long userId);

    int insert(IntegrationUserMapping mapping);

    int update(IntegrationUserMapping mapping);

    int deleteByIds(Long[] ids);
}
