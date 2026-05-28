package org.timetodo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.auth.SessionAuthenticationFilter;
import org.timetodo.config.SecurityConfig;
import org.timetodo.dto.CalendarDTO;
import org.timetodo.dto.CalendarRequestDto;
import org.timetodo.entity.UserEntity;
import org.timetodo.exception.GlobalExceptionHandler;
import org.timetodo.repository.UserRepository;
import org.timetodo.service.CalendarService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
@Import({SecurityConfig.class, SessionAuthenticationFilter.class, CurrentUserService.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {
        CalendarController.class,
        SecurityConfig.class,
        SessionAuthenticationFilter.class,
        CurrentUserService.class,
        GlobalExceptionHandler.class
})
class CalendarControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getCalendarsRejectsMissingSession() throws Exception {
        mockMvc.perform(get("/api/calendars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCalendarsUsesAuthenticatedSessionUser() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(calendarService.getCalendarsByUserId(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/calendars").session(session(7L)))
                .andExpect(status().isOk());

        verify(calendarService).getCalendarsByUserId(7L);
    }

    @Test
    void createCalendarRejectsInvalidTimeRange() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));

        mockMvc.perform(post("/api/calendars")
                        .session(session(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "역전된 일정",
                                  "startTime": "2026-05-28T12:00:00",
                                  "end_time": "2026-05-28T11:00:00",
                                  "categoryId": 3
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCalendarUsesAuthenticatedSessionUser() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(calendarService.addCalendar(any(CalendarRequestDto.class), eq(7L), eq(3L))).thenReturn(new CalendarDTO(
                21L,
                "테스트 일정",
                "설명",
                LocalDateTime.of(2026, 5, 28, 10, 0),
                LocalDateTime.of(2026, 5, 28, 11, 0),
                "회의실",
                null,
                7L,
                3L,
                List.of()
        ));

        mockMvc.perform(post("/api/calendars")
                        .session(session(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 일정",
                                  "description": "설명",
                                  "startTime": "2026-05-28T10:00:00",
                                  "end_time": "2026-05-28T11:00:00",
                                  "location": "회의실",
                                  "categoryId": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calendarId").value(21L));

        verify(calendarService).addCalendar(any(CalendarRequestDto.class), eq(7L), eq(3L));
    }

    @Test
    void deleteCalendarMapsForbiddenFromService() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 일정에 접근할 수 없습니다."))
                .when(calendarService).deleteCalendar(99L, 7L);

        mockMvc.perform(delete("/api/calendars/99").session(session(7L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    private MockHttpSession session(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CurrentUserService.SESSION_USER_ID, userId);
        return session;
    }

    private UserEntity user(Long id) {
        UserEntity user = new UserEntity();
        user.setUserId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("password");
        return user;
    }
}
