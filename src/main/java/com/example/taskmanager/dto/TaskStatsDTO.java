package com.example.taskmanager.dto;

public class TaskStatsDTO {
    private long total;
    private long todo;
    private long inProgress;
    private long completed;
    private long urgent;

    public TaskStatsDTO() {}

    public TaskStatsDTO(long total, long todo, long inProgress, long completed, long urgent) {
        this.total = total;
        this.todo = todo;
        this.inProgress = inProgress;
        this.completed = completed;
        this.urgent = urgent;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTodo() {
        return todo;
    }

    public void setTodo(long todo) {
        this.todo = todo;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getUrgent() {
        return urgent;
    }

    public void setUrgent(long urgent) {
        this.urgent = urgent;
    }
}
