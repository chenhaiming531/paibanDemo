package com.ruoyi.staff.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 排班人员信息对象 roster_staff
 *
 * @author xiao
 * @date 2025-08-12
 */
public class RosterStaff extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 人员ID */
    private Long id;

    /** 人员姓名 */
    @Excel(name = "人员姓名")
    private String staffName;

    /** 人员类型ID */
    @Excel(name = "人员类型ID")
    private Long staffTypeId;

    /** 人员排班排序 */
    @Excel(name = "人员排班排序")
    private Integer staffSort;

    /** 人员值班排序 */
    @Excel(name = "人员值班排序")
    private Integer staffDutySort;

    /** 人员优先级 */
    @Excel(name = "人员优先级")
    private Integer staffPriority;

    @Excel(name = "人员标签")
    private String staffLeave;

    @Excel(name = "标签失效时间")
    private String staffLeaveEndTime;

    @Excel(name = "休假开始时间")
    private String staffLeaveStartTime;

    /** 性别（0女 1男） */
    @Excel(name = "性别", readConverterExp = "0=女,1=男")
    private String staffGender;

    /** 科室 */
    @Excel(name = "科室")
    private String department;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 职称 */
    @Excel(name = "职称")
    private String jobTitle;

    /** 在院时间 */
    @Excel(name = "在院时间")
    private Integer hospitalTime;

    /** 开始入职时间 */
    @Excel(name = "开始入职时间")
    private String beginHospitalTime;

    /** 结束入职时间 */
    @Excel(name = "结束入职时间")
    private String endHospitalTime;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setStaffPriority(Integer staffPriority) {
        this.staffPriority = staffPriority;
    }

    public Integer getStaffPriority() {
        return staffPriority;
    }

    public void setStaffDutySort(Integer staffDutySort)
        {
        this.staffDutySort = staffDutySort;
    }

    public Integer getStaffDutySort()
        {
        return staffDutySort;
    }

    public String getStaffLeaveEndTime() {
        return staffLeaveEndTime;
    }

    public void setStaffLeaveEndTime(String staffLeaveEndTime) {
        this.staffLeaveEndTime = staffLeaveEndTime;
    }

    public String getStaffLeaveStartTime() {
        return staffLeaveStartTime;
    }

    public void setStaffLeaveStartTime(String staffLeaveStartTime) {
        this.staffLeaveStartTime = staffLeaveStartTime;
    }

    public String getStaffLeave() {
        return staffLeave;
    }

    public void setStaffLeave(String staffLeave) {
        this.staffLeave = staffLeave;
    }
    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setStaffName(String staffName)
    {
        this.staffName = staffName;
    }

    public String getStaffName()
    {
        return staffName;
    }

    public void setStaffTypeId(Long staffTypeId)
    {
        this.staffTypeId = staffTypeId;
    }

    public Long getStaffTypeId()
    {
        return staffTypeId;
    }

    public void setStaffGender(String staffGender)
    {
        this.staffGender = staffGender;
    }

    public String getStaffGender()
    {
        return staffGender;
    }

    public void setDepartment(String department)
    {
        this.department = department;
    }

    public String getDepartment()
    {
        return department;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setJobTitle(String jobTitle)
    {
        this.jobTitle = jobTitle;
    }

    public String getJobTitle()
    {
        return jobTitle;
    }

    public Integer getHospitalTime()
    {
        return hospitalTime;
    }

    public void setHospitalTime(Integer hospitalTime)
    {
        this.hospitalTime = hospitalTime;
    }

    public String getBeginHospitalTime()
    {
        return beginHospitalTime;
    }

    public void setBeginHospitalTime(String beginHospitalTime)
    {
        this.beginHospitalTime = beginHospitalTime;
    }

    public String getEndHospitalTime()
    {
        return endHospitalTime;
    }

    public void setEndHospitalTime(String endHospitalTime)
    {
        this.endHospitalTime = endHospitalTime;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStaffSort(Integer staffSort)
    {
        this.staffSort = staffSort;
    }

    public Integer getStaffSort()
    {
        return staffSort;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("staffName", getStaffName())
                .append("staffTypeId", getStaffTypeId())
                .append("staffLeave", getStaffLeave())
                .append("staffSort", getStaffSort())
                .append("staffPriority", getStaffPriority())
                .append("staffDutySort", getStaffDutySort())
                .append("staffLeaveEndTime", getStaffLeaveEndTime())
                .append("staffLeaveStartTime", getStaffLeaveStartTime())
                .append("staffGender", getStaffGender())
                .append("department", getDepartment())
                .append("unit", getUnit())
                .append("jobTitle", getJobTitle())
                .append("hospitalTime", getHospitalTime())
                .append("beginHospitalTime", getBeginHospitalTime())
                .append("endHospitalTime", getEndHospitalTime())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}