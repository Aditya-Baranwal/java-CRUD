package com.aditya.lms.dto;

import com.aditya.lms.entity.Enrollment;

/**
 * Service-layer read model wrapping an {@link Enrollment} together with derived,
 * course-lifecycle-dependent fields (see docs/decisions/enrollment.decisions.md).
 * <p>
 * These derived fields are not persisted on the {@code Enrollment} entity itself; they are
 * computed by {@code EnrollmentServiceImpl} so all business logic stays in the service layer.
 * {@code EnrollmentMapper} maps this record directly onto the OpenAPI response DTOs without
 * needing to recompute or resolve anything itself.
 */
public record EnrollmentView(
        Enrollment enrollment,
        boolean canEnrolledStudentViewCourseContent,
        String courseAccessMessage) {
}
