package org.timetodo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.dto.CategoryRequestDto;
import org.timetodo.entity.CategoryEntity;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.CategoryRepository;
import org.timetodo.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void updateCategoryRejectsDifferentOwner() {
        UserEntity requester = user(2L);
        CategoryEntity category = category(10L, user(1L));
        CategoryRequestDto request = categoryRequest();

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.updateCategory(10L, request, 2L)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(categoryRepository, never()).save(category);
    }

    @Test
    void deleteCategoryRejectsDifferentOwner() {
        UserEntity requester = user(2L);
        CategoryEntity category = category(10L, user(1L));

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.deleteCategoryById(2L, 10L)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(categoryRepository, never()).delete(category);
    }

    @Test
    void getOrCreateAiDefaultCategoryIdReturnsExistingCategory() {
        UserEntity user = user(7L);
        CategoryEntity category = category(30L, user);
        category.setCategoryName("AI 기본-7");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByUserAndCategoryName(user, "AI 기본-7")).thenReturn(category);

        Long categoryId = categoryService.getOrCreateAiDefaultCategoryId(7L);

        assertEquals(30L, categoryId);
        verify(categoryRepository, never()).save(category);
    }

    @Test
    void getOrCreateAiDefaultCategoryIdCreatesCategoryWhenMissing() {
        UserEntity user = user(7L);
        CategoryEntity savedCategory = category(31L, user);
        savedCategory.setCategoryName("AI 기본-7");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByUserAndCategoryName(user, "AI 기본-7")).thenReturn(null);
        when(categoryRepository.save(argThat(category ->
                "AI 기본-7".equals(category.getCategoryName())
                        && "#808080".equals(category.getColor())
                        && user.equals(category.getUser())
        ))).thenReturn(savedCategory);

        Long categoryId = categoryService.getOrCreateAiDefaultCategoryId(7L);

        assertEquals(31L, categoryId);
    }

    private UserEntity user(Long id) {
        UserEntity user = new UserEntity();
        user.setUserId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("password");
        return user;
    }

    private CategoryEntity category(Long id, UserEntity user) {
        CategoryEntity category = new CategoryEntity();
        category.setCategoryId(id);
        category.setCategoryName("category");
        category.setColor("#808080");
        category.setUser(user);
        return category;
    }

    private CategoryRequestDto categoryRequest() {
        CategoryRequestDto request = new CategoryRequestDto();
        request.setCategoryName("updated");
        request.setColor("#000000");
        return request;
    }
}
