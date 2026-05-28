package org.timetodo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.auth.SessionAuthenticationFilter;
import org.timetodo.config.SecurityConfig;
import org.timetodo.dto.TaskDto;
import org.timetodo.dto.TaskRequestDto;
import org.timetodo.entity.UserEntity;
import org.timetodo.exception.GlobalExceptionHandler;
import org.timetodo.repository.UserRepository;
import org.timetodo.service.TaskService;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, SessionAuthenticationFilter.class, CurrentUserService.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {
        TaskController.class,
        SecurityConfig.class,
        SessionAuthenticationFilter.class,
        CurrentUserService.class,
        GlobalExceptionHandler.class
})
class TaskControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getTasksRejectsMissingSession() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasksUsesAuthenticatedSessionUser() throws Exception {
        UserEntity user = new UserEntity();
        user.setUserId(7L);
        user.setUsername("member");
        user.setEmail("member@example.com");
        user.setPassword("password");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(taskService.getTasksByUserId(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks").session(session(7L)))
                .andExpect(status().isOk());

        verify(taskService).getTasksByUserId(7L);
    }

    @Test
    void createTaskRejectsInvalidRequest() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));

        mockMvc.perform(post("/api/tasks")
                        .session(session(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTaskUsesAuthenticatedSessionUser() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(taskService.addTask(any(TaskRequestDto.class), eq(7L), eq(3L))).thenReturn(new TaskDto(
                11L,
                "테스트 할 일",
                LocalDateTime.of(2026, 5, 28, 10, 0),
                "중간",
                "미완료",
                null,
                7L,
                3L,
                List.of()
        ));

        mockMvc.perform(post("/api/tasks")
                        .session(session(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 할 일",
                                  "dueDate": "2026-05-28T10:00:00",
                                  "priority": "중간",
                                  "status": "미완료",
                                  "categoryId": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(11L));

        verify(taskService).addTask(any(TaskRequestDto.class), eq(7L), eq(3L));
    }

    @Test
    void deleteTaskMapsForbiddenFromService() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 할 일에 접근할 수 없습니다."))
                .when(taskService).deleteTask(99L, 7L);

        mockMvc.perform(delete("/api/tasks/99").session(session(7L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    private MockHttpSession session(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CurrentUserService.SESSION_USER_ID, userId);
        return session;
    }

    private UserEntity user(Long id) {
        UserEntity user = new UserEntity();
        user.setUserId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("password");
        return user;
    }
}
