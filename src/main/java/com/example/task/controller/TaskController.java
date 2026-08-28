package com.example.task.controller;

import com.example.common.dto.ApiResponse;
import com.example.task.dto.TaskDTO;
import com.example.task.dto.TaskStatsDTO;
import com.example.task.model.Priority;
import com.example.task.model.Status;
import com.example.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ApiResponse<List<TaskDTO>> getTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        List<TaskDTO> tasks = taskService.getFilteredTasks(principal.getName(), status, priority, categoryId, search);
        return ApiResponse.ok("Tasks retrieved successfully", tasks);
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskDTO> getTaskById(@PathVariable Long id, Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        TaskDTO task = taskService.getTaskById(id, principal.getName());
        return ApiResponse.ok("Task retrieved", task);
    }

    @PostMapping
    public ApiResponse<TaskDTO> createTask(@RequestBody TaskDTO dto, Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        TaskDTO created = taskService.createTask(dto, principal.getName());
        return ApiResponse.ok("Task created successfully", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO dto, Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        TaskDTO updated = taskService.updateTask(id, dto, principal.getName());
        return ApiResponse.ok("Task updated successfully", updated);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TaskDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        Status newStatus = Status.valueOf(body.get("status"));
        TaskDTO updated = taskService.updateTaskStatus(id, newStatus, principal.getName());
        return ApiResponse.ok("Task status updated", updated);
    }

    @PatchMapping("/{id}/move")
    public ApiResponse<TaskDTO> moveTask(@PathVariable Long id, @RequestBody Map<String, Object> body, Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        Status status = Status.valueOf((String) body.get("status"));
        Integer position = body.get("position") != null ? ((Number) body.get("position")).intValue() : null;

        TaskDTO moved = taskService.moveTask(id, status, position, principal.getName());
        return ApiResponse.ok("Task moved successfully", moved);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id, Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        taskService.deleteTask(id, principal.getName());
        return ApiResponse.ok("Task deleted successfully", null);
    }

    @GetMapping("/stats")
    public ApiResponse<TaskStatsDTO> getStats(Principal principal) {
        if (principal == null) return ApiResponse.error("Not authenticated");
        TaskStatsDTO stats = taskService.getStats(principal.getName());
        return ApiResponse.ok("Stats retrieved", stats);
    }
}
