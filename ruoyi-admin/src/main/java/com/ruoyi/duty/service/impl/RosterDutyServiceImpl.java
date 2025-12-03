package com.ruoyi.duty.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.duty.mapper.RosterDutyMapper;
import com.ruoyi.duty.domain.RosterDuty;
import com.ruoyi.duty.service.IRosterDutyService;

/**
 * 人员值班Service业务层处理
 * 
 * @author xiao
 * @date 2025-09-03
 */
@Service
public class RosterDutyServiceImpl implements IRosterDutyService 
{
    @Autowired
    private RosterDutyMapper rosterDutyMapper;

    /**
     * 查询人员值班
     * 
     * @param id 人员值班主键
     * @return 人员值班
     */
    @Override
    public RosterDuty selectRosterDutyById(Long id)
    {
        return rosterDutyMapper.selectRosterDutyById(id);
    }

    /**
     * 查询人员值班列表
     * 
     * @param rosterDuty 人员值班
     * @return 人员值班
     */
    @Override
    public List<RosterDuty> selectRosterDutyList(RosterDuty rosterDuty)
    {
        return rosterDutyMapper.selectRosterDutyList(rosterDuty);
    }

    /**
     * 新增人员值班
     * 
     * @param rosterDuty 人员值班
     * @return 结果
     */
    @Override
    public int insertRosterDuty(RosterDuty rosterDuty)
    {
        rosterDuty.setCreateTime(DateUtils.getNowDate());
        return rosterDutyMapper.insertRosterDuty(rosterDuty);
    }

    /**
     * 修改人员值班
     * 
     * @param rosterDuty 人员值班
     * @return 结果
     */
    @Override
    public int updateRosterDuty(RosterDuty rosterDuty)
    {
        rosterDuty.setUpdateTime(DateUtils.getNowDate());
        return rosterDutyMapper.updateRosterDuty(rosterDuty);
    }

    /**
     * 批量删除人员值班
     * 
     * @param ids 需要删除的人员值班主键
     * @return 结果
     */
    @Override
    public int deleteRosterDutyByIds(Long[] ids)
    {
        return rosterDutyMapper.deleteRosterDutyByIds(ids);
    }

    /**
     * 删除人员值班信息
     * 
     * @param id 人员值班主键
     * @return 结果
     */
    @Override
    public int deleteRosterDutyById(Long id)
    {
        return rosterDutyMapper.deleteRosterDutyById(id);
    }
}
