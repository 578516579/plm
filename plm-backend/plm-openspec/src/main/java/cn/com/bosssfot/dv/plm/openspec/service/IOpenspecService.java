package cn.com.bosssfot.dv.plm.openspec.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;

/**
 * AI规范中心 Service 接口
 */
public interface IOpenspecService
{
    /**
     * 查询规范列表
     */
    List<Openspec> selectOpenspecList(Openspec openspec);

    /**
     * 按主键查询规范
     */
    Openspec selectOpenspecById(Long openspecId);

    /**
     * 新增规范
     */
    int insertOpenspec(Openspec openspec);

    /**
     * 修改规范（含状态流转校验）
     */
    int updateOpenspec(Openspec openspec);

    /**
     * 批量删除规范
     */
    int deleteOpenspecByIds(Long[] openspecIds);

    /**
     * AI生成规范内容（OpenAPI 3.1 YAML + x-agrikb-ref），状态变为「审核中」
     */
    Openspec aiGenerate(Long openspecId);
}
