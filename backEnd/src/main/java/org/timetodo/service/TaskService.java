package org.timetodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.dto.TaskDto;
import org.timetodo.dto.TaskRequestDto;
import org.timetodo.entity.CategoryEntity;
import org.timetodo.entity.ReminderEntity;
import org.timetodo.entity.TaskEntity;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.CategoryRepository;
import org.timetodo.repository.TaskRepository;
import org.timetodo.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // 의존성 주입을 위한 생성자 자동 생성
public class TaskService{

    private final TaskRepository taskRepository; // TaskRepository 주입

    private final UserRepository userRepository;  // UserRepository 주입

    private final CategoryRepository categoryRepository;

    public TaskDto addTask(TaskRequestDto taskRequestDto, Long userId, Long categoryId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
        validateCategoryOwner(category, userId);

        log.info("TaskSevice > addTask메소드 > userId, categoryId 정보 : " + userId + " , " + categoryId); //로그

        // DTO로부터 받은 데이터를 TaskEntity로 변환하여 저장
        TaskEntity task = new TaskEntity();
        task.setTitle(taskRequestDto.getTitle()); //타이틀
        task.setDueDate(taskRequestDto.getDueDate()); //마감기한
        task.setPriority(taskRequestDto.getPriority()); //우선순위
        switch (taskRequestDto.getPriority()){
            case "우선순위 없음":
                break;
            case "중요":
                break;
            case "일반":
                break;
            default:
                throw new IllegalArgumentException("오류 Invalid Status type: " + taskRequestDto.getPriority());
        }
        task.setStatus(taskRequestDto.getStatus()); // 진행상태
        switch (taskRequestDto.getStatus()){
            case "보류 중":
                //보류 중, 진행 중, 완료
                break;
            case "진행 중":
                //진행중
                break;
            case "완료" :
                //완료
                break;
            default:
                throw new IllegalArgumentException("오류 Invalid Status type: " + taskRequestDto.getStatus());
        }
        task.setRepeatType(taskRequestDto.getRepeatType()); //반복일정 여부
        switch (taskRequestDto.getRepeatType()) {
            case "반복 없음":
                //반복없음
                break;
            case "매일":
                // 매일 반복 설정 로직 (예: 매일 일정 생성)
                break;
            case "매주":
                // 매주 반복 설정 로직
                break;
            case "매월":
                // 매월 반복 설정 로직
                break;
            case "매년":
                // 매년 반복 설정 로직
                break;
            default:
                throw new IllegalArgumentException("Invalid repeat type: " + taskRequestDto.getRepeatType());
        }
        // 2. 현재 사용자와 카테고리 관계 설정
        task.setUser(user);
        task.setCategory(category);

        // 3. CalendarEntity 저장
        TaskEntity saveTask = taskRepository.save(task);

        // Category 및 User는 따로 설정 필요
        return convertTask(saveTask); // 저장 후 반환
    }

    private TaskDto convertTask(TaskEntity task) {
        TaskDto dto = new TaskDto();
        dto.setTaskId(task.getTaskId());
        dto.setTitle(task.getTitle());
        dto.setDueDate(task.getDueDate());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setRepeatType(task.getRepeatType());

        // UserEntity의 ID를 설정
        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getUserId());
        }

        // CategoryEntity의 ID를 설정
        if(task.getCategory() != null) {
            dto.setCategoryId(task.getCategory().getCategoryId());
        }

        // ReminderEntity 리스트의 ID들을 설정
        if(task.getReminders() != null) {
            List<Long> reminderIds = task.getReminders().stream()
                    .map(ReminderEntity::getReminderId)
                    .collect(Collectors.toList());
            dto.setReminderIds(reminderIds);
        }

        return dto;

    }

    public List<TaskDto> getTasksByUserId(Long userId) {
        // User 엔티티를 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        // 사용자와 연관된 할 일 엔티티 조회
        List<TaskEntity> tasks = taskRepository.findAllByUser(user);

        // 할 일 엔티티를 DTO로 변환
        return tasks.stream().map(this::convertTask).collect(Collectors.toList());
    }

    public TaskDto updateTask(TaskRequestDto taskRequestDto, Long categoryId, Long userId) {
        Long taskId = taskRequestDto.getTaskId();
        // 기존 할 일 조회
        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 할 일이 없습니다."));
        validateTaskOwner(existingTask, userId);

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "할당된 카테고리 ID가 없습니다."));
        validateCategoryOwner(category, userId);

        // 기존 할 일을 새로운 데이터로 업데이트
        existingTask.setTitle(taskRequestDto.getTitle());
        existingTask.setDueDate(taskRequestDto.getDueDate());
        existingTask.setPriority(taskRequestDto.getPriority());
        existingTask.setStatus(taskRequestDto.getStatus());
        existingTask.setRepeatType(taskRequestDto.getRepeatType());
        existingTask.setCategory(category);

        return convertTask(taskRepository.save(existingTask)); // 업데이트 후 저장
    }

    public void updateTaskStatus(Long taskId, String status, Long userId) {
        // 기존 할 일 조회
        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 할 일이 없습니다."));
        validateTaskOwner(existingTask, userId);

        // 상태 업데이트
        existingTask.setStatus(status);

        // 업데이트된 할 일을 저장
        taskRepository.save(existingTask);
    }



    public void deleteTask(Long id, Long userId) {
        // 특정 할 일 삭제
        TaskEntity existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 할 일이 없습니다."));
        validateTaskOwner(existingTask, userId);
        taskRepository.delete(existingTask);
    }

    private void validateTaskOwner(TaskEntity task, Long userId) {
        if (task.getUser() == null || !userId.equals(task.getUser().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 할 일에 접근할 수 없습니다.");
        }
    }

    private void validateCategoryOwner(CategoryEntity category, Long userId) {
        if (category.getUser() == null || !userId.equals(category.getUser().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 카테고리에 접근할 수 없습니다.");
        }
    }
}
