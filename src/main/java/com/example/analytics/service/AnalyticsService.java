package com.example.analytics.service;

import com.example.analytics.dto.AnalyticsDTO;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.task.model.Status;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public AnalyticsService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<Task> getTasksForExport(String username) {
        User currentUser = userRepository.findByUsername(username).orElse(null);
        User queryUser = (currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN) ? null : currentUser;
        return taskRepository.filterTasks(queryUser, null, null, null, null);
    }

    public AnalyticsDTO getAnalytics(String username) {
        List<Task> tasks = getTasksForExport(username);

        long total = tasks.size();
        long completedCount = tasks.stream().filter(t -> t.getStatus() == Status.COMPLETED).count();
        double completionRate = total > 0 ? Math.round((double) completedCount / total * 1000.0) / 10.0 : 0.0;

        Map<String, Long> categoryDist = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "General",
                        Collectors.counting()
                ));

        Map<String, Long> statusDist = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        Collectors.counting()
                ));

        Map<String, Long> priorityDist = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority().name(),
                        Collectors.counting()
                ));

        return new AnalyticsDTO(total, completionRate, categoryDist, statusDist, priorityDist);
    }
}
