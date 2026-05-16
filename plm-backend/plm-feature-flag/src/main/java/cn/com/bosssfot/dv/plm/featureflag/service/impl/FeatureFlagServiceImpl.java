package cn.com.bosssfot.dv.plm.featureflag.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Feature Flag Service — DevOps 扩展 + 原型 featureflag.html
 *
 * 落地:
 * - 灰度策略: all_on / canary (按 userId 哈希) / all_off
 * - 环境隔离: (flagKey, environment) 唯一
 * - status: 00 开启, 01 关闭; 紧急开关 → 一键 status=01 + rollout=0
 */
@Service
public class FeatureFlagServiceImpl implements IFeatureFlagService {
    private static final Logger log = LoggerFactory.getLogger(FeatureFlagServiceImpl.class);

    private static final Set<String> ALLOWED_ENV      = Set.of("test","staging","prod");
    private static final Set<String> ALLOWED_STRATEGY = Set.of("all_on","canary","all_off");

    @Autowired private FeatureFlagMapper flagMapper;

    @Override
    public List<FeatureFlag> selectFeatureFlagList(FeatureFlag t) { return flagMapper.selectFeatureFlagList(t); }

    @Override
    public FeatureFlag selectFeatureFlagById(Long id) { return flagMapper.selectFeatureFlagById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFeatureFlag(FeatureFlag t) {
        if (StringUtils.isBlank(t.getFlagKey()))     throw new ServiceException("Flag Key 不能为空", 602);
        if (StringUtils.isBlank(t.getTitle()))       throw new ServiceException("功能说明不能为空", 602);
        if (StringUtils.isBlank(t.getEnvironment())) throw new ServiceException("环境不能为空", 602);
        if (!ALLOWED_ENV.contains(t.getEnvironment()))
            throw new ServiceException("无效的环境: " + t.getEnvironment(), 604);
        if (t.getAuthorUserId() == null)             throw new ServiceException("创建者不能为空", 602);

        // 校验 flagKey snake_case
        if (!t.getFlagKey().matches("^[a-z][a-z0-9_]*$"))
            throw new ServiceException("Flag Key 必须 snake_case (小写字母数字下划线)", 604);

        // 默认: 关闭 0%
        if (StringUtils.isBlank(t.getRolloutStrategy())) t.setRolloutStrategy("all_off");
        if (!ALLOWED_STRATEGY.contains(t.getRolloutStrategy()))
            throw new ServiceException("无效的灰度策略: " + t.getRolloutStrategy(), 604);
        if (t.getRolloutPercentage() == null) t.setRolloutPercentage(0);
        if (t.getRolloutPercentage() < 0 || t.getRolloutPercentage() > 100)
            throw new ServiceException("灰度百分比必须 0-100", 604);

        // 校验策略 ↔ 百分比 一致
        validateStrategyPercent(t.getRolloutStrategy(), t.getRolloutPercentage());

        if (StringUtils.isBlank(t.getStatus()))  t.setStatus("01"); // 默认关闭
        if (StringUtils.isBlank(t.getFlagNo()))  t.setFlagNo(generateFlagNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return flagMapper.insertFeatureFlag(t);
        } catch (DuplicateKeyException e) {
            log.warn("flag 唯一键冲突: {}", e.getMessage());
            // 重试一次 no
            t.setFlagNo(generateFlagNo());
            try {
                return flagMapper.insertFeatureFlag(t);
            } catch (DuplicateKeyException e2) {
                throw new ServiceException("Flag " + t.getFlagKey() + "@" + t.getEnvironment() + " 已存在", 701);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFeatureFlag(FeatureFlag t) {
        FeatureFlag old = flagMapper.selectFeatureFlagById(t.getFlagId());
        if (old == null) throw new ServiceException("Feature Flag 不存在", 404);
        if (t.getEnvironment() != null && !ALLOWED_ENV.contains(t.getEnvironment()))
            throw new ServiceException("无效的环境: " + t.getEnvironment(), 604);
        if (t.getRolloutStrategy() != null && !ALLOWED_STRATEGY.contains(t.getRolloutStrategy()))
            throw new ServiceException("无效的灰度策略: " + t.getRolloutStrategy(), 604);
        if (t.getRolloutPercentage() != null && (t.getRolloutPercentage() < 0 || t.getRolloutPercentage() > 100))
            throw new ServiceException("灰度百分比必须 0-100", 604);

        String strategy = t.getRolloutStrategy() != null ? t.getRolloutStrategy() : old.getRolloutStrategy();
        Integer percent = t.getRolloutPercentage() != null ? t.getRolloutPercentage() : old.getRolloutPercentage();
        validateStrategyPercent(strategy, percent);

        t.setUpdateBy(SecurityUtils.getUsername());
        return flagMapper.updateFeatureFlag(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFeatureFlagByIds(Long[] ids) { return flagMapper.deleteFeatureFlagByIds(ids); }

    @Override
    public boolean isEnabled(String flagKey, String environment, Long userId) {
        FeatureFlag probe = new FeatureFlag();
        probe.setFlagKey(flagKey);
        probe.setEnvironment(environment);
        List<FeatureFlag> rows = flagMapper.selectFeatureFlagList(probe);
        FeatureFlag flag = rows.stream()
                .filter(f -> flagKey.equals(f.getFlagKey()) && environment.equals(f.getEnvironment()))
                .findFirst().orElse(null);
        if (flag == null) return false;
        if (!"00".equals(flag.getStatus())) return false;

        switch (flag.getRolloutStrategy()) {
            case "all_on":  return true;
            case "all_off": return false;
            case "canary":
                if (userId == null) return false;
                int hash = Math.abs(Long.hashCode(userId)) % 100;
                return hash < flag.getRolloutPercentage();
            default: return false;
        }
    }

    private void validateStrategyPercent(String strategy, Integer percent) {
        if ("all_on".equals(strategy) && percent != null && percent != 100)
            throw new ServiceException("all_on 策略灰度百分比必须 100", 604);
        if ("all_off".equals(strategy) && percent != null && percent != 0)
            throw new ServiceException("all_off 策略灰度百分比必须 0", 604);
        if ("canary".equals(strategy) && (percent == null || percent <= 0 || percent >= 100))
            throw new ServiceException("canary 策略灰度百分比必须 1-99", 604);
    }

    private String generateFlagNo() {
        int year = LocalDate.now().getYear();
        String prefix = "FF-" + year + "-";
        Integer maxSeq = flagMapper.selectMaxSeqOfYear(prefix);
        return String.format("%s%04d", prefix, (maxSeq == null ? 0 : maxSeq) + 1);
    }
}
