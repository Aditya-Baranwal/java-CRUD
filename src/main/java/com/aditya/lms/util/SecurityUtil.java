package com.aditya.lms.util;

import com.aditya.lms.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class SecurityUtil {

    public static User getRequesterUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if(!(authentication.getPrincipal() instanceof User)) {
                throw new RuntimeException("authentication principal must be of type User");
            }
            return (User) authentication.getPrincipal();
        }
        throw new RuntimeException("failed to find authenticated user");
    }


}
