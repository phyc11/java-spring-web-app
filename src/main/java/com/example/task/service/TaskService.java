package com.example.task.service;

import com.example.common.exception.ResourceNotFoundException;
import com.example.audit.service.AuditLogService;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.task.dto.TaskDTO;
import com.example.task.dto.TaskStatsDTO;
import com.example.task.model.Category;
import com.example.task.model.Priority;
import com.example.task.model.Status;
import com.example.task.model.Task;
import com.example.task.repository.CategoryRepository;
import com.example.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository,
                       UserRepository userRepository, AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<TaskDTO> getFilteredTasks(String username, Status status, Priority priority, Long categoryId, String search) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        User queryUser = (currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN) ? null : currentUser;

        List<Task> tasks = taskRepository.filterTasks(queryUser, status, priority, categoryId, search);
        return tasks.stream().map(TaskDTO::new).collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        verifyUserPermission(task, username);
        return new TaskDTO(task);
    }

    public TaskDTO createTask(TaskDTO dto, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.TODO);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM);
        task.setUser(currentUser);
        task.setDueDate(dto.getDueDate());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            task.setCategory(category);
        }

        int maxPos = taskRepository.findMaxPositionByStatus(task.getStatus());
        task.setPosition(maxPos + 1);

        Task saved = taskRepository.save(task);

        auditLogService.logAction(
                "CREATE", "Task", saved.getId(),
                "Tạo task mới: " + saved.getTitle(),
                null, saved.getStatus().name(), username
        );

        return new TaskDTO(saved);
    }

    public TaskDTO updateTask(Long id, TaskDTO dto, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        verifyUserPermission(task, username);

        String oldTitle = task.getTitle();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            task.setCategory(category);
        }

        Task updated = taskRepository.save(task);

        auditLogService.logAction(
                "UPDATE", "Task", updated.getId(),
                "Cập nhật thông tin task ID: " + updated.getId(),
                oldTitle, updated.getTitle(), username
        );

        return new TaskDTO(updated);
    }

    public TaskDTO updateTaskStatus(Long id, Status newStatus, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        verifyUserPermission(task, username);

        Status oldStatus = task.getStatus();
        if (oldStatus != newStatus) {
            task.setStatus(newStatus);
            int maxPos = taskRepository.findMaxPositionByStatus(newStatus);
            task.setPosition(maxPos + 1);

            Task saved = taskRepository.save(task);

            auditLogService.logAction(
                    "STATUS_CHANGE", "Task", saved.getId(),
                    "Đổi trạng thái task '" + saved.getTitle() + "' từ " + oldStatus + " -> " + newStatus,
                    oldStatus.name(), newStatus.name(), username
            );

            return new TaskDTO(saved);
        }

        return new TaskDTO(task);
    }

    public TaskDTO moveTask(Long id, Status targetStatus, Integer position, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        verifyUserPermission(task, username);

        Status oldStatus = task.getStatus();
        Integer oldPosition = task.getPosition();

        task.setStatus(targetStatus);
        if (position != null) {
            task.setPosition(position);
        } else {
            int maxPos = taskRepository.findMaxPositionByStatus(targetStatus);
            task.setPosition(maxPos + 1);
        }

        Task saved = taskRepository.save(task);

        auditLogService.logAction(
                "MOVE", "Task", saved.getId(),
                "Kéo thả Task '" + saved.getTitle() + "' sang cột " + targetStatus,
                oldStatus.name() + " (pos: " + oldPosition + ")",
                targetStatus.name() + " (pos: " + saved.getPosition() + ")", username
        );

        return new TaskDTO(saved);
    }

    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        verifyUserPermission(task, username);

        auditLogService.logAction(
                "DELETE", "Task", task.getId(),
                "Xóa task: " + task.getTitle(),
                task.getTitle(), null, username
        );

        taskRepository.delete(task);
    }

    public TaskStatsDTO getStats(String username) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null && user.getRole() == Role.ROLE_ADMIN) {
            long total = taskRepository.count();
            long todo = taskRepository.countByStatus(Status.TODO);
            long progress = taskRepository.countByStatus(Status.IN_PROGRESS);
            long completed = taskRepository.countByStatus(Status.COMPLETED);
            return new TaskStatsDTO(total, todo, progress, completed);
        } else if (user != null) {
            long total = taskRepository.countByUser(user);
            long todo = taskRepository.countByUserAndStatus(user, Status.TODO);
            long progress = taskRepository.countByUserAndStatus(user, Status.IN_PROGRESS);
            long completed = taskRepository.countByUserAndStatus(user, Status.COMPLETED);
            return new TaskStatsDTO(total, todo, progress, completed);
        }

        return new TaskStatsDTO(0, 0, 0, 0);
    }

    private void verifyUserPermission(Task task, String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (currentUser.getRole() != Role.ROLE_ADMIN && (task.getUser() == null || !task.getUser().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("Permission denied: You can only modify your own tasks");
        }
    }
}
