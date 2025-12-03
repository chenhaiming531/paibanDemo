package com.ruoyi.relation.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.staff.domain.RosterStaff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.relation.mapper.RosterStaffRelationMapper;
import com.ruoyi.relation.domain.RosterStaffRelation;
import com.ruoyi.relation.service.IRosterStaffRelationService;

/**
 * 排班人员关联Service业务层处理
 * 
 * @author xiao
 * @date 2025-08-13
 */
@Service
public class RosterStaffRelationServiceImpl implements IRosterStaffRelationService 
{
    @Autowired
    private RosterStaffRelationMapper rosterStaffRelationMapper;

    /**
     * 查询排班人员关联
     * 
     * @param id 排班人员关联主键
     * @return 排班人员关联
     */
    @Override
    public RosterStaffRelation selectRosterStaffRelationById(Long id)
    {
        return rosterStaffRelationMapper.selectRosterStaffRelationById(id);
    }

    /**
     * 查询排班人员关联列表
     * 
     * @param rosterStaffRelation 排班人员关联
     * @return 排班人员关联
     */
    @Override
    public List<RosterStaffRelation> selectRosterStaffRelationList(RosterStaffRelation rosterStaffRelation)
    {
        return rosterStaffRelationMapper.selectRosterStaffRelationList(rosterStaffRelation);
    }

    /**
     * 新增排班人员关联
     * 
     * @param rosterStaffRelation 排班人员关联
     * @return 结果
     */
    @Transactional
    @Override
    public int insertRosterStaffRelation(RosterStaffRelation rosterStaffRelation)
    {
        rosterStaffRelation.setCreateTime(DateUtils.getNowDate());
        int rows = rosterStaffRelationMapper.insertRosterStaffRelation(rosterStaffRelation);
        insertRosterStaff(rosterStaffRelation);
        return rows;
    }

    /**
     * 修改排班人员关联
     * 
     * @param rosterStaffRelation 排班人员关联
     * @return 结果
     */
    @Transactional
    @Override
    public int updateRosterStaffRelation(RosterStaffRelation rosterStaffRelation)
    {
        rosterStaffRelation.setUpdateTime(DateUtils.getNowDate());
        rosterStaffRelationMapper.deleteRosterStaffById(rosterStaffRelation.getId());
        insertRosterStaff(rosterStaffRelation);
        return rosterStaffRelationMapper.updateRosterStaffRelation(rosterStaffRelation);
    }

    /**
     * 批量删除排班人员关联
     * 
     * @param ids 需要删除的排班人员关联主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteRosterStaffRelationByIds(Long[] ids)
    {
        rosterStaffRelationMapper.deleteRosterStaffByIds(ids);
        return rosterStaffRelationMapper.deleteRosterStaffRelationByIds(ids);
    }

    /**
     * 删除排班人员关联信息
     * 
     * @param id 排班人员关联主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteRosterStaffRelationById(Long id)
    {
        rosterStaffRelationMapper.deleteRosterStaffById(id);
        return rosterStaffRelationMapper.deleteRosterStaffRelationById(id);
    }

    /**
     * 新增排班人员信息信息
     * 
     * @param rosterStaffRelation 排班人员关联对象
     */
    public void insertRosterStaff(RosterStaffRelation rosterStaffRelation)
    {
//        List<RosterStaff> rosterStaffList = rosterStaffRelation.getRosterStaffList();
//        Long id = rosterStaffRelation.getId();
//        if (StringUtils.isNotNull(rosterStaffList))
//        {
//            List<RosterStaff> list = new ArrayList<RosterStaff>();
//            for (RosterStaff rosterStaff : rosterStaffList)
//            {
//                rosterStaff.setId(id);
//                list.add(rosterStaff);
//            }
//            if (list.size() > 0)
//            {
//                rosterStaffRelationMapper.batchRosterStaff(list);
//            }
//        }
    }
}
