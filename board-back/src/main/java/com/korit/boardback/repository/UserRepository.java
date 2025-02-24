package com.korit.boardback.repository;

import com.korit.boardback.entity.User;
import com.korit.boardback.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    @Autowired
    UserMapper userMapper;

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectByUsername(username));
    }

    public User save(User user) {
        userMapper.insert(user);
        return user;
    }
}
