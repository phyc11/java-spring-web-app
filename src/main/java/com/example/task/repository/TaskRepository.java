package com.example.task.repository;

import com.example.auth.model.User;
import com.example.task.model.Priority;
import com.example.task.model.Status;
import com.example.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user);

    @Query("SELECT t FROM Task t WHERE " +
           "(:user IS NULL OR t.user = :user) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.position ASC, t.createdAt DESC")
    List<Task> filterTasks(@Param("user") User user,
                           @Param("status") Status status,
                           @Param("priority") Priority priority,
                           @Param("categoryId") Long categoryId,
                           @Param("search") String search);

    long countByUserAndStatus(User user, Status status);
    long countByStatus(Status status);

    long countByUser(User user);

    @Query("SELECT COALESCE(MAX(t.position), 0) FROM Task t WHERE t.status = :status")
    int findMaxPositionByStatus(@Param("status") Status status);
}
