package com.ruoyi.relation.service;

import java.util.List;
import com.ruoyi.relation.domain.RosterStaffRelation;

/**
 * 排班人员关联Service接口
 * 
 * @author xiao
 * @date 2025-08-13
 */
public interface IRosterStaffRelationService 
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
     * 批量删除排班人员关联
     * 
     * @param ids 需要删除的排班人员关联主键集合
     * @return 结果
     */
    public int deleteRosterStaffRelationByIds(Long[] ids);

    /**
     * 删除排班人员关联信息
     * 
     * @param id 排班人员关联主键
     * @return 结果
     */
    public int deleteRosterStaffRelationById(Long id);
}
