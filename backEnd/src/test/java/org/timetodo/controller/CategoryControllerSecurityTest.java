package org.timetodo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.auth.SessionAuthenticationFilter;
import org.timetodo.config.SecurityConfig;
import org.timetodo.dto.CategoryResponseDto;
import org.timetodo.entity.UserEntity;
import org.timetodo.exception.GlobalExceptionHandler;
import org.timetodo.repository.UserRepository;
import org.timetodo.service.CategoryService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, SessionAuthenticationFilter.class, CurrentUserService.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {
        CategoryController.class,
        SecurityConfig.class,
        SessionAuthenticationFilter.class,
        CurrentUserService.class,
        GlobalExceptionHandler.class
})
class CategoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getCategoriesRejectsMissingSession() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCategoriesUsesAuthenticatedSessionUser() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(categoryService.getCategoriesByUser(7L)).thenReturn(List.of(
                new CategoryResponseDto(10L, "개발", "#4f46e5")
        ));

        mockMvc.perform(get("/api/categories").session(session(7L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(10L));

        verify(categoryService).getCategoriesByUser(7L);
    }

    @Test
    void createCategoryRejectsInvalidRequest() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));

        mockMvc.perform(post("/api/categories")
                        .session(session(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteCategoryReturnsNoContent() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));

        mockMvc.perform(delete("/api/categories/10").session(session(7L)))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategoryById(7L, 10L);
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
