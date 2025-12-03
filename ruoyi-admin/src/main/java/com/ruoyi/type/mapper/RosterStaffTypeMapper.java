package com.ruoyi.type.mapper;

import java.util.List;
import com.ruoyi.type.domain.RosterStaffType;

/**
 * 人员类型Mapper接口
 * 
 * @author xiao
 * @date 2025-08-12
 */
public interface RosterStaffTypeMapper 
{
    /**
     * 查询人员类型
     * 
     * @param id 人员类型主键
     * @return 人员类型
     */
    public RosterStaffType selectRosterStaffTypeById(Long id);

    /**
     * 查询人员类型列表
     * 
     * @param rosterStaffType 人员类型
     * @return 人员类型集合
     */
    public List<RosterStaffType> selectRosterStaffTypeList(RosterStaffType rosterStaffType);

    /**
     * 新增人员类型
     * 
     * @param rosterStaffType 人员类型
     * @return 结果
     */
    public int insertRosterStaffType(RosterStaffType rosterStaffType);

    /**
     * 修改人员类型
     * 
     * @param rosterStaffType 人员类型
     * @return 结果
     */
    public int updateRosterStaffType(RosterStaffType rosterStaffType);

    /**
     * 删除人员类型
     * 
     * @param id 人员类型主键
     * @return 结果
     */
    public int deleteRosterStaffTypeById(Long id);

    /**
     * 批量删除人员类型
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRosterStaffTypeByIds(Long[] ids);
}
