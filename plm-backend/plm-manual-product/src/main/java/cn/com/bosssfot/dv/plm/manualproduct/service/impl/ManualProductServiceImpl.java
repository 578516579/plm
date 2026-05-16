package cn.com.bosssfot.dv.plm.manualproduct.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import cn.com.bosssfot.dv.plm.manualproduct.domain.ManualProduct;
import cn.com.bosssfot.dv.plm.manualproduct.mapper.ManualProductMapper;
import cn.com.bosssfot.dv.plm.manualproduct.service.IManualProductService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 产品手册 Service — PRD §F5.1 + 原型 productmanual.html
 *
 * 落地:
 * - ADR: generateManualProductNo() — PM-YYYY-NNNN
 * - PRD §F5.1: AI 一键生成 + 截图自动描述 + 多格式导出 (word/pdf/html/h5)
 * - 4 状态机: 00 草稿 → 01 生成中 → 02 已生成 → 03 已发布
 *   - 00→{01}, 01→{02}, 02→{00,03}, 03→{} (终态)
 * - 02 (已生成) 时自动填 generatedAt
 */
@Service
public class ManualProductServiceImpl implements IManualProductService
{
    private static final Logger log = LoggerFactory.getLogger(ManualProductServiceImpl.class);

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of("00", "03"));
        STATUS_TRANSITIONS.put("03", Set.of());
    }

    @Autowired private ManualProductMapper manualproductMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<ManualProduct> selectManualProductList(ManualProduct t) {
        return manualproductMapper.selectManualProductList(t);
    }

    @Override
    public ManualProduct selectManualProductById(Long id) {
        return manualproductMapper.selectManualProductById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertManualProduct(ManualProduct t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("手册标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (StringUtils.isBlank(t.getProductVersion())) {
            throw new ServiceException("产品版本不能为空", 602);
        }
        if (StringUtils.isBlank(t.getIncludeModules())) {
            throw new ServiceException("包含模块不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("作者不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        if (StringUtils.isBlank(t.getOutputFormats())) t.setOutputFormats("pdf");
        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (t.getScreenshotsCount() == null) t.setScreenshotsCount(0);
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus()) && !"01".equals(t.getStatus())) {
            throw new ServiceException("新建手册状态必须为「草稿」或「生成中」", 601);
        }

        if (StringUtils.isBlank(t.getManualproductNo())) {
            t.setManualproductNo(generateManualproductNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return manualproductMapper.insertManualProduct(t);
        } catch (DuplicateKeyException e) {
            log.warn("manualproduct_no 重号,重试一次: {}", t.getManualproductNo());
            t.setManualproductNo(generateManualproductNo());
            return manualproductMapper.insertManualProduct(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateManualProduct(ManualProduct t) {
        ManualProduct old = manualproductMapper.selectManualProductById(t.getManualproductId());
        if (old == null) {
            throw new ServiceException("产品手册不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "手册状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
            // 进入 02 (已生成) 自动填 generatedAt
            if ("02".equals(t.getStatus()) && t.getGeneratedAt() == null && old.getGeneratedAt() == null) {
                t.setGeneratedAt(new Date());
            }
        }

        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return manualproductMapper.updateManualProduct(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteManualProductByIds(Long[] ids) {
        return manualproductMapper.deleteManualProductByIds(ids);
    }

    private String generateManualproductNo() {
        int year = LocalDate.now().getYear();
        String prefix = "PM-" + year + "-";
        Integer maxSeq = manualproductMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "生成中";
            case "02": return "已生成";
            case "03": return "已发布";
            default:   return "未知(" + status + ")";
        }
    }
}
