package com.ruoyi.duty.controller;

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
import com.ruoyi.duty.domain.RosterDuty;
import com.ruoyi.duty.service.IRosterDutyService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 人员值班Controller
 * 
 * @author xiao
 * @date 2025-09-03
 */
@RestController
@RequestMapping("/duty/duty")
public class RosterDutyController extends BaseController
{
    @Autowired
    private IRosterDutyService rosterDutyService;

    /**
     * 查询人员值班列表
     */
    @PreAuthorize("@ss.hasPermi('duty:duty:list')")
    @GetMapping("/list")
    public TableDataInfo list(RosterDuty rosterDuty)
    {
        startPage();
        List<RosterDuty> list = rosterDutyService.selectRosterDutyList(rosterDuty);
        return getDataTable(list);
    }

    /**
     * 导出人员值班列表
     */
    @PreAuthorize("@ss.hasPermi('duty:duty:export')")
    @Log(title = "人员值班", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RosterDuty rosterDuty)
    {
        List<RosterDuty> list = rosterDutyService.selectRosterDutyList(rosterDuty);
        ExcelUtil<RosterDuty> util = new ExcelUtil<RosterDuty>(RosterDuty.class);
        util.exportExcel(response, list, "人员值班数据");
    }

    /**
     * 获取人员值班详细信息
     */
//    @PreAuthorize("@ss.hasPermi('duty:duty:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(rosterDutyService.selectRosterDutyById(id));
    }

    /**
     * 新增人员值班
     */
    @PreAuthorize("@ss.hasPermi('duty:duty:add')")
    @Log(title = "人员值班", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RosterDuty rosterDuty)
    {
        return toAjax(rosterDutyService.insertRosterDuty(rosterDuty));
    }

    /**
     * 修改人员值班
     */
//    @PreAuthorize("@ss.hasPermi('duty:duty:edit')")
    @Log(title = "人员值班", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RosterDuty rosterDuty)
    {
        return toAjax(rosterDutyService.updateRosterDuty(rosterDuty));
    }

    /**
     * 删除人员值班
     */
    @PreAuthorize("@ss.hasPermi('duty:duty:remove')")
    @Log(title = "人员值班", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(rosterDutyService.deleteRosterDutyByIds(ids));
    }
}
