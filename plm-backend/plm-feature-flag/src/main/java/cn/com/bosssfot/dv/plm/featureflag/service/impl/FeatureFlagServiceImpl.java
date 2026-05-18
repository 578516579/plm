package cn.com.bosssfot.dv.plm.featureflag.service.impl;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.featureflag.domain.FeatureFlag;
import cn.com.bosssfot.dv.plm.featureflag.mapper.FeatureFlagMapper;
import cn.com.bosssfot.dv.plm.featureflag.service.IFeatureFlagService;

/**
 * 功能开关 Service 实现
 *
 * 落地:
 * - flagKey 全局唯一（uk_feature_flag_key）
 * - environment 白名单: dev/test/pre/prod
 * - rolloutStrategy 白名单: all_on/all_off/percentage/user_list
 * - rolloutPercentage: percentage→1-99; all_on→100; all_off→0
 * - toggleEnabled(): Y↔N 切换
 */
@Service
public class FeatureFlagServiceImpl implements IFeatureFlagService
{
    private static final Set<String> ALLOWED_ENV =
        Set.of("dev", "test", "pre", "prod");

    private static final Set<String> ALLOWED_STRATEGY =
        Set.of("all_on", "all_off", "percentage", "user_list");

    @Autowired private FeatureFlagMapper featureFlagMapper;

    @Override
    public List<FeatureFlag> selectFeatureFlagList(FeatureFlag featureFlag) {
        return featureFlagMapper.selectFeatureFlagList(featureFlag);
    }

    @Override
    public FeatureFlag selectFeatureFlagById(Long flagId) {
        return featureFlagMapper.selectFeatureFlagById(flagId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFeatureFlag(FeatureFlag featureFlag) {
        validateFeatureFlag(featureFlag);
        applyStrategyDefaults(featureFlag);
        if (StringUtils.isBlank(featureFlag.getEnabled())) {
            featureFlag.setEnabled("N");
        }
        featureFlag.setCreateBy(SecurityUtils.getUsername());
        try {
            return featureFlagMapper.insertFeatureFlag(featureFlag);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("功能开关Key已存在: " + featureFlag.getFlagKey(), 701);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFeatureFlag(FeatureFlag featureFlag) {
        FeatureFlag old = featureFlagMapper.selectFeatureFlagById(featureFlag.getFlagId());
        if (old == null) {
            throw new ServiceException("功能开关不存在", 404);
        }
        if (StringUtils.isNotBlank(featureFlag.getFlagKey())
                && !featureFlag.getFlagKey().equals(old.getFlagKey())) {
            throw new ServiceException("功能开关Key不允许修改", 601);
        }
        if (StringUtils.isNotBlank(featureFlag.getEnvironment())
                && !ALLOWED_ENV.contains(featureFlag.getEnvironment())) {
            throw new ServiceException("环境仅支持 dev/test/pre/prod", 604);
        }
        if (StringUtils.isNotBlank(featureFlag.getRolloutStrategy())
                && !ALLOWED_STRATEGY.contains(featureFlag.getRolloutStrategy())) {
            throw new ServiceException("灰度策略仅支持 all_on/all_off/percentage/user_list", 604);
        }
        // Recalculate percentage if strategy is being changed
        String strategy = StringUtils.isNotBlank(featureFlag.getRolloutStrategy())
            ? featureFlag.getRolloutStrategy() : old.getRolloutStrategy();
        applyStrategyDefaultsForUpdate(featureFlag, strategy);

        featureFlag.setUpdateBy(SecurityUtils.getUsername());
        try {
            return featureFlagMapper.updateFeatureFlag(featureFlag);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("功能开关Key已存在", 701);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFeatureFlagByIds(Long[] flagIds) {
        return featureFlagMapper.deleteFeatureFlagByIds(flagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeatureFlag toggleEnabled(Long flagId) {
        FeatureFlag flag = featureFlagMapper.selectFeatureFlagById(flagId);
        if (flag == null) {
            throw new ServiceException("功能开关不存在", 404);
        }
        flag.setEnabled("Y".equals(flag.getEnabled()) ? "N" : "Y");
        flag.setUpdateBy(SecurityUtils.getUsername());
        featureFlagMapper.updateFeatureFlag(flag);
        return flag;
    }

    private void validateFeatureFlag(FeatureFlag f) {
        if (StringUtils.isBlank(f.getFlagKey())) {
            throw new ServiceException("开关Key不能为空", 602);
        }
        if (StringUtils.isBlank(f.getFlagName())) {
            throw new ServiceException("功能名称不能为空", 602);
        }
        if (StringUtils.isNotBlank(f.getEnvironment())
                && !ALLOWED_ENV.contains(f.getEnvironment())) {
            throw new ServiceException("环境仅支持 dev/test/pre/prod", 604);
        }
        if (StringUtils.isNotBlank(f.getRolloutStrategy())
                && !ALLOWED_STRATEGY.contains(f.getRolloutStrategy())) {
            throw new ServiceException("灰度策略仅支持 all_on/all_off/percentage/user_list", 604);
        }
    }

    private void applyStrategyDefaults(FeatureFlag f) {
        if (StringUtils.isBlank(f.getRolloutStrategy())) return;
        switch (f.getRolloutStrategy()) {
            case "all_on":
                f.setRolloutPercentage(100);
                break;
            case "all_off":
                f.setRolloutPercentage(0);
                break;
            case "percentage":
                if (f.getRolloutPercentage() == null
                        || f.getRolloutPercentage() < 1 || f.getRolloutPercentage() > 99) {
                    throw new ServiceException("灰度百分比策略下 rolloutPercentage 必须在 1-99 之间", 602);
                }
                break;
            default:
                break;
        }
    }

    private void applyStrategyDefaultsForUpdate(FeatureFlag f, String effectiveStrategy) {
        if (effectiveStrategy == null) return;
        switch (effectiveStrategy) {
            case "all_on":
                f.setRolloutPercentage(100);
                break;
            case "all_off":
                f.setRolloutPercentage(0);
                break;
            case "percentage":
                if (f.getRolloutPercentage() != null
                        && (f.getRolloutPercentage() < 1 || f.getRolloutPercentage() > 99)) {
                    throw new ServiceException("灰度百分比策略下 rolloutPercentage 必须在 1-99 之间", 602);
                }
                break;
            default:
                break;
        }
    }
}
