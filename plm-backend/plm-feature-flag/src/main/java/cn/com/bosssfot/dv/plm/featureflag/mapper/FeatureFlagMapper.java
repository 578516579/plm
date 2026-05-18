package cn.com.bosssfot.dv.plm.featureflag.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.featureflag.domain.FeatureFlag;

/**
 * 功能开关 Mapper 接口
 */
public interface FeatureFlagMapper
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
     * 软删除功能开关（设 del_flag='2'）
     */
    int deleteFeatureFlagByIds(Long[] flagIds);
}
