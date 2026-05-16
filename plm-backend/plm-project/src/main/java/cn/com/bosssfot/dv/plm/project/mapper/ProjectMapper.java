package cn.com.bosssfot.dv.plm.project.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.project.domain.Project;

/**
 * 项目 Mapper 接口
 */
public interface ProjectMapper
{
    /** 查询项目列表 */
    public List<Project> selectProjectList(Project project);

    /** 根据ID查询项目 */
    public Project selectProjectById(Long id);

    /** 新增项目 */
    public int insertProject(Project project);

    /** 修改项目 */
    public int updateProject(Project project);

    /** 逻辑删除项目（更新 del_flag = '2'） */
    public int deleteProjectByIds(Long[] ids);

    /** ADR-0001：查"以 prefix 开头的 project_no 中"最大的 4 位流水号；无则返 null */
    public Integer selectMaxSeqOfYear(String prefix);
}
