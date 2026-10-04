package com.aditya.lms.service;

import com.aditya.lms.dto.EnrollmentView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.CourseNotFoundException;
import com.aditya.lms.exception.EnrollmentConflictException;
import com.aditya.lms.exception.EnrollmentForbiddenException;
import com.aditya.lms.exception.EnrollmentNotFoundException;
import com.aditya.lms.exception.EnrollmentValidationException;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.repository.EnrollmentRepository;
import com.aditya.lms.repository.LessonRepository;
import com.aditya.lms.repository.ProgressRepository;
import com.aditya.lms.service.interfaces.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Business logic for Enrollment lifecycle, following the rules documented in
 * docs/decisions/enrollment.decisions.md.
 * Role-based access is expressed as separate methods per caller role (student/admin) rather than
 * a runtime role parameter, so unauthorized actions (e.g. instructor creating an enrollment) are
 * caught at compile time by simply not exposing such a method.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ProgressRepository progressRepository;

    @Override
    @Transactional
    public EnrollmentView createEnrollmentAsStudent(Enrollment enrollment, Long studentId) {
        if (studentId == null) {
            throw new EnrollmentValidationException(ErrorMessages.ENROLLMENT_REQUESTER_ID_MANDATORY);
        }
        validateEnrollmentForCreate(enrollment);

        if (!Objects.equals(enrollment.getUserId(), studentId)) {
            throw new EnrollmentForbiddenException(ErrorMessages.ENROLLMENT_SELF_ENROLL_ONLY);
        }

        Course course = findCourseForEnrollment(enrollment.getCourse().getId());
        validateCoursePublished(course);

        if (Boolean.FALSE.equals(course.getCanEnrollment())) {
            throw new EnrollmentConflictException(ErrorMessages.ENROLLMENT_COURSE_CLOSED);
        }

        return toView(saveEnrollment(enrollment, course));
    }

    @Override
    @Transactional
    public EnrollmentView createEnrollmentAsAdmin(Enrollment enrollment, Long adminId) {
        if (adminId == null) {
            throw new EnrollmentValidationException(ErrorMessages.ENROLLMENT_REQUESTER_ID_MANDATORY);
        }
        validateEnrollmentForCreate(enrollment);

        Course course = findCourseForEnrollment(enrollment.getCourse().getId());
        validateCoursePublished(course);

        return toView(saveEnrollment(enrollment, course));
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentView getEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findByIdAndIsActiveTrue(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
        return toView(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentView> listEnrollments(Long userId, Integer pageNo, Integer pageSize,
                                           CourseCompletionStatus status, String sortBy, String sortOrder) {
        if (userId == null) {
            throw new EnrollmentValidationException(ErrorMessages.ENROLLMENT_USER_ID_MANDATORY);
        }

        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Page<Enrollment> page = status == null
                ? enrollmentRepository.findByUserId(userId, pageable)
                : enrollmentRepository.findByUserIdAndCourseCompletionStatus(userId, status, pageable);
        return page.map(this::toView);
    }

    @Override
    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        if (Boolean.FALSE.equals(enrollment.getIsActive())) {
            return;
        }

        progressRepository.findByUserIdAndLesson_Module_Course_Id(enrollment.getUserId(), enrollment.getCourse().getId())
                .forEach(progressRepository::delete);
        enrollmentRepository.delete(enrollment);
        log.info("Enrollment cancelled and progress deleted enrollmentId={}", enrollmentId);
    }

    @Override
    @Transactional
    public void refreshCompletionStatus(Long userId, Long courseId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserIdAndCourse_Id(userId, courseId);
        if (enrollmentOpt.isEmpty()) {
            return;
        }

        List<Progress> progressRecords = progressRepository.findByUserIdAndLesson_Module_Course_Id(userId, courseId);
        if (progressRecords.isEmpty()) {
            return;
        }

        boolean allLessonsFinished = progressRecords.stream()
                .allMatch(progress -> progress.getLessonStatus() == LessonStatus.FINISHED);
        CourseCompletionStatus newStatus = allLessonsFinished ? CourseCompletionStatus.COMPLETE : CourseCompletionStatus.INCOMPLETE;

        Enrollment enrollment = enrollmentOpt.get();
        if (enrollment.getCourseCompletionStatus() == newStatus) {
            return;
        }

        enrollment.setCourseCompletionStatus(newStatus);
        enrollmentRepository.save(enrollment);
        log.info("Enrollment completion status updated userId={}, courseId={}, status={}", userId, courseId, newStatus);
    }

    /** Whether an enrolled student can currently view the course content, per the course's lifecycle state. */
    private boolean canEnrolledStudentViewCourseContent(Course course) {
        CourseStatus status = course.getCourseStatus();
        return status == CourseStatus.PUBLISHED;
    }

    /**
     * Human-readable message explaining course content accessibility for the course's current
     * state; may be {@code null}. See docs/decisions/enrollment.decisions.md for the derived-
     * fields table this implements.
     */
    private String courseAccessMessage(Course course) {
        CourseStatus status = course.getCourseStatus();
        boolean canEnrollment = Boolean.TRUE.equals(course.getCanEnrollment());
        return switch (status) {
            case DRAFT, READY_TO_PUBLISH -> null;
            case PUBLISHED -> canEnrollment ? "Course is open for enrollment." : "Course is closed for enrollment.";
            case PLANNED_TO_UNPUBLISH, READY_TO_UNPUBLISH -> "Course will be soon removed.";
            case UNPUBLISHED -> "Course is removed by instructor.";
            case MANUAL_UNPUBLISHED -> "Course is removed.";
        };
    }

    /**
     * Wraps an {@link Enrollment} together with its derived, course-lifecycle-dependent fields
     * into an {@link EnrollmentView} for the mapper/controller to consume directly.
     */
    private EnrollmentView toView(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        return new EnrollmentView(enrollment, canEnrolledStudentViewCourseContent(course), courseAccessMessage(course));
    }

    private Course findCourseForEnrollment(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private void validateCoursePublished(Course course) {
        if (course.getCourseStatus() != CourseStatus.PUBLISHED) {
            throw new EnrollmentConflictException(ErrorMessages.enrollmentCourseNotPublished(course.getCourseStatus()));
        }
    }

    private Enrollment saveEnrollment(Enrollment enrollment, Course course) {
        if (enrollmentRepository.existsByUserIdAndCourse_Id(enrollment.getUserId(), course.getId())) {
            throw new EnrollmentConflictException(ErrorMessages.ENROLLMENT_DUPLICATE);
        }

        enrollment.setCourse(course);
        enrollment.setCourseCompletionStatus(CourseCompletionStatus.INCOMPLETE);
        enrollment.setIsActive(Boolean.TRUE);

        Enrollment created = enrollmentRepository.save(enrollment);
        createProgressForCourseEnrollment(created.getUserId(), course.getId());
        log.info("Enrollment created successfully enrollmentId={}, userId={}, courseId={}",
                created.getId(), created.getUserId(), course.getId());
        return created;
    }

    private void validateEnrollmentForCreate(Enrollment enrollment) {
        if (enrollment == null) {
            throw new EnrollmentValidationException(ErrorMessages.ENROLLMENT_PAYLOAD_REQUIRED);
        }
        if (enrollment.getUserId() == null) {
            throw new EnrollmentValidationException(ErrorMessages.ENROLLMENT_USER_ID_MANDATORY);
        }
        if (enrollment.getCourse() == null || enrollment.getCourse().getId() == null) {
            throw new EnrollmentValidationException(ErrorMessages.ENROLLMENT_COURSE_ID_MANDATORY);
        }
    }

    private void createProgressForCourseEnrollment(Long userId, Long courseId) {
        List<Lesson> lessons = lessonRepository.findByModule_Course_Id(courseId);
        if (lessons.isEmpty()) {
            return;
        }

        List<Progress> newProgressRecords = new ArrayList<>();
        for (Lesson lesson : lessons) {
            Progress progress = Progress.builder()
                    .userId(userId)
                    .lesson(lesson)
                    .lessonStatus(LessonStatus.UNSTARTED)
                    .completedAt(null)
                    .startedAt(null)
                    .build();
            newProgressRecords.add(progress);
        }

        if (!newProgressRecords.isEmpty()) {
            progressRepository.saveAll(newProgressRecords);
        }
    }

    private Pageable buildPageable(Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        int safePage = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safeSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "enrolledAt" : sortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage - 1, safeSize, Sort.by(direction, safeSortBy));
    }
}
