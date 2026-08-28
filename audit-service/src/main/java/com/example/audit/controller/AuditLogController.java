package com.example.audit.controller;

import com.example.common.dto.ApiResponse;
import com.example.audit.dto.AuditLogDTO;
import com.example.audit.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<List<AuditLogDTO>> getAllAuditLogs() {
        List<AuditLogDTO> logs = auditLogService.getAllAuditLogs();
        return ApiResponse.ok("Audit logs retrieved successfully", logs);
    }

    @PostMapping
    public ApiResponse<AuditLogDTO> createLog(@RequestBody AuditLogDTO dto) {
        AuditLogDTO log = auditLogService.recordLog(
                dto.getAction(),
                dto.getEntityName(),
                dto.getEntityId(),
                dto.getOldState(),
                dto.getNewState(),
                dto.getDetails(),
                dto.getPerformedBy()
        );
        return ApiResponse.ok("Log recorded successfully", log);
    }
}
