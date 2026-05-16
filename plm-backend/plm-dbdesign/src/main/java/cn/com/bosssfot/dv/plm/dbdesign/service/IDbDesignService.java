package cn.com.bosssfot.dv.plm.dbdesign.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.dbdesign.domain.DbDesign;

public interface IDbDesignService {
    List<DbDesign> selectDbDesignList(DbDesign dbdesign);
    DbDesign selectDbDesignById(Long dbdesignId);
    int insertDbDesign(DbDesign dbdesign);
    int updateDbDesign(DbDesign dbdesign);
    int deleteDbDesignByIds(Long[] dbdesignIds);

    /** AI 生成 ER 图 + 建表 SQL + 数据字典 (PRD §F3.2 - db-design-flow);本期 mock */
    DbDesign aiGenerate(Long dbdesignId);
}
