package com.example.analytics.service;

import com.example.task.model.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportToExcel(List<Task> tasks) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TaskCraft Tasks");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Tiêu đề Task", "Mô tả", "Trạng thái", "Độ ưu tiên", "Danh mục", "Người tạo", "Hạn chót", "Thời gian tạo"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle() != null ? task.getTitle() : "");
                row.createCell(2).setCellValue(task.getDescription() != null ? task.getDescription() : "");
                row.createCell(3).setCellValue(task.getStatus() != null ? task.getStatus().name() : "");
                row.createCell(4).setCellValue(task.getPriority() != null ? task.getPriority().name() : "");
                row.createCell(5).setCellValue(task.getCategory() != null ? task.getCategory().getName() : "General");
                row.createCell(6).setCellValue(task.getUser() != null ? task.getUser().getUsername() : "System");
                row.createCell(7).setCellValue(task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : "");
                row.createCell(8).setCellValue(task.getCreatedAt() != null ? task.getCreatedAt().format(DATE_FORMATTER) : "");
            }

            // Auto fit column widths
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportToCsv(List<Task> tasks) {
        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM for Excel Vietnamese characters display support
        csv.append('\uFEFF');
        csv.append("ID,Tiêu đề Task,Mô tả,Trạng thái,Độ ưu tiên,Danh mục,Người tạo,Hạn chót,Thời gian tạo\n");

        for (Task task : tasks) {
            csv.append(task.getId()).append(",")
               .append(escapeCsv(task.getTitle())).append(",")
               .append(escapeCsv(task.getDescription())).append(",")
               .append(task.getStatus()).append(",")
               .append(task.getPriority()).append(",")
               .append(escapeCsv(task.getCategory() != null ? task.getCategory().getName() : "General")).append(",")
               .append(escapeCsv(task.getUser() != null ? task.getUser().getUsername() : "System")).append(",")
               .append(task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : "").append(",")
               .append(task.getCreatedAt() != null ? task.getCreatedAt().format(DATE_FORMATTER) : "").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        String escaped = input.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
