package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User createUser(User user);

    User getUser(Long userId);

    User getUserByEmail(String emailId);

    User updateUser(Long userId, User user);
}
