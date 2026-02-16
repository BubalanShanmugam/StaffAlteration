package com.staffalteration.service;

import com.staffalteration.dto.AlterationDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ExcelExportService {

    public byte[] generateAlterationReport(List<AlterationDTO> alterations, String departmentName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Alterations Report");

            // Create header styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeight((short) (12 * 20));
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            // Create data styles
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true);

            // Create title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Staff Alteration Report - " + departmentName);
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

            // Create column headers
            Row headerRow = sheet.createRow(2);
            String[] headers = {
                    "Original Staff",
                    "Substitute Staff",
                    "Class",
                    "Subject",
                    "Date",
                    "Duration",
                    "Status",
                    "Period",
                    "Remarks"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Set column widths
            sheet.setColumnWidth(0, 20 * 256); // Original Staff
            sheet.setColumnWidth(1, 20 * 256); // Substitute Staff
            sheet.setColumnWidth(2, 15 * 256); // Class
            sheet.setColumnWidth(3, 20 * 256); // Subject
            sheet.setColumnWidth(4, 15 * 256); // Date
            sheet.setColumnWidth(5, 20 * 256); // Duration
            sheet.setColumnWidth(6, 15 * 256); // Status
            sheet.setColumnWidth(7, 15 * 256); // Period
            sheet.setColumnWidth(8, 25 * 256); // Remarks

            // Add data rows
            int rowNum = 3;
            for (AlterationDTO alteration : alterations) {
                Row row = sheet.createRow(rowNum++);

                // Original Staff
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(alteration.getOriginalStaffName());
                cell0.setCellStyle(dataStyle);

                // Substitute Staff
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(alteration.getSubstituteStaffName() != null ? alteration.getSubstituteStaffName() : "Pending");
                cell1.setCellStyle(dataStyle);

                // Class
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(alteration.getClassCode());
                cell2.setCellStyle(dataStyle);

                // Subject
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(alteration.getSubjectName());
                cell3.setCellStyle(dataStyle);

                // Date
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(alteration.getAlterationDate().toString());
                cell4.setCellStyle(dataStyle);

                // Duration (Absence Type)
                Cell cell5 = row.createCell(5);
                String duration = getDurationDisplay(alteration.getAbsenceType());
                cell5.setCellValue(duration);
                cell5.setCellStyle(dataStyle);

                // Status
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(alteration.getStatus());
                cell6.setCellStyle(dataStyle);

                // Period
                Cell cell7 = row.createCell(7);
                String periodDisplay = getPeriodDisplay(alteration.getPeriodNumber(), alteration.getAbsenceType());
                cell7.setCellValue(periodDisplay);
                cell7.setCellStyle(dataStyle);

                // Remarks
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(alteration.getRemarks() != null ? alteration.getRemarks() : "");
                cell8.setCellStyle(dataStyle);
            }

            // Add summary row
            int summaryRow = rowNum + 1;
            Row summaryHeaderRow = sheet.createRow(summaryRow);
            Cell summaryCell = summaryHeaderRow.createCell(0);
            summaryCell.setCellValue("Total Alterations: " + alterations.size());
            summaryCell.setCellStyle(headerStyle);

            // Write to ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    private String getDurationDisplay(String absenceType) {
        switch (absenceType) {
            case "FN":
                return "Full Day Leave";
            case "AN":
                return "Half Day Morning (9AM - 1PM)";
            case "AF":
                return "Half Day Afternoon (1PM - 5PM)";
            case "ONDUTY":
                return "On Duty - Full Day";
            case "PERIOD_WISE_ABSENT":
                return "Period Wise";
            default:
                return absenceType;
        }
    }

    private String getPeriodDisplay(Integer periodNumber, String absenceType) {
        if ("PERIOD_WISE_ABSENT".equals(absenceType) && periodNumber != null) {
            return "Period " + periodNumber + " (" + getPeriodTime(periodNumber) + ")";
        } else if (absenceType != null && !absenceType.equals("PERIOD_WISE_ABSENT")) {
            return "All Periods";
        }
        return "-";
    }

    private String getPeriodTime(Integer periodNumber) {
        if (periodNumber == null) return "Unknown";
        return switch (periodNumber) {
            case 1 -> "9:00-10:00";
            case 2 -> "10:00-11:00";
            case 3 -> "11:00-12:00";
            case 4 -> "12:00-1:00";
            case 5 -> "1:00-2:00";
            case 6 -> "2:00-3:00";
            default -> "Unknown";
        };
    }
}
