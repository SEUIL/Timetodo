package org.timetodo.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.UserRepository;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            resolveUserId(request)
                    .flatMap(userRepository::findById)
                    .ifPresent(this::setAuthentication);
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<Long> resolveUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return java.util.Optional.empty();
        }

        Object userId = session.getAttribute(CurrentUserService.SESSION_USER_ID);
        if (userId instanceof Long id) {
            return java.util.Optional.of(id);
        }
        if (userId instanceof String id) {
            try {
                return java.util.Optional.of(Long.valueOf(id));
            } catch (NumberFormatException ignored) {
                return java.util.Optional.empty();
            }
        }
        return java.util.Optional.empty();
    }

    private void setAuthentication(UserEntity user) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(user.getUserId(), user.getUsername());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
