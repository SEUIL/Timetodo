package org.timetodo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.dto.TaskRequestDto;
import org.timetodo.entity.CategoryEntity;
import org.timetodo.entity.TaskEntity;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.CategoryRepository;
import org.timetodo.repository.TaskRepository;
import org.timetodo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void updateTaskRejectsDifferentOwner() {
        UserEntity owner = user(1L);
        TaskEntity task = task(10L, owner, category(2L, owner));
        TaskRequestDto request = taskRequest(10L);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(request, 2L, 99L)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(taskRepository, never()).save(task);
    }

    @Test
    void deleteTaskRejectsDifferentOwner() {
        UserEntity owner = user(1L);
        TaskEntity task = task(10L, owner, category(2L, owner));

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.deleteTask(10L, 99L)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(taskRepository, never()).delete(task);
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
        category.setCategoryName("category" + id);
        category.setColor("#808080");
        category.setUser(user);
        return category;
    }

    private TaskEntity task(Long id, UserEntity user, CategoryEntity category) {
        TaskEntity task = new TaskEntity();
        task.setTaskId(id);
        task.setTitle("task");
        task.setDueDate(LocalDateTime.now());
        task.setPriority("일반");
        task.setStatus("보류 중");
        task.setRepeatType("반복 없음");
        task.setUser(user);
        task.setCategory(category);
        return task;
    }

    private TaskRequestDto taskRequest(Long id) {
        TaskRequestDto request = new TaskRequestDto();
        request.setTaskId(id);
        request.setTitle("updated");
        request.setDueDate(LocalDateTime.now());
        request.setPriority("일반");
        request.setStatus("진행 중");
        request.setRepeatType("반복 없음");
        return request;
    }
}
