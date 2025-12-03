package com.ruoyi.room.controller;

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
import com.ruoyi.room.domain.RosterRoom;
import com.ruoyi.room.service.IRosterRoomService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 房间信息Controller
 * 
 * @author xiao
 * @date 2025-08-12
 */
@RestController
@RequestMapping("/room/room")
public class RosterRoomController extends BaseController
{
    @Autowired
    private IRosterRoomService rosterRoomService;

    /**
     * 查询房间信息列表
     */
    @PreAuthorize("@ss.hasPermi('room:room:list')")
    @GetMapping("/list")
    public TableDataInfo list(RosterRoom rosterRoom)
    {
        startPage();
        List<RosterRoom> list = rosterRoomService.selectRosterRoomList(rosterRoom);
        return getDataTable(list);
    }

    /**
     * 导出房间信息列表
     */
    @PreAuthorize("@ss.hasPermi('room:room:export')")
    @Log(title = "房间信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RosterRoom rosterRoom)
    {
        List<RosterRoom> list = rosterRoomService.selectRosterRoomList(rosterRoom);
        ExcelUtil<RosterRoom> util = new ExcelUtil<RosterRoom>(RosterRoom.class);
        util.exportExcel(response, list, "房间信息数据");
    }

    /**
     * 获取房间信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('room:room:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(rosterRoomService.selectRosterRoomById(id));
    }

    /**
     * 新增房间信息
     */
    @PreAuthorize("@ss.hasPermi('room:room:add')")
    @Log(title = "房间信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RosterRoom rosterRoom)
    {
        return toAjax(rosterRoomService.insertRosterRoom(rosterRoom));
    }

    /**
     * 修改房间信息
     */
    @PreAuthorize("@ss.hasPermi('room:room:edit')")
    @Log(title = "房间信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RosterRoom rosterRoom)
    {
        return toAjax(rosterRoomService.updateRosterRoom(rosterRoom));
    }

    /**
     * 删除房间信息
     */
    @PreAuthorize("@ss.hasPermi('room:room:remove')")
    @Log(title = "房间信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(rosterRoomService.deleteRosterRoomByIds(ids));
    }
}
