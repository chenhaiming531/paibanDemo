package com.ruoyi.type.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.type.mapper.RosterStaffTypeMapper;
import com.ruoyi.type.domain.RosterStaffType;
import com.ruoyi.type.service.IRosterStaffTypeService;

/**
 * 人员类型Service业务层处理
 * 
 * @author xiao
 * @date 2025-08-12
 */
@Service
public class RosterStaffTypeServiceImpl implements IRosterStaffTypeService 
{
    @Autowired
    private RosterStaffTypeMapper rosterStaffTypeMapper;

    /**
     * 查询人员类型
     * 
     * @param id 人员类型主键
     * @return 人员类型
     */
    @Override
    public RosterStaffType selectRosterStaffTypeById(Long id)
    {
        return rosterStaffTypeMapper.selectRosterStaffTypeById(id);
    }

    /**
     * 查询人员类型列表
     * 
     * @param rosterStaffType 人员类型
     * @return 人员类型
     */
    @Override
    public List<RosterStaffType> selectRosterStaffTypeList(RosterStaffType rosterStaffType)
    {
        return rosterStaffTypeMapper.selectRosterStaffTypeList(rosterStaffType);
    }

    /**
     * 新增人员类型
     * 
     * @param rosterStaffType 人员类型
     * @return 结果
     */
    @Override
    public int insertRosterStaffType(RosterStaffType rosterStaffType)
    {
        rosterStaffType.setCreateTime(DateUtils.getNowDate());
        return rosterStaffTypeMapper.insertRosterStaffType(rosterStaffType);
    }

    /**
     * 修改人员类型
     * 
     * @param rosterStaffType 人员类型
     * @return 结果
     */
    @Override
    public int updateRosterStaffType(RosterStaffType rosterStaffType)
    {
        rosterStaffType.setUpdateTime(DateUtils.getNowDate());
        return rosterStaffTypeMapper.updateRosterStaffType(rosterStaffType);
    }

    /**
     * 批量删除人员类型
     * 
     * @param ids 需要删除的人员类型主键
     * @return 结果
     */
    @Override
    public int deleteRosterStaffTypeByIds(Long[] ids)
    {
        return rosterStaffTypeMapper.deleteRosterStaffTypeByIds(ids);
    }

    /**
     * 删除人员类型信息
     * 
     * @param id 人员类型主键
     * @return 结果
     */
    @Override
    public int deleteRosterStaffTypeById(Long id)
    {
        return rosterStaffTypeMapper.deleteRosterStaffTypeById(id);
    }
}
