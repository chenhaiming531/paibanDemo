package com.ruoyi.table.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.table.mapper.OperatingTableMapper;
import com.ruoyi.table.domain.OperatingTable;
import com.ruoyi.table.service.IOperatingTableService;

/**
 * 手术台信息Service业务层处理
 * 
 * @author xiao
 * @date 2025-08-20
 */
@Service
public class OperatingTableServiceImpl implements IOperatingTableService 
{
    @Autowired
    private OperatingTableMapper operatingTableMapper;

    /**
     * 查询手术台信息
     * 
     * @param id 手术台信息主键
     * @return 手术台信息
     */
    @Override
    public OperatingTable selectOperatingTableById(Long id)
    {
        return operatingTableMapper.selectOperatingTableById(id);
    }

    /**
     * 查询手术台信息列表
     * 
     * @param operatingTable 手术台信息
     * @return 手术台信息
     */
    @Override
    public List<OperatingTable> selectOperatingTableList(OperatingTable operatingTable)
    {
        return operatingTableMapper.selectOperatingTableList(operatingTable);
    }

    /**
     * 新增手术台信息
     * 
     * @param operatingTable 手术台信息
     * @return 结果
     */
    @Override
    public int insertOperatingTable(OperatingTable operatingTable)
    {
        operatingTable.setCreateTime(DateUtils.getNowDate());
        return operatingTableMapper.insertOperatingTable(operatingTable);
    }

    /**
     * 修改手术台信息
     * 
     * @param operatingTable 手术台信息
     * @return 结果
     */
    @Override
    public int updateOperatingTable(OperatingTable operatingTable)
    {
        operatingTable.setUpdateTime(DateUtils.getNowDate());
        return operatingTableMapper.updateOperatingTable(operatingTable);
    }

    /**
     * 批量删除手术台信息
     * 
     * @param ids 需要删除的手术台信息主键
     * @return 结果
     */
    @Override
    public int deleteOperatingTableByIds(Long[] ids)
    {
        return operatingTableMapper.deleteOperatingTableByIds(ids);
    }

    /**
     * 删除手术台信息信息
     * 
     * @param id 手术台信息主键
     * @return 结果
     */
    @Override
    public int deleteOperatingTableById(Long id)
    {
        return operatingTableMapper.deleteOperatingTableById(id);
    }

    @Override
    public List<OperatingTable> selectOperatingTableListByDetailId(Long detailId) {
        return operatingTableMapper.selectOperatingTableListByDetailId(detailId);
    }
}
