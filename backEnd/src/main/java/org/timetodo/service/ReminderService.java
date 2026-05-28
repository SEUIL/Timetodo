package org.timetodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.dto.ReminderDto;
import org.timetodo.dto.ReminderRequestDto;
import org.timetodo.entity.CalendarEntity;
import org.timetodo.entity.ReminderEntity;
import org.timetodo.entity.TaskEntity;
import org.timetodo.repository.ReminderRepository;
import org.timetodo.repository.TaskRepository;
import org.timetodo.repository.CalendarRepository;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final CalendarRepository calendarRepository;

    /**
     * Task에 대한 알림 생성 로직
     *
     * @param reminderRequestDto 알림 설정 요청 데이터
     * @param taskId Task의 FK
     * @return 생성된 Task 알림
     */
    public ReminderDto createTaskReminder(ReminderRequestDto reminderRequestDto, Long taskId, Long userId) {
        // 1. TaskEntity를 ID로 조회
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));
        validateTaskOwner(task, userId);

        // 2. ReminderEntity 생성
        ReminderEntity reminder = new ReminderEntity();
        reminder.setTaskId(task); // ManyToOne 관계로 설정된 Task를 연결
        reminder.setNotificationsEnabled(reminderRequestDto.isNotificationsEnabled()); //알림 설정 여부 (True/False)
        reminder.setTimeBefore(reminderRequestDto.getTimeBefore()); // 알림 시작 시간 (분 단위)
        reminder.setRepeats(reminderRequestDto.isRepeats()); // 알림 반복 여부

        // 3. ReminderEntity 저장
        ReminderEntity savedReminder = reminderRepository.save(reminder);

        return converToDto(savedReminder);
    }

    /**
     * Calendar에 대한 알림 생성 로직
     *
     * @param reminderRequestDto 알림 설정 요청 데이터
     * @param calendarId Calendar의 FK
     * @return 생성된 Calendar 알림
     */
    public ReminderDto createCalendarReminder(ReminderRequestDto reminderRequestDto, Long calendarId, Long userId) {
        // 1. CalendarEntity를 ID로 조회
        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar not found with ID: " + calendarId));
        validateCalendarOwner(calendar, userId);

        // 2. ReminderEntity 생성
        ReminderEntity reminder = new ReminderEntity();
        reminder.setCalendarId(calendar); // ManyToOne 관계로 설정된 Calendar를 연결
        reminder.setNotificationsEnabled(reminderRequestDto.isNotificationsEnabled());
        reminder.setTimeBefore(reminderRequestDto.getTimeBefore());
        reminder.setRepeats(reminderRequestDto.isRepeats());

        // 3. ReminderEntity 저장
        ReminderEntity savedReminder = reminderRepository.save(reminder);

        return converToDto(savedReminder);
    }

    /**
     * ReminderEntity를 ReminderDto로 변환하는 메서드
     */
    private ReminderDto converToDto(ReminderEntity reminder) {
        ReminderDto dto = new ReminderDto();
        dto.setReminderId(reminder.getReminderId());
        dto.setNotificationsEnabled(reminder.isNotificationsEnabled());
        dto.setTimeBefore(reminder.getTimeBefore());
        dto.setRepeats(reminder.isRepeats());

        if(reminder.getTaskId() != null) {
            dto.setTaskId(reminder.getTaskId().getTaskId());
        }

        if(reminder.getCalendarId() != null) {
            dto.setCalendarId(reminder.getCalendarId().getCalendarId());
        }

        return dto;
    }

    /**
     * task의 알림 조회
     * @param taskId 사용자 ID
     * @return 알림 목록
     */
    public List<ReminderDto> getReminderByTaskId(Long taskId, Long userId) {
        // TaskEntity와 CalendarEntity를 통해 간접적으로 알림을 조회
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));
        validateTaskOwner(task, userId);
        return reminderRepository.findByTaskId(task).stream()
                .map(this::converToDto)
                .collect(Collectors.toList());
    }

    /**
     * Calendar의 알림 조회
     * @param calendarId 사용자 ID
     * @return 알림 목록
     */
    public List<ReminderDto> getReminderByCalendarId(Long calendarId, Long userId) {
        // TaskEntity와 CalendarEntity를 통해 간접적으로 알림을 조회
        CalendarEntity calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar not found with Id: " + calendarId));
        validateCalendarOwner(calendar, userId);
        return reminderRepository.findByCalendarId(calendar).stream()
                .map(this::converToDto)
                .collect(Collectors.toList());
    }

    /**
     * 알림 삭제
     * @param reminderId 알림 ID
     */
    public void deleteReminder(Long reminderId, Long userId) {
        ReminderEntity reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found with ID: " + reminderId));

        if (reminder.getTaskId() != null) {
            validateTaskOwner(reminder.getTaskId(), userId);
        } else if (reminder.getCalendarId() != null) {
            validateCalendarOwner(reminder.getCalendarId(), userId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "알림 소유자를 확인할 수 없습니다.");
        }

        reminderRepository.delete(reminder);
    }

    private void validateTaskOwner(TaskEntity task, Long userId) {
        if (task.getUserId() == null || !userId.equals(task.getUserId().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 할 일에 접근할 수 없습니다.");
        }
    }

    private void validateCalendarOwner(CalendarEntity calendar, Long userId) {
        if (calendar.getUserId() == null || !userId.equals(calendar.getUserId().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 일정에 접근할 수 없습니다.");
        }
    }
}
