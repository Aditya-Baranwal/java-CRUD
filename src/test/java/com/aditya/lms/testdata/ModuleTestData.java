package com.aditya.lms.testdata;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.CourseStatus;

import java.time.OffsetDateTime;

/**
 * Test fixture factory for {@link Module} entities used across service and mapper unit tests.
 */
public final class ModuleTestData {

    private ModuleTestData() {}

    public static Course courseWithStatus(CourseStatus status) {
        return Course.builder()
                .id(1L)
                .title("Java Spring Boot")
                .instructorId(101L)
                .courseStatus(status)
                .canEnrollment(Boolean.FALSE)
                .build();
    }

    public static Module.ModuleBuilder defaultModuleBuilder() {
        return Module.builder()
                .id(1L)
                .course(courseWithStatus(CourseStatus.DRAFT))
                .title("Introduction")
                .description("Getting started")
                .sequence(1)
                .isActive(Boolean.TRUE)
                .createdAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .version(0L);
    }

    public static Module draftModule() {
        return defaultModuleBuilder().build();
    }

    public static Module moduleWithCourseStatus(CourseStatus status) {
        return defaultModuleBuilder().course(courseWithStatus(status)).build();
    }

    public static Module newUnsavedModule() {
        return Module.builder()
                .course(Course.builder().id(1L).build())
                .title("Introduction")
                .description("Getting started")
                .sequence(1)
                .build();
    }
}
