package com.ruoyi.room.mapper;

import java.util.List;
import com.ruoyi.room.domain.RosterRoom;

/**
 * 房间信息Mapper接口
 * 
 * @author xiao
 * @date 2025-08-12
 */
public interface RosterRoomMapper 
{
    /**
     * 查询房间信息
     * 
     * @param id 房间信息主键
     * @return 房间信息
     */
    public RosterRoom selectRosterRoomById(Long id);

    /**
     * 查询房间信息列表
     * 
     * @param rosterRoom 房间信息
     * @return 房间信息集合
     */
    public List<RosterRoom> selectRosterRoomList(RosterRoom rosterRoom);

    /**
     * 新增房间信息
     * 
     * @param rosterRoom 房间信息
     * @return 结果
     */
    public int insertRosterRoom(RosterRoom rosterRoom);

    /**
     * 修改房间信息
     * 
     * @param rosterRoom 房间信息
     * @return 结果
     */
    public int updateRosterRoom(RosterRoom rosterRoom);

    /**
     * 删除房间信息
     * 
     * @param id 房间信息主键
     * @return 结果
     */
    public int deleteRosterRoomById(Long id);

    /**
     * 批量删除房间信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRosterRoomByIds(Long[] ids);

    public int selectRosterRoomTotalCount();
}
