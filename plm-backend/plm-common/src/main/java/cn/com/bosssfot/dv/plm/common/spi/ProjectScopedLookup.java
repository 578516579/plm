package cn.com.bosssfot.dv.plm.common.spi;

/**
 * 跨模块"实体归属项目"查询 SPI。
 *
 * 用途:解决业务模块之间互引(如 release ↔ pipeline)在 Maven Reactor 循环依赖
 * 下无法直接互注 Mapper 的问题。各业务 ServiceImpl 旁挂一个 @Component 实现并
 * 由 bean 名注入到 Map<String, ProjectScopedLookup>,跨模块校验时按 entityType
 * 查找对应实现。
 *
 * 实现约定:
 * - bean 名 = entityType(如 "release" / "pipeline" / "testplan" / "testcase")
 * - resolveProjectId 接受 entity 主键 id,返回该实体所属 projectId
 * - 实体不存在 → 返回 null(由调用方判断 → 抛 702)
 *
 * 设计出处:Proposal 0028 §5 known limitation 修复;P0-2A 引入。
 */
public interface ProjectScopedLookup {

    /**
     * 业务模块标识(对应 bean 名),用作 Map&lt;String, ProjectScopedLookup&gt; 注入的 key。
     */
    String entityType();

    /**
     * 给定实体 id,返回所属 projectId。实体不存在返回 null。
     */
    Long resolveProjectId(Long entityId);
}
