package cn.com.bosssfot.dv.plm.project.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.project.domain.Project;

/**
 * 项目 Service 接口
 */
public interface IProjectService
{
    /** 查询项目列表 */
    public List<Project> selectProjectList(Project project);

    /** 根据ID查询项目 */
    public Project selectProjectById(Long id);

    /** 新增项目 */
    public int insertProject(Project project);

    /** 修改项目 */
    public int updateProject(Project project);

    /** 批量删除项目 */
    public int deleteProjectByIds(Long[] ids);
}
