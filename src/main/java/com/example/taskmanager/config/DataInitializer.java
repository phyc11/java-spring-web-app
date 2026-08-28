package com.example.taskmanager.config;

import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    public DataInitializer(CategoryRepository categoryRepository, TaskRepository taskRepository) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        // Create sample categories
        Category dev = categoryRepository.save(new Category("Development", "#6366f1", "code-2"));
        Category design = categoryRepository.save(new Category("Design", "#ec4899", "palette"));
        Category devops = categoryRepository.save(new Category("DevOps & CI/CD", "#10b981", "server"));
        Category docs = categoryRepository.save(new Category("Documentation", "#f59e0b", "file-text"));

        // Create sample tasks
        Task t1 = new Task();
        t1.setTitle("Setup Spring Boot REST Backend API");
        t1.setDescription("Build modular controllers, services, JPA repositories, and exception handlers.");
        t1.setStatus(Status.COMPLETED);
        t1.setPriority(Priority.HIGH);
        t1.setCategory(dev);
        t1.setDueDate(LocalDateTime.now().plusDays(1));
        taskRepository.save(t1);

        Task t2 = new Task();
        t2.setTitle("Design Web UI Glassmorphism Interface");
        t2.setDescription("Create responsive component-based UI with dark theme, smooth micro-animations, and live search.");
        t2.setStatus(Status.IN_PROGRESS);
        t2.setPriority(Priority.HIGH);
        t2.setCategory(design);
        t2.setDueDate(LocalDateTime.now().plusDays(2));
        taskRepository.save(t2);

        Task t3 = new Task();
        t3.setTitle("Configure GitHub Actions & CI/CD Pipeline");
        t3.setDescription("Prepare Maven build steps, automated test execution, and deployment scripts.");
        t3.setStatus(Status.TODO);
        t3.setPriority(Priority.URGENT);
        t3.setCategory(devops);
        t3.setDueDate(LocalDateTime.now().plusDays(3));
        taskRepository.save(t3);

        Task t4 = new Task();
        t4.setTitle("Write Project README & GitHub Setup Guide");
        t4.setDescription("Document architecture, API endpoints, setup instructions, and git commit workflow.");
        t4.setStatus(Status.TODO);
        t4.setPriority(Priority.MEDIUM);
        t4.setCategory(docs);
        t4.setDueDate(LocalDateTime.now().plusDays(5));
        taskRepository.save(t4);
    }
}
