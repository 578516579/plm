package cn.com.bosssfot.dv.plm.prd.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.prd.domain.Prd;

public interface IPrdService {
    List<Prd> selectPrdList(Prd prd);
    Prd selectPrdById(Long prdId);
    int insertPrd(Prd prd);
    int updatePrd(Prd prd);
    int deletePrdByIds(Long[] prdIds);

    /** AI 生成 PRD (PRD §F2.2 - prd-generation-flow Dify);本期占位:写库 + 返回 mock 7 段 Markdown */
    Prd aiGenerate(Long prdId);
}
