package cn.com.bosssfot.dv.plm.dora.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.dora.domain.Dora;

/**
 * DORA效能指标 Service 接口
 */
public interface IDoraService
{
    public List<Dora> selectDoraList(Dora dora);

    public Dora selectDoraById(Long doraId);

    public int insertDora(Dora dora);

    public int updateDora(Dora dora);

    public int deleteDoraByIds(Long[] doraIds);

    /**
     * AI生成DORA分析
     * 根据四大指标计算 doraLevel，设置 status='01', aiGenerated='Y', aiGeneratedAt=now
     */
    public int aiGenerate(Long doraId);
}
