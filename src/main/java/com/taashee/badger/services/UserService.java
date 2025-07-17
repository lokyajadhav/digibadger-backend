package com.taashee.badger.services;

import com.taashee.badger.models.User;

public interface UserService {
    User findByEmail(String email);
    void saveUser(User user);
    java.util.List<User> findAll();
    User findById(Long id);
    boolean existsByEmail(String email);
    void deleteUser(Long userId);
}