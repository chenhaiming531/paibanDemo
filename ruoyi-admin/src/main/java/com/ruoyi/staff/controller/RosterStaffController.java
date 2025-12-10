package com.ruoyi.staff.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.detail.domain.Pair;
import com.ruoyi.detail.domain.RosterScheduleDetail;
import com.ruoyi.detail.service.IRosterScheduleDetailService;
import com.ruoyi.duty.domain.RosterDuty;
import com.ruoyi.duty.service.IRosterDutyService;
import com.ruoyi.relation.domain.RosterStaffRelation;
import com.ruoyi.relation.service.IRosterStaffRelationService;
import com.ruoyi.room.domain.RosterRoom;
import com.ruoyi.room.service.IRosterRoomService;
import com.ruoyi.table.domain.OperatingTable;
import com.ruoyi.table.service.IOperatingTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.ruoyi.staff.domain.RosterStaff;
import com.ruoyi.staff.service.IRosterStaffService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 排班人员信息Controller
 * 
 * @author xiao
 * @date 2025-08-12
 */
@RestController
@RequestMapping("/staff/staff")
public class RosterStaffController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(com.ruoyi.staff.controller.RosterStaffController.class);

    @Autowired
    private IRosterStaffService rosterStaffService;
    @Autowired
    private IRosterStaffRelationService rosterStaffRelationService;
    @Autowired
    private IRosterDutyService rosterDutyService;
    @Autowired
    private IRosterScheduleDetailService rosterScheduleDetailService;
    @Autowired
    private IRosterRoomService rosterRoomService;
    @Autowired
    private IOperatingTableService operatingTableService;

//    /**
//     * 批量设置标签
//     */
//    @PreAuthorize("@ss.hasPermi('staff:staff:edit')")
//    @Log(title = "排班人员信息-设置标签", businessType = BusinessType.UPDATE)
//    @PostMapping("/batchTag")
//    public AjaxResult batchTag(@RequestBody List<RosterStaff> staffList) {
//        System.out.println(staffList);
//        return toAjax(rosterStaffService.batchUpdateStaffLeave(staffList));
//    }

    /**
     * 批量设置标签
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:edit')")
    @Log(title = "排班人员信息-设置标签", businessType = BusinessType.UPDATE)
    @PostMapping("/batchTag")
    public AjaxResult batchTag(@RequestBody List<Map<String, Object>> paramsList) {
        try {
            // 提取所有时间范围（从第一个人员获取，因为所有人员的时间范围相同）
            List<Map<String, String>> allTimeRanges = (List<Map<String, String>>) paramsList.get(0).get("timeRanges");
            
            // 获取休假标签类型
            String staffLeaveTag = (String) paramsList.get(0).get("staffLeave");
            
            // 获取最早的开始时间和最晚的结束时间
            String earliestStartTime = null;
            String latestEndTime = null;
            
            if (allTimeRanges != null && !allTimeRanges.isEmpty()) {
                for (Map<String, String> timeRange : allTimeRanges) {
                    String startTime = timeRange.get("startTime");
                    String endTime = timeRange.get("endTime");
                    
                    if (startTime != null) {
                        if (earliestStartTime == null || startTime.compareTo(earliestStartTime) < 0) {
                            earliestStartTime = startTime;
                        }
                    }
                    if (endTime != null) {
                        if (latestEndTime == null || endTime.compareTo(latestEndTime) > 0) {
                            latestEndTime = endTime;
                        }
                    }
                }
            }

            // 处理所有时间范围，获取每个时间段的中间日期
            List<String> allDates = new ArrayList<>();
            for (Map<String, String> timeRange : allTimeRanges) {
                String startTime = timeRange.get("startTime");
                String endTime = timeRange.get("endTime");

                // 获取该时间范围内的所有中间日期
                List<String> datesBetween = getDatesBetween(startTime, endTime);
                allDates.addAll(datesBetween);

                System.out.println("时间段 " + startTime + " 到 " + endTime + " 的中间日期: " + datesBetween);
            }

            // 去重（如果有重复的时间段）
            allDates = allDates.stream().distinct().collect(Collectors.toList());
            System.out.println("所有中间日期: " + allDates);

            // 提取所有ID - 安全地转换ID
            List<Integer> ids = new ArrayList<>();
            for (Map<String, Object> param : paramsList) {
                Object idObj = param.get("id");
                if (idObj != null) {
                    if (idObj instanceof Integer) {
                        ids.add((Integer) idObj);
                    } else if (idObj instanceof String) {
                        ids.add(Integer.parseInt((String) idObj));
                    } else if (idObj instanceof Number) {
                        ids.add(((Number) idObj).intValue());
                    }
                }
            }
            
            // 保存休假时间到员工记录
            logger.warn("【诊断】batchTag: earliestStartTime={}, latestEndTime={}, staffLeaveTag={}",
                    earliestStartTime, latestEndTime, staffLeaveTag);
            
            for (Integer staffId : ids) {
                RosterStaff staff = new RosterStaff();
                staff.setId(staffId.longValue());
                staff.setStaffLeave(staffLeaveTag);
                staff.setStaffLeaveStartTime(earliestStartTime);
                staff.setStaffLeaveEndTime(latestEndTime);
                rosterStaffService.updateRosterStaff(staff);
                logger.warn("【诊断】已保存员工 ID={} 的休假数据: startTime={}, endTime={}",
                        staffId, earliestStartTime, latestEndTime);
            }
            
            for (int i = 0; i < allDates.size(); i++) {
                // 传入的日期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date inputDate = sdf.parse(allDates.get(i));

                // 转换为Calendar操作
                Calendar cal = Calendar.getInstance();
                cal.setTime(inputDate);
                intelligentScheduleForDay(inputDate, ids);
            }

            return AjaxResult.success("操作成功", allDates);
        } catch (Exception e) {
            log.error("批量设置标签失败", e);
            return AjaxResult.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取两个日期之间的所有日期（包含开始和结束日期）
     * @param startDateStr 开始日期，格式：yyyy-MM-dd
     * @param endDateStr 结束日期，格式：yyyy-MM-dd
     * @return 日期字符串列表，格式：yyyy-MM-dd
     */
    private List<String> getDatesBetween(String startDateStr, String endDateStr) {
        List<String> dates = new ArrayList<>();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);

            // 从开始日期开始，到结束日期（包含）
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                dates.add(currentDate.format(formatter));
                currentDate = currentDate.plusDays(1);
            }
        } catch (Exception e) {
            log.error("解析日期失败: startDate={}, endDate={}", startDateStr, endDateStr, e);
        }

        return dates;
    }

    /**
     * 对单日进行智能排班（修改后的逻辑，考虑前一天次班人员，并加入优先级逻辑）
     */
    private boolean intelligentScheduleForDay(Date date, List<Integer> ids) {
        try {
            // 使用Java 8的时间API将时间部分截断
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);
            Date truncatedDate = Date.from(truncatedDateTime.atZone(ZoneId.systemDefault()).toInstant());

            // 获取前一天的日期
            LocalDateTime previousDay = truncatedDateTime.minusDays(1);
            Date previousDate = Date.from(previousDay.atZone(ZoneId.systemDefault()).toInstant());

            // 获取当日的值班人员
            RosterDuty dutyQuery = new RosterDuty();
            dutyQuery.setDutyTime(truncatedDate);
            List<RosterDuty> dutyList = rosterDutyService.selectRosterDutyList(dutyQuery);

            RosterScheduleDetail rosterScheduleDetail = new RosterScheduleDetail();
            rosterScheduleDetail.setDate(truncatedDate);
            List<RosterScheduleDetail> rosterScheduleDetails = rosterScheduleDetailService.selectRosterScheduleDetailList(rosterScheduleDetail);

            // 获取前一天的所有排班详情
            RosterScheduleDetail previousDayDetail = new RosterScheduleDetail();
            previousDayDetail.setDate(previousDate);
            List<RosterScheduleDetail> previousDayScheduleDetails = rosterScheduleDetailService.selectRosterScheduleDetailList(previousDayDetail);

            // 获取当天所有已经排班的各类型人员ID
            Map<Long, Set<Long>> alreadyScheduledStaffIds = new HashMap<>();
            for (RosterScheduleDetail detail : rosterScheduleDetails) {
                RosterStaffRelation relationQuery = new RosterStaffRelation();
                relationQuery.setScheduleDetailId(detail.getId());
                List<RosterStaffRelation> relations = rosterStaffRelationService.selectRosterStaffRelationList(relationQuery);

                for (RosterStaffRelation relation : relations) {
                    RosterStaff staff = relation.getRosterStaff();
                    if (staff != null && staff.getStaffTypeId() != null) {
                        Long staffTypeId = staff.getStaffTypeId();
                        alreadyScheduledStaffIds.computeIfAbsent(staffTypeId, k -> new HashSet<>()).add(staff.getId());
                    }
                }
            }

            // 获取前一天的所有次班人员（类型为7L、8L、10L）
            Set<Long> previousDaySecondaryStaff = new HashSet<>();
            for (RosterScheduleDetail detail : previousDayScheduleDetails) {
                RosterStaffRelation relationQuery = new RosterStaffRelation();
                relationQuery.setScheduleDetailId(detail.getId());
                List<RosterStaffRelation> relations = rosterStaffRelationService.selectRosterStaffRelationList(relationQuery);

                for (RosterStaffRelation relation : relations) {
                    RosterStaff staff = relation.getRosterStaff();
                    if (staff != null && (staff.getStaffTypeId() == 7L || staff.getStaffTypeId() == 8L || staff.getStaffTypeId() == 10L)) {
                        previousDaySecondaryStaff.add(staff.getId());
                    }
                }
            }

            // 查询所有可用的各类型人员（排除当前被标记的人员、已经排班的人员和前一天的次班人员）
            Map<Long, List<RosterStaff>> availableStaffMap = new HashMap<>();
            Map<Long, List<Integer>> randomIndicesMap = new HashMap<>();
            Map<Long, Integer> randomIndexCounterMap = new HashMap<>();

            // 处理7L类型
            processStaffType(7L, ids, alreadyScheduledStaffIds, previousDaySecondaryStaff, availableStaffMap, randomIndicesMap, randomIndexCounterMap, truncatedDate);

            // 处理13L类型（用于顶替7L人员）
            processStaffType(13L, ids, alreadyScheduledStaffIds, previousDaySecondaryStaff, availableStaffMap, randomIndicesMap, randomIndexCounterMap, truncatedDate);

            // 处理8L、10L类型
            processStaffType(8L, ids, alreadyScheduledStaffIds, previousDaySecondaryStaff, availableStaffMap, randomIndicesMap, randomIndexCounterMap, truncatedDate);
            processStaffType(10L, ids, alreadyScheduledStaffIds, previousDaySecondaryStaff, availableStaffMap, randomIndicesMap, randomIndexCounterMap, truncatedDate);

            // 统计当前已经使用的7L人员数量
            int current7LUsageCount = alreadyScheduledStaffIds.getOrDefault(7L, new HashSet<>()).size();

            // 计算需要补足的13L人员数量
            List<RosterStaff> available7LStaff = availableStaffMap.get(7L);
            int nursingShortage = 3 - (available7LStaff != null ? available7LStaff.size() : 0);
            if (nursingShortage < 0) {
                nursingShortage = 0;
            }

            int thirteenIndexCounter = 0; // 13L随机索引计数器

            // 存储所有需要替换的房间及其优先级信息
            List<Pair<RosterScheduleDetail, RosterStaff>> roomsToReplace = new ArrayList<>();

            // 第一遍：收集所有需要替换的房间和人员信息
            for (int i = 0; i < rosterScheduleDetails.size(); i++) {
                RosterStaffRelation rosterStaffRelation = new RosterStaffRelation();
                rosterStaffRelation.setScheduleDetailId(rosterScheduleDetails.get(i).getId());
                List<RosterStaffRelation> rosterStaffRelations = rosterStaffRelationService.selectRosterStaffRelationList(rosterStaffRelation);

                for (RosterStaffRelation relation : rosterStaffRelations) {
                    Long staffId = relation.getStaffId();

                    if (staffId != null && ids.contains(staffId.intValue())) {
                        RosterStaff staff = relation.getRosterStaff();

                        if (staff != null && staff.getStaffTypeId() != null) {
                            Long staffTypeId = staff.getStaffTypeId();

                            if (staffTypeId == 5L || staffTypeId == 6L || staffTypeId == 14L) {
                                // 主班人员（5L、6L、14L），记录房间和人员信息（包含优先级）
                                roomsToReplace.add(new Pair<>(rosterScheduleDetails.get(i), staff));
                            } else if (staffTypeId == 7L || staffTypeId == 8L || staffTypeId == 10L) {
                                // 次班和护理人员，直接记录需要替换
                                roomsToReplace.add(new Pair<>(rosterScheduleDetails.get(i), staff));
                            }
                        }
                    }
                }
            }

            // 按人员优先级排序（优先级高的先处理）
            roomsToReplace.sort((a, b) -> {
                int priorityA = Optional.ofNullable(a.getRight().getStaffPriority()).orElse(0);
                int priorityB = Optional.ofNullable(b.getRight().getStaffPriority()).orElse(0);
                return Integer.compare(priorityB, priorityA); // 降序排序，优先级高的在前
            });

            // 第二遍：按优先级顺序处理替换
            for (Pair<RosterScheduleDetail, RosterStaff> pair : roomsToReplace) {
                RosterScheduleDetail scheduleDetail = pair.getLeft();
                RosterStaff originalStaff = pair.getRight();
                Long originalStaffTypeId = originalStaff.getStaffTypeId();

                // 查找对应的关联记录
                RosterStaffRelation relationToReplace = null;
                RosterStaffRelation relationQuery = new RosterStaffRelation();
                relationQuery.setScheduleDetailId(scheduleDetail.getId());
                List<RosterStaffRelation> relations = rosterStaffRelationService.selectRosterStaffRelationList(relationQuery);

                for (RosterStaffRelation relation : relations) {
                    if (relation.getStaffId().equals(originalStaff.getId())) {
                        relationToReplace = relation;
                        break;
                    }
                }

                if (relationToReplace == null) {
                    continue;
                }

                if (originalStaffTypeId == 5L || originalStaffTypeId == 6L || originalStaffTypeId == 14L) {
                    // 主班人员替换逻辑
                    System.out.println("主班人员: " + originalStaff.getStaffName() + " (ID: " + originalStaff.getId() + ", Type: " + originalStaffTypeId + ", 优先级: " + originalStaff.getStaffPriority() + ")");

                    if (current7LUsageCount < 3) {
                        // 优先使用7L人员替换
                        List<RosterStaff> filtered7LStaffList = availableStaffMap.get(7L);
                        List<Integer> random7LIndices = randomIndicesMap.get(7L);
                        Integer random7LIndexCounter = randomIndexCounterMap.get(7L);

                        if (filtered7LStaffList != null && random7LIndices != null && random7LIndexCounter != null &&
                                random7LIndexCounter < random7LIndices.size()) {
                            int randomIndex = random7LIndices.get(random7LIndexCounter);
                            RosterStaff replacementStaff = filtered7LStaffList.get(randomIndex);

                            // 创建新的关联关系
                            RosterStaffRelation newRelation = new RosterStaffRelation();
                            newRelation.setScheduleDetailId(scheduleDetail.getId());
                            newRelation.setStaffId(replacementStaff.getId());
                            rosterStaffRelationService.insertRosterStaffRelation(newRelation);

                            // 删除原来的关联关系
                            rosterStaffRelationService.deleteRosterStaffRelationById(relationToReplace.getId());

                            randomIndexCounterMap.put(7L, random7LIndexCounter + 1);
                            current7LUsageCount++;

                            // 更新已排班人员集合
                            alreadyScheduledStaffIds.computeIfAbsent(7L, k -> new HashSet<>()).add(replacementStaff.getId());

                            System.out.println("使用7L人员顶替: " + replacementStaff.getStaffName() + " (优先级: " + replacementStaff.getStaffPriority() + ")");
                        } else if (thirteenIndexCounter < nursingShortage) {
                            // 7L不足，使用13L人员
                            List<RosterStaff> filtered13LStaffList = availableStaffMap.get(13L);
                            List<Integer> random13LIndices = randomIndicesMap.get(13L);
                            Integer random13LIndexCounter = randomIndexCounterMap.get(13L);

                            if (filtered13LStaffList != null && random13LIndices != null && random13LIndexCounter != null &&
                                    random13LIndexCounter < random13LIndices.size()) {
                                int randomIndex = random13LIndices.get(random13LIndexCounter);
                                RosterStaff replacementStaff = filtered13LStaffList.get(randomIndex);

                                RosterStaffRelation newRelation = new RosterStaffRelation();
                                newRelation.setScheduleDetailId(scheduleDetail.getId());
                                newRelation.setStaffId(replacementStaff.getId());
                                rosterStaffRelationService.insertRosterStaffRelation(newRelation);

                                rosterStaffRelationService.deleteRosterStaffRelationById(relationToReplace.getId());

                                randomIndexCounterMap.put(13L, random13LIndexCounter + 1);
                                thirteenIndexCounter++;
                                current7LUsageCount++;

                                alreadyScheduledStaffIds.computeIfAbsent(13L, k -> new HashSet<>()).add(replacementStaff.getId());

                                System.out.println("使用13L人员顶替: " + replacementStaff.getStaffName() + " (优先级: " + replacementStaff.getStaffPriority() + ")");
                            } else {
                                rosterStaffRelationService.deleteRosterStaffRelationById(relationToReplace.getId());
                                System.out.println("没有可用顶替人员，删除原主班人员");
                            }
                        } else {
                            rosterStaffRelationService.deleteRosterStaffRelationById(relationToReplace.getId());
                            System.out.println("7L使用已达上限，删除原主班人员");
                        }
                    } else {
                        rosterStaffRelationService.deleteRosterStaffRelationById(relationToReplace.getId());
                        System.out.println("7L使用已达上限，删除原主班人员");
                    }
                } else if (originalStaffTypeId == 7L || originalStaffTypeId == 8L || originalStaffTypeId == 10L) {
                    // 次班和护理人员替换逻辑
                    System.out.println(originalStaffTypeId + "L人员: " + originalStaff.getStaffName() + " (优先级: " + originalStaff.getStaffPriority() + ")");

                    List<RosterStaff> filteredStaffList = availableStaffMap.get(originalStaffTypeId);
                    List<Integer> randomIndices = randomIndicesMap.get(originalStaffTypeId);
                    Integer randomIndexCounter = randomIndexCounterMap.get(originalStaffTypeId);

                    if (filteredStaffList != null && randomIndices != null && randomIndexCounter != null &&
                            randomIndexCounter < randomIndices.size()) {
                        // 按优先级排序可用人员（优先级高的优先）
                        filteredStaffList.sort((a, b) -> {
                            int priorityA = Optional.ofNullable(a.getStaffPriority()).orElse(0);
                            int priorityB = Optional.ofNullable(b.getStaffPriority()).orElse(0);
                            return Integer.compare(priorityB, priorityA); // 降序排序
                        });

                        int randomIndex = randomIndices.get(randomIndexCounter);
                        RosterStaff replacementStaff = filteredStaffList.get(randomIndex);

                        RosterStaffRelation newRelation = new RosterStaffRelation();
                        newRelation.setScheduleDetailId(scheduleDetail.getId());
                        newRelation.setStaffId(replacementStaff.getId());
                        rosterStaffRelationService.insertRosterStaffRelation(newRelation);

                        rosterStaffRelationService.deleteRosterStaffRelationById(relationToReplace.getId());

                        randomIndexCounterMap.put(originalStaffTypeId, randomIndexCounter + 1);
                        alreadyScheduledStaffIds.computeIfAbsent(originalStaffTypeId, k -> new HashSet<>()).add(replacementStaff.getId());

                        System.out.println("替换为随机" + originalStaffTypeId + "L人员: " + replacementStaff.getStaffName() + " (优先级: " + replacementStaff.getStaffPriority() + ")");
                    } else {
                        rosterStaffRelationService.deleteRosterStaffRelationById(relationToReplace.getId());
                        System.out.println("没有可用替换人员，删除原人员");
                    }
                }
            }

            return true;

        } catch (Exception e) {
            logger.error("单日智能排班失败", e);
            return false;
        }
    }

    /**
     * 处理特定类型的人员（修改后，考虑前一天次班人员和休假时间）
     */
    private void processStaffType(Long staffTypeId, List<Integer> ids,
                                  Map<Long, Set<Long>> alreadyScheduledStaffIds,
                                  Set<Long> previousDaySecondaryStaff,
                                  Map<Long, List<RosterStaff>> availableStaffMap,
                                  Map<Long, List<Integer>> randomIndicesMap,
                                  Map<Long, Integer> randomIndexCounterMap,
                                  Date truncatedDate) {
        try {
            // 查询所有可用的该类型人员
            RosterStaff staffType = new RosterStaff();
            staffType.setStaffTypeId(staffTypeId);
            staffType.setStatus("0"); // 添加状态过滤条件
            List<RosterStaff> availableStaffList = rosterStaffService.selectRosterStaffList2(staffType);

            // 过滤掉当前被标记的人员（ids中的该类型人员）、已经排班的人员、前一天的次班人员和休假期间的人员
            Set<Long> alreadyScheduled = alreadyScheduledStaffIds.getOrDefault(staffTypeId, new HashSet<>());
            List<RosterStaff> filteredStaffList = availableStaffList.stream()
                    .filter(staff -> !ids.contains(staff.getId().intValue())) // 排除当前被标记的人员
                    .filter(staff -> !alreadyScheduled.contains(staff.getId())) // 排除已经排班的人员
                    .filter(staff -> !previousDaySecondaryStaff.contains(staff.getId())) // 排除前一天的次班人员
                    .filter(staff -> !isStaffOnLeave(staff, truncatedDate)) // 排除休假期间的人员
                    .collect(Collectors.toList());

            // 创建随机索引列表
            List<Integer> randomIndices = new ArrayList<>();
            for (int i = 0; i < filteredStaffList.size(); i++) {
                randomIndices.add(i);
            }
            Collections.shuffle(randomIndices);

            availableStaffMap.put(staffTypeId, filteredStaffList);
            randomIndicesMap.put(staffTypeId, randomIndices);
            randomIndexCounterMap.put(staffTypeId, 0);

            System.out.println("类型 " + staffTypeId + "L 可用人员数量: " + filteredStaffList.size());

        } catch (Exception e) {
            logger.error("处理{}L类型人员时出错", staffTypeId, e);
        }
    }

    /**
     * 判断员工在指定日期是否在休假
     */
    private boolean isStaffOnLeave(RosterStaff staff, Date checkDate) {
        if (staff == null || checkDate == null) {
            return false;
        }

        String staffLeaveStartTime = staff.getStaffLeaveStartTime();
        String staffLeaveEndTime = staff.getStaffLeaveEndTime();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String checkDateStr = sdf.format(checkDate);
        String staffName = staff.getStaffName();
        
        // 沒有设置休假时间，则不在休假
        if (staffLeaveStartTime == null || staffLeaveEndTime == null) {
            return false;
        }

        try {
            // 提取开始时间的日期部分（处理 "2025-12-18 00:00:00" 格式）
            String startDateStr = staffLeaveStartTime;
            if (startDateStr.contains(" ")) {
                startDateStr = startDateStr.substring(0, 10);
            }

            // 提取结束时间的日期部分（处理 "2025-12-18 00:00:00" 格式）
            String endDateStr = staffLeaveEndTime;
            if (endDateStr.contains(" ")) {
                endDateStr = endDateStr.substring(0, 10);
            }

            // 比较日期，如果在休假时间段内，则返回true
            boolean isOnLeave = checkDateStr.compareTo(startDateStr) >= 0 && checkDateStr.compareTo(endDateStr) <= 0;
            
            if (isOnLeave) {
                logger.warn("*** 【】 {} 在日期 {} 处于休假期间（{}~{}）***",
                        staffName, checkDateStr, startDateStr, endDateStr);
            }
            
            return isOnLeave;
        } catch (Exception e) {
            logger.error("判断休假时间失败", e);
            return false;
        }
    }

    /**
     * 批量取消标签
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:edit')")
    @Log(title = "排班人员信息-取消标签", businessType = BusinessType.UPDATE)
    @PostMapping("/batchCancelTag")
    public AjaxResult batchCancelTag(@RequestBody List<RosterStaff> staffList) {
        return toAjax(rosterStaffService.updateExpiredLeaveRecords(staffList));
    }

    public void autoExpireLeaveRecords() {
        log.info("开始自动处理过期请假记录...");

        // 获取昨天的日期（因为当前时间是凌晨，今天的0点1分，昨天就是"今天"的前一天）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();

        // 执行批量更新
        int affectedRows = rosterStaffService.batchExpireLeaveRecords(yesterday);

        log.info("请假记录自动过期处理完成，共更新了 {} 条记录", affectedRows);
    }

    /**
     * 查询排班人员信息列表
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:list')")
    @GetMapping("/list")
    public TableDataInfo list(RosterStaff rosterStaff)
    {
        autoExpireLeaveRecords();
        startPage();
        List<RosterStaff> list = rosterStaffService.selectRosterStaffList(rosterStaff);
        return getDataTable(list);
    }

    /**
     * 导出排班人员信息列表
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:export')")
    @Log(title = "排班人员信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RosterStaff rosterStaff)
    {
        List<RosterStaff> list = rosterStaffService.selectRosterStaffList(rosterStaff);
        ExcelUtil<RosterStaff> util = new ExcelUtil<RosterStaff>(RosterStaff.class);
        util.exportExcel(response, list, "排班人员信息数据");
    }

    /**
     * 获取排班人员信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(rosterStaffService.selectRosterStaffById(id));
    }

    /**
     * 新增排班人员信息
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:add')")
    @Log(title = "排班人员信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RosterStaff rosterStaff)
    {
        return toAjax(rosterStaffService.insertRosterStaff(rosterStaff));
    }

    /**
     * 修改排班人员信息
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:edit')")
    @Log(title = "排班人员信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RosterStaff rosterStaff)
    {
        return toAjax(rosterStaffService.updateRosterStaff(rosterStaff));
    }

    /**
     * 删除排班人员信息
     */
    @PreAuthorize("@ss.hasPermi('staff:staff:remove')")
    @Log(title = "排班人员信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(rosterStaffService.deleteRosterStaffByIds(ids));
    }
}
