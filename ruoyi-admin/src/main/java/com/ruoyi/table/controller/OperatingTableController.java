package com.ruoyi.table.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.table.domain.OperatingTable;
import com.ruoyi.table.service.IOperatingTableService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 手术台信息Controller
 * 
 * @author xiao
 * @date 2025-08-20
 */
@RestController
@RequestMapping("/table/table")
public class OperatingTableController extends BaseController
{
    @Autowired
    private IOperatingTableService operatingTableService;

    /**
     * 查询手术台信息列表
     */
    @PreAuthorize("@ss.hasPermi('table:table:list')")
    @GetMapping("/list")
    public TableDataInfo list(OperatingTable operatingTable)
    {
        startPage();
        List<OperatingTable> list = operatingTableService.selectOperatingTableList(operatingTable);
        return getDataTable(list);
    }

    /**
     * 导出手术台信息列表
     */
    @PreAuthorize("@ss.hasPermi('table:table:export')")
    @Log(title = "手术台信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OperatingTable operatingTable)
    {
        List<OperatingTable> list = operatingTableService.selectOperatingTableList(operatingTable);
        ExcelUtil<OperatingTable> util = new ExcelUtil<OperatingTable>(OperatingTable.class);
        util.exportExcel(response, list, "手术台信息数据");
    }

    /**
     * 获取手术台信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('table:table:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(operatingTableService.selectOperatingTableById(id));
    }

    /**
     * 新增手术台信息
     */
    @PreAuthorize("@ss.hasPermi('table:table:add')")
    @Log(title = "手术台信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OperatingTable operatingTable)
    {
        return toAjax(operatingTableService.insertOperatingTable(operatingTable));
    }

    /**
     * 修改手术台信息
     */
    @PreAuthorize("@ss.hasPermi('table:table:edit')")
    @Log(title = "手术台信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OperatingTable operatingTable)
    {
        return toAjax(operatingTableService.updateOperatingTable(operatingTable));
    }

    /**
     * 删除手术台信息
     */
    @PreAuthorize("@ss.hasPermi('table:table:remove')")
    @Log(title = "手术台信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(operatingTableService.deleteOperatingTableByIds(ids));
    }
}
