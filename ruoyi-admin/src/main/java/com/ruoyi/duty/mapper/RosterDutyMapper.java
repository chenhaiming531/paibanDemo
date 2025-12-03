package com.ruoyi.duty.mapper;

import java.util.List;
import com.ruoyi.duty.domain.RosterDuty;

/**
 * 人员值班Mapper接口
 * 
 * @author xiao
 * @date 2025-09-03
 */
public interface RosterDutyMapper 
{
    /**
     * 查询人员值班
     * 
     * @param id 人员值班主键
     * @return 人员值班
     */
    public RosterDuty selectRosterDutyById(Long id);

    /**
     * 查询人员值班列表
     * 
     * @param rosterDuty 人员值班
     * @return 人员值班集合
     */
    public List<RosterDuty> selectRosterDutyList(RosterDuty rosterDuty);

    /**
     * 新增人员值班
     * 
     * @param rosterDuty 人员值班
     * @return 结果
     */
    public int insertRosterDuty(RosterDuty rosterDuty);

    /**
     * 修改人员值班
     * 
     * @param rosterDuty 人员值班
     * @return 结果
     */
    public int updateRosterDuty(RosterDuty rosterDuty);

    /**
     * 删除人员值班
     * 
     * @param id 人员值班主键
     * @return 结果
     */
    public int deleteRosterDutyById(Long id);

    /**
     * 批量删除人员值班
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRosterDutyByIds(Long[] ids);
}
