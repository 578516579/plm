package cn.com.bosssfot.dv.plm.competitive.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.competitive.domain.Competitive;

public interface ICompetitiveService {
    List<Competitive> selectCompetitiveList(Competitive competitive);
    Competitive selectCompetitiveById(Long competitiveId);
    int insertCompetitive(Competitive competitive);
    int updateCompetitive(Competitive competitive);
    int deleteCompetitiveByIds(Long[] competitiveIds);

    /** AI 生成 SWOT + 综合报告 (PRD §F1.3 - competitive-analysis-flow);本期 mock 实现 */
    Competitive aiAnalyze(Long competitiveId);
}
