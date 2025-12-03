package com.ruoyi.relation.mapper;

import java.util.List;
import com.ruoyi.relation.domain.RosterStaffRelation;
import com.ruoyi.staff.domain.RosterStaff;

/**
 * 排班人员关联Mapper接口
 * 
 * @author xiao
 * @date 2025-08-13
 */
public interface RosterStaffRelationMapper 
{
    /**
     * 查询排班人员关联
     * 
     * @param id 排班人员关联主键
     * @return 排班人员关联
     */
    public RosterStaffRelation selectRosterStaffRelationById(Long id);

    /**
     * 查询排班人员关联列表
     * 
     * @param rosterStaffRelation 排班人员关联
     * @return 排班人员关联集合
     */
    public List<RosterStaffRelation> selectRosterStaffRelationList(RosterStaffRelation rosterStaffRelation);

    /**
     * 新增排班人员关联
     * 
     * @param rosterStaffRelation 排班人员关联
     * @return 结果
     */
    public int insertRosterStaffRelation(RosterStaffRelation rosterStaffRelation);

    /**
     * 修改排班人员关联
     * 
     * @param rosterStaffRelation 排班人员关联
     * @return 结果
     */
    public int updateRosterStaffRelation(RosterStaffRelation rosterStaffRelation);

    /**
     * 删除排班人员关联
     * 
     * @param id 排班人员关联主键
     * @return 结果
     */
    public int deleteRosterStaffRelationById(Long id);

    /**
     * 批量删除排班人员关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRosterStaffRelationByIds(Long[] ids);

    /**
     * 批量删除排班人员信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRosterStaffByIds(Long[] ids);
    
    /**
     * 批量新增排班人员信息
     * 
     * @param rosterStaffList 排班人员信息列表
     * @return 结果
     */
    public int batchRosterStaff(List<RosterStaff> rosterStaffList);
    

    /**
     * 通过排班人员关联主键删除排班人员信息信息
     * 
     * @param id 排班人员关联ID
     * @return 结果
     */
    public int deleteRosterStaffById(Long id);
}
