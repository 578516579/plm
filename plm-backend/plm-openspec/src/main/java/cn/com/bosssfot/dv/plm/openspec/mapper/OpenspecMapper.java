package cn.com.bosssfot.dv.plm.openspec.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;

/**
 * AI规范中心 Mapper 接口
 */
public interface OpenspecMapper
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
     * 修改规范
     */
    int updateOpenspec(Openspec openspec);

    /**
     * 软删除规范（设 del_flag='2'）
     */
    int deleteOpenspecByIds(Long[] openspecIds);

    /**
     * 查询指定年份前缀的最大序号（用于编号生成）
     */
    Integer selectMaxSeqOfYear(String prefix);
}
