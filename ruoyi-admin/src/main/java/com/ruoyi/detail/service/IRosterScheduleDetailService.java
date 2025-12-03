package com.ruoyi.detail.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.detail.domain.RosterScheduleDetail;

/**
 * 安排排班Service接口
 * 
 * @author xiao
 * @date 2025-08-12
 */
public interface IRosterScheduleDetailService 
{
    /**
     * 查询安排排班
     * 
     * @param id 安排排班主键
     * @return 安排排班
     */
    public RosterScheduleDetail selectRosterScheduleDetailById(Long id);

    /**
     * 查询安排排班列表
     * 
     * @param rosterScheduleDetail 安排排班
     * @return 安排排班集合
     */
    public List<RosterScheduleDetail> selectRosterScheduleDetailList(RosterScheduleDetail rosterScheduleDetail);

    /**
     * 新增安排排班
     * 
     * @param rosterScheduleDetail 安排排班
     * @return 结果
     */
    public int insertRosterScheduleDetail(RosterScheduleDetail rosterScheduleDetail);

    /**
     * 修改安排排班
     * 
     * @param rosterScheduleDetail 安排排班
     * @return 结果
     */
    public int updateRosterScheduleDetail(RosterScheduleDetail rosterScheduleDetail);

    /**
     * 批量删除安排排班
     * 
     * @param ids 需要删除的安排排班主键集合
     * @return 结果
     */
    public int deleteRosterScheduleDetailByIds(Long[] ids);

    /**
     * 删除安排排班信息
     * 
     * @param id 安排排班主键
     * @return 结果
     */
    public int deleteRosterScheduleDetailById(Long id);

    public int selectCountByDate(Date date);

    List<Date> selectDistinctDates();
}
