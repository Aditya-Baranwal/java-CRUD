package com.aditya.lms.testdata;

import com.aditya.lms.entity.Course;
import com.aditya.lms.enums.CourseStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Test fixture factory for {@link Course} entities used across service and mapper unit tests.
 */
public final class CourseTestData {

    private CourseTestData() {}

    public static Course.CourseBuilder defaultCourseBuilder() {
        return Course.builder()
                .id(1L)
                .title("Java Spring Boot")
                .description("Complete Spring Boot course")
                .tags(List.of("java", "spring"))
                .instructorId(101L)
                .canEnrollment(Boolean.FALSE)
                .courseStatus(CourseStatus.DRAFT)
                .createdAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .version(0L);
    }

    public static Course draftCourse() {
        return defaultCourseBuilder().build();
    }

    public static Course courseWithStatus(CourseStatus status, Boolean canEnrollment) {
        return defaultCourseBuilder()
                .courseStatus(status)
                .canEnrollment(canEnrollment)
                .build();
    }

    public static Course newUnsavedCourse() {
        return Course.builder()
                .title("Java Spring Boot")
                .description("Complete Spring Boot course")
                .tags(List.of("java", "spring"))
                .instructorId(101L)
                .build();
    }
}
