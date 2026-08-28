package com.example.audit.service;

import com.example.audit.dto.AuditLogDTO;
import com.example.audit.model.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public void logAction(String action, String entityName, Long entityId, String details, String oldState, String newState, String performedBy) {
        AuditLog log = new AuditLog(action, entityName, entityId, details, oldState, newState, performedBy);
        auditLogRepository.save(log);
    }

    public List<AuditLogDTO> getLogsForUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        List<AuditLog> logs;
        if (user != null && user.getRole() == Role.ROLE_ADMIN) {
            logs = auditLogRepository.findTop100ByOrderByTimestampDesc();
        } else {
            logs = auditLogRepository.findTop50ByPerformedByOrderByTimestampDesc(username);
        }
        return logs.stream().map(AuditLogDTO::new).collect(Collectors.toList());
    }
}
