package com.example.taskmanager.controller;

import com.example.taskmanager.dto.AnalyticsDTO;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.AnalyticsService;
import com.example.taskmanager.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalyticsExportController {

    private final AnalyticsService analyticsService;
    private final ExportService exportService;

    @Autowired
    public AnalyticsExportController(AnalyticsService analyticsService, ExportService exportService) {
        this.analyticsService = analyticsService;
        this.exportService = exportService;
    }

    @GetMapping("/analytics")
    public ApiResponse<AnalyticsDTO> getAnalytics(Principal principal) {
        if (principal == null) {
            return ApiResponse.error("Unauthenticated");
        }
        AnalyticsDTO analytics = analyticsService.getAnalytics(principal.getName());
        return ApiResponse.ok("Analytics data retrieved", analytics);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(Principal principal) throws IOException {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        List<Task> tasks = analyticsService.getTasksForExport(principal.getName());
        byte[] excelBytes = exportService.exportToExcel(tasks);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"TaskCraft_Report.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        List<Task> tasks = analyticsService.getTasksForExport(principal.getName());
        byte[] csvBytes = exportService.exportToCsv(tasks);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"TaskCraft_Report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
