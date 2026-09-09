package com.aditya.lms.testdata;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.ContentType;
import com.aditya.lms.enums.CourseStatus;

import java.time.OffsetDateTime;

/**
 * Test fixture factory for {@link Lesson} entities used across service and mapper unit tests.
 */
public final class LessonTestData {

    private LessonTestData() {}

    public static Course courseWithStatus(CourseStatus status) {
        return Course.builder()
                .id(1L)
                .title("Java Spring Boot")
                .instructorId(101L)
                .courseStatus(status)
                .canEnrollment(Boolean.FALSE)
                .build();
    }

    public static Module moduleWithCourseStatus(CourseStatus status) {
        return Module.builder()
                .id(1L)
                .course(courseWithStatus(status))
                .title("Introduction")
                .sequence(1)
                .isActive(Boolean.TRUE)
                .build();
    }

    public static Lesson.LessonBuilder defaultLessonBuilder() {
        return Lesson.builder()
                .id(1L)
                .module(moduleWithCourseStatus(CourseStatus.DRAFT))
                .contentType(ContentType.MP4)
                .contentLink("https://example.com/lesson.mp4")
                .sequence(1)
                .isActive(Boolean.TRUE)
                .createdAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .version(0L);
    }

    public static Lesson draftLesson() {
        return defaultLessonBuilder().build();
    }

    public static Lesson lessonWithCourseStatus(CourseStatus status) {
        return defaultLessonBuilder().module(moduleWithCourseStatus(status)).build();
    }

    public static Lesson newUnsavedLesson() {
        return Lesson.builder()
                .module(Module.builder().id(1L).build())
                .contentType(ContentType.MP4)
                .contentLink("https://example.com/lesson.mp4")
                .sequence(1)
                .build();
    }
}
