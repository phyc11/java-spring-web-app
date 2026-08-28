package com.example.taskmanager.service;

import com.example.taskmanager.dto.AuditLogDTO;
import com.example.taskmanager.model.AuditLog;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.AuditLogRepository;
import com.example.taskmanager.repository.UserRepository;
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

    public void recordLog(String action, String entityName, Long entityId, String details, String oldState, String newState, String performedBy) {
        AuditLog log = new AuditLog(action, entityName, entityId, details, oldState, newState, performedBy);
        auditLogRepository.save(log);
    }

    public List<AuditLogDTO> getLogsForUser(String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        List<AuditLog> logs;

        if (currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN) {
            logs = auditLogRepository.findTop100ByOrderByTimestampDesc();
        } else {
            logs = auditLogRepository.findTop50ByPerformedByOrderByTimestampDesc(username);
        }

        return logs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private AuditLogDTO convertToDTO(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(log.getId());
        dto.setAction(log.getAction());
        dto.setEntityName(log.getEntityName());
        dto.setEntityId(log.getEntityId());
        dto.setDetails(log.getDetails());
        dto.setOldState(log.getOldState());
        dto.setNewState(log.getNewState());
        dto.setPerformedBy(log.getPerformedBy());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }
}
