package com.aditya.lms.dto;

import com.aditya.lms.entity.Lesson;
import com.aditya.lms.enums.LessonStatus;

import java.time.OffsetDateTime;

/**
 * Service-layer read model wrapping a {@link Lesson} together with the lesson-completion/
 * progress-tracking fields documented in docs/decisions/lesson.decisions.md ("tracking lesson
 * completion").
 * <p>
 * These fields are derived from the requested {@code userId}'s enrollment and {@code Progress}
 * record for the lesson — they are not columns on {@code Lesson} itself. All of this derivation
 * is computed by {@code LessonServiceImpl} so business logic stays in the service layer;
 * {@code LessonMapper} maps this record directly onto the OpenAPI response DTOs.
 * <p>
 * When progress details are requested, {@code userId} echoes the queried user. The remaining
 * derived fields are {@code null} when that user is not enrolled in the lesson's course.
 */
public record LessonView(
        Lesson lesson,
        Long userId,
        Long progressId,
        Boolean isLessonCompleted,
        LessonStatus progressStatus,
        OffsetDateTime progressStartedAt,
        OffsetDateTime progressCompletedAt) {
}
