package org.timetodo.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.timetodo.dto.UserDTO;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.CalendarRepository;
import org.timetodo.repository.CategoryRepository;
import org.timetodo.repository.TaskRepository;
import org.timetodo.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserServiceImpl userService = new UserServiceImpl(
            userRepository,
            mock(TaskRepository.class),
            mock(CategoryRepository.class),
            mock(CalendarRepository.class),
            passwordEncoder
    );

    @Test
    void registerUserStoresHashedPassword() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("member");
        userDTO.setEmail("member@example.com");
        userDTO.setPassword("password123");

        userService.registerUser(userDTO);

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                !"password123".equals(user.getPassword())
                        && passwordEncoder.matches("password123", user.getPassword())
        ));
    }

    @Test
    void authenticateUserRejectsWrongPassword() {
        UserEntity user = new UserEntity();
        user.setUsername("member");
        user.setPassword(passwordEncoder.encode("password123"));

        when(userRepository.findByUsername("member")).thenReturn(Optional.of(user));

        assertTrue(userService.authenticateUser("member", "password123").isPresent());
        assertFalse(userService.authenticateUser("member", "wrong").isPresent());
    }
}
