package com.ruoyi.room.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.room.mapper.RosterRoomMapper;
import com.ruoyi.room.domain.RosterRoom;
import com.ruoyi.room.service.IRosterRoomService;

/**
 * 房间信息Service业务层处理
 * 
 * @author xiao
 * @date 2025-08-12
 */
@Service
public class RosterRoomServiceImpl implements IRosterRoomService 
{
    @Autowired
    private RosterRoomMapper rosterRoomMapper;

    /**
     * 查询房间信息
     * 
     * @param id 房间信息主键
     * @return 房间信息
     */
    @Override
    public RosterRoom selectRosterRoomById(Long id)
    {
        return rosterRoomMapper.selectRosterRoomById(id);
    }

    /**
     * 查询房间信息列表
     * 
     * @param rosterRoom 房间信息
     * @return 房间信息
     */
    @Override
    public List<RosterRoom> selectRosterRoomList(RosterRoom rosterRoom)
    {
        return rosterRoomMapper.selectRosterRoomList(rosterRoom);
    }

    /**
     * 新增房间信息
     * 
     * @param rosterRoom 房间信息
     * @return 结果
     */
    @Override
    public int insertRosterRoom(RosterRoom rosterRoom)
    {
        rosterRoom.setCreateTime(DateUtils.getNowDate());
        return rosterRoomMapper.insertRosterRoom(rosterRoom);
    }

    /**
     * 修改房间信息
     * 
     * @param rosterRoom 房间信息
     * @return 结果
     */
    @Override
    public int updateRosterRoom(RosterRoom rosterRoom)
    {
        rosterRoom.setUpdateTime(DateUtils.getNowDate());
        return rosterRoomMapper.updateRosterRoom(rosterRoom);
    }

    /**
     * 批量删除房间信息
     * 
     * @param ids 需要删除的房间信息主键
     * @return 结果
     */
    @Override
    public int deleteRosterRoomByIds(Long[] ids)
    {
        return rosterRoomMapper.deleteRosterRoomByIds(ids);
    }

    /**
     * 删除房间信息信息
     * 
     * @param id 房间信息主键
     * @return 结果
     */
    @Override
    public int deleteRosterRoomById(Long id)
    {
        return rosterRoomMapper.deleteRosterRoomById(id);
    }

    @Override
    public int selectRosterRoomTotalCount() {
        return rosterRoomMapper.selectRosterRoomTotalCount();
    }
}
