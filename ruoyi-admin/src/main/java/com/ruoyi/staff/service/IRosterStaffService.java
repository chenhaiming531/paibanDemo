package com.ruoyi.staff.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.staff.domain.RosterStaff;
import org.apache.ibatis.annotations.Param;

/**
 * 排班人员信息Service接口
 * 
 * @author xiao
 * @date 2025-08-12
 */
public interface IRosterStaffService 
{
    /**
     * 查询排班人员信息
     * 
     * @param id 排班人员信息主键
     * @return 排班人员信息
     */
    public RosterStaff selectRosterStaffById(Long id);

    /**
     * 查询排班人员信息列表
     * 
     * @param rosterStaff 排班人员信息
     * @return 排班人员信息集合
     */
    public List<RosterStaff> selectRosterStaffList(RosterStaff rosterStaff);
    public List<RosterStaff> selectRosterStaffList2(RosterStaff rosterStaff);

    /**
     * 新增排班人员信息
     * 
     * @param rosterStaff 排班人员信息
     * @return 结果
     */
    public int insertRosterStaff(RosterStaff rosterStaff);

    /**
     * 修改排班人员信息
     * 
     * @param rosterStaff 排班人员信息
     * @return 结果
     */
    public int updateRosterStaff(RosterStaff rosterStaff);

    /**
     * 批量删除排班人员信息
     * 
     * @param ids 需要删除的排班人员信息主键集合
     * @return 结果
     */
    public int deleteRosterStaffByIds(Long[] ids);

    /**
     * 删除排班人员信息信息
     * 
     * @param id 排班人员信息主键
     * @return 结果
     */
    public int deleteRosterStaffById(Long id);

    public int batchInsertRosterStaff(List<RosterStaff> staffList);

    public int batchUpdateStaffLeave(List<RosterStaff> staffList);

    /**
     * 批量处理过期的请假记录
     * @param today 今天的日期
     * @return 处理的记录数
     */
    int batchExpireLeaveRecords(Date today);

    int updateExpiredLeaveRecords(List<RosterStaff> staffList);
    /**
     * 批量将staff_leave_end_time=3的记录的相关字段置空
     * @return 影响的记录数（多条数据的总条数）
     */
    int clearByStaffLeaveEquals3();

    int countStaffLeaveByMonth(@Param("year") int year, @Param("month") int month);
}
