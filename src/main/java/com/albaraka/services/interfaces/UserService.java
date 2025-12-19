package com.albaraka.services.interfaces;

import com.albaraka.models.User;

public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User findByEmail(String email);
    User findById(Long id);
}

