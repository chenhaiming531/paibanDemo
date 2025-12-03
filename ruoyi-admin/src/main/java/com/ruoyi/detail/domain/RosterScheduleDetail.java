package com.ruoyi.detail.domain;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.relation.domain.RosterStaffRelation;
import com.ruoyi.room.domain.RosterRoom;
import com.ruoyi.staff.domain.RosterStaff;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 安排排班对象 roster_schedule_detail
 * 
 * @author xiao
 * @date 2025-08-12
 */
public class RosterScheduleDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 排班日历 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "排班日历", width = 30, dateFormat = "yyyy-MM-dd")
    private Date date;

    /** 房间ID */
    private Long roomId;

    @Excel(name = "房间对象",targetAttr = "roomName")
    private RosterRoom room;

    private List<RosterStaffRelation> staff;

    private List<RosterStaffRelation> secondaryStaff;

    private List<RosterStaffRelation> nursings;

    /** 班次类型（1白班 2夜班） */
    private String shiftType;

    /** 是否调整过（0否 1是） */
    private String isAdjusted;

    /** 调整原因 */
    private String adjustReason;

    @Excel(name = "医生")
    private String staffName;

    @Excel(name = "护理")
    private String nursingStaffName;

    @Excel(name = "进修")
    private String secondaryStaffName;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setDate(Date date) 
    {
        this.date = date;
    }

    public Date getDate() 
    {
        return date;
    }

    public void setRoomId(Long roomId) 
    {
        this.roomId = roomId;
    }

    public Long getRoomId() 
    {
        return roomId;
    }

    public void setShiftType(String shiftType) 
    {
        this.shiftType = shiftType;
    }

    public String getShiftType() 
    {
        return shiftType;
    }

    public void setIsAdjusted(String isAdjusted) 
    {
        this.isAdjusted = isAdjusted;
    }

    public String getIsAdjusted() 
    {
        return isAdjusted;
    }

    public void setAdjustReason(String adjustReason) 
    {
        this.adjustReason = adjustReason;
    }

    public String getAdjustReason() 
    {
        return adjustReason;
    }

    public void convertNursingStaffNames() {
        if (nursings != null && !nursings.isEmpty()) {
            nursingStaffName = nursings.stream()
                    .map(relation -> relation.getRosterStaff().getStaffName())
                    .collect(Collectors.joining("、"));
        } else {
            nursingStaffName = "";
        }
    }
    public void setNursingStaffName(String nursingStaffName) {
        this.nursingStaffName = nursingStaffName;
    }

    public String getNursingStaffName() {
        return nursingStaffName;
    }
    public void setNursings(List<RosterStaffRelation> nursings) {
        this.nursings = nursings;
    }

    public List<RosterStaffRelation> getNursings() {
        return nursings;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public void setSecondaryStaffName(String secondaryStaffName) {
        this.secondaryStaffName = secondaryStaffName;
    }

    public String getSecondaryStaffName() {
        return secondaryStaffName;
    }

    public void setStaff(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffName() {
        return staffName;
    }

    public RosterRoom getRoom() {
        return room;
    }

    public void setRoom(RosterRoom room) {
        this.room = room;
    }

    public List<RosterStaffRelation> getStaff() {
        return staff;
    }

    public void setStaff(List<RosterStaffRelation> staff) {
        this.staff = staff;
    }

    public List<RosterStaffRelation> getSecondaryStaff() {
        return secondaryStaff;
    }

    public void setSecondaryStaff(List<RosterStaffRelation> secondaryStaff) {
        this.secondaryStaff = secondaryStaff;
    }
    // 添加将人员列表转换为名称字符串的方法
    public void convertStaffNames() {
        this.staffName = convertStaffListToString(this.staff);
        this.secondaryStaffName = convertStaffListToString(this.secondaryStaff);
    }

    private String convertStaffListToString(List<RosterStaffRelation> staffList) {
        if (staffList == null || staffList.isEmpty()) {
            return "";
        }
        return staffList.stream()
                .map(relation -> relation.getRosterStaff() != null ?
                        relation.getRosterStaff().getStaffName() : "")
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("date", getDate())
            .append("roomId", getRoomId())
            .append("shiftType", getShiftType())
            .append("isAdjusted", getIsAdjusted())
            .append("adjustReason", getAdjustReason())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
                .append("room", getRoom())
                .append("staff", getStaff())
                .append("secondaryStaff", getSecondaryStaff())
                .append("nursings", getNursings())
            .toString();
    }
}
