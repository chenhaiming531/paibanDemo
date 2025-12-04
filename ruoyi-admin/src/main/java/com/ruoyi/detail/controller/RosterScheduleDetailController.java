package com.ruoyi.detail.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.detail.domain.Pair;
import com.ruoyi.duty.domain.RosterDuty;
import com.ruoyi.duty.service.IRosterDutyService;
import com.ruoyi.relation.domain.RosterStaffRelation;
import com.ruoyi.relation.service.IRosterStaffRelationService;
import com.ruoyi.room.domain.RosterRoom;
import com.ruoyi.room.service.IRosterRoomService;
import com.ruoyi.staff.domain.RosterStaff;
import com.ruoyi.staff.service.IRosterStaffService;
import com.ruoyi.table.domain.OperatingTable;
import com.ruoyi.table.service.IOperatingTableService;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.detail.domain.RosterScheduleDetail;
import com.ruoyi.detail.service.IRosterScheduleDetailService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 安排排班Controller
 *
 * @author xiao
 * @date 2025-08-12
 */
@RestController
@RequestMapping("/detail/detail")
public class RosterScheduleDetailController extends BaseController
{
    @Autowired
    private IRosterScheduleDetailService rosterScheduleDetailService;

    @Autowired
    private IRosterRoomService rosterRoomService;

    @Autowired
    private IRosterStaffService rosterStaffService;

    @Autowired
    private IRosterStaffRelationService rosterStaffRelationService;

    @Autowired
    private IOperatingTableService operatingTableService;

    @Autowired
    private IRosterDutyService rosterDutyService;
    private static final Logger log = LoggerFactory.getLogger(com.ruoyi.detail.controller.RosterScheduleDetailController.class);

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
     * 获取所有不重复的排班日期
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:list')")
    @GetMapping("/distinctDates")
    public AjaxResult getDistinctDates() {
        List<Date> dates = rosterScheduleDetailService.selectDistinctDates();
        autoExpireLeaveRecords();
        return AjaxResult.success(dates);
    }

    /**
     * 获取人员值班列表
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:list')")
    @GetMapping("/listRosterDuty")
    public TableDataInfo listRosterDuty()
    {
        RosterDuty rosterDuty = new RosterDuty();
        List<RosterDuty> list = rosterDutyService.selectRosterDutyList(rosterDuty);
        return getDataTable(list);
    }

    /**
     * 检查指定月份是否有排班数据
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:list')")
    @GetMapping("/checkMonthData")
    public AjaxResult checkMonthData(@RequestParam String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date inputDate = sdf.parse(date);

        // 转换为Calendar操作
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDate);

        // 获取当月的第一天和最后一天
        cal.set(Calendar.DAY_OF_MONTH, 1);
        LocalDateTime firstDateTime = LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime truncatedFirstDateTime = firstDateTime.truncatedTo(ChronoUnit.DAYS);
        Date firstDayOfMonth = Date.from(truncatedFirstDateTime.atZone(ZoneId.systemDefault()).toInstant());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        LocalDateTime lastDateTime = LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime truncatedLastDateTime = lastDateTime.truncatedTo(ChronoUnit.DAYS);
        // 将最后一天的时间设置为23:59:59以包含整个月份的最后一天
        LocalDateTime endOfLastDay = truncatedLastDateTime.plusDays(1).minusSeconds(1);
        Date lastDayOfMonth = Date.from(endOfLastDay.atZone(ZoneId.systemDefault()).toInstant());

        // 查询该月份的所有值班记录
        RosterDuty dutyQuery = new RosterDuty();
        List<RosterDuty> allDuties = rosterDutyService.selectRosterDutyList(dutyQuery);

        // 过滤出指定月份的值班记录
        List<RosterDuty> monthlyDuties = allDuties.stream()
                .filter(duty -> !duty.getDutyTime().before(firstDayOfMonth) &&
                        !duty.getDutyTime().after(lastDayOfMonth))
                .collect(Collectors.toList());

        // 如果该月份有数据，返回true；否则返回false
        boolean hasData = !monthlyDuties.isEmpty();
        return AjaxResult.success(hasData);
    }

    /**
     * 导出安排排班列表
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:export')")
    @Log(title = "导出安排排班", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestParam(required = false) String date) throws ParseException, IOException {
        RosterScheduleDetail rosterScheduleDetail = new RosterScheduleDetail();

        if (date != null && !date.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = sdf.parse(date);
            rosterScheduleDetail.setDate(parsedDate);
        }

        List<RosterScheduleDetail> list = rosterScheduleDetailService.selectRosterScheduleDetailList(rosterScheduleDetail);

        // 处理数据
        for (RosterScheduleDetail detail : list) {
            // 处理日期
            LocalDateTime localDateTime = LocalDateTime.ofInstant(detail.getDate().toInstant(), ZoneId.systemDefault());
            LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);
            Date truncatedDate = Date.from(truncatedDateTime.atZone(ZoneId.systemDefault()).toInstant());

            // 查询主班人员（5L、6L、12L、14L类型）
            List<RosterStaffRelation> mainStaff = new ArrayList<>();
            List<Long> mainStaffTypes = Arrays.asList(5L, 6L, 12L, 14L);

            for (Long staffTypeId : mainStaffTypes) {
                RosterStaffRelation relation = new RosterStaffRelation();
                relation.setScheduleDetailId(detail.getId());
                RosterStaff staff = new RosterStaff();
                staff.setStaffTypeId(staffTypeId);
                relation.setRosterStaff(staff);
                mainStaff.addAll(rosterStaffRelationService.selectRosterStaffRelationList(relation));
            }
            detail.setStaff(mainStaff);

            // 查询次班人员（8L、10L类型）
            List<RosterStaffRelation> secondaryStaff = new ArrayList<>();
            List<Long> secondaryStaffTypes = Arrays.asList(8L, 10L);

            for (Long staffTypeId : secondaryStaffTypes) {
                RosterStaffRelation relation = new RosterStaffRelation();
                relation.setScheduleDetailId(detail.getId());
                RosterStaff staff = new RosterStaff();
                staff.setStaffTypeId(staffTypeId);
                relation.setRosterStaff(staff);
                secondaryStaff.addAll(rosterStaffRelationService.selectRosterStaffRelationList(relation));
            }
            detail.setSecondaryStaff(secondaryStaff);

            // 查询护理人员（7L、13L类型）
            List<RosterStaffRelation> nursingStaff = new ArrayList<>();
            List<Long> nursingStaffTypes = Arrays.asList(7L, 13L);

            for (Long staffTypeId : nursingStaffTypes) {
                RosterStaffRelation relation = new RosterStaffRelation();
                relation.setScheduleDetailId(detail.getId());
                RosterStaff staff = new RosterStaff();
                staff.setStaffTypeId(staffTypeId);
                relation.setRosterStaff(staff);
                nursingStaff.addAll(rosterStaffRelationService.selectRosterStaffRelationList(relation));
            }
            detail.setNursings(nursingStaff);

            // 转换人员列表为名称字符串
            detail.convertStaffNames();
            detail.convertNursingStaffNames();
        }

        // 创建Word文档
        XWPFDocument document = new XWPFDocument();

        // 添加标题 - 显示几号的排班表（居中）
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText(date != null ? date + " 排班表" : "排班表");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.addBreak();

        // 添加排班人数统计 - 靠左显示
        XWPFParagraph countParagraph = document.createParagraph();
        countParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun countRun = countParagraph.createRun();
        countRun.setText("排班人数: " + calculateStaffCount(list));
        countRun.setFontSize(12);
        countRun.addBreak();

        // 计算需要多少行（每个房间一行 + 每个房间的手术信息行）
        int totalRows = list.size() + 1; // 表头 + 每个房间一行
        for (RosterScheduleDetail detail : list) {
            // 查询手术信息
            List<RosterStaffRelation> allStaff = new ArrayList<>();
            if (detail.getStaff() != null) allStaff.addAll(detail.getStaff());
            if (detail.getSecondaryStaff() != null) allStaff.addAll(detail.getSecondaryStaff());
            if (detail.getNursings() != null) allStaff.addAll(detail.getNursings());

            int surgeryCount = 0;
            for (RosterStaffRelation staffRelation : allStaff) {
                OperatingTable query = new OperatingTable();
                query.setRelationId(staffRelation.getId());
                List<OperatingTable> surgeries = operatingTableService.selectOperatingTableList(query);
                surgeryCount += surgeries.size();
            }
            totalRows += surgeryCount; // 为每个手术添加一行
        }

        // 创建主表格 - 5列：房间名称、医生、护理、进修、备注
        XWPFTable table = document.createTable(totalRows, 5);

        // 设置表格整体样式
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        if (tblPr == null) {
            tblPr = table.getCTTbl().addNewTblPr();
        }

        // 设置表格宽度为100%
        CTTblWidth tblWidth = tblPr.addNewTblW();
        tblWidth.setType(STTblWidth.PCT);
        tblWidth.setW(BigInteger.valueOf(5000));

        // 设置表格边框
        CTTblBorders borders = tblPr.addNewTblBorders();
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);

        // 设置表头行
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.setHeight(400);

        // 设置表头单元格
        String[] headers = {"房间名称", "医生", "护理", "进修", "备注"};
        for (int i = 0; i < headers.length; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            if (cell == null) {
                cell = headerRow.addNewTableCell();
            }
            cell.setText(headers[i]);

            // 设置表头样式
            cell.setColor("D3D3D3"); // 灰色背景
            for (XWPFParagraph p : cell.getParagraphs()) {
                p.setAlignment(ParagraphAlignment.CENTER);
                for (XWPFRun r : p.getRuns()) {
                    r.setBold(true);
                    r.setFontSize(12);
                }
            }

            // 设置单元格宽度
            CTTblWidth cellWidth = cell.getCTTc().addNewTcPr().addNewTcW();
            cellWidth.setType(STTblWidth.DXA);
            switch (i) {
                case 0: cellWidth.setW(BigInteger.valueOf(1200)); break;
                case 1: cellWidth.setW(BigInteger.valueOf(1200)); break;
                case 2: cellWidth.setW(BigInteger.valueOf(1200)); break;
                case 3: cellWidth.setW(BigInteger.valueOf(1200)); break;
                case 4: cellWidth.setW(BigInteger.valueOf(1200)); break;
            }
        }

        // 填充数据行
        int currentRow = 1; // 从第1行开始（0是表头）
        for (int i = 0; i < list.size(); i++) {
            RosterScheduleDetail detail = list.get(i);

            // 房间行
            XWPFTableRow roomRow = table.getRow(currentRow);
            if (roomRow == null) {
                roomRow = table.createRow();
            }
            roomRow.setHeight(300);

            // 设置交替行颜色
            String rowColor = (currentRow % 2 == 0) ? "F5F5F5" : "FFFFFF";
            for (int j = 0; j < 5; j++) {
                XWPFTableCell cell = roomRow.getCell(j);
                if (cell == null) {
                    cell = roomRow.addNewTableCell();
                }
                cell.setColor(rowColor);
            }

            // 房间名称
            setCellValue(roomRow, 0, detail.getRoom() != null ? detail.getRoom().getRoomName() : "");

            // 医生信息（主班人员）
            setCellValue(roomRow, 1, detail.getStaffName());

            // 护理信息
            setCellValue(roomRow, 2, detail.getNursingStaffName());

            // 进修信息（次班人员）
            setCellValue(roomRow, 3, detail.getSecondaryStaffName());

            // 备注
            setCellValue(roomRow, 4, detail.getAdjustReason());
            currentRow++;

            // 手术信息行
            List<RosterStaffRelation> allStaff = new ArrayList<>();
            if (detail.getStaff() != null) allStaff.addAll(detail.getStaff());
            if (detail.getSecondaryStaff() != null) allStaff.addAll(detail.getSecondaryStaff());
            if (detail.getNursings() != null) allStaff.addAll(detail.getNursings());

            // 收集所有手术信息
            List<OperatingTable> allSurgeries = new ArrayList<>();
            for (RosterStaffRelation staffRelation : allStaff) {
                OperatingTable query = new OperatingTable();
                query.setRelationId(staffRelation.getId());
                List<OperatingTable> surgeries = operatingTableService.selectOperatingTableList(query);

                if (surgeries != null) {
                    allSurgeries.addAll(surgeries);
                }
            }

            // 如果有手术信息，将所有手术信息放在同一行
            if (!allSurgeries.isEmpty()) {
                XWPFTableRow surgeryRow = table.getRow(currentRow);
                if (surgeryRow == null) {
                    surgeryRow = table.createRow();
                }
                surgeryRow.setHeight(250);

                // 合并第一列到第五列，显示手术信息
                mergeCellsHorizontally(table, currentRow, 0, 4);

                // 构建手术信息字符串（所有手术信息放在同一行）
                StringBuilder surgeryText = new StringBuilder();
                for (int k = 0; k < allSurgeries.size(); k++) {
                    OperatingTable surgery = allSurgeries.get(k);
                    // 查找对应的医生信息
                    String doctorName = "";
                    for (RosterStaffRelation staffRelation : allStaff) {
                        if (staffRelation.getId().equals(surgery.getRelationId())) {
                            doctorName = staffRelation.getRosterStaff().getStaffName();
                            break;
                        }
                    }
                    if (k > 0) {
                        surgeryText.append("  ,"); // 手术信息之间用空格分隔
                    }
                    surgeryText
                            .append(surgery.getOperationName())
                            .append("-")
                            .append(getDifficultyText(surgery.getDifficulty()))
                            .append("-")
                            .append(doctorName);
                }

                setCellValue(surgeryRow, 0, surgeryText.toString());
                currentRow++;
            } else {
                // 如果没有手术信息，添加空的手术行
                XWPFTableRow emptySurgeryRow = table.getRow(currentRow);
                if (emptySurgeryRow == null) {
                    emptySurgeryRow = table.createRow();
                }
                emptySurgeryRow.setHeight(250);

                // 合并所有列显示空行
                mergeCellsHorizontally(table, currentRow, 0, 4);
                setCellValue(emptySurgeryRow, 0, ""); // 空内容

                currentRow++;
            }
        }

        // 添加日期和系统信息 - 靠右显示
        XWPFParagraph footerParagraph = document.createParagraph();
        footerParagraph.setAlignment(ParagraphAlignment.RIGHT);

        XWPFRun dateRun = footerParagraph.createRun();
        SimpleDateFormat exportDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        String formattedDate;
        if (date != null) {
            formattedDate = exportDateFormat.format(new SimpleDateFormat("yyyy-MM-dd").parse(date));
        } else {
            formattedDate = "全部日期";
        }
        dateRun.setText(formattedDate);
        dateRun.setFontSize(12);
        dateRun.addBreak();

        XWPFRun systemRun = footerParagraph.createRun();
        systemRun.setText("排班系统自动排班");
        systemRun.setFontSize(10);
        systemRun.setItalic(true);

        // 设置响应头
        String fileName = "排班表_" + (date != null ? date : "全部") + ".docx";
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // 输出文档
        document.write(response.getOutputStream());
        document.close();
    }

    // 辅助方法：水平合并单元格
    private void mergeCellsHorizontally(XWPFTable table, int row, int startCol, int endCol) {
        for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(colIndex);
            CTTcPr tcPr = cell.getCTTc().getTcPr();
            if (tcPr == null) {
                tcPr = cell.getCTTc().addNewTcPr();
            }

            CTHMerge hMerge = tcPr.getHMerge();
            if (hMerge == null) {
                hMerge = tcPr.addNewHMerge();
            }
            if (colIndex == startCol) {
                hMerge.setVal(STMerge.RESTART);
            } else {
                hMerge.setVal(STMerge.CONTINUE);
            }
        }
    }

    // 辅助方法：获取难度文本
    private String getDifficultyText(Integer difficulty) {
        if (difficulty == null) return "";
        switch (difficulty) {
            case 1: return "初级";
            case 2: return "中级";
            case 3: return "高级";
            default: return String.valueOf(difficulty);
        }
    }

    // 辅助方法：设置单元格值
    private void setCellValue(XWPFTableRow row, int col, String value) {
        XWPFTableCell cell = row.getCell(col);
        if (cell == null) {
            cell = row.addNewTableCell();
        }

        // 清除现有内容
        for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }

        // 添加新段落
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        run.setText(value != null ? value : "");
        run.setFontSize(11);

        // 设置垂直居中
        CTTcPr tcPr = cell.getCTTc().addNewTcPr();
        tcPr.addNewVAlign().setVal(STVerticalJc.CENTER);
    }

    // 辅助方法：计算排班人数
    private int calculateStaffCount(List<RosterScheduleDetail> details) {
        int count = 0;
        for (RosterScheduleDetail detail : details) {
            if (detail.getStaff() != null) {
                count += detail.getStaff().size();
            }
            if (detail.getSecondaryStaff() != null) {
                count += detail.getSecondaryStaff().size();
            }
            if (detail.getNursings() != null) {
                count += detail.getNursings().size();
            }
        }
        return count;
    }

    /**
     * 月度值班表导出
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:export')")
    @Log(title = "导出月度值班表", businessType = BusinessType.EXPORT)
    @PostMapping("/monthlyExport")
    public void monthlyExport(HttpServletResponse response, @RequestParam(required = false) String date)
            throws ParseException, IOException {
        try {
            // 解析日期参数
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date inputDate = date != null ? sdf.parse(date) : new Date();

            // 转换为Calendar操作
            Calendar cal = Calendar.getInstance();
            cal.setTime(inputDate);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1; // 月份从0开始，需要+1

            // 获取当月的第一天和最后一天，并截断时间部分为00:00:00
            cal.set(Calendar.DAY_OF_MONTH, 1);
            LocalDateTime firstDateTime = LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime truncatedFirstDateTime = firstDateTime.truncatedTo(ChronoUnit.DAYS);
            Date firstDayOfMonth = Date.from(truncatedFirstDateTime.atZone(ZoneId.systemDefault()).toInstant());

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            LocalDateTime lastDateTime = LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime truncatedLastDateTime = lastDateTime.truncatedTo(ChronoUnit.DAYS);
            // 将最后一天的时间设置为23:59:59以包含整个月份的最后一天
            LocalDateTime endOfLastDay = truncatedLastDateTime.plusDays(1).minusSeconds(1);
            Date lastDayOfMonth = Date.from(endOfLastDay.atZone(ZoneId.systemDefault()).toInstant());

            // 查询该月份的所有值班记录
            RosterDuty dutyQuery = new RosterDuty();
            List<RosterDuty> allDuties = rosterDutyService.selectRosterDutyList(dutyQuery);

            // 过滤出指定月份的值班记录（使用截断后的日期进行比较）
            List<RosterDuty> monthlyDuties = allDuties.stream()
                    .filter(duty -> !duty.getDutyTime().before(firstDayOfMonth) &&
                            !duty.getDutyTime().after(lastDayOfMonth))
                    .collect(Collectors.toList());

            // 按日期和人员分组（日期截断为日期部分以确保正确的分组）
            Map<Date, List<RosterDuty>> dutiesByDate = monthlyDuties.stream()
                    .collect(Collectors.groupingBy(duty -> {
                        // 截断dutyTime的时间部分，只保留日期部分
                        LocalDateTime localDateTime = LocalDateTime.ofInstant(duty.getDutyTime().toInstant(), ZoneId.systemDefault());
                        LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);
                        return Date.from(truncatedDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    }));

            // 创建Word文档
            XWPFDocument document = new XWPFDocument();

            // 添加标题 - xxxx年xxxx月值班表（左上角）
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun titleRun = title.createRun();
            titleRun.setText(year + "年" + month + "月值班表");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.addBreak();

            // 创建表格 - 7列（星期一到星期日）
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int rowsNeeded = (int) Math.ceil(daysInMonth / 7.0) + 1; // 表头行 + 数据行

            XWPFTable table = document.createTable(rowsNeeded, 7);

            // 设置表格整体样式
            CTTblPr tblPr = table.getCTTbl().getTblPr();
            if (tblPr == null) {
                tblPr = table.getCTTbl().addNewTblPr();
            }

            // 设置表格宽度为100%
            CTTblWidth tblWidth = tblPr.addNewTblW();
            tblWidth.setType(STTblWidth.PCT);
            tblWidth.setW(BigInteger.valueOf(14000));

            // 设置表格边框
            CTTblBorders borders = tblPr.addNewTblBorders();
            borders.addNewBottom().setVal(STBorder.SINGLE);
            borders.addNewLeft().setVal(STBorder.SINGLE);
            borders.addNewRight().setVal(STBorder.SINGLE);
            borders.addNewTop().setVal(STBorder.SINGLE);
            borders.addNewInsideH().setVal(STBorder.SINGLE);
            borders.addNewInsideV().setVal(STBorder.SINGLE);

            // 设置表头行
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.setHeight(400);

            // 设置表头单元格（星期一到星期日）
            String[] headers = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
            for (int i = 0; i < headers.length; i++) {
                XWPFTableCell cell = headerRow.getCell(i);
                if (cell == null) {
                    cell = headerRow.addNewTableCell();
                }
                cell.setText(headers[i]);

                // 设置表头样式
                cell.setColor("D3D3D3"); // 灰色背景
                for (XWPFParagraph p : cell.getParagraphs()) {
                    p.setAlignment(ParagraphAlignment.CENTER);
                    for (XWPFRun r : p.getRuns()) {
                        r.setBold(true);
                        r.setFontSize(12);
                    }
                }

                // 设置单元格宽度（增加列宽）
                CTTblWidth cellWidth = cell.getCTTc().addNewTcPr().addNewTcW();
                cellWidth.setType(STTblWidth.DXA);
                cellWidth.setW(BigInteger.valueOf(2000)); // 增加列宽
            }

            // 填充数据行
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(firstDayOfMonth);

            int currentRow = 1;
            int currentDay = 1;

            while (currentDay <= daysInMonth) {
                XWPFTableRow dataRow = table.getRow(currentRow);
                if (dataRow == null) {
                    dataRow = table.createRow();
                }
                dataRow.setHeight(400); // 增加行高以容纳更多内容

                // 设置交替行颜色
                String rowColor = (currentRow % 2 == 0) ? "F5F5F5" : "FFFFFF";

                // 填充一周的数据
                for (int dayOfWeek = 0; dayOfWeek < 7 && currentDay <= daysInMonth; dayOfWeek++) {
                    Date currentDate = dateCal.getTime();
                    
                    // 截断currentDate的时间部分以匹配dutiesByDate中的key
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
                    LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);
                    Date truncatedCurrentDate = Date.from(truncatedDateTime.atZone(ZoneId.systemDefault()).toInstant());

                    XWPFTableCell cell = dataRow.getCell(dayOfWeek);
                    if (cell == null) {
                        cell = dataRow.addNewTableCell();
                    }
                    cell.setColor(rowColor);

                    // 清除现有内容
                    for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) {
                        cell.removeParagraph(i);
                    }

                    // 添加日期段落（左上角显示日期）
                    XWPFParagraph dateParagraph = cell.addParagraph();
                    dateParagraph.setAlignment(ParagraphAlignment.LEFT);
                    XWPFRun dateRun = dateParagraph.createRun();
                    dateRun.setText(currentDay + "号");
                    dateRun.setBold(true);
                    dateRun.setFontSize(11);

                    // 获取当天的值班人员（使用截断后的日期作为key）
                    List<RosterDuty> dayDuties = dutiesByDate.getOrDefault(truncatedCurrentDate, new ArrayList<>());

                    // 按值班人员类型排序（确保顺序一致）
                    dayDuties.sort((d1, d2) -> {
                        try {
                            RosterStaff staff1 = rosterStaffService.selectRosterStaffById(d1.getDutyStaffId());
                            RosterStaff staff2 = rosterStaffService.selectRosterStaffById(d2.getDutyStaffId());
                            Long type1 = staff1 != null ? staff1.getStaffTypeId() : 0L;
                            Long type2 = staff2 != null ? staff2.getStaffTypeId() : 0L;
                            return type1.compareTo(type2);
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    List<String> dutyNames = new ArrayList<>();
                    for (int i = 0; i < dayDuties.size(); i++) {
                        RosterDuty duty = dayDuties.get(i);
                        RosterStaff staff = rosterStaffService.selectRosterStaffById(duty.getDutyStaffId());
                        String name = staff != null ? staff.getStaffName() : "未知人员";

                        // 根据位置添加前缀
                        String prefix = "";
                        if (i < 2) {
                            prefix = "医:"; // 第一、二个加"医："
                        } else if (i < 4) {
                            prefix = "护:"; // 第三、四个加"护："
                        } else if (i == 4) {
                            prefix = "C:";   // 第五个加"C"
                        } else if (i == 5) {
                            prefix = "E:";   // 第六个加"E"
                        }

                        dutyNames.add(prefix + name);
                    }

                    // 按两两分组显示值班人员
                    if (!dutyNames.isEmpty()) {
                        XWPFParagraph namesParagraph = cell.addParagraph();
                        namesParagraph.setAlignment(ParagraphAlignment.LEFT);

                        StringBuilder currentLine = new StringBuilder();
                        int namesInLine = 0;

                        for (int i = 0; i < dutyNames.size(); i++) {
                            if (namesInLine == 2) {
                                // 换行
                                XWPFRun run = namesParagraph.createRun();
                                run.setText(currentLine.toString());
                                run.setFontSize(10);
                                run.addBreak();

                                currentLine = new StringBuilder();
                                namesInLine = 0;
                            }

                            if (namesInLine > 0) {
                                currentLine.append("、");
                            }
                            currentLine.append(dutyNames.get(i));
                            namesInLine++;
                        }

                        // 添加最后一行
                        if (currentLine.length() > 0) {
                            XWPFRun run = namesParagraph.createRun();
                            run.setText(currentLine.toString());
                            run.setFontSize(10);
                        }
                    }

                    currentDay++;
                    dateCal.add(Calendar.DATE, 1);
                }

                currentRow++;
            }

            // 添加页脚信息
            XWPFParagraph footerParagraph = document.createParagraph();
            footerParagraph.setAlignment(ParagraphAlignment.RIGHT);

            XWPFRun dateRun = footerParagraph.createRun();
            dateRun.setText("生成时间: " + new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date()));
            dateRun.setFontSize(10);
            dateRun.addBreak();

            XWPFRun systemRun = footerParagraph.createRun();
            systemRun.setText("排班系统自动生成");
            systemRun.setFontSize(10);
            systemRun.setItalic(true);

            // 设置响应头
            String fileName = year + "年" + month + "月值班表.docx";
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 输出文档
            document.write(response.getOutputStream());
            document.close();

            logger.info("月度值班表导出成功: {}年{}月", year, month);

        } catch (Exception e) {
            logger.error("月度值班表导出失败", e);
            throw new IOException("导出月度值班表失败", e);
        }
    }

    /**
     * 删除指定日期的现有排班数据
     */
    private void deleteExistingScheduleData(Date date) {
        try {
            // 1. 查询该日期的所有排班详情
            RosterScheduleDetail query = new RosterScheduleDetail();
            query.setDate(date);
            List<RosterScheduleDetail> existingDetails = rosterScheduleDetailService.selectRosterScheduleDetailList(query);

            // 2. 删除相关的人员关联和手术台数据
            for (RosterScheduleDetail detail : existingDetails) {
                // 删除人员关联
                RosterStaffRelation relationQuery = new RosterStaffRelation();
                relationQuery.setScheduleDetailId(detail.getId());
                List<RosterStaffRelation> relations = rosterStaffRelationService.selectRosterStaffRelationList(relationQuery);

                for (RosterStaffRelation relation : relations) {
                    // 删除手术台数据
                    OperatingTable tableQuery = new OperatingTable();
                    tableQuery.setRelationId(relation.getId());
                    List<OperatingTable> tables = operatingTableService.selectOperatingTableList(tableQuery);
                    for (OperatingTable table : tables) {
                        operatingTableService.deleteOperatingTableById(table.getId());
                    }
                    // 删除人员关联
                    rosterStaffRelationService.deleteRosterStaffRelationById(relation.getId());
                }

                // 3. 删除排班详情
                rosterScheduleDetailService.deleteRosterScheduleDetailById(detail.getId());
            }
        } catch (Exception e) {
            logger.error("删除现有排班数据时发生错误", e);
            throw new RuntimeException("删除现有排班数据失败", e);
        }
    }

    /**
     * 查看数据
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:list')")
    @Log(title = "安排排班", businessType = BusinessType.INSERT)
    @PostMapping("/view")
    public TableDataInfo view(@RequestBody Date date) {
        // 使用Java 8的时间API将时间部分截断
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);
        Date truncatedDate = Date.from(truncatedDateTime.atZone(ZoneId.systemDefault()).toInstant());

        RosterScheduleDetail rosterScheduleDetail = new RosterScheduleDetail();
        rosterScheduleDetail.setDate(truncatedDate);

        List<RosterScheduleDetail> list = rosterScheduleDetailService.selectRosterScheduleDetailList(rosterScheduleDetail);

        for (RosterScheduleDetail detail : list) {
            List<RosterStaffRelation> mainStaffList = new ArrayList<>();

            // 查询所有主班人员类型（5L、6L、12L、14L）
            List<Long> mainStaffTypes = Arrays.asList(5L, 6L, 12L, 14L);

            for (Long staffTypeId : mainStaffTypes) {
                RosterStaffRelation relation = new RosterStaffRelation();
                relation.setScheduleDetailId(detail.getId());
                RosterStaff staff = new RosterStaff();
                staff.setStaffTypeId(staffTypeId);
                relation.setRosterStaff(staff);
                List<RosterStaffRelation> staffList = rosterStaffRelationService.selectRosterStaffRelationList(relation);
                mainStaffList.addAll(staffList);
            }

            // 为每个人员关联查询手术台信息
            for (RosterStaffRelation staffRelation : mainStaffList) {
                OperatingTable operatingTableQuery = new OperatingTable();
                operatingTableQuery.setRelationId(staffRelation.getId());
                List<OperatingTable> operatingTables = operatingTableService.selectOperatingTableList(operatingTableQuery);
                staffRelation.setOperatingTables(operatingTables);
            }

            // 查询所有次班人员（8L、10L类型）
            List<RosterStaffRelation> secondaryStaffList = new ArrayList<>();

            // 查询8L类型人员
            RosterStaffRelation secondaryRelation8 = new RosterStaffRelation();
            secondaryRelation8.setScheduleDetailId(detail.getId());
            RosterStaff secondaryStaff8 = new RosterStaff();
            secondaryStaff8.setStaffTypeId(8L);
            secondaryRelation8.setRosterStaff(secondaryStaff8);
            secondaryStaffList.addAll(rosterStaffRelationService.selectRosterStaffRelationList(secondaryRelation8));

            // 查询10L类型人员
            RosterStaffRelation secondaryRelation10 = new RosterStaffRelation();
            secondaryRelation10.setScheduleDetailId(detail.getId());
            RosterStaff secondaryStaff10 = new RosterStaff();
            secondaryStaff10.setStaffTypeId(10L);
            secondaryRelation10.setRosterStaff(secondaryStaff10);
            secondaryStaffList.addAll(rosterStaffRelationService.selectRosterStaffRelationList(secondaryRelation10));

            // 查询护理人员（7L类型）
            RosterStaffRelation nursingRelation = new RosterStaffRelation();
            nursingRelation.setScheduleDetailId(detail.getId());
            RosterStaff nursingStaff = new RosterStaff();
            nursingStaff.setStaffTypeId(7L); // 7L表示护理人员
            nursingRelation.setRosterStaff(nursingStaff);
            List<RosterStaffRelation> nursingStaffList = rosterStaffRelationService.selectRosterStaffRelationList(nursingRelation);

            // 查询13L类型人员（顶替护理的人员）
            RosterStaffRelation nursingRelation13 = new RosterStaffRelation();
            nursingRelation13.setScheduleDetailId(detail.getId());
            RosterStaff nursingStaff13 = new RosterStaff();
            nursingStaff13.setStaffTypeId(13L);
            nursingRelation13.setRosterStaff(nursingStaff13);
            nursingStaffList.addAll(rosterStaffRelationService.selectRosterStaffRelationList(nursingRelation13));

            detail.setStaff(mainStaffList);
            detail.setSecondaryStaff(secondaryStaffList);
            detail.setNursings(nursingStaffList);
        }
        return getDataTable(list);
    }

    /**
     * 调整
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:edit')")
    @Log(title = "调整安排排班", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Map<String, Object> params) {
        try {
            // 获取基本类型数据
            Long id = Long.valueOf(params.get("id").toString());
            String adjustReason = (String) params.get("adjustReason");
            String isAdjusted = (String) params.get("isAdjusted");

            // 获取列表类型数据
            List<Long> mainStaffIds = ((List<Integer>) params.get("mainStaffIds"))
                    .stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());

            List<Long> secondaryStaffIds = ((List<Integer>) params.get("secondaryStaffIds"))
                    .stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());

            List<Long> nursingStaffIds = ((List<Integer>) params.get("nursingStaffIds"))
                    .stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());

            // ========== 处理主班人员 ==========
            // 查询数据库现有主班人员数据（包括5L、6L、12L、14L类型）
            RosterStaffRelation mainStaffQuery = new RosterStaffRelation();
            mainStaffQuery.setScheduleDetailId(id);

            // 合并所有主班人员类型
            List<RosterStaffRelation> dbMainStaffList = new ArrayList<>();
            List<Long> mainStaffTypes = Arrays.asList(5L, 6L, 12L, 14L);

            for (Long staffTypeId : mainStaffTypes) {
                RosterStaff mainStaffType = new RosterStaff();
                mainStaffType.setStaffTypeId(staffTypeId);
                mainStaffQuery.setRosterStaff(mainStaffType);
                dbMainStaffList.addAll(rosterStaffRelationService.selectRosterStaffRelationList(mainStaffQuery));
            }

            // 提取数据库中的主班staffId列表
            List<Long> dbMainStaffIds = dbMainStaffList.stream()
                    .map(relation -> relation.getRosterStaff().getId())
                    .collect(Collectors.toList());

            // 找出需要新增的主班人员
            List<Long> toAddMainStaffIds = mainStaffIds.stream()
                    .filter(staffId -> !dbMainStaffIds.contains(staffId))
                    .collect(Collectors.toList());

            // 找出需要删除的主班人员
            List<Long> toDeleteMainStaffIds = dbMainStaffIds.stream()
                    .filter(staffId -> !mainStaffIds.contains(staffId))
                    .collect(Collectors.toList());

            // ========== 处理次班人员 ==========
            // 查询数据库现此次班人员数据（包括8L、10L类型）
            RosterStaffRelation secondaryStaffQuery = new RosterStaffRelation();
            secondaryStaffQuery.setScheduleDetailId(id);

            List<RosterStaffRelation> dbSecondaryStaffList = new ArrayList<>();

            // 查询所有次班人员类型
            for (Long staffTypeId : Arrays.asList(8L, 10L)) {
                RosterStaff secondaryStaffType = new RosterStaff();
                secondaryStaffType.setStaffTypeId(staffTypeId);
                secondaryStaffQuery.setRosterStaff(secondaryStaffType);
                dbSecondaryStaffList.addAll(rosterStaffRelationService.selectRosterStaffRelationList(secondaryStaffQuery));
            }

            // 提取数据库中的次班staffId列表
            List<Long> dbSecondaryStaffIds = dbSecondaryStaffList.stream()
                    .map(relation -> relation.getRosterStaff().getId())
                    .collect(Collectors.toList());

            // 找出需要新增的次班人员
            List<Long> toAddSecondaryStaffIds = secondaryStaffIds.stream()
                    .filter(staffId -> !dbSecondaryStaffIds.contains(staffId))
                    .collect(Collectors.toList());

            // 找出需要删除的次班人员
            List<Long> toDeleteSecondaryStaffIds = dbSecondaryStaffIds.stream()
                    .filter(staffId -> !secondaryStaffIds.contains(staffId))
                    .collect(Collectors.toList());

            // ========== 处理护理人员 ==========
            // 查询数据库现有护理人员数据（包括7L和13L类型）
            RosterStaffRelation nursingStaffQuery = new RosterStaffRelation();
            nursingStaffQuery.setScheduleDetailId(id);

            List<RosterStaffRelation> dbNursingStaffList = new ArrayList<>();

            // 查询7L类型人员
            RosterStaff nursingStaffType7 = new RosterStaff();
            nursingStaffType7.setStaffTypeId(7L);
            nursingStaffQuery.setRosterStaff(nursingStaffType7);
            dbNursingStaffList.addAll(rosterStaffRelationService.selectRosterStaffRelationList(nursingStaffQuery));

            // 查询13L类型人员
            RosterStaff nursingStaffType13 = new RosterStaff();
            nursingStaffType13.setStaffTypeId(13L);
            nursingStaffQuery.setRosterStaff(nursingStaffType13);
            dbNursingStaffList.addAll(rosterStaffRelationService.selectRosterStaffRelationList(nursingStaffQuery));

            // 提取数据库中的护理staffId列表
            List<Long> dbNursingStaffIds = dbNursingStaffList.stream()
                    .map(relation -> relation.getRosterStaff().getId())
                    .collect(Collectors.toList());

            // 找出需要新增的护理人员
            List<Long> toAddNursingStaffIds = nursingStaffIds.stream()
                    .filter(staffId -> !dbNursingStaffIds.contains(staffId))
                    .collect(Collectors.toList());

            // 找出需要删除的护理人员
            List<Long> toDeleteNursingStaffIds = dbNursingStaffIds.stream()
                    .filter(staffId -> !nursingStaffIds.contains(staffId))
                    .collect(Collectors.toList());

            // ========== 处理手术台数据 ==========
            List<Map<String, Object>> operatingTables = (List<Map<String, Object>>) params.get("operatingTables");
            if (operatingTables != null) {
                // 先删除该排班详情下所有医生关联的手术台数据
                for (RosterStaffRelation mainRelation : dbMainStaffList) {
                    OperatingTable deleteQuery = new OperatingTable();
                    deleteQuery.setRelationId(mainRelation.getId());
                    List<OperatingTable> existingTables = operatingTableService.selectOperatingTableList(deleteQuery);
                    for (OperatingTable table : existingTables) {
                        operatingTableService.deleteOperatingTableById(table.getId());
                    }
                }

                // 添加新的手术台数据
                for (Map<String, Object> tableData : operatingTables) {
                    String operationName = (String) tableData.get("operationName");
                    Integer difficulty = (Integer) tableData.get("difficulty");
                    Long staffId = Long.valueOf(tableData.get("staffId").toString());

                    // 查找对应的关联记录
                    RosterStaffRelation targetRelation = null;
                    for (RosterStaffRelation relation : dbMainStaffList) {
                        if (relation.getRosterStaff().getId().equals(staffId)) {
                            targetRelation = relation;
                            break;
                        }
                    }

                    // 如果找不到对应的关联记录，创建新的
                    if (targetRelation == null) {
                        RosterStaffRelation newRelation = new RosterStaffRelation();
                        newRelation.setStaffId(staffId);
                        newRelation.setScheduleDetailId(id);
                        rosterStaffRelationService.insertRosterStaffRelation(newRelation);
                        targetRelation = newRelation;
                    }

                    // 创建手术台记录
                    OperatingTable newTable = new OperatingTable();
                    newTable.setRelationId(targetRelation.getId());
                    newTable.setOperationName(operationName);
                    newTable.setDifficulty(difficulty);
                    operatingTableService.insertOperatingTable(newTable);
                }
            }

            // 执行数据库操作
            // 先删除所有需要删除的记录
            deleteStaffRelations(toDeleteMainStaffIds, dbMainStaffList, id);
            deleteStaffRelations(toDeleteSecondaryStaffIds, dbSecondaryStaffList, id);
            deleteStaffRelations(toDeleteNursingStaffIds, dbNursingStaffList, id);

            // 再添加所有需要新增的记录
            addStaffRelations(toAddMainStaffIds, id);
            addStaffRelations(toAddSecondaryStaffIds, id);
            addStaffRelations(toAddNursingStaffIds, id);

            // 更新排班详情
            RosterScheduleDetail scheduleDetail = new RosterScheduleDetail();
            scheduleDetail.setId(id);
            scheduleDetail.setAdjustReason(adjustReason);
            scheduleDetail.setIsAdjusted(isAdjusted);
            rosterScheduleDetailService.updateRosterScheduleDetail(scheduleDetail);

            return AjaxResult.success("调整成功");

        } catch (Exception e) {
            logger.error("调整失败", e);
            return AjaxResult.error("调整失败: " + e.getMessage());
        }
    }

    /**
     * 删除人员关联关系
     */
    private void deleteStaffRelations(List<Long> staffIdsToDelete, List<RosterStaffRelation> dbRelations, Long scheduleDetailId) {
        for (Long staffId : staffIdsToDelete) {
            // 找到对应的relation记录
            RosterStaffRelation relationToDelete = dbRelations.stream()
                    .filter(relation -> relation.getRosterStaff().getId().equals(staffId)
                            && relation.getScheduleDetailId().equals(scheduleDetailId))
                    .findFirst()
                    .orElse(null);

            if (relationToDelete != null) {
                // 先删除关联的手术台数据
                OperatingTable tableQuery = new OperatingTable();
                tableQuery.setRelationId(relationToDelete.getId());
                List<OperatingTable> tables = operatingTableService.selectOperatingTableList(tableQuery);
                for (OperatingTable table : tables) {
                    operatingTableService.deleteOperatingTableById(table.getId());
                }

                // 删除人员关联
                rosterStaffRelationService.deleteRosterStaffRelationById(relationToDelete.getId());
            }
        }
    }

    /**
     * 添加人员关联关系
     */
    private void addStaffRelations(List<Long> staffIdsToAdd, Long scheduleDetailId) {
        for (Long staffId : staffIdsToAdd) {
            // 检查是否已存在相同的关联
            RosterStaffRelation checkQuery = new RosterStaffRelation();
            checkQuery.setStaffId(staffId);
            checkQuery.setScheduleDetailId(scheduleDetailId);
            List<RosterStaffRelation> existing = rosterStaffRelationService.selectRosterStaffRelationList(checkQuery);

            if (existing.isEmpty()) {
                RosterStaffRelation newRelation = new RosterStaffRelation();
                newRelation.setStaffId(staffId);
                newRelation.setScheduleDetailId(scheduleDetailId);
                rosterStaffRelationService.insertRosterStaffRelation(newRelation);
            }
        }
    }

    /**
     * 月度排班与值班安排
     */
    @PreAuthorize("@ss.hasPermi('detail:detail:add')")
    @Log(title = "月度排班与值班安排", businessType = BusinessType.INSERT)
    @PostMapping("/monthlyDuty")
    public AjaxResult monthlyArrangement(@RequestBody Map<String, Object> params) {
        try {
            // 获取传入的日期
            String dateStr = (String) params.get("date");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date inputDate = sdf.parse(dateStr);

            // 转换为Calendar操作
            Calendar cal = Calendar.getInstance();
            cal.setTime(inputDate);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1; // 月份从0开始，需要+1

            int result = rosterStaffService.countStaffLeaveByMonth(year, month);
            System.out.println("月"+month);
            System.out.println("result====>"+result);
            if (result==0){
                rosterStaffService.clearByStaffLeaveEquals3();
            }
            logger.info("清理所有下夜");

            // 获取当月的第一天和最后一天
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date firstDayOfMonth = cal.getTime();

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date lastDayOfMonth = cal.getTime();

            logger.info("开始月度排班与值班安排: {} 到 {}",
                    sdf.format(firstDayOfMonth), sdf.format(lastDayOfMonth));

            // 1. 先删除本月已有的所有排班和值班相关数据
            deleteExistingScheduleDataForMonth(firstDayOfMonth, lastDayOfMonth);
            deleteMonthlyDuties(firstDayOfMonth, lastDayOfMonth);

            // 查询各类型员工
            RosterStaff staffType5 = new RosterStaff();
            staffType5.setStaffTypeId(5L);
            staffType5.setStatus("0");
            List<RosterStaff> staffList5 = rosterStaffService.selectRosterStaffList(staffType5);
            // 按 staffDutySort 从小到大排序
            staffList5.sort(Comparator.comparingInt(staff ->
                    Optional.ofNullable(staff.getStaffDutySort()).orElse(0)
            ));

            RosterStaff staffType6 = new RosterStaff();
            staffType6.setStaffTypeId(6L);
            staffType6.setStatus("0");
            List<RosterStaff> staffList6 = rosterStaffService.selectRosterStaffList(staffType6);
            // 按 staffDutySort 从小到大排序
            staffList6.sort(Comparator.comparingInt(staff ->
                    Optional.ofNullable(staff.getStaffDutySort()).orElse(0)
            ));

            // 查询13L类型人员（值班人员）
            RosterStaff staffType13 = new RosterStaff();
            staffType13.setStaffTypeId(13L);
            staffType13.setStatus("0");
            List<RosterStaff> staffList13 = rosterStaffService.selectRosterStaffList(staffType13);
            // 按 staffDutySort 从小到大排序
            staffList13.sort(Comparator.comparingInt(staff ->
                    Optional.ofNullable(staff.getStaffDutySort()).orElse(0)
            ));

            // 查询8L类型人员
            RosterStaff staffType8 = new RosterStaff();
            staffType8.setStaffTypeId(8L);
            staffType8.setStatus("0");
            List<RosterStaff> staffList8 = rosterStaffService.selectRosterStaffList(staffType8);
            // 按 staffDutySort 从小到大排序
            staffList8.sort(Comparator.comparingInt(staff ->
                    Optional.ofNullable(staff.getStaffDutySort()).orElse(0)
            ));

            // 查询10L类型人员
            RosterStaff staffType10 = new RosterStaff();
            staffType10.setStaffTypeId(10L);
            staffType10.setStatus("0");
            List<RosterStaff> staffList10 = rosterStaffService.selectRosterStaffList(staffType10);
            // 按 staffDutySort 从小到大排序
            staffList10.sort(Comparator.comparingInt(staff ->
                    Optional.ofNullable(staff.getStaffDutySort()).orElse(0)
            ));

            // 为13L人员创建两个排序：原排序和新排序（最后两个放到最前面）
            List<RosterStaff> staffList13Original = new ArrayList<>(staffList13);
            List<RosterStaff> staffList13New = createNewSortFor13L(staffList13);

            // 初始化索引
            int index5 = 0; // 5L人员索引
            int index6 = 0; // 6L人员索引
            int index8 = 0; // 8L人员索引
            int index10 = 0; // 10L人员索引
            int index13Original = 0; // 13L原排序索引
            int index13New = 0; // 13L新排序索引

            // 获取上个月最后的值班索引
            Map<String, Integer> lastMonthIndices = getLastMonthDutyIndices(firstDayOfMonth,
                    staffList5, staffList6, staffList8, staffList10, staffList13Original, staffList13New);
            if (lastMonthIndices != null) {
                index5 = lastMonthIndices.getOrDefault("index5", 0);
                index6 = lastMonthIndices.getOrDefault("index6", 0);
                index8 = lastMonthIndices.getOrDefault("index8", 0);
                index10 = lastMonthIndices.getOrDefault("index10", 0);
                index13Original = lastMonthIndices.getOrDefault("index13Original", 0);
                index13New = lastMonthIndices.getOrDefault("index13New", 0);
                
                // 添加调试日志，查看获取到的索引值
                logger.info("从上个月获取到的索引值: index5={}, index6={}, index8={}, index10={}, index13Original={}, index13New={}",
                        index5, index6, index8, index10, index13Original, index13New);
            } else {
                logger.info("未获取到上个月的值班索引，使用默认索引值");
            }

            // 存储下夜人员信息
            Map<Date, Long> nightShiftStaffMap = new HashMap<>();

            // 跟踪天数和计数
            int dayCount = 0;
            int scheduledDays = 0;
            int dutyCount = 0;

            // 重置日历到月初
            cal.setTime(firstDayOfMonth);

            logger.info("开始智能月度排班和值班安排...");

            // 合并后的循环：同时处理排班和值班安排
            while (!cal.getTime().after(lastDayOfMonth)) {
                Date currentDate = cal.getTime();
                try {
                    // 1. 月度值班安排操作
                    // 安排6L人员值班
                    RosterStaff current6LStaff = staffList6.get(index6 % staffList6.size());
                    createDutyRecord(current6LStaff.getId(), currentDate);
                    dutyCount++;
                    logger.info("日期 {}: 安排6L人员 {} 值班",
                            sdf.format(currentDate), current6LStaff.getStaffName());

                    // 安排5L人员值班
                    RosterStaff current5LStaff = staffList5.get(index5 % staffList5.size());
                    createDutyRecord(current5LStaff.getId(), currentDate);
                    dutyCount++;
                    logger.info("日期 {}: 安排5L人员 {} 值班",
                            sdf.format(currentDate), current5LStaff.getStaffName());

                    // 安排13L人员值班（两个人：一个从原排序，一个从新排序）
                    if (!staffList13Original.isEmpty()) {
                        RosterStaff current13LStaffOriginal = staffList13Original.get(index13Original % staffList13Original.size());
                        createDutyRecord(current13LStaffOriginal.getId(), currentDate);
                        dutyCount++;
                        logger.info("日期 {}: 安排13L人员(原排序) {} 值班",
                                sdf.format(currentDate), current13LStaffOriginal.getStaffName());
                    }

                    if (!staffList13New.isEmpty()) {
                        RosterStaff current13LStaffNew = staffList13New.get(index13New % staffList13New.size());
                        createDutyRecord(current13LStaffNew.getId(), currentDate);
                        dutyCount++;
                        logger.info("日期 {}: 安排13L人员(新排序) {} 值班",
                                sdf.format(currentDate), current13LStaffNew.getStaffName());
                    }

                    // 安排8L人员值班
                    if (!staffList8.isEmpty()) {
                        RosterStaff current8LStaff = staffList8.get(index8 % staffList8.size());
                        createDutyRecord(current8LStaff.getId(), currentDate);
                        dutyCount++;
                        logger.info("日期 {}: 安排8L人员 {} 值班",
                                sdf.format(currentDate), current8LStaff.getStaffName());
                    }

                    // 安排10L人员值班
                    if (!staffList10.isEmpty()) {
                        RosterStaff current10LStaff = staffList10.get(index10 % staffList10.size());
                        createDutyRecord(current10LStaff.getId(), currentDate);
                        dutyCount++;
                        logger.info("日期 {}: 安排10L人员 {} 值班",
                                sdf.format(currentDate), current10LStaff.getStaffName());
                    }

                    // 2. 智能排班操作
                    boolean success = intelligentScheduleForDay(currentDate);
                    if (success) {
                        scheduledDays++;
                    }

                    // 标记6L人员明天要下夜
                    markNightShiftForTomorrow(current6LStaff.getId(), currentDate);
                    nightShiftStaffMap.put(currentDate, current6LStaff.getId());

                    //标记13L人员夜班明天要下夜
                    if (!staffList13New.isEmpty()) {
                        RosterStaff current13LStaffNew = staffList13New.get(index13New % staffList13New.size());
                        markNightShiftForTomorrow(current13LStaffNew.getId(), currentDate);
                        nightShiftStaffMap.put(currentDate, current13LStaffNew.getId());
                    }

                    // 标记8L人员明天要下夜
                    if (!staffList8.isEmpty()) {
                        RosterStaff current8LStaff = staffList8.get(index8 % staffList8.size());
                        markNightShiftForTomorrow(current8LStaff.getId(), currentDate);
                        nightShiftStaffMap.put(currentDate, current8LStaff.getId());
                    }

                    // 标记10L人员明天要下夜
                    if (!staffList10.isEmpty()) {
                        RosterStaff current10LStaff = staffList10.get(index10 % staffList10.size());
                        markNightShiftForTomorrow(current10LStaff.getId(), currentDate);
                        nightShiftStaffMap.put(currentDate, current10LStaff.getId());
                    }
                    // 索引前进
                    index6++;
                    index8++;
                    index10++;
                    index13Original++;
                    index13New++;

                    // 每两天前进一次5L索引
                    if (dayCount % 2 == 1) {
                        index5++;
                    }

                } catch (Exception e) {
                    logger.error("处理日期 {} 时出错", sdf.format(currentDate), e);
                }

                dayCount++;
                cal.add(Calendar.DATE, 1);
            }
            logger.info("智能排班完成，成功安排 {} 天", scheduledDays);
            logger.info("月度值班安排完成，成功安排 {} 条值班记录", dutyCount);

            return AjaxResult.success("月度安排完成：成功安排 " + scheduledDays + " 天排班，" + dutyCount + " 条值班记录");

        } catch (Exception e) {
            logger.error("月度排班与值班安排失败", e);
            return AjaxResult.error("月度安排失败: " + e.getMessage());
        }
    }

    /**
     * 为13L人员创建新的排序（最后两个放到最前面）
     */
    private List<RosterStaff> createNewSortFor13L(List<RosterStaff> originalList) {
        if (originalList.size() <= 2) {
            return new ArrayList<>(originalList);
        }

        List<RosterStaff> newList = new ArrayList<>();
        // 添加最后两个元素到最前面
        newList.add(originalList.get(originalList.size() - 2));
        newList.add(originalList.get(originalList.size() - 1));
        // 添加剩余的元素
        newList.addAll(originalList.subList(0, originalList.size() - 2));
        return newList;
    }

    /**
     * 对单日进行智能排班（修改后的逻辑）
     */
    private boolean intelligentScheduleForDay(Date date) {
        try {
            // 使用Java 8的时间API将时间部分截断
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);
            Date truncatedDate = Date.from(truncatedDateTime.atZone(ZoneId.systemDefault()).toInstant());

            // 获取当日的值班人员
            RosterDuty dutyQuery = new RosterDuty();
            dutyQuery.setDutyTime(truncatedDate);
            List<RosterDuty> dutyList = rosterDutyService.selectRosterDutyList(dutyQuery);

            // 处理下夜的人员
            int affectedRows = rosterStaffService.batchExpireLeaveRecords(truncatedDate);

            // 检查是否已存在该日期的排班数据
            int existingCount = rosterScheduleDetailService.selectCountByDate(truncatedDate);
            if (existingCount > 0) {
                // 如果已存在数据，先删除该日期的所有排班相关数据
                deleteExistingScheduleData(truncatedDate);
            }

            // 获取房间总数
            int roomCount = rosterRoomService.selectRosterRoomTotalCount();
            // 查询所有房间
            RosterRoom rosterRoom = new RosterRoom();
            List<RosterRoom> roomList = rosterRoomService.selectRosterRoomList(rosterRoom);

            // 查询各类型员工（排除请假人员）
            RosterStaff staffType5 = new RosterStaff();
            staffType5.setStaffTypeId(5L);
            List<RosterStaff> staffList5 = rosterStaffService.selectRosterStaffList(staffType5);

            RosterStaff staffType6 = new RosterStaff();
            staffType6.setStaffTypeId(6L);
            List<RosterStaff> staffList6 = rosterStaffService.selectRosterStaffList(staffType6);

            // 新增14L类型人员查询
            RosterStaff staffType14 = new RosterStaff();
            staffType14.setStaffTypeId(14L);
            List<RosterStaff> staffList14 = rosterStaffService.selectRosterStaffList(staffType14);

            RosterStaff staffType7 = new RosterStaff();
            staffType7.setStaffTypeId(7L);
            staffType7.setStaffLeaveEndTime(null);
            staffType7.setStatus("0");
            List<RosterStaff> staffList7 = rosterStaffService.selectRosterStaffList2(staffType7);

            // 查询8L、10L类型人员并合并
            RosterStaff staffType8 = new RosterStaff();
            staffType8.setStaffTypeId(8L);
            staffType8.setStaffLeaveEndTime(null);
            staffType8.setStatus("0");
            List<RosterStaff> staffList8 = rosterStaffService.selectRosterStaffList2(staffType8);

            RosterStaff staffType10 = new RosterStaff();
            staffType10.setStaffTypeId(10L);
            staffType10.setStaffLeaveEndTime(null);
            staffType10.setStatus("0");
            List<RosterStaff> staffList10 = rosterStaffService.selectRosterStaffList2(staffType10);

            RosterStaff staffType13 = new RosterStaff();
            staffType13.setStaffTypeId(13L);
            staffType13.setStaffLeaveEndTime(null);
            staffType13.setStatus("0");
            List<RosterStaff> staffList13 = rosterStaffService.selectRosterStaffList2(staffType13);

            // 合并8L、10L人员
            List<RosterStaff> secondaryStaffList = new ArrayList<>();
            secondaryStaffList.addAll(staffList8);
            secondaryStaffList.addAll(staffList10);

            // 创建次班人员随机索引列表
            List<Integer> randomSecondaryIndices = new ArrayList<>();
            for (int i = 0; i < secondaryStaffList.size(); i++) {
                randomSecondaryIndices.add(i);
            }
            // 随机打乱索引顺序
            Collections.shuffle(randomSecondaryIndices);
            int secondaryIndexCounter = 0; // 随机索引计数器

            // 合并5L、6L和14L人员并按staffSort排序
            List<RosterStaff> mainStaffList = new ArrayList<>();
            mainStaffList.addAll(staffList5);
            mainStaffList.addAll(staffList6);
            mainStaffList.addAll(staffList14);
            // 按staffSort从小到大排序
            mainStaffList.sort(Comparator.comparingInt(staff ->
                    Optional.ofNullable(staff.getStaffSort()).orElse(0)
            ));

            // 创建7L人员的随机索引列表
            List<Integer> randomNursingIndices = new ArrayList<>();
            for (int i = 0; i < staffList7.size(); i++) {
                randomNursingIndices.add(i);
            }
            // 随机打乱索引顺序
            Collections.shuffle(randomNursingIndices);

            // 创建13L人员的随机索引列表（用于顶替7L人员）
            List<Integer> random13LIndices = new ArrayList<>();
            for (int i = 0; i < staffList13.size(); i++) {
                random13LIndices.add(i);
            }
            // 随机打乱索引顺序
            Collections.shuffle(random13LIndices);
            int thirteenIndexCounter = 0; // 13L随机索引计数器

            // 循环房间进行排班
            int nursingRoomCount = 0; // 记录使用7L人员的房间数量
            int nursingRoomCount2 = 0; // 记录使用7L人员的房间数量
            int nursingIndexCounter = 0; // 随机索引计数器

            // 计算需要补足的13L人员数量
            int nursingShortage = 3 - staffList7.size(); // 需要补足的数量
            if (nursingShortage < 0) {
                nursingShortage = 0;
            }

            // 存储所有成功分配主班人员的房间及其优先级信息
            List<Pair<RosterScheduleDetail, RosterStaff>> mainStaffWithPriority = new ArrayList<>();

            // 存储所有成功分配护理人员的房间及其优先级信息
            List<Pair<RosterScheduleDetail, RosterStaff>> nursingStaffWithPriority = new ArrayList<>();

            for (int j = 0; j < roomCount; j++) {
                // 创建排班记录
                RosterScheduleDetail scheduleDetail = new RosterScheduleDetail();
                scheduleDetail.setDate(date);
                if (roomList.get(j).getStatus().equals("0")) {
                    scheduleDetail.setRoomId(roomList.get(j).getId());

                    rosterScheduleDetailService.insertRosterScheduleDetail(scheduleDetail);

                    boolean hasEnoughMainStaff = true; // 标记是否有足够的主班人员

                    // 处理主班人员：每个房间对应固定的主班人员（按排序顺序）
                    if (j < mainStaffList.size()) {
                        if (mainStaffList.get(j).getStatus().equals("0") && mainStaffList.get(j).getStaffLeave() == null) {
                            // 分配对应房间序号的主班人员
                            RosterStaff mainStaff = mainStaffList.get(j);
                            RosterStaffRelation mainStaffRelation = new RosterStaffRelation();
                            mainStaffRelation.setStaffId(mainStaff.getId());
                            mainStaffRelation.setScheduleDetailId(scheduleDetail.getId());
                            rosterStaffRelationService.insertRosterStaffRelation(mainStaffRelation);

                            // 记录该房间的主班人员及其优先级
                            mainStaffWithPriority.add(new Pair<>(scheduleDetail, mainStaff));

                            if(dutyList.get(0).getStaff().getId()==mainStaffList.get(j).getId()){
                                // 主班人员不足，尝试使用7L护理人员填补（最多只能填3个房间）
                                if (nursingIndexCounter < randomNursingIndices.size() && nursingRoomCount < 3) {
                                    // 使用随机索引获取7L人员
                                    int randomIndex = randomNursingIndices.get(nursingIndexCounter);
                                    RosterStaff nursingStaff = staffList7.get(randomIndex);
                                    RosterStaffRelation nursingRelation = new RosterStaffRelation();
                                    nursingRelation.setStaffId(nursingStaff.getId());
                                    nursingRelation.setScheduleDetailId(scheduleDetail.getId());
                                    rosterStaffRelationService.insertRosterStaffRelation(nursingRelation);

                                    // 记录该房间的护理人员及其优先级
                                    nursingStaffWithPriority.add(new Pair<>(scheduleDetail, nursingStaff));

                                    nursingRoomCount++; // 增加使用7L人员的房间计数
                                    nursingIndexCounter++; // 移动到下一个随机索引
                                } else if (thirteenIndexCounter < nursingShortage) {
                                    // 使用13L人员补足
                                    RosterStaff thirteenStaff = staffList13.get(thirteenIndexCounter);
                                    RosterStaffRelation thirteenRelation = new RosterStaffRelation();
                                    thirteenRelation.setStaffId(thirteenStaff.getId());
                                    thirteenRelation.setScheduleDetailId(scheduleDetail.getId());
                                    rosterStaffRelationService.insertRosterStaffRelation(thirteenRelation);

                                    // 记录该房间的护理人员及其优先级（13L人员）
                                    nursingStaffWithPriority.add(new Pair<>(scheduleDetail, thirteenStaff));

                                    thirteenIndexCounter++; // 移动到下一个13L人员
                                    nursingRoomCount++; // 增加使用补足人员的房间计数
                                } else {
                                    // 主班人员（5L+6L+14L）和7L人员都不足，或者已经达到7L人员的最大使用房间数（3个）
                                    hasEnoughMainStaff = false;
                                }
                            }
                        } else {
                            // 主班人员不足，尝试使用7L护理人员填补（最多只能填3个房间）
                            if (nursingIndexCounter < randomNursingIndices.size() && nursingRoomCount < 3) {
                                // 使用随机索引获取7L人员
                                int randomIndex = randomNursingIndices.get(nursingIndexCounter);
                                RosterStaff nursingStaff = staffList7.get(randomIndex);
                                RosterStaffRelation nursingRelation = new RosterStaffRelation();
                                nursingRelation.setStaffId(nursingStaff.getId());
                                nursingRelation.setScheduleDetailId(scheduleDetail.getId());
                                rosterStaffRelationService.insertRosterStaffRelation(nursingRelation);

                                // 根据当前是第几次使用7L人员来决定添加哪个值班人员
                                if (nursingRoomCount2 == 0 && dutyList.size() > 0) {
                                    // 第一次使用7L：添加dutyList.get(1)
                                    RosterStaffRelation dutyRelation1 = new RosterStaffRelation();
                                    dutyRelation1.setStaffId(dutyList.get(1).getStaff().getId());
                                    dutyRelation1.setScheduleDetailId(scheduleDetail.getId());
                                    rosterStaffRelationService.insertRosterStaffRelation(dutyRelation1);
                                }
                                // 记录该房间的护理人员及其优先级
                                nursingStaffWithPriority.add(new Pair<>(scheduleDetail, nursingStaff));

                                nursingRoomCount++; // 增加使用7L人员的房间计数
                                nursingRoomCount2++; // 增加使用7L人员的房间计数
                                nursingIndexCounter++; // 移动到下一个随机索引
                            } else if (thirteenIndexCounter < nursingShortage) {
                                // 使用13L人员补足
                                RosterStaff thirteenStaff = staffList13.get(thirteenIndexCounter);
                                RosterStaffRelation thirteenRelation = new RosterStaffRelation();
                                thirteenRelation.setStaffId(thirteenStaff.getId());
                                thirteenRelation.setScheduleDetailId(scheduleDetail.getId());
                                rosterStaffRelationService.insertRosterStaffRelation(thirteenRelation);


                                // 根据当前是第几次使用7L人员来决定添加哪个值班人员
                                if (nursingRoomCount2 == 0 && dutyList.size() > 0) {
                                    // 第一次使用7L：添加dutyList.get(1)
                                    RosterStaffRelation dutyRelation1 = new RosterStaffRelation();
                                    dutyRelation1.setStaffId(dutyList.get(1).getStaff().getId());
                                    dutyRelation1.setScheduleDetailId(scheduleDetail.getId());
                                    rosterStaffRelationService.insertRosterStaffRelation(dutyRelation1);
                                }

                                // 记录该房间的护理人员及其优先级（13L人员）
                                nursingStaffWithPriority.add(new Pair<>(scheduleDetail, thirteenStaff));

                                thirteenIndexCounter++; // 移动到下一个13L人员
                                nursingRoomCount++; // 增加使用补足人员的房间计数
                                nursingRoomCount2++; // 增加使用补足人员的房间计数
                            } else {
                                // 主班人员（5L+6L+14L）和7L人员都不足，或者已经达到7L人员的最大使用房间数（3个）
                                hasEnoughMainStaff = false;
                            }
                        }
                    } else {
                        // 主班人员不足，尝试使用7L护理人员填补（最多只能填3个房间）
                        if (nursingIndexCounter < randomNursingIndices.size() && nursingRoomCount < 3) {
                            // 使用随机索引获取7L人员
                            int randomIndex = randomNursingIndices.get(nursingIndexCounter);
                            RosterStaff nursingStaff = staffList7.get(randomIndex);
                            RosterStaffRelation nursingRelation = new RosterStaffRelation();
                            nursingRelation.setStaffId(nursingStaff.getId());
                            nursingRelation.setScheduleDetailId(scheduleDetail.getId());
                            rosterStaffRelationService.insertRosterStaffRelation(nursingRelation);

                            // 根据当前是第几次使用7L人员来决定添加哪个值班人员
                            if (nursingRoomCount == 0 && dutyList.size() > 0) {
                                // 第一次使用7L：添加dutyList.get(1)
                                RosterStaffRelation dutyRelation1 = new RosterStaffRelation();
                                dutyRelation1.setStaffId(dutyList.get(1).getStaff().getId());
                                dutyRelation1.setScheduleDetailId(scheduleDetail.getId());
                                rosterStaffRelationService.insertRosterStaffRelation(dutyRelation1);
                            }
                            // 记录该房间的护理人员及其优先级
                            nursingStaffWithPriority.add(new Pair<>(scheduleDetail, nursingStaff));

                            nursingRoomCount++; // 增加使用7L人员的房间计数
                            nursingIndexCounter++; // 移动到下一个随机索引
                        } else if (thirteenIndexCounter < nursingShortage) {
                            // 使用13L人员补足
                            RosterStaff thirteenStaff = staffList13.get(thirteenIndexCounter);
                            RosterStaffRelation thirteenRelation = new RosterStaffRelation();
                            thirteenRelation.setStaffId(thirteenStaff.getId());
                            thirteenRelation.setScheduleDetailId(scheduleDetail.getId());
                            rosterStaffRelationService.insertRosterStaffRelation(thirteenRelation);

                            // 记录该房间的护理人员及其优先级（13L人员）
                            nursingStaffWithPriority.add(new Pair<>(scheduleDetail, thirteenStaff));

                            thirteenIndexCounter++; // 移动到下一个13L人员
                            nursingRoomCount++; // 增加使用补足人员的房间计数
                        } else {
                            // 主班人员（5L+6L+14L）和7L人员都不足，或者已经达到7L人员的最大使用房间数（3个）
                            hasEnoughMainStaff = false;
                        }
                    }

                    // 暂时不分配次班人员，等待后续按优先级统一分配
                }
            }

            // ========== 关键修改：按主班人员优先级重新分配次班人员 ==========
            // 对已分配主班人员的房间按优先级排序（优先级高的在前）
            mainStaffWithPriority.sort((a, b) -> {
                int priorityA = Optional.ofNullable(a.getRight().getStaffPriority()).orElse(0);
                int priorityB = Optional.ofNullable(b.getRight().getStaffPriority()).orElse(0);
                return Integer.compare(priorityA, priorityB); // 降序排序，优先级高的在前
            });

            // 优先为高优先级的主班人员分配次班人员
            for (Pair<RosterScheduleDetail, RosterStaff> pair : mainStaffWithPriority) {
                RosterScheduleDetail scheduleDetail = pair.getLeft();

                // 检查该房间是否已有次班人员（可能之前已经分配过）
                RosterStaffRelation checkQuery = new RosterStaffRelation();
                checkQuery.setScheduleDetailId(scheduleDetail.getId());
                List<RosterStaffRelation> existingRelations = rosterStaffRelationService.selectRosterStaffRelationList(checkQuery);
                boolean hasSecondary = existingRelations.stream()
                        .anyMatch(rel -> {
                            Long typeId = rel.getRosterStaff().getStaffTypeId();
                            return typeId.equals(8L) || typeId.equals(10L);
                        });

                // 如果没有次班人员，且有可用的次班人员，则分配
                if (!hasSecondary && secondaryIndexCounter < randomSecondaryIndices.size()) {
                    int randomIndex = randomSecondaryIndices.get(secondaryIndexCounter);
                    RosterStaff secondaryStaff = secondaryStaffList.get(randomIndex);
                    RosterStaffRelation secondaryStaffRelation = new RosterStaffRelation();
                    secondaryStaffRelation.setStaffId(secondaryStaff.getId());
                    secondaryStaffRelation.setScheduleDetailId(scheduleDetail.getId());
                    rosterStaffRelationService.insertRosterStaffRelation(secondaryStaffRelation);

                    // 标记8L和10L排班人员下夜（明天休息）
                    if (secondaryStaff.getStaffTypeId() == 8L || secondaryStaff.getStaffTypeId() == 10L) {
                        markNightShiftForTomorrow(secondaryStaff.getId(), date);
                    }
                    secondaryIndexCounter++;

                    logger.info("为房间 {} 的主班人员 {} (优先级: {}) 分配次班人员 {}",
                            scheduleDetail.getRoomId(),
                            pair.getRight().getStaffName(),
                            pair.getRight().getStaffPriority(),
                            secondaryStaff.getStaffName());
                }
            }

            // ========== 新增：按护理人员优先级分配次班人员 ==========
            // 对已分配护理人员的房间按优先级排序（优先级高的在前）
            nursingStaffWithPriority.sort((a, b) -> {
                int priorityA = Optional.ofNullable(a.getRight().getStaffPriority()).orElse(0);
                int priorityB = Optional.ofNullable(b.getRight().getStaffPriority()).orElse(0);
                return Integer.compare(priorityA, priorityB); // 降序排序，优先级高的在前
            });

            // 优先为高优先级的护理人员分配次班人员
            for (Pair<RosterScheduleDetail, RosterStaff> pair : nursingStaffWithPriority) {
                RosterScheduleDetail scheduleDetail = pair.getLeft();

                // 检查该房间是否已有次班人员
                RosterStaffRelation checkQuery = new RosterStaffRelation();
                checkQuery.setScheduleDetailId(scheduleDetail.getId());
                List<RosterStaffRelation> existingRelations = rosterStaffRelationService.selectRosterStaffRelationList(checkQuery);
                boolean hasSecondary = existingRelations.stream()
                        .anyMatch(rel -> {
                            Long typeId = rel.getRosterStaff().getStaffTypeId();
                            return typeId.equals(8L) || typeId.equals(10L);
                        });

                // 如果没有次班人员，且有可用的次班人员，则分配
                if (!hasSecondary && secondaryIndexCounter < randomSecondaryIndices.size()) {
                    int randomIndex = randomSecondaryIndices.get(secondaryIndexCounter);
                    RosterStaff secondaryStaff = secondaryStaffList.get(randomIndex);
                    RosterStaffRelation secondaryStaffRelation = new RosterStaffRelation();
                    secondaryStaffRelation.setStaffId(secondaryStaff.getId());
                    secondaryStaffRelation.setScheduleDetailId(scheduleDetail.getId());
                    rosterStaffRelationService.insertRosterStaffRelation(secondaryStaffRelation);

                    // 标记8L和10L排班人员下夜（明天休息）
                    if (secondaryStaff.getStaffTypeId() == 8L || secondaryStaff.getStaffTypeId() == 10L) {
                        markNightShiftForTomorrow(secondaryStaff.getId(), date);
                    }
                    secondaryIndexCounter++;

                    logger.info("为房间 {} 的护理人员 {} (优先级: {}) 分配次班人员 {}",
                            scheduleDetail.getRoomId(),
                            pair.getRight().getStaffName(),
                            pair.getRight().getStaffPriority(),
                            secondaryStaff.getStaffName());
                }
            }

            return true;

        } catch (Exception e) {
            logger.error("单日智能排班失败", e);
            return false;
        }
    }

    /**
     * 删除指定月份的所有排班相关数据
     */
    private void deleteExistingScheduleDataForMonth(Date firstDay, Date lastDay) {
        try {
            // 查询该月份的所有排班详情
            RosterScheduleDetail query = new RosterScheduleDetail();
            List<RosterScheduleDetail> allDetails = rosterScheduleDetailService.selectRosterScheduleDetailList(query);
            List<RosterScheduleDetail> monthlyDetails = allDetails.stream()
                    .filter(detail -> !detail.getDate().before(firstDay) && !detail.getDate().after(lastDay))
                    .collect(Collectors.toList());

            // 删除相关的人员关联和手术台数据
            for (RosterScheduleDetail detail : monthlyDetails) {
                // 删除人员关联
                RosterStaffRelation relationQuery = new RosterStaffRelation();
                relationQuery.setScheduleDetailId(detail.getId());
                List<RosterStaffRelation> relations = rosterStaffRelationService.selectRosterStaffRelationList(relationQuery);

                for (RosterStaffRelation relation : relations) {
                    // 删除手术台数据
                    OperatingTable tableQuery = new OperatingTable();
                    tableQuery.setRelationId(relation.getId());
                    List<OperatingTable> tables = operatingTableService.selectOperatingTableList(tableQuery);
                    for (OperatingTable table : tables) {
                        operatingTableService.deleteOperatingTableById(table.getId());
                    }
                    // 删除人员关联
                    rosterStaffRelationService.deleteRosterStaffRelationById(relation.getId());
                }

                // 删除排班详情
                rosterScheduleDetailService.deleteRosterScheduleDetailById(detail.getId());
            }

            logger.info("删除 {} 个月的排班数据", monthlyDetails.size());

        } catch (Exception e) {
            logger.error("删除月度排班数据失败", e);
            throw new RuntimeException("删除月度排班数据失败", e);
        }
    }

    /**
     * 删除指定月份的所有值班记录
     */
    private void deleteMonthlyDuties(Date firstDay, Date lastDay) {
        try {
            // 查询该月份的所有值班记录
            RosterDuty query = new RosterDuty();
            List<RosterDuty> allDuties = rosterDutyService.selectRosterDutyList(query);

            // 过滤出在指定日期范围内的记录
            List<Long> dutyIdsToDelete = allDuties.stream()
                    .filter(duty -> !duty.getDutyTime().before(firstDay) && !duty.getDutyTime().after(lastDay))
                    .map(RosterDuty::getId)
                    .collect(Collectors.toList());

            if (!dutyIdsToDelete.isEmpty()) {
                // 批量删除
                rosterDutyService.deleteRosterDutyByIds(dutyIdsToDelete.toArray(new Long[0]));
                logger.info("删除 {} 条值班记录", dutyIdsToDelete.size());
            }

        } catch (Exception e) {
            logger.error("删除月度值班记录失败", e);
            throw new RuntimeException("删除值班记录失败", e);
        }
    }


    /**
     * 创建值班记录
     */
    private void createDutyRecord(Long staffId, Date dutyTime) {
        try {
            // 截断dutyTime的时间部分，确保只比较日期
            LocalDateTime localDateTime = LocalDateTime.ofInstant(dutyTime.toInstant(), ZoneId.systemDefault());
            LocalDateTime truncatedDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);
            Date truncatedDutyTime = Date.from(truncatedDateTime.atZone(ZoneId.systemDefault()).toInstant());
            
            // 检查是否已存在该人员该天的值班记录
            RosterDuty checkQuery = new RosterDuty();
            checkQuery.setDutyStaffId(staffId);
            checkQuery.setDutyTime(truncatedDutyTime);
            List<RosterDuty> existing = rosterDutyService.selectRosterDutyList(checkQuery);

            if (existing.isEmpty()) {
                RosterDuty duty = new RosterDuty();
                duty.setDutyStaffId(staffId);
                duty.setDutyTime(truncatedDutyTime);
                duty.setCreateTime(new Date());
                rosterDutyService.insertRosterDuty(duty);

                logger.info("创建值班记录: 人员ID={}, 日期={}",
                        staffId, new SimpleDateFormat("yyyy-MM-dd").format(truncatedDutyTime));
            }
        } catch (Exception e) {
            logger.error("创建值班记录失败 staffId: {}, date: {}", staffId, dutyTime, e);
        }
    }


    /**
     * 标记人员明天要下夜（staff_leave=3）
     */
    private void markNightShiftForTomorrow(Long staffId, Date dutyDate) {
        try {
            Calendar tomorrowCal = Calendar.getInstance();
            tomorrowCal.setTime(dutyDate);
            tomorrowCal.add(Calendar.DATE, 2);
            Date tomorrow = tomorrowCal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tomorrowStr = sdf.format(tomorrow);

            // 更新人员请假状态
            RosterStaff staff = rosterStaffService.selectRosterStaffById(staffId);
            if (staff != null) {
                staff.setStaffLeave("3"); // 下夜标记
                staff.setStaffLeaveEndTime(tomorrowStr);
                rosterStaffService.updateRosterStaff(staff);

                logger.info("标记人员 {} 明天({})下夜休息",
                        staff.getStaffName(),
                        new SimpleDateFormat("yyyy-MM-dd").format(tomorrow));
            }
        } catch (Exception e) {
            logger.error("标记下夜人员失败 staffId: {}", staffId, e);
        }
    }

    /**
     * 获取上个月最后的值班索引（更新版本，包含所有类型索引）
     * 修改说明：直接根据上个月最后几天的值班记录中各类型人员的位置，
     * 计算出当前月应该从哪个位置开始轮转
     */
    private Map<String, Integer> getLastMonthDutyIndices(Date currentMonthFirstDay,
                                                         List<RosterStaff> staffList5,
                                                         List<RosterStaff> staffList6,
                                                         List<RosterStaff> staffList8,
                                                         List<RosterStaff> staffList10,
                                                         List<RosterStaff> staffList13Original,
                                                         List<RosterStaff> staffList13New) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentMonthFirstDay);
            cal.add(Calendar.MONTH, -1); // 上个月

            Date lastMonthFirstDay = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date lastMonthLastDay = cal.getTime();

            // 查询上个月的值班记录
            RosterDuty query = new RosterDuty();
            List<RosterDuty> allDuties = rosterDutyService.selectRosterDutyList(query);
            List<RosterDuty> lastMonthDuties = allDuties.stream()
                    .filter(duty -> !duty.getDutyTime().before(lastMonthFirstDay) && !duty.getDutyTime().after(lastMonthLastDay))
                    .collect(Collectors.toList());

            if (lastMonthDuties.isEmpty()) {
                logger.info("上个月没有值班记录，使用默认索引");
                return null;
            }

            // 按日期排序
            lastMonthDuties.sort(Comparator.comparing(RosterDuty::getDutyTime));

            // 获取最后一天的所有值班记录
            Date lastDay = lastMonthDuties.get(lastMonthDuties.size() - 1).getDutyTime();
            // 截断lastDay的时间部分以进行正确的日期比较
            LocalDateTime lastDayDateTime = LocalDateTime.ofInstant(lastDay.toInstant(), ZoneId.systemDefault());
            LocalDateTime truncatedLastDay = lastDayDateTime.truncatedTo(ChronoUnit.DAYS);
            Date lastDayTruncated = Date.from(truncatedLastDay.atZone(ZoneId.systemDefault()).toInstant());
            
            List<RosterDuty> lastDayDuties = lastMonthDuties.stream()
                    .filter(duty -> {
                        LocalDateTime dutyDateTime = LocalDateTime.ofInstant(duty.getDutyTime().toInstant(), ZoneId.systemDefault());
                        LocalDateTime truncatedDutyDate = dutyDateTime.truncatedTo(ChronoUnit.DAYS);
                        Date truncatedDutyDay = Date.from(truncatedDutyDate.atZone(ZoneId.systemDefault()).toInstant());
                        return truncatedDutyDay.equals(lastDayTruncated);
                    })
                    .collect(Collectors.toList());

            if (lastDayDuties.isEmpty()) {
                logger.info("上个月最后一天({}) 没有值班记录", lastDayTruncated);
                return null;
            }

            Map<String, Integer> indices = new HashMap<>();
            
            logger.info("上个月最后一天({}) 的值班人员顺序:", lastDayTruncated);
            
            // 根据9月30日的实际值班顺序来设置索引
            // 每一天的值班顺序是：6L, 5L, 13L原, 13L新, 8L, 10L
            int count5L = 0, count6L = 0, count8L = 0, count10L = 0, count13LOriginal = 0, count13LNew = 0;
            
            for (RosterDuty duty : lastDayDuties) {
                RosterStaff staff = rosterDutyService.selectRosterDutyById(duty.getId()).getStaff();
                if (staff == null) {
                    staff = rosterStaffService.selectRosterStaffById(duty.getDutyStaffId());
                }
                
                if (staff != null) {
                    Long staffTypeId = staff.getStaffTypeId();
                    logger.info("  9月份值班: {} ({}L)", staff.getStaffName(), staffTypeId);
                    
                    if (staffTypeId.equals(6L) && count6L == 0) {
                        // 第一个6L人员
                        for (int i = 0; i < staffList6.size(); i++) {
                            if (staffList6.get(i).getId().equals(staff.getId())) {
                                indices.put("index6", (i + 1) % staffList6.size());
                                logger.info("    设置 index6 = {}", (i + 1) % staffList6.size());
                                count6L++;
                                break;
                            }
                        }
                    } else if (staffTypeId.equals(5L) && count5L == 0) {
                        // 第一个5L人员
                        for (int i = 0; i < staffList5.size(); i++) {
                            if (staffList5.get(i).getId().equals(staff.getId())) {
                                indices.put("index5", (i + 1) % staffList5.size());
                                logger.info("    设置 index5 = {}", (i + 1) % staffList5.size());
                                count5L++;
                                break;
                            }
                        }
                    } else if (staffTypeId.equals(13L)) {
                        // 两个13L人员：一个来自原排序，一个来自新排序
                        boolean foundInOriginal = false;
                        if (count13LOriginal == 0) {
                            for (int i = 0; i < staffList13Original.size(); i++) {
                                if (staffList13Original.get(i).getId().equals(staff.getId())) {
                                    indices.put("index13Original", (i + 1) % staffList13Original.size());
                                    logger.info("    设置 index13Original = {}", (i + 1) % staffList13Original.size());
                                    count13LOriginal++;
                                    foundInOriginal = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!foundInOriginal && count13LNew == 0) {
                            for (int i = 0; i < staffList13New.size(); i++) {
                                if (staffList13New.get(i).getId().equals(staff.getId())) {
                                    indices.put("index13New", (i + 1) % staffList13New.size());
                                    logger.info("    设置 index13New = {}", (i + 1) % staffList13New.size());
                                    count13LNew++;
                                    break;
                                }
                            }
                        }
                    } else if (staffTypeId.equals(8L) && count8L == 0) {
                        // 第一个8L人员
                        for (int i = 0; i < staffList8.size(); i++) {
                            if (staffList8.get(i).getId().equals(staff.getId())) {
                                indices.put("index8", (i + 1) % staffList8.size());
                                logger.info("    设置 index8 = {}", (i + 1) % staffList8.size());
                                count8L++;
                                break;
                            }
                        }
                    } else if (staffTypeId.equals(10L) && count10L == 0) {
                        // 第一个10L人员
                        for (int i = 0; i < staffList10.size(); i++) {
                            if (staffList10.get(i).getId().equals(staff.getId())) {
                                indices.put("index10", (i + 1) % staffList10.size());
                                logger.info("    设置 index10 = {}", (i + 1) % staffList10.size());
                                count10L++;
                                break;
                            }
                        }
                    }
                }
            }
            
            logger.info("上个月最后一天({}) 计算出的索引: {}", lastDayTruncated, indices);

            return indices;

        } catch (Exception e) {
            logger.error("获取上个月值班索引失败", e);
            return null;
        }
    }

}