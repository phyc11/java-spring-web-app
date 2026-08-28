package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.TaskStatsDTO;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.service.TaskService;
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
            Principal principal
    ) {
        List<TaskDTO> tasks = taskService.filterTasks(status, priority, categoryId, search, principal.getName());
        return ApiResponse.ok("Tasks fetched successfully", tasks);
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskDTO> getTaskById(@PathVariable Long id, Principal principal) {
        TaskDTO task = taskService.getTaskById(id, principal.getName());
        return ApiResponse.ok("Task details retrieved", task);
    }

    @GetMapping("/stats")
    public ApiResponse<TaskStatsDTO> getStats(Principal principal) {
        TaskStatsDTO stats = taskService.getStats(principal.getName());
        return ApiResponse.ok("Dashboard statistics retrieved", stats);
    }

    @PostMapping
    public ApiResponse<TaskDTO> createTask(@RequestBody TaskDTO dto, Principal principal) {
        TaskDTO created = taskService.createTask(dto, principal.getName());
        return ApiResponse.ok("Task created successfully", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO dto, Principal principal) {
        TaskDTO updated = taskService.updateTask(id, dto, principal.getName());
        return ApiResponse.ok("Task updated successfully", updated);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TaskDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ApiResponse.error("Status value is required");
        }
        Status status = Status.valueOf(statusStr.toUpperCase());
        TaskDTO updated = taskService.updateTaskStatus(id, status, principal.getName());
        return ApiResponse.ok("Task status updated", updated);
    }

    @PatchMapping("/{id}/move")
    public ApiResponse<TaskDTO> moveTask(@PathVariable Long id, @RequestBody Map<String, Object> body, Principal principal) {
        String statusStr = (String) body.get("status");
        Integer position = body.get("position") != null ? ((Number) body.get("position")).intValue() : null;

        Status status = statusStr != null ? Status.valueOf(statusStr.toUpperCase()) : null;
        TaskDTO updated = taskService.moveTask(id, status, position, principal.getName());
        return ApiResponse.ok("Task moved successfully", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id, Principal principal) {
        taskService.deleteTask(id, principal.getName());
        return ApiResponse.ok("Task deleted successfully", null);
    }
}
