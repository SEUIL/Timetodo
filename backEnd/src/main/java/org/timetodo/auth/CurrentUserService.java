package org.timetodo.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    public static final String SESSION_USER_ID = "userId";

    private final UserRepository userRepository;

    public Long requireCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId instanceof Long id) {
            return id;
        }
        if (userId instanceof String id) {
            try {
                return Long.valueOf(id);
            } catch (NumberFormatException ignored) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "세션 사용자 정보가 올바르지 않습니다.");
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    public UserEntity requireCurrentUser(HttpServletRequest request) {
        Long userId = requireCurrentUserId(request);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
    }
}
