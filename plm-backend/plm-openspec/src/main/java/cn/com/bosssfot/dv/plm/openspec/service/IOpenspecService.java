package cn.com.bosssfot.dv.plm.openspec.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;

public interface IOpenspecService {
    List<Openspec> selectOpenspecList(Openspec openspec);
    Openspec selectOpenspecById(Long openspecId);
    int insertOpenspec(Openspec openspec);
    int updateOpenspec(Openspec openspec);
    int deleteOpenspecByIds(Long[] openspecIds);
    /** AI 生成规范骨架 (OpenAPI/AsyncAPI/AI Function/GraphQL) */
    Openspec aiGenerate(Long openspecId);
}
