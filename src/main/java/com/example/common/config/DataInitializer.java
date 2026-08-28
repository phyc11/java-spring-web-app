package com.example.common.config;

import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.task.model.Category;
import com.example.task.model.Priority;
import com.example.task.model.Status;
import com.example.task.model.Task;
import com.example.task.repository.CategoryRepository;
import com.example.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
    public void run(String... args) throws Exception {
        // Initialize Default Users
        User adminUser = null;
        if (!userRepository.existsByUsername("admin")) {
            adminUser = userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "Quản Trị Viên", Role.ROLE_ADMIN));
        } else {
            adminUser = userRepository.findByUsername("admin").orElse(null);
        }

        User normalUser = null;
        if (!userRepository.existsByUsername("user")) {
            normalUser = userRepository.save(new User("user", passwordEncoder.encode("user123"), "Người Dùng Mẫu", Role.ROLE_USER));
        } else {
            normalUser = userRepository.findByUsername("user").orElse(null);
        }

        // Initialize Categories
        if (categoryRepository.count() == 0) {
            Category dev = categoryRepository.save(new Category("Phát triển Software", "#6366f1"));
            Category design = categoryRepository.save(new Category("Thiết kế UI/UX", "#ec4899"));
            Category marketing = categoryRepository.save(new Category("Marketing & Content", "#10b981"));
            Category ops = categoryRepository.save(new Category("Vận hành System", "#f59e0b"));

            // Sample Tasks
            if (taskRepository.count() == 0) {
                Task t1 = new Task();
                t1.setTitle("Thiết kế giao diện Dark Mode Glassmorphism");
                t1.setDescription("Tạo giao diện hiện đại với các hiệu ứng làm mờ kính ma mị cho TaskCraft UI.");
                t1.setStatus(Status.COMPLETED);
                t1.setPriority(Priority.HIGH);
                t1.setCategory(design);
                t1.setUser(normalUser);
                t1.setPosition(1);
                t1.setDueDate(LocalDateTime.now().plusDays(2));
                taskRepository.save(t1);

                Task t2 = new Task();
                t2.setTitle("Tích hợp JWT Authentication & Security");
                t2.setDescription("Bảo mật API Spring Boot với Spring Security và JSON Web Token.");
                t2.setStatus(Status.IN_PROGRESS);
                t2.setPriority(Priority.URGENT);
                t2.setCategory(dev);
                t2.setUser(normalUser);
                t2.setPosition(1);
                t2.setDueDate(LocalDateTime.now().plusDays(5));
                taskRepository.save(t2);

                Task t3 = new Task();
                t3.setTitle("Xây dựng Kanban Board kéo thả HTML5");
                t3.setDescription("Triển khai tính năng Kanban Drag & Drop tương tác trực tiếp bằng Javascript.");
                t3.setStatus(Status.TODO);
                t3.setPriority(Priority.MEDIUM);
                t3.setCategory(dev);
                t3.setUser(adminUser);
                t3.setPosition(1);
                t3.setDueDate(LocalDateTime.now().plusDays(7));
                taskRepository.save(t3);
            }
        }
    }
}
