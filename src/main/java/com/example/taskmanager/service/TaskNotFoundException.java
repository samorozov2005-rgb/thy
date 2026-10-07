package com.example.taskmanager.service;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Задача с id " + id + " не найдена");
    }
}
