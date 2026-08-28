package com.example.comment.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private String author;

    private String authorAvatarColor = "#6366f1";

    @Column(length = 2000, nullable = false)
    private String content;

    private Long parentId; // For Threaded Replies

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Comment() {}

    public Comment(Long taskId, String author, String authorAvatarColor, String content, Long parentId) {
        this.taskId = taskId;
        this.author = author;
        this.authorAvatarColor = authorAvatarColor != null ? authorAvatarColor : "#6366f1";
        this.content = content;
        this.parentId = parentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorAvatarColor() { return authorAvatarColor; }
    public void setAuthorAvatarColor(String authorAvatarColor) { this.authorAvatarColor = authorAvatarColor; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
