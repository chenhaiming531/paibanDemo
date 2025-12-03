package com.ruoyi.staff.service.impl;

import java.util.Date;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.staff.mapper.RosterStaffMapper;
import com.ruoyi.staff.domain.RosterStaff;
import com.ruoyi.staff.service.IRosterStaffService;

/**
 * 排班人员信息Service业务层处理
 * 
 * @author xiao
 * @date 2025-08-12
 */
@Service
public class RosterStaffServiceImpl implements IRosterStaffService 
{
    @Autowired
    private RosterStaffMapper rosterStaffMapper;

    /**
     * 查询排班人员信息
     * 
     * @param id 排班人员信息主键
     * @return 排班人员信息
     */
    @Override
    public RosterStaff selectRosterStaffById(Long id)
    {
        return rosterStaffMapper.selectRosterStaffById(id);
    }

    /**
     * 查询排班人员信息列表
     * 
     * @param rosterStaff 排班人员信息
     * @return 排班人员信息
     */
    @Override
    public List<RosterStaff> selectRosterStaffList(RosterStaff rosterStaff)
    {
        return rosterStaffMapper.selectRosterStaffList(rosterStaff);
    }

    @Override
    public List<RosterStaff> selectRosterStaffList2(RosterStaff rosterStaff) {
        return rosterStaffMapper.selectRosterStaffList2(rosterStaff);
    }

    /**
     * 新增排班人员信息
     * 
     * @param rosterStaff 排班人员信息
     * @return 结果
     */
    @Override
    public int insertRosterStaff(RosterStaff rosterStaff)
    {
        rosterStaff.setCreateTime(DateUtils.getNowDate());
        return rosterStaffMapper.insertRosterStaff(rosterStaff);
    }

    /**
     * 修改排班人员信息
     * 
     * @param rosterStaff 排班人员信息
     * @return 结果
     */
    @Override
    public int updateRosterStaff(RosterStaff rosterStaff)
    {
        rosterStaff.setUpdateTime(DateUtils.getNowDate());
        return rosterStaffMapper.updateRosterStaff(rosterStaff);
    }

    /**
     * 批量删除排班人员信息
     * 
     * @param ids 需要删除的排班人员信息主键
     * @return 结果
     */
    @Override
    public int deleteRosterStaffByIds(Long[] ids)
    {
        return rosterStaffMapper.deleteRosterStaffByIds(ids);
    }

    /**
     * 删除排班人员信息信息
     * 
     * @param id 排班人员信息主键
     * @return 结果
     */
    @Override
    public int deleteRosterStaffById(Long id)
    {
        return rosterStaffMapper.deleteRosterStaffById(id);
    }

    @Override
    public int batchInsertRosterStaff(List<RosterStaff> staffList) {
        int count = 0;
        for (RosterStaff staff : staffList) {
            staff.setCreateTime(DateUtils.getNowDate());
            count += rosterStaffMapper.insertRosterStaff(staff);
        }
        return count;
    }

    @Override
    public int batchUpdateStaffLeave(List<RosterStaff> staffList) {
        return rosterStaffMapper.batchUpdateStaffLeave(staffList);
    }

    @Override
    public int batchExpireLeaveRecords(Date today) {
        return rosterStaffMapper.batchExpireLeaveRecords(today);
    }

    @Override
    public int updateExpiredLeaveRecords(List<RosterStaff> staffList) {
        return rosterStaffMapper.updateExpiredLeaveRecords(staffList);
    }

    @Override
    public int clearByStaffLeaveEquals3() {
        return rosterStaffMapper.clearByStaffLeaveEquals3();
    }

    @Override
    public int countStaffLeaveByMonth(int year, int month) {
        return rosterStaffMapper.countStaffLeaveByMonth(year, month);
    }
}
