package org.timetodo.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.timetodo.dto.CalendarRequestDto;
import org.timetodo.dto.TaskRequestDto;
import org.timetodo.service.CalendarService;
import org.timetodo.service.CategoryService;
import org.timetodo.service.ReminderService;
import org.timetodo.service.TaskService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleCommandServiceTest {

    @Mock
    private CalendarService calendarService;

    @Mock
    private TaskService taskService;

    @Mock
    private ReminderService reminderService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ScheduleCommandService scheduleCommandService;

    @Test
    void createTaskUsesUserAiDefaultCategory() {
        when(categoryService.getOrCreateAiDefaultCategoryId(7L)).thenReturn(30L);

        scheduleCommandService.createTask(Map.of(
                "title", "report",
                "dueDate", "2026-05-28T18:00:00",
                "priority", "일반",
                "status", "보류 중",
                "repeatType", "반복 없음"
        ), 7L);

        verify(taskService).addTask(argThat((TaskRequestDto request) ->
                "report".equals(request.getTitle())
                        && "일반".equals(request.getPriority())
                        && "보류 중".equals(request.getStatus())
        ), org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq(30L));
    }

    @Test
    void createCalendarUsesUserAiDefaultCategory() {
        when(categoryService.getOrCreateAiDefaultCategoryId(7L)).thenReturn(31L);

        scheduleCommandService.createCalendar(Map.of(
                "title", "meeting",
                "description", "sync",
                "location", "online",
                "startTime", "2026-05-28T10:00:00",
                "endTime", "2026-05-28T11:00:00",
                "repeatType", "반복 없음"
        ), 7L);

        verify(calendarService).addCalendar(argThat((CalendarRequestDto request) ->
                "meeting".equals(request.getTitle())
                        && "sync".equals(request.getDescription())
                        && "online".equals(request.getLocation())
        ), org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq(31L));
    }
}
