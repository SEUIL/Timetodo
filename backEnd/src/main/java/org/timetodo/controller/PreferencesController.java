package org.timetodo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.dto.PreferencesDTO;
import org.timetodo.dto.PreferencesRequestDTO;
import org.timetodo.service.PreferencesService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/preferences")
public class PreferencesController {

    private final PreferencesService preferencesService;
    private final CurrentUserService currentUserService;

    // 유저 선호도 가져오기
    @GetMapping
    public ResponseEntity<PreferencesDTO> getPreferences(HttpServletRequest request) {
        try {
            Long userId = currentUserService.requireCurrentUserId(request);
            PreferencesDTO preferences = preferencesService.getUserPreferences(userId);
            if (preferences != null) {
                return ResponseEntity.ok(preferences);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 테마 업데이트
    @PostMapping
    public ResponseEntity<Void> updateTheme(HttpServletRequest request, @RequestBody PreferencesRequestDTO requestDTO) {
        try {
            Long userId = currentUserService.requireCurrentUserId(request);

            // 요청 DTO 유효성 검사
            if (requestDTO == null || requestDTO.getTheme() == null || requestDTO.getTheme().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 서비스 메서드 호출
            preferencesService.updateUserTheme(userId, requestDTO);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
