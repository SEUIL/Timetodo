package org.timetodo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.dto.TaskDto;
import org.timetodo.dto.TaskRequestDto;
import org.timetodo.dto.TaskStatusUpdateDto;
import org.timetodo.service.TaskService;

import java.util.List;

@Slf4j
@RestController // HTML 템플릿을 렌더링
@RequiredArgsConstructor // final 필드로 선언된 의존성(서비스)을 자동으로 주입하는 생성자를 생성해줍니다.
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService; // TaskService를 주입받아 사용합니다.
    private final CurrentUserService currentUserService;


    @PostMapping
    public ResponseEntity<?> addTask(
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        Long categoryId = taskRequestDto.getCategoryId();
        TaskDto task = taskService.addTask(taskRequestDto, userId, categoryId);
        log.info("생성된 Task: {}", task);
        return ResponseEntity.ok(task);
    }


    // 특정 user 의 모든 할 일을 조회하는 엔드포인트
    @GetMapping
    public List<TaskDto> getUserTasks(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        return taskService.getTasksByUserId(userId);
    }


    // 특정 할 일을 업데이트하는 엔드포인트
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequestDto taskRequestDto,
            HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        taskRequestDto.setTaskId(taskId);
        Long categoryId = taskRequestDto.getCategoryId();
        TaskDto updatedTask = taskService.updateTask(taskRequestDto, categoryId, userId);
        return ResponseEntity.ok(updatedTask); // 성공 시 업데이트된 할 일을 반환
    }


    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Void> updateTaskStatus(@PathVariable Long taskId,
                                                 @Valid @RequestBody TaskStatusUpdateDto taskStatusUpdateDto,
                                                 HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        taskService.updateTaskStatus(taskId, taskStatusUpdateDto.getStatus(), userId);
        return ResponseEntity.ok().build(); // 상태 업데이트 완료 후 OK 응답
    }

    // 특정 할 일을 삭제하는 엔드포인트
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId, HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.noContent().build(); // 성공 시 응답 본문 없이 204 상태코드 반환
    }
}

/*
TaskController 흐름 요약
할 일 추가: POST /task/add → 사용자가 보낸 할 일을 추가.
모든 할 일 조회: GET /task/all → 모든 할 일을 조회하고 반환.
할 일 업데이트: PUT /task/update/{id} → 특정 할 일을 수정하고 업데이트.
할 일 삭제: DELETE /task/delete/{id} → 특정 할 일을 삭제.
*/
