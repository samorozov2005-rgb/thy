package com.example.taskmanager.controller;

import com.example.taskmanager.service.TaskNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = TaskWebController.class)
public class WebExceptionHandler {
    @ExceptionHandler(TaskNotFoundException.class)
    public String notFound(TaskNotFoundException exception, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("message", exception.getMessage());
        return "error/404";
    }
}
