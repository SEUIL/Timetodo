package org.timetodo.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.timetodo.dto.CalendarDTO;
import org.timetodo.dto.CalendarRequestDto;
import org.timetodo.dto.ReminderRequestDto;
import org.timetodo.dto.TaskDto;
import org.timetodo.dto.TaskRequestDto;
import org.timetodo.service.CalendarService;
import org.timetodo.service.CategoryService;
import org.timetodo.service.ReminderService;
import org.timetodo.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleCommandService {

    private final CalendarService calendarService;
    private final TaskService taskService;
    private final ReminderService reminderService;
    private final CategoryService categoryService;

    public String createCalendar(Map<String, Object> data, Long userId) {
        try {
            CalendarRequestDto requestDto = new CalendarRequestDto();
            requestDto.setTitle(safeGet(data, "title", "제목 없음"));
            requestDto.setDescription(safeGet(data, "description", "설명이 없습니다."));
            requestDto.setLocation(safeGet(data, "location", "위치 정보 없음"));
            requestDto.setRepeatType(safeGet(data, "repeatType", "NONE"));

            LocalDateTime startTime = LocalDateTime.parse(safeGet(data, "startTime", null));
            LocalDateTime endTime = LocalDateTime.parse(safeGet(data, "endTime", startTime.plusHours(1).toString()));

            requestDto.setStartTime(startTime);
            requestDto.setEndTime(endTime);

            calendarService.addCalendar(requestDto, userId, categoryService.getOrCreateAiDefaultCategoryId(userId));
            return "일정이 성공적으로 생성되었습니다.";
        } catch (Exception e) {
            log.error("handleCalendar 처리 중 오류 발생: {}", data, e);
            throw new RuntimeException("일정 처리 중 오류가 발생했습니다.", e);
        }
    }

    public String createTask(Map<String, Object> data, Long userId) {
        try {
            TaskRequestDto requestDto = new TaskRequestDto();
            requestDto.setTitle(safeGet(data, "title", "제목 없음"));
            requestDto.setPriority(safeGet(data, "priority", "LOW"));
            requestDto.setStatus(safeGet(data, "status", "PENDING"));
            requestDto.setRepeatType(safeGet(data, "repeatType", "NONE"));
            requestDto.setDueDate(LocalDateTime.parse(safeGet(data, "dueDate", null)));

            taskService.addTask(requestDto, userId, categoryService.getOrCreateAiDefaultCategoryId(userId));
            return "할 일이 성공적으로 생성되었습니다.";
        } catch (Exception e) {
            log.error("handleTask 처리 중 오류 발생: {}", data, e);
            throw new RuntimeException("할 일 처리 중 오류가 발생했습니다.", e);
        }
    }

    public String createReminder(Map<String, Object> selectionData, Long userId) {
        try {
            String type = safeGet(selectionData, "detailsType", "unknown");
            Long id = Long.parseLong(safeGet(selectionData, "id", "0"));
            boolean notificationsEnabled = Boolean.parseBoolean(safeGet(selectionData, "notificationsEnabled", "true"));
            int timeBefore = Integer.parseInt(safeGet(selectionData, "timeBefore", "10"));
            boolean repeats = Boolean.parseBoolean(safeGet(selectionData, "repeats", "false"));

            ReminderRequestDto reminderRequestDto = new ReminderRequestDto();
            reminderRequestDto.setNotificationsEnabled(notificationsEnabled);
            reminderRequestDto.setTimeBefore(timeBefore);
            reminderRequestDto.setRepeats(repeats);

            if ("task".equalsIgnoreCase(type)) {
                reminderService.createTaskReminder(reminderRequestDto, id, userId);
                return "알림이 성공적으로 생성되었습니다.";
            }
            if ("calendar".equalsIgnoreCase(type)) {
                reminderService.createCalendarReminder(reminderRequestDto, id, userId);
                return "알림이 성공적으로 생성되었습니다.";
            }
            throw new IllegalArgumentException("Invalid selection type: " + type);
        } catch (Exception e) {
            log.error("Error processing reminder selection: {}", selectionData, e);
            throw new RuntimeException("알림 생성 중 오류가 발생했습니다.");
        }
    }

    public String buildReminderSelection(Long userId) {
        try {
            List<TaskDto> tasks = taskService.getTasksByUserId(userId);
            List<CalendarDTO> calendars = calendarService.getCalendarsByUserId(userId);

            StringBuilder response = new StringBuilder();
            response.append("일정(Calendar)\n");
            if (calendars.isEmpty()) {
                response.append("  - 등록된 일정이 없습니다.\n");
            } else {
                calendars.forEach(calendar ->
                        response.append(String.format("  %d번 일정: %s\n",
                                calendar.getCalendarId(), calendar.getTitle()))
                );
            }

            response.append("\n할일(Task)\n");
            if (tasks.isEmpty()) {
                response.append("  - 등록된 할일이 없습니다.\n");
            } else {
                tasks.forEach(task ->
                        response.append(String.format("  %d번 할일: %s\n",
                                task.getTaskId(), task.getTitle()))
                );
            }

            return response.toString();
        } catch (Exception e) {
            log.error("Error building reminder selection for user {}", userId, e);
            throw new RuntimeException("리마인더 목록 처리 중 오류가 발생했습니다.");
        }
    }

    private String safeGet(Map<String, Object> data, String key, String defaultValue) {
        if (!data.containsKey(key)) {
            return defaultValue;
        }

        Object value = data.get(key);
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof Boolean booleanValue) {
            return Boolean.toString(booleanValue);
        }
        if (value instanceof Number numberValue) {
            return String.valueOf(numberValue);
        }
        return defaultValue;
    }
}
