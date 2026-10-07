package com.example.taskmanager.dto;

import com.example.taskmanager.entity.Priority;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskRequest {
    @NotBlank(message = "Название обязательно")
    @Size(min = 3, max = 100, message = "Название должно содержать от 3 до 100 символов")
    private String title;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Выберите приоритет")
    private Priority priority;

    @NotNull(message = "Выберите статус")
    private TaskStatus status;

    public TaskRequest() {
        this.priority = Priority.MEDIUM;
        this.status = TaskStatus.TODO;
    }

    public static TaskRequest from(Task task) {
        TaskRequest request = new TaskRequest();
        request.setTitle(task.getTitle());
        request.setDescription(task.getDescription());
        request.setPriority(task.getPriority());
        request.setStatus(task.getStatus());
        return request;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
