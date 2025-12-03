package com.ruoyi.duty.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.staff.domain.RosterStaff;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 人员值班对象 roster_duty
 * 
 * @author xiao
 * @date 2025-09-03
 */
public class RosterDuty extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 人员值班id */
    private Long id;

    /** 人员信息Id */
    @Excel(name = "人员信息Id")
    private Long dutyStaffId;

    /** 人员值班时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "人员值班时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dutyTime;

    // 添加关联对象
    private RosterStaff staff;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setDutyStaffId(Long dutyStaffId) 
    {
        this.dutyStaffId = dutyStaffId;
    }

    public Long getDutyStaffId() 
    {
        return dutyStaffId;
    }

    public void setDutyTime(Date dutyTime) 
    {
        this.dutyTime = dutyTime;
    }

    public Date getDutyTime() 
    {
        return dutyTime;
    }

    public RosterStaff getStaff() {
        return staff;
    }

    public void setStaff(RosterStaff staff) {
        this.staff = staff;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("dutyStaffId", getDutyStaffId())
            .append("dutyTime", getDutyTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
                .append("staff", getStaff())
            .toString();
    }
}
