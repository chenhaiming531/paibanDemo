package com.ruoyi.table.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 手术台信息对象 operating_table
 *
 * @author xiao
 * @date 2025-08-20
 */
public class OperatingTable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 手术台ID */
    private Long id;

    /** 手术名称 */
    @Excel(name = "手术名称")
    private String operationName;

    /** 关联ID */
    @Excel(name = "关联ID")
    private Long relationId;

    /** 难度（1初级 2中级 3高级） */
    @Excel(name = "难度", readConverterExp = "1=初级,2=中级,3=高级")
    private int difficulty;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setOperationName(String operationName)
    {
        this.operationName = operationName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setRelationId(Long relationId)
    {
        this.relationId = relationId;
    }

    public Long getRelationId()
    {
        return relationId;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("operationName", getOperationName())
                .append("relationId", getRelationId())
                .append("difficulty", getDifficulty())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}