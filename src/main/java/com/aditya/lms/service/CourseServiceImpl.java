package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.exception.CourseConflictException;
import com.aditya.lms.exception.CourseNotFoundException;
import com.aditya.lms.exception.CourseValidationException;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.service.interfaces.CourseQueryService;
import com.aditya.lms.service.interfaces.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService, CourseQueryService {

    private static final Map<CourseStatus, Set<CourseStatus>> ALLOWED_TRANSITIONS = Map.of(
            CourseStatus.DRAFT, Set.of(CourseStatus.READY_TO_PUBLISH, CourseStatus.PUBLISHED),
            CourseStatus.READY_TO_PUBLISH, Set.of(CourseStatus.PUBLISHED, CourseStatus.DRAFT),
            CourseStatus.PUBLISHED, Set.of(CourseStatus.READY_TO_UNPUBLISH, CourseStatus.PLANNED_TO_UNPUBLISH, CourseStatus.MANUAL_UNPUBLISHED),
            CourseStatus.PLANNED_TO_UNPUBLISH, Set.of(CourseStatus.READY_TO_UNPUBLISH, CourseStatus.PUBLISHED, CourseStatus.MANUAL_UNPUBLISHED),
            CourseStatus.READY_TO_UNPUBLISH, Set.of(CourseStatus.UNPUBLISHED, CourseStatus.MANUAL_UNPUBLISHED),
            CourseStatus.UNPUBLISHED, Set.of(),
            CourseStatus.MANUAL_UNPUBLISHED, Set.of()
    );

    private static final Set<CourseStatus> STUDENT_VISIBLE_STATUSES = Set.of(
            CourseStatus.PUBLISHED,
            CourseStatus.PLANNED_TO_UNPUBLISH
    );

    // In these states only courseStatus and canEnrollment are editable
    private static final Set<CourseStatus> LIMITED_EDIT_STATUSES = Set.of(
            CourseStatus.PUBLISHED,
            CourseStatus.PLANNED_TO_UNPUBLISH,
            CourseStatus.READY_TO_UNPUBLISH
    );

    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Course createCourse(Course course) {
        validateCourseForCreate(course);
        applyCreateDefaults(course);
        Course created = courseRepository.save(course);
        log.info("Course created successfully courseId={}, instructorId={}", created.getId(), created.getInstructorId());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> listCourses(Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        if (courseStatus == null) {
            return courseRepository.findAll(pageable);
        }
        return courseRepository.findByCourseStatus(courseStatus, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> listCoursesForAdmin(Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        if (courseStatus == null) {
            return courseRepository.findAll(pageable);
        }
        return courseRepository.findByCourseStatus(courseStatus, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> listCoursesForInstructor(Long instructorId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        if (instructorId == null) {
            throw new CourseValidationException(ErrorMessages.COURSE_INSTRUCTOR_ID_MANDATORY);
        }
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        if (courseStatus == null) {
            return courseRepository.findByInstructorId(instructorId, pageable);
        }
        return courseRepository.findByInstructorIdAndCourseStatus(instructorId, courseStatus, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> listCoursesForStudent(Long studentId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        return courseRepository.findByCourseStatusIn(STUDENT_VISIBLE_STATUSES, pageable);
    }

    @Override
    @Transactional
    public Course updateCourse(Long courseId, Course course) {
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        validateCourseForUpdate(existingCourse, course);
        applyUpdates(existingCourse, course);

        Course updated = courseRepository.save(existingCourse);
        log.info("Course updated successfully courseId={}", updated.getId());
        return updated;
    }

    private void applyUpdates(Course existing, Course incoming) {
        if (incoming.getTitle() != null) {
            existing.setTitle(incoming.getTitle());
        }
        if (incoming.getDescription() != null) {
            existing.setDescription(incoming.getDescription());
        }
        if (incoming.getTags() != null) {
            existing.setTags(incoming.getTags());
        }
        if (incoming.getCourseStatus() != null) {
            existing.setCourseStatus(incoming.getCourseStatus());
        }
        if (incoming.getCanEnrollment() != null) {
            existing.setCanEnrollment(incoming.getCanEnrollment());
        }
        if (incoming.getUpdatedBy() != null) {
            existing.setUpdatedBy(incoming.getUpdatedBy());
        }

        if (existing.getCourseStatus() != CourseStatus.PUBLISHED) {
            existing.setCanEnrollment(Boolean.FALSE);
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Course existing = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (existing.getCourseStatus().isTerminal()) {
            throw new CourseConflictException(ErrorMessages.courseTerminalDelete(existing.getCourseStatus()));
        }

        existing.setCanEnrollment(Boolean.FALSE);
        existing.setCourseStatus(CourseStatus.MANUAL_UNPUBLISHED);
        courseRepository.save(existing);
        log.info("Course soft deleted courseId={}", courseId);
    }

    private void validateCourseForCreate(Course course) {
        if (course == null) {
            throw new CourseValidationException(ErrorMessages.COURSE_PAYLOAD_REQUIRED);
        }
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new CourseValidationException(ErrorMessages.COURSE_TITLE_MANDATORY);
        }
        if (course.getInstructorId() == null) {
            throw new CourseValidationException(ErrorMessages.COURSE_INSTRUCTOR_ID_MANDATORY);
        }
        validateUniqueCourseTitle(course.getTitle(), course.getInstructorId(), null);
    }

    private void validateCourseForUpdate(Course existingCourse, Course incomingCourse) {
        if (incomingCourse == null) {
            throw new CourseValidationException(ErrorMessages.COURSE_PAYLOAD_REQUIRED);
        }
        if (incomingCourse.getInstructorId() != null && !incomingCourse.getInstructorId().equals(existingCourse.getInstructorId())) {
            throw new CourseConflictException(ErrorMessages.COURSE_INSTRUCTOR_UPDATE);
        }

        validateFieldEditabilityByStatus(existingCourse.getCourseStatus(), incomingCourse);

        String effectiveTitle = incomingCourse.getTitle() != null ? incomingCourse.getTitle() : existingCourse.getTitle();
        validateUniqueCourseTitle(effectiveTitle, existingCourse.getInstructorId(), existingCourse.getId());

        CourseStatus targetStatus = incomingCourse.getCourseStatus() != null
                ? incomingCourse.getCourseStatus()
                : existingCourse.getCourseStatus();
        Boolean targetCanEnrollment = incomingCourse.getCanEnrollment() != null
                ? incomingCourse.getCanEnrollment()
                : existingCourse.getCanEnrollment();

        validateStatusTransition(existingCourse.getCourseStatus(), targetStatus);
        validateVisibilityByStatus(targetStatus, targetCanEnrollment);
    }

    private void validateFieldEditabilityByStatus(CourseStatus currentStatus, Course incomingCourse) {
        if (currentStatus.isTerminal()) {
            throw new CourseConflictException(ErrorMessages.courseTerminalEdit(currentStatus));
        }
        if (LIMITED_EDIT_STATUSES.contains(currentStatus)) {
            if (incomingCourse.getTitle() != null
                    || incomingCourse.getDescription() != null
                    || incomingCourse.getTags() != null) {
                throw new CourseValidationException(ErrorMessages.courseLimitedEdit(currentStatus));
            }
        }
    }

    private void applyCreateDefaults(Course course) {
        course.setCourseStatus(CourseStatus.DRAFT);
        course.setCanEnrollment(Boolean.FALSE);
    }

    private void validateStatusTransition(CourseStatus currentStatus, CourseStatus nextStatus) {
        if (nextStatus == null) {
            return;
        }
        if (currentStatus == null) {
            if (nextStatus == CourseStatus.DRAFT) {
                return;
            }
            throw new CourseConflictException(ErrorMessages.COURSE_CREATE_DRAFT_ONLY);
        }
        if (currentStatus.isTerminal()) {
            throw new CourseConflictException(ErrorMessages.courseTerminalTransition(currentStatus));
        }
        if (currentStatus.equals(nextStatus)) {
            return;
        }
        Set<CourseStatus> allowedTransitions = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTransitions.contains(nextStatus)) {
            throw new CourseConflictException(ErrorMessages.courseInvalidTransition(currentStatus, nextStatus));
        }
    }

    private void validateVisibilityByStatus(CourseStatus status, Boolean canEnrollment) {
        if (status == null) return;
        if (status != CourseStatus.PUBLISHED && Boolean.TRUE.equals(canEnrollment)) {
            throw new CourseValidationException(ErrorMessages.COURSE_CAN_ENROLLMENT_PUBLISHED_ONLY);
        }
    }

    private void validateUniqueCourseTitle(String title, Long instructorId, Long currentCourseId) {
        if (title == null || instructorId == null) {
            return;
        }
        boolean duplicateExists = currentCourseId == null
                ? courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrue(title, instructorId)
                : courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(title, instructorId, currentCourseId);
        if (duplicateExists) {
            throw new CourseConflictException(ErrorMessages.COURSE_DUPLICATE_TITLE);
        }
    }

    private Pageable buildPageable(Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        int safePage = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safeSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage - 1, safeSize, Sort.by(direction, safeSortBy));
    }
}
