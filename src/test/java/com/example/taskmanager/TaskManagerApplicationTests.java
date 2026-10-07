package com.example.taskmanager;

import com.example.taskmanager.entity.Priority;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class TaskManagerApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void loginIsAvailableForAnonymousUser() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("Task Manager")));
    }

    @Test
    void tasksRedirectAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/web/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userCanOpenTaskListButDoesNotSeeDeleteButton() throws Exception {
        mockMvc.perform(get("/web/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(content().string(containsString("Редактировать")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Удалить"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanOpenTaskListAndSeesDeleteButton() throws Exception {
        mockMvc.perform(get("/web/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Удалить")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userCannotDeleteTaskEvenWithDirectRequest() throws Exception {
        Task task = taskRepository.save(new Task("Нельзя удалить", "Проверка роли",
                Priority.LOW, TaskStatus.TODO));

        mockMvc.perform(post("/web/tasks/{id}/delete", task.getId()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanDeleteTask() throws Exception {
        Task task = taskRepository.save(new Task("Удалить задачу", "Проверка администратора",
                Priority.LOW, TaskStatus.DONE));

        mockMvc.perform(post("/web/tasks/{id}/delete", task.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/tasks"));

        org.junit.jupiter.api.Assertions.assertFalse(taskRepository.existsById(task.getId()));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void blankTitleReturnsValidationError() throws Exception {
        mockMvc.perform(post("/web/tasks")
                        .with(csrf())
                        .param("title", " ")
                        .param("description", "Описание")
                        .param("priority", "HIGH")
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeHasFieldErrors("taskRequest", "title"))
                .andExpect(content().string(containsString("Название обязательно")));
    }

    @Test
    void jwtLoginReturnsTokenAndTokenOpensRestApi() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"user123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("token").asText();

        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
