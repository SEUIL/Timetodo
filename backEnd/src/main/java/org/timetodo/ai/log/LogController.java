package org.timetodo.ai.log;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.dto.LogDto;

import java.util.List;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;
    private final CurrentUserService currentUserService;

    @GetMapping("/all")
    public ResponseEntity<List<LogDto>> getAllLogs(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        return ResponseEntity.ok(logService.getLogsByUserId(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<LogDto>> getLogsByCurrentUser(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        return ResponseEntity.ok(logService.getLogsByUserId(userId));
    }
}
