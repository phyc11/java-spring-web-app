package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.AuditLogDTO;
import com.example.taskmanager.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogDTO>> getAuditLogs(Principal principal) {
        if (principal == null) {
            return ApiResponse.error("Unauthenticated");
        }
        List<AuditLogDTO> logs = auditLogService.getLogsForUser(principal.getName());
        return ApiResponse.ok("Audit logs retrieved successfully", logs);
    }
}
