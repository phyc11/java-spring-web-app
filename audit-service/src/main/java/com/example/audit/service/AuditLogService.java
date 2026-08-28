package com.example.audit.service;

import com.example.audit.dto.AuditLogDTO;
import com.example.audit.model.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogDTO> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(AuditLogDTO::new)
                .collect(Collectors.toList());
    }

    public AuditLogDTO recordLog(String action, String entityName, Long entityId, String oldState, String newState, String details, String performedBy) {
        AuditLog log = new AuditLog(action, entityName, entityId, oldState, newState, details, performedBy != null ? performedBy : "System");
        AuditLog savedLog = auditLogRepository.save(log);
        return new AuditLogDTO(savedLog);
    }
}
