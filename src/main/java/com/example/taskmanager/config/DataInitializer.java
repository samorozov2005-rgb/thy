package com.example.taskmanager.config;

import com.example.taskmanager.entity.Priority;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner loadDemoTasks(TaskRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Task("Изучить Thymeleaf", "Освоить шаблоны и выражения Thymeleaf",
                        Priority.HIGH, TaskStatus.IN_PROGRESS));
                repository.save(new Task("Настроить Spring Security", "Добавить JWT и form-login",
                        Priority.HIGH, TaskStatus.TODO));
                repository.save(new Task("Подготовиться к защите", "Повторить архитектуру проекта",
                        Priority.MEDIUM, TaskStatus.TODO));
            }
        };
    }
}
