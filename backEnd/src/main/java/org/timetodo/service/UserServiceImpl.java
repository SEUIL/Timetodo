package org.timetodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.timetodo.dto.UserDTO;
import org.timetodo.entity.CalendarEntity;
import org.timetodo.entity.CategoryEntity;
import org.timetodo.entity.TaskEntity;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.CalendarRepository;
import org.timetodo.repository.CategoryRepository;
import org.timetodo.repository.TaskRepository;
import org.timetodo.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final CalendarRepository calendarRepository;
    private final PasswordEncoder passwordEncoder;


    public void registerUser(UserDTO userDTO) {
        // DTO에서 엔티티로 변환
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userEntity.setEmail(userDTO.getEmail());
        // 필요시 other fields 설정

        // taskIds를 TaskEntity 리스트로 변환하여 설정
        if (userDTO.getTaskIds() != null) {
            List<TaskEntity> tasks = userDTO.getTaskIds().stream()
                    .map(taskRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            userEntity.setTasks(tasks);
        }

        // categoryIds를 CategoryEntity 리스트로 변환하여 설정
        if (userDTO.getCategoryIds() != null) {
            List<CategoryEntity> categories = userDTO.getCategoryIds().stream()
                    .map(categoryRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            userEntity.setCategories(categories);
        }

        // calendarIds를 CalendarEntity 리스트로 변환하여 설정
        if (userDTO.getCalendarIds() != null) {
            List<CalendarEntity> calendars = userDTO.getCalendarIds().stream()
                    .map(calendarRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            userEntity.setCalendars(calendars);
        }

        // 엔티티 저장
        userRepository.save(userEntity);
    }
    /*public void saveUser(UserEntity userEntity) {

        userRepository.save(userEntity);
    }*/

    @Override
    public Optional<UserEntity> authenticateUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    // 추가 기능들: 사용자 검색, 삭제 등
}
