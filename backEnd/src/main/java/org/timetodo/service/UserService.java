package org.timetodo.service;

import org.timetodo.dto.UserDTO;
import org.timetodo.entity.UserEntity;

import java.util.Optional;

public interface UserService {

//    void saveUser(UserEntity userEntity);
    void registerUser(UserDTO userDTO);

    Optional<UserEntity> authenticateUser(String username, String password);

}
