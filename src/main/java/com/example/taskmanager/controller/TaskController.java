package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.TaskStatsDTO;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String search
    ) {
        List<TaskDTO> tasks = taskService.filterTasks(status, priority, categoryId, search);
        return ApiResponse.ok("Tasks fetched successfully", tasks);
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ApiResponse.ok("Task details retrieved", task);
    }

    @GetMapping("/stats")
    public ApiResponse<TaskStatsDTO> getStats() {
        TaskStatsDTO stats = taskService.getStats();
        return ApiResponse.ok("Dashboard statistics retrieved", stats);
    }

    @PostMapping
    public ApiResponse<TaskDTO> createTask(@RequestBody TaskDTO dto) {
        TaskDTO created = taskService.createTask(dto);
        return ApiResponse.ok("Task created successfully", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO dto) {
        TaskDTO updated = taskService.updateTask(id, dto);
        return ApiResponse.ok("Task updated successfully", updated);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TaskDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ApiResponse.error("Status value is required");
        }
        Status status = Status.valueOf(statusStr.toUpperCase());
        TaskDTO updated = taskService.updateTaskStatus(id, status);
        return ApiResponse.ok("Task status updated", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.ok("Task deleted successfully", null);
    }
}
