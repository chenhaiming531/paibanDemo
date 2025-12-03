package com.ruoyi.type.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.staff.domain.RosterStaff;
import com.ruoyi.staff.service.IRosterStaffService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.type.domain.RosterStaffType;
import com.ruoyi.type.service.IRosterStaffTypeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 人员类型Controller
 * 
 * @author xiao
 * @date 2025-08-12
 */
@RestController
@RequestMapping("/type/type")
public class RosterStaffTypeController extends BaseController
{
    @Autowired
    private IRosterStaffTypeService rosterStaffTypeService;

    @Autowired
    private IRosterStaffService rosterStaffService;

    /**
     * 导入排班人员信息
     */
    @PreAuthorize("@ss.hasPermi('type:type:add')")
    @Log(title = "排班人员信息", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam("file") MultipartFile file, @RequestParam Long staffTypeId) throws Exception
    {
        ExcelUtil<RosterStaff> util = new ExcelUtil<>(RosterStaff.class);
        List<RosterStaff> staffList = util.importExcel(file.getInputStream());

        // 设置人员类型ID
        for (RosterStaff staff : staffList) {
            staff.setStaffTypeId(staffTypeId);
        }

        return toAjax(rosterStaffService.batchInsertRosterStaff(staffList));
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<RosterStaff> util = new ExcelUtil<>(RosterStaff.class);
        util.importTemplateExcel(response, "排班人员数据");
    }

    /**
     * 查询人员类型列表
     */
    @PreAuthorize("@ss.hasPermi('type:type:list')")
    @GetMapping("/list")
    public TableDataInfo list(RosterStaffType rosterStaffType)
    {
        startPage();
        List<RosterStaffType> list = rosterStaffTypeService.selectRosterStaffTypeList(rosterStaffType);
        return getDataTable(list);
    }

    /**
     * 导出人员类型列表
     */
    @PreAuthorize("@ss.hasPermi('type:type:export')")
    @Log(title = "人员类型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RosterStaffType rosterStaffType)
    {
        List<RosterStaffType> list = rosterStaffTypeService.selectRosterStaffTypeList(rosterStaffType);
        ExcelUtil<RosterStaffType> util = new ExcelUtil<RosterStaffType>(RosterStaffType.class);
        util.exportExcel(response, list, "人员类型数据");
    }

    /**
     * 获取人员类型详细信息
     */
    @PreAuthorize("@ss.hasPermi('type:type:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(rosterStaffTypeService.selectRosterStaffTypeById(id));
    }

    /**
     * 新增人员类型
     */
    @PreAuthorize("@ss.hasPermi('type:type:add')")
    @Log(title = "人员类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RosterStaffType rosterStaffType)
    {
        return toAjax(rosterStaffTypeService.insertRosterStaffType(rosterStaffType));
    }

    /**
     * 修改人员类型
     */
    @PreAuthorize("@ss.hasPermi('type:type:edit')")
    @Log(title = "人员类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RosterStaffType rosterStaffType)
    {
        return toAjax(rosterStaffTypeService.updateRosterStaffType(rosterStaffType));
    }

    /**
     * 删除人员类型
     */
    @PreAuthorize("@ss.hasPermi('type:type:remove')")
    @Log(title = "人员类型", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(rosterStaffTypeService.deleteRosterStaffTypeByIds(ids));
    }
}
