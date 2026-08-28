package com.example.taskmanager.repository;

import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatusOrderByPositionAsc(Status status);

    @Query("SELECT t FROM Task t WHERE " +
           "(:user IS NULL OR t.user = :user) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.position ASC, t.updatedAt DESC")
    List<Task> filterTasks(@Param("user") User user,
                           @Param("status") Status status,
                           @Param("priority") Priority priority,
                           @Param("categoryId") Long categoryId,
                           @Param("search") String search);

    @Query("SELECT COUNT(t) FROM Task t WHERE (:user IS NULL OR t.user = :user)")
    long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status AND (:user IS NULL OR t.user = :user)")
    long countByStatusAndUser(@Param("status") Status status, @Param("user") User user);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.priority = :priority AND (:user IS NULL OR t.user = :user)")
    long countByPriorityAndUser(@Param("priority") Priority priority, @Param("user") User user);
}
