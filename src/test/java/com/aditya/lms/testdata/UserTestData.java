package com.aditya.lms.testdata;

import com.aditya.lms.entity.User;
import com.aditya.lms.enums.UserRole;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class UserTestData {

    private UserTestData() {}

    public static User.UserBuilder defaultUserBuilder() {
        return User.builder()
                .id(1L)
                .name("Aditya Baranwal")
                .dob(LocalDate.parse("1998-01-15"))
                .mobileNo("9876543210")
                .emailId("aditya@example.com")
                .passwordHash("$2a$10$abcdefghijklmnopqrstuv")
                .role(UserRole.STUDENT)
                .createdAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .updatedBy(1L)
                .version(0L);
    }

    public static User persistedUser() {
        return defaultUserBuilder().build();
    }

    public static User newUnsavedUser() {
        return User.builder()
                .name("  Aditya Baranwal  ")
                .dob(LocalDate.parse("1998-01-15"))
                .mobileNo("9876543210")
                .emailId("aditya@example.com")
                .passwordHash("$2a$10$abcdefghijklmnopqrstuv")
                .role(UserRole.STUDENT)
                .build();
    }
}
