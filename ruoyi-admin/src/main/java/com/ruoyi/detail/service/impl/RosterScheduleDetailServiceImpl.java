package com.ruoyi.detail.service.impl;

import java.util.Date;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.detail.mapper.RosterScheduleDetailMapper;
import com.ruoyi.detail.domain.RosterScheduleDetail;
import com.ruoyi.detail.service.IRosterScheduleDetailService;

/**
 * 安排排班Service业务层处理
 * 
 * @author xiao
 * @date 2025-08-12
 */
@Service
public class RosterScheduleDetailServiceImpl implements IRosterScheduleDetailService 
{
    @Autowired
    private RosterScheduleDetailMapper rosterScheduleDetailMapper;

    /**
     * 查询安排排班
     * 
     * @param id 安排排班主键
     * @return 安排排班
     */
    @Override
    public RosterScheduleDetail selectRosterScheduleDetailById(Long id)
    {
        return rosterScheduleDetailMapper.selectRosterScheduleDetailById(id);
    }

    /**
     * 查询安排排班列表
     * 
     * @param rosterScheduleDetail 安排排班
     * @return 安排排班
     */
    @Override
    public List<RosterScheduleDetail> selectRosterScheduleDetailList(RosterScheduleDetail rosterScheduleDetail)
    {
        return rosterScheduleDetailMapper.selectRosterScheduleDetailList(rosterScheduleDetail);
    }

    /**
     * 新增安排排班
     * 
     * @param rosterScheduleDetail 安排排班
     * @return 结果
     */
    @Override
    public int insertRosterScheduleDetail(RosterScheduleDetail rosterScheduleDetail)
    {
        rosterScheduleDetail.setCreateTime(DateUtils.getNowDate());
        return rosterScheduleDetailMapper.insertRosterScheduleDetail(rosterScheduleDetail);
    }

    /**
     * 修改安排排班
     * 
     * @param rosterScheduleDetail 安排排班
     * @return 结果
     */
    @Override
    public int updateRosterScheduleDetail(RosterScheduleDetail rosterScheduleDetail)
    {
        rosterScheduleDetail.setUpdateTime(DateUtils.getNowDate());
        return rosterScheduleDetailMapper.updateRosterScheduleDetail(rosterScheduleDetail);
    }

    /**
     * 批量删除安排排班
     * 
     * @param ids 需要删除的安排排班主键
     * @return 结果
     */
    @Override
    public int deleteRosterScheduleDetailByIds(Long[] ids)
    {
        return rosterScheduleDetailMapper.deleteRosterScheduleDetailByIds(ids);
    }

    /**
     * 删除安排排班信息
     * 
     * @param id 安排排班主键
     * @return 结果
     */
    @Override
    public int deleteRosterScheduleDetailById(Long id)
    { 
        return rosterScheduleDetailMapper.deleteRosterScheduleDetailById(id);
    }

    @Override
    public int selectCountByDate(Date date) {
        return rosterScheduleDetailMapper.selectCountByDate(date);
    }

    @Override
    public List<Date> selectDistinctDates() {
        return rosterScheduleDetailMapper.selectDistinctDates();
    }
}
