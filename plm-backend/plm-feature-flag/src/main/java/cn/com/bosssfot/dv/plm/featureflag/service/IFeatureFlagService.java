package cn.com.bosssfot.dv.plm.featureflag.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.featureflag.domain.FeatureFlag;

public interface IFeatureFlagService {
    List<FeatureFlag> selectFeatureFlagList(FeatureFlag flag);
    FeatureFlag selectFeatureFlagById(Long flagId);
    int insertFeatureFlag(FeatureFlag flag);
    int updateFeatureFlag(FeatureFlag flag);
    int deleteFeatureFlagByIds(Long[] flagIds);
    /** 给定 userId 判断是否命中灰度 (按 userId 哈希) */
    boolean isEnabled(String flagKey, String environment, Long userId);
}
