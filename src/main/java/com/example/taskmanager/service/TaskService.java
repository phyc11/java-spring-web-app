package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.TaskStatsDTO;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    public TaskService(TaskRepository taskRepository,
                       CategoryRepository categoryRepository,
                       UserRepository userRepository,
                       AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<TaskDTO> filterTasks(Status status, Priority priority, Long categoryId, String search, String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        User queryUser = (currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN) ? null : currentUser;

        return taskRepository.filterTasks(queryUser, status, priority, categoryId, search)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        
        checkTaskPermission(task, username);
        return convertToDTO(task);
    }

    public TaskDTO createTask(TaskDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.TODO);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM);
        task.setPosition(dto.getPosition() != null ? dto.getPosition() : 0);
        task.setDueDate(dto.getDueDate());
        task.setUser(user);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
            task.setCategory(category);
        }

        Task saved = taskRepository.save(task);

        // Record Audit Log
        auditLogService.recordLog(
                "CREATE",
                "TASK",
                saved.getId(),
                "Tạo mới công việc '" + saved.getTitle() + "'",
                null,
                "Status: " + saved.getStatus() + " | Priority: " + saved.getPriority(),
                username
        );

        return convertToDTO(saved);
    }

    public TaskDTO updateTask(Long id, TaskDTO dto, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        checkTaskPermission(task, username);

        String oldState = "Title: " + task.getTitle() + " | Status: " + task.getStatus() + " | Priority: " + task.getPriority();

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        if (dto.getPosition() != null) task.setPosition(dto.getPosition());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
            task.setCategory(category);
        }

        Task updated = taskRepository.save(task);
        String newState = "Title: " + updated.getTitle() + " | Status: " + updated.getStatus() + " | Priority: " + updated.getPriority();

        // Record Audit Log
        auditLogService.recordLog(
                "UPDATE",
                "TASK",
                updated.getId(),
                "Chỉnh sửa công việc '" + updated.getTitle() + "'",
                oldState,
                newState,
                username
        );

        return convertToDTO(updated);
    }

    public TaskDTO updateTaskStatus(Long id, Status status, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        checkTaskPermission(task, username);
        Status oldStatus = task.getStatus();
        task.setStatus(status);
        Task updated = taskRepository.save(task);

        // Record Audit Log
        auditLogService.recordLog(
                "STATUS_CHANGE",
                "TASK",
                updated.getId(),
                "Đổi trạng thái task '" + updated.getTitle() + "' từ " + oldStatus + " ➔ " + status,
                oldStatus.name(),
                status.name(),
                username
        );

        return convertToDTO(updated);
    }

    public TaskDTO moveTask(Long id, Status targetStatus, Integer targetPosition, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        checkTaskPermission(task, username);

        Status oldStatus = task.getStatus();
        Integer oldPosition = task.getPosition();

        if (targetStatus != null) {
            task.setStatus(targetStatus);
        }
        if (targetPosition != null) {
            task.setPosition(targetPosition);
        }

        Task updated = taskRepository.save(task);

        // Record Audit Log
        auditLogService.recordLog(
                "MOVE",
                "TASK",
                updated.getId(),
                "Kéo thả Task '" + updated.getTitle() + "' sang cột " + updated.getStatus(),
                "Status: " + oldStatus + ", Pos: " + oldPosition,
                "Status: " + updated.getStatus() + ", Pos: " + updated.getPosition(),
                username
        );

        return convertToDTO(updated);
    }

    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        checkTaskPermission(task, username);
        String taskTitle = task.getTitle();
        taskRepository.deleteById(id);

        // Record Audit Log
        auditLogService.recordLog(
                "DELETE",
                "TASK",
                id,
                "Xóa công việc '" + taskTitle + "'",
                "Task ID: " + id + ", Title: " + taskTitle,
                "DELETED",
                username
        );
    }

    public TaskStatsDTO getStats(String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        User queryUser = (currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN) ? null : currentUser;

        long total = taskRepository.countByUser(queryUser);
        long todo = taskRepository.countByStatusAndUser(Status.TODO, queryUser);
        long inProgress = taskRepository.countByStatusAndUser(Status.IN_PROGRESS, queryUser);
        long completed = taskRepository.countByStatusAndUser(Status.COMPLETED, queryUser);
        long urgent = taskRepository.countByPriorityAndUser(Priority.URGENT, queryUser);

        return new TaskStatsDTO(total, todo, inProgress, completed, urgent);
    }

    private void checkTaskPermission(Task task, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return; // Admin can edit everything
        }

        if (task.getUser() != null && !task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa task này!");
        }
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setPosition(task.getPosition());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        if (task.getCategory() != null) {
            dto.setCategoryId(task.getCategory().getId());
            dto.setCategoryName(task.getCategory().getName());
            dto.setCategoryColor(task.getCategory().getColor());
            dto.setCategoryIcon(task.getCategory().getIcon());
        }

        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
            dto.setUsername(task.getUser().getUsername());
        }

        return dto;
    }
}
