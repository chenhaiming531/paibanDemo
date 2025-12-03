package com.ruoyi.relation.controller;

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
import com.ruoyi.relation.domain.RosterStaffRelation;
import com.ruoyi.relation.service.IRosterStaffRelationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 排班人员关联Controller
 * 
 * @author xiao
 * @date 2025-08-13
 */
@RestController
@RequestMapping("/relation/relation")
public class RosterStaffRelationController extends BaseController
{
    @Autowired
    private IRosterStaffRelationService rosterStaffRelationService;

    /**
     * 查询排班人员关联列表
     */
    @PreAuthorize("@ss.hasPermi('relation:relation:list')")
    @GetMapping("/list")
    public TableDataInfo list(RosterStaffRelation rosterStaffRelation)
    {
        startPage();
        List<RosterStaffRelation> list = rosterStaffRelationService.selectRosterStaffRelationList(rosterStaffRelation);
        return getDataTable(list);
    }

    /**
     * 导出排班人员关联列表
     */
    @PreAuthorize("@ss.hasPermi('relation:relation:export')")
    @Log(title = "排班人员关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RosterStaffRelation rosterStaffRelation)
    {
        List<RosterStaffRelation> list = rosterStaffRelationService.selectRosterStaffRelationList(rosterStaffRelation);
        ExcelUtil<RosterStaffRelation> util = new ExcelUtil<RosterStaffRelation>(RosterStaffRelation.class);
        util.exportExcel(response, list, "排班人员关联数据");
    }

    /**
     * 获取排班人员关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('relation:relation:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(rosterStaffRelationService.selectRosterStaffRelationById(id));
    }

    /**
     * 新增排班人员关联
     */
    @PreAuthorize("@ss.hasPermi('relation:relation:add')")
    @Log(title = "排班人员关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RosterStaffRelation rosterStaffRelation)
    {
        return toAjax(rosterStaffRelationService.insertRosterStaffRelation(rosterStaffRelation));
    }

    /**
     * 修改排班人员关联
     */
    @PreAuthorize("@ss.hasPermi('relation:relation:edit')")
    @Log(title = "排班人员关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RosterStaffRelation rosterStaffRelation)
    {
        return toAjax(rosterStaffRelationService.updateRosterStaffRelation(rosterStaffRelation));
    }

    /**
     * 删除排班人员关联
     */
    @PreAuthorize("@ss.hasPermi('relation:relation:remove')")
    @Log(title = "排班人员关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(rosterStaffRelationService.deleteRosterStaffRelationByIds(ids));
    }
}
