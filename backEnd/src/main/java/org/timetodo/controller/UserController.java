package org.timetodo.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.timetodo.dto.UserDTO;
import org.timetodo.entity.UserEntity;
import org.timetodo.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;

import java.time.Duration;
import java.util.Optional;


@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserServiceImpl userService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {

        userService.registerUser(userDTO);// DTO를 서비스 계층에서 엔티티로 변환 및 저장
        return ResponseEntity.ok("UserEntity registered successfully");
    }

    // 로그인 엔드포인트 추가
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDTO userDTO, HttpServletResponse response) {

        // 사용자 인증 로직
        Optional<UserEntity> authenticatedUser = userService.authenticateUser(userDTO.getUsername(), userDTO.getPassword());

        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        UserEntity user = authenticatedUser.get();

        // userId 쿠키 설정
        ResponseCookie userIdCookie = ResponseCookie.from("userId", String.valueOf(user.getUserId()))
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        // username 쿠키 추가 설정
        ResponseCookie usernameCookie = ResponseCookie.from("username", user.getUsername())
                .httpOnly(false)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, userIdCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, usernameCookie.toString());

        log.info("로그인 성공 UserId : {}", user.getUserId());

        return ResponseEntity.ok("Login successful");
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // 세션 무효화하여 모든 사용자 정보 삭제
        return ResponseEntity.ok("Logout successful");
    }

    // 추가 기능 필요 시 여기에 작성 (예: 로그인, 사용자 정보 조회 등)
}
