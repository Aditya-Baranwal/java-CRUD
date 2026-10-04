package com.aditya.lms.service.interfaces;

import com.aditya.lms.dto.EnrollmentView;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.enums.CourseCompletionStatus;
import org.springframework.data.domain.Page;

/**
 * Business logic for Enrollment lifecycle, following the rules documented in
 * docs/decisions/enrollment.decisions.md.
 * <p>
 * Role-based access is expressed as separate methods per caller role (student/admin) rather than
 * a runtime role parameter. Instructors have no enrollment-creation capability at all, so that is
 * enforced at compile time by simply not exposing such a method.
 * <p>
 * The current enrollment rules are:
 * <ul>
 *   <li>Students may only enroll themselves in a course; a student cannot create an enrollment
 *       for another user.</li>
 *   <li>Enrollment is allowed only when the course is {@code PUBLISHED} and, for the student
 *       and admin paths, {@code canEnrollment} is {@code true}.</li>
 *   <li>Admins may enroll any student on behalf of that student, and may also enroll themselves,
 *       when the course is {@code PUBLISHED} and open for enrollment.</li>
 *   <li>Derived course-lifecycle values (content visibility and access message) are computed in
 *       this service and returned via {@link EnrollmentView}.</li>
 * </ul>
 * <p>
 * Read methods return {@link EnrollmentView} rather than the bare {@code Enrollment} entity so
 * that derived, course-lifecycle-dependent fields (course-content viewability, access message)
 * are computed once, in this service, and simply mapped onto the API response by
 * {@code EnrollmentMapper} — keeping that business logic out of the mapper/controller layers.
 */
public interface EnrollmentService {

    /** Student enrollment flow for the given target user. */
    EnrollmentView createEnrollmentAsStudent(Enrollment enrollment, Long studentId);

    /** Admin enrolling an arbitrary student on their behalf. */
    EnrollmentView createEnrollmentAsAdmin(Enrollment enrollment, Long adminId);

    EnrollmentView getEnrollment(Long enrollmentId);

    Page<EnrollmentView> listEnrollments(Long userId, Integer pageNo, Integer pageSize, CourseCompletionStatus status, String sortBy, String sortOrder);

    void cancelEnrollment(Long enrollmentId);

    /**
     * Recomputes and persists the enrollment's {@link CourseCompletionStatus} for the given
     * user/course pair, based on whether every lesson in the course has {@code FINISHED} progress
     * for that user. No-op when no matching enrollment or progress records exist.
     */
    void refreshCompletionStatus(Long userId, Long courseId);
}
