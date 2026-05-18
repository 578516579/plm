package cn.com.bosssfot.dv.plm.dora.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.dora.domain.Dora;

/**
 * DORA效能指标 Mapper 接口
 */
public interface DoraMapper
{
    public List<Dora> selectDoraList(Dora dora);

    public Dora selectDoraById(Long doraId);

    public int insertDora(Dora dora);

    public int updateDora(Dora dora);

    public int deleteDoraByIds(Long[] doraIds);

    /** 查最大流水号（DOR-YYYY- 前缀） */
    public Integer selectMaxSeqOfYear(String prefix);
}
