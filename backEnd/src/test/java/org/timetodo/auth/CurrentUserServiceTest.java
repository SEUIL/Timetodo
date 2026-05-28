package org.timetodo.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService(mock(UserRepository.class));

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireCurrentUserIdRejectsMissingSession() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> currentUserService.requireCurrentUserId(new MockHttpServletRequest())
        );

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void requireCurrentUserIdUsesSecurityContextPrincipalFirst() {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(7L, "member");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        Long userId = currentUserService.requireCurrentUserId(new MockHttpServletRequest());

        assertEquals(7L, userId);
    }
}
