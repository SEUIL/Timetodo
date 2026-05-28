package org.timetodo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.auth.SessionAuthenticationFilter;
import org.timetodo.config.SecurityConfig;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.UserRepository;
import org.timetodo.service.TaskService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, SessionAuthenticationFilter.class, CurrentUserService.class})
@ContextConfiguration(classes = {
        TaskController.class,
        SecurityConfig.class,
        SessionAuthenticationFilter.class,
        CurrentUserService.class
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

    private MockHttpSession session(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CurrentUserService.SESSION_USER_ID, userId);
        return session;
    }
}
