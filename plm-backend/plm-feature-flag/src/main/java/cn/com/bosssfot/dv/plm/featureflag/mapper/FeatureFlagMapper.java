package cn.com.bosssfot.dv.plm.featureflag.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.featureflag.domain.FeatureFlag;

public interface FeatureFlagMapper {
    List<FeatureFlag> selectFeatureFlagList(FeatureFlag flag);
    FeatureFlag selectFeatureFlagById(Long flagId);
    int insertFeatureFlag(FeatureFlag flag);
    int updateFeatureFlag(FeatureFlag flag);
    int deleteFeatureFlagByIds(Long[] flagIds);
    Integer selectMaxSeqOfYear(String prefix);
}
