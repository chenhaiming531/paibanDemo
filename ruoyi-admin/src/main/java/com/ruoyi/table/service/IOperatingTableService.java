package com.ruoyi.table.service;

import java.util.List;
import com.ruoyi.table.domain.OperatingTable;

/**
 * 手术台信息Service接口
 * 
 * @author xiao
 * @date 2025-08-20
 */
public interface IOperatingTableService 
{
    /**
     * 查询手术台信息
     * 
     * @param id 手术台信息主键
     * @return 手术台信息
     */
    public OperatingTable selectOperatingTableById(Long id);

    /**
     * 查询手术台信息列表
     * 
     * @param operatingTable 手术台信息
     * @return 手术台信息集合
     */
    public List<OperatingTable> selectOperatingTableList(OperatingTable operatingTable);

    /**
     * 新增手术台信息
     * 
     * @param operatingTable 手术台信息
     * @return 结果
     */
    public int insertOperatingTable(OperatingTable operatingTable);

    /**
     * 修改手术台信息
     * 
     * @param operatingTable 手术台信息
     * @return 结果
     */
    public int updateOperatingTable(OperatingTable operatingTable);

    /**
     * 批量删除手术台信息
     * 
     * @param ids 需要删除的手术台信息主键集合
     * @return 结果
     */
    public int deleteOperatingTableByIds(Long[] ids);

    /**
     * 删除手术台信息信息
     * 
     * @param id 手术台信息主键
     * @return 结果
     */
    public int deleteOperatingTableById(Long id);

    public List<OperatingTable> selectOperatingTableListByDetailId(Long detailId);
}
