package org.timetodo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.dto.CalendarRequestDto;
import org.timetodo.entity.CalendarEntity;
import org.timetodo.entity.CategoryEntity;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.CalendarRepository;
import org.timetodo.repository.CategoryRepository;
import org.timetodo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void updateCalendarRejectsDifferentOwner() {
        UserEntity owner = user(1L);
        CalendarEntity calendar = calendar(10L, owner, category(2L, owner));
        CalendarRequestDto request = calendarRequest(10L);

        when(calendarRepository.findById(10L)).thenReturn(Optional.of(calendar));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> calendarService.updateCalendar(request, 2L, 99L)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(calendarRepository, never()).save(calendar);
    }

    @Test
    void deleteCalendarRejectsDifferentOwner() {
        UserEntity owner = user(1L);
        CalendarEntity calendar = calendar(10L, owner, category(2L, owner));

        when(calendarRepository.findById(10L)).thenReturn(Optional.of(calendar));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> calendarService.deleteCalendar(10L, 99L)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(calendarRepository, never()).delete(calendar);
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

    private CalendarEntity calendar(Long id, UserEntity user, CategoryEntity category) {
        CalendarEntity calendar = new CalendarEntity();
        calendar.setCalendarId(id);
        calendar.setTitle("calendar");
        calendar.setStartTime(LocalDateTime.now());
        calendar.setEndTime(LocalDateTime.now().plusHours(1));
        calendar.setRepeatType("반복 없음");
        calendar.setUser(user);
        calendar.setCategory(category);
        return calendar;
    }

    private CalendarRequestDto calendarRequest(Long id) {
        CalendarRequestDto request = new CalendarRequestDto();
        request.setCalendarId(id);
        request.setTitle("updated");
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(1));
        request.setRepeatType("반복 없음");
        return request;
    }
}
