package com.ruoyi.relation.domain;

import java.util.List;

import com.ruoyi.staff.domain.RosterStaff;
import com.ruoyi.table.domain.OperatingTable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 排班人员关联对象 roster_staff_relation
 * 
 * @author xiao
 * @date 2025-08-13
 */
public class RosterStaffRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 排班明细ID */
    @Excel(name = "排班明细ID")
    private Long scheduleDetailId;

    /** 人员ID */
    @Excel(name = "人员ID")
    private Long staffId;

    /** 排班人员信息信息 */
    private RosterStaff rosterStaff;

    /** 关联的多个手术台信息（一对多） */
    private List<OperatingTable> operatingTables;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setScheduleDetailId(Long scheduleDetailId) 
    {
        this.scheduleDetailId = scheduleDetailId;
    }

    public Long getScheduleDetailId() 
    {
        return scheduleDetailId;
    }

    public void setStaffId(Long staffId) 
    {
        this.staffId = staffId;
    }

    public Long getStaffId() 
    {
        return staffId;
    }

    public RosterStaff getRosterStaff()
    {
        return rosterStaff;
    }

    public void setRosterStaff(RosterStaff rosterStaff)
    {
        this.rosterStaff = rosterStaff;
    }

    public List<OperatingTable> getOperatingTables()
    {
        return operatingTables;
    }

    public void setOperatingTables(List<OperatingTable> operatingTables)
    {
        this.operatingTables = operatingTables;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("scheduleDetailId", getScheduleDetailId())
            .append("staffId", getStaffId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("rosterStaffList", getRosterStaff())
                .append("operatingTables", getOperatingTables())
            .toString();
    }
}
