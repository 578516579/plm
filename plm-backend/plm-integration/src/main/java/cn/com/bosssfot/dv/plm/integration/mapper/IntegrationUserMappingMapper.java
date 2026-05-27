package cn.com.bosssfot.dv.plm.integration.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationUserMapping;

/**
 * 集成用户映射 Mapper
 */
public interface IntegrationUserMappingMapper {

    List<IntegrationUserMapping> selectList(IntegrationUserMapping query);

    IntegrationUserMapping selectById(Long id);

    /** 入站常用:按 connector + 外部账号 反查 PLM user_id */
    IntegrationUserMapping selectByConnectorAndAccount(
        @Param("connectorId") Long connectorId,
        @Param("externalAccount") String externalAccount);

    /** 出站常用:按 connector + PLM userId 反查外部账号 */
    IntegrationUserMapping selectByConnectorAndUserId(
        @Param("connectorId") Long connectorId,
        @Param("userId") Long userId);

    int insert(IntegrationUserMapping mapping);

    int update(IntegrationUserMapping mapping);

    int deleteByIds(Long[] ids);

    /** 触发"最近一次使用"时间戳更新(不阻塞主路径) */
    int touchLastUsed(@Param("id") Long id);
}
