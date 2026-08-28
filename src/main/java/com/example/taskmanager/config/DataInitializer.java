package com.example.taskmanager.config;

import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryRepository categoryRepository,
                           TaskRepository taskRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create Seed Users
        User admin = userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "System Admin", Role.ROLE_ADMIN));
        User user = userRepository.save(new User("user", passwordEncoder.encode("user123"), "Demo Developer", Role.ROLE_USER));

        // Create Seed Categories
        Category dev = categoryRepository.save(new Category("Development", "#6366f1", "code-2"));
        Category design = categoryRepository.save(new Category("Design", "#ec4899", "palette"));
        Category devops = categoryRepository.save(new Category("DevOps & CI/CD", "#10b981", "server"));
        Category docs = categoryRepository.save(new Category("Documentation", "#f59e0b", "file-text"));

        // Create Seed Tasks assigned to Demo User
        Task t1 = new Task();
        t1.setTitle("Setup Spring Boot REST & JWT Authentication");
        t1.setDescription("Integrate Spring Security, JJWT token provider, and BCrypt PasswordEncoder.");
        t1.setStatus(Status.COMPLETED);
        t1.setPriority(Priority.HIGH);
        t1.setPosition(1);
        t1.setCategory(dev);
        t1.setUser(user);
        t1.setDueDate(LocalDateTime.now().plusDays(1));
        taskRepository.save(t1);

        Task t2 = new Task();
        t2.setTitle("Implement Drag & Drop Kanban Board Component");
        t2.setDescription("Create Kanban column layout in frontend JS with HTML5 Drag and Drop events.");
        t2.setStatus(Status.IN_PROGRESS);
        t2.setPriority(Priority.HIGH);
        t2.setPosition(1);
        t2.setCategory(design);
        t2.setUser(user);
        t2.setDueDate(LocalDateTime.now().plusDays(2));
        taskRepository.save(t2);

        Task t3 = new Task();
        t3.setTitle("Configure GitHub Actions CI/CD Pipeline");
        t3.setDescription("Prepare Maven build steps, test execution, and deployment verification.");
        t3.setStatus(Status.TODO);
        t3.setPriority(Priority.URGENT);
        t3.setPosition(1);
        t3.setCategory(devops);
        t3.setUser(user);
        t3.setDueDate(LocalDateTime.now().plusDays(3));
        taskRepository.save(t3);

        Task t4 = new Task();
        t4.setTitle("System Security Audit & Role Permission Verification");
        t4.setDescription("Test ROLE_ADMIN global access vs ROLE_USER task level access boundaries.");
        t4.setStatus(Status.TODO);
        t4.setPriority(Priority.MEDIUM);
        t4.setPosition(2);
        t4.setCategory(docs);
        t4.setUser(admin);
        t4.setDueDate(LocalDateTime.now().plusDays(5));
        taskRepository.save(t4);
    }
}
