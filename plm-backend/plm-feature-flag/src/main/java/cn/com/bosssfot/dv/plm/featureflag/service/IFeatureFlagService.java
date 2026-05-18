package cn.com.bosssfot.dv.plm.featureflag.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.featureflag.domain.FeatureFlag;

/**
 * 功能开关 Service 接口
 */
public interface IFeatureFlagService
{
    /**
     * 查询功能开关列表
     */
    List<FeatureFlag> selectFeatureFlagList(FeatureFlag featureFlag);

    /**
     * 按主键查询功能开关
     */
    FeatureFlag selectFeatureFlagById(Long flagId);

    /**
     * 新增功能开关
     */
    int insertFeatureFlag(FeatureFlag featureFlag);

    /**
     * 修改功能开关
     */
    int updateFeatureFlag(FeatureFlag featureFlag);

    /**
     * 批量删除功能开关
     */
    int deleteFeatureFlagByIds(Long[] flagIds);

    /**
     * 切换开关状态 Y↔N
     */
    FeatureFlag toggleEnabled(Long flagId);
}
