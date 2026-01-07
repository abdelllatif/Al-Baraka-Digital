package com.albaraka.services.interfaces;

import com.albaraka.models.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User findByEmail(String email);
    User findById(Long id);
}

