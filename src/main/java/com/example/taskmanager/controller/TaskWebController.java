package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.entity.Priority;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/tasks")
public class TaskWebController {
    private final TaskService taskService;

    public TaskWebController(TaskService taskService) {
        this.taskService = taskService;
    }

    @ModelAttribute
    void addEnumValues(Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", TaskStatus.values());
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tasks", taskService.findAll());
        return "tasks/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.findById(id));
        return "tasks/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("taskRequest", new TaskRequest());
        return "tasks/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("taskRequest") TaskRequest request,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "tasks/form";
        }
        taskService.create(request);
        return "redirect:/web/tasks";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("taskRequest", TaskRequest.from(taskService.findById(id)));
        model.addAttribute("taskId", id);
        return "tasks/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("taskRequest") TaskRequest request,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("taskId", id);
            return "tasks/form";
        }
        taskService.update(id, request);
        return "redirect:/web/tasks";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        taskService.delete(id);
        return "redirect:/web/tasks";
    }
}
