package com.aditya.lms.testdata;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.ContentType;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.enums.LessonStatus;

import java.time.OffsetDateTime;

/**
 * Test fixture factory for {@link Enrollment}, {@link Course}, {@link Lesson} and
 * {@link Progress} entities used across Enrollment service/mapper unit tests.
 */
public final class EnrollmentTestData {

    private EnrollmentTestData() {}

    public static Course courseWithStatus(CourseStatus status, boolean canEnrollment) {
        return Course.builder()
                .id(1L)
                .title("Java Spring Boot")
                .instructorId(101L)
                .courseStatus(status)
                .canEnrollment(canEnrollment)
                .build();
    }

    public static Enrollment.EnrollmentBuilder defaultEnrollmentBuilder() {
        return Enrollment.builder()
                .id(1L)
                .userId(201L)
                .course(courseWithStatus(CourseStatus.PUBLISHED, Boolean.TRUE))
                .courseCompletionStatus(CourseCompletionStatus.INCOMPLETE)
                .enrolledAt(OffsetDateTime.parse("2026-08-02T10:30:00Z"))
                .isActive(Boolean.TRUE)
                .version(0L);
    }

    public static Enrollment activeEnrollment() {
        return defaultEnrollmentBuilder().build();
    }

    public static Enrollment newUnsavedEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        Course course = new Course();
        course.setId(courseId);
        enrollment.setCourse(course);
        return enrollment;
    }

    public static Lesson lesson(Long id, Course course) {
        return Lesson.builder()
                .id(id)
                .contentType(ContentType.MP4)
                .contentLink("https://example.com/video")
                .sequence(1)
                .isActive(Boolean.TRUE)
                .build();
    }

    public static Progress progress(Long userId, Lesson lesson, LessonStatus status) {
        return Progress.builder()
                .userId(userId)
                .lesson(lesson)
                .lessonStatus(status)
                .build();
    }
}
