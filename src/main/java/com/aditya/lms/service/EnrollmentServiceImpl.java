package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.CourseNotFoundException;
import com.aditya.lms.exception.EnrollmentConflictException;
import com.aditya.lms.exception.EnrollmentNotFoundException;
import com.aditya.lms.exception.EnrollmentValidationException;
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
    public Enrollment createEnrollment(Enrollment enrollment) {
        validateEnrollmentForCreate(enrollment);

        Course course = courseRepository.findById(enrollment.getCourse().getId())
                .orElseThrow(() -> new CourseNotFoundException(enrollment.getCourse().getId()));

        if (Boolean.FALSE.equals(course.getCanEnrollment())) {
            throw new EnrollmentConflictException("Course enrollment is disabled");
        }

        if (enrollmentRepository.existsByUserIdAndCourse_Id(enrollment.getUserId(), course.getId())) {
            throw new EnrollmentConflictException("User already enrolled");
        }

        enrollment.setCourse(course);
        enrollment.setCourseCompletionStatus(enrollment.getCourseCompletionStatus() == null
                ? CourseCompletionStatus.INCOMPLETE
                : enrollment.getCourseCompletionStatus());
        enrollment.setIsActive(Boolean.TRUE);

        Enrollment created = enrollmentRepository.save(enrollment);
        createProgressForCourseEnrollment(created.getUserId(), course.getId());
        log.info("Enrollment created successfully enrollmentId={}, userId={}, courseId={}",
                created.getId(), created.getUserId(), course.getId());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Enrollment getEnrollment(Long enrollmentId) {
        return enrollmentRepository.findByIdAndIsActiveTrue(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Enrollment> listEnrollments(Long userId, Integer pageNo, Integer pageSize,
                                           CourseCompletionStatus status, String sortBy, String sortOrder) {
        if (userId == null) {
            throw new EnrollmentValidationException("userId is mandatory");
        }

        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        if (status == null) {
            return enrollmentRepository.findByUserId(userId, pageable);
        }
        return enrollmentRepository.findByUserIdAndCourseCompletionStatus(userId, status, pageable);
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

    private void validateEnrollmentForCreate(Enrollment enrollment) {
        if (enrollment == null) {
            throw new EnrollmentValidationException("Enrollment payload is required");
        }
        if (enrollment.getUserId() == null) {
            throw new EnrollmentValidationException("userId is mandatory");
        }
        if (enrollment.getCourse() == null || enrollment.getCourse().getId() == null) {
            throw new EnrollmentValidationException("courseId is mandatory");
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
