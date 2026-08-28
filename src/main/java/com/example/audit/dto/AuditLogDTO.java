package com.example.audit.dto;

import com.example.audit.model.AuditLog;

import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private String action;
    private String entityName;
    private Long entityId;
    private String details;
    private String oldState;
    private String newState;
    private String performedBy;
    private LocalDateTime timestamp;

    public AuditLogDTO() {}

    public AuditLogDTO(AuditLog log) {
        this.id = log.getId();
        this.action = log.getAction();
        this.entityName = log.getEntityName();
        this.entityId = log.getEntityId();
        this.details = log.getDetails();
        this.oldState = log.getOldState();
        this.newState = log.getNewState();
        this.performedBy = log.getPerformedBy();
        this.timestamp = log.getTimestamp();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
