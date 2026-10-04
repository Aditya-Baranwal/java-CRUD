package com.aditya.lms.service;

import com.aditya.lms.dto.CourseView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.CourseConflictException;
import com.aditya.lms.exception.CourseNotFoundException;
import com.aditya.lms.exception.CourseValidationException;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.repository.EnrollmentRepository;
import com.aditya.lms.repository.LessonRepository;
import com.aditya.lms.repository.ModuleRepository;
import com.aditya.lms.repository.ProgressRepository;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final EnrollmentRepository enrollmentRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final ProgressRepository progressRepository;

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
    public CourseView getCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        return new CourseView(course, null, null, null, countActiveModules(course.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseView getCourseWithProgress(Long courseId, Long userId) {
        validateProgressUserId(userId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        int totalModuleCount = countActiveModules(courseId);
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourse_Id(userId, courseId).orElse(null);
        if (enrollment == null) {
            return new CourseView(course, userId, null, null, totalModuleCount);
        }
        int completedModuleCount = completedModuleCount(userId, courseId);
        boolean isCourseCompleted = enrollment.getCourseCompletionStatus() == CourseCompletionStatus.COMPLETE;
        return new CourseView(course, userId, isCourseCompleted, completedModuleCount, totalModuleCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCourses(Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Page<Course> page;
        if (courseStatus == null) {
            page = courseRepository.findAll(pageable);
        } else {
            page = courseRepository.findByCourseStatus(courseStatus, pageable);
        }
        return attachCounts(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCoursesWithProgress(Long userId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        validateProgressUserId(userId);
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Page<Course> page;
        if (courseStatus == null) {
            page = courseRepository.findAll(pageable);
        } else {
            page = courseRepository.findByCourseStatus(courseStatus, pageable);
        }
        return attachProgress(userId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCoursesForAdmin(Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        return listCourses(pageNo, pageSize, courseStatus, sortBy, sortOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCoursesForAdminWithProgress(Long userId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        return listCoursesWithProgress(userId, pageNo, pageSize, courseStatus, sortBy, sortOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCoursesForInstructor(Long instructorId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        if (instructorId == null) {
            throw new CourseValidationException(ErrorMessages.COURSE_INSTRUCTOR_ID_MANDATORY);
        }
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Page<Course> page;
        if (courseStatus == null) {
            page = courseRepository.findByInstructorId(instructorId, pageable);
        } else {
            page = courseRepository.findByInstructorIdAndCourseStatus(instructorId, courseStatus, pageable);
        }
        return attachCounts(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCoursesForInstructorWithProgress(Long instructorId, Long userId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder) {
        if (instructorId == null) {
            throw new CourseValidationException(ErrorMessages.COURSE_INSTRUCTOR_ID_MANDATORY);
        }
        validateProgressUserId(userId);
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Page<Course> page;
        if (courseStatus == null) {
            page = courseRepository.findByInstructorId(instructorId, pageable);
        } else {
            page = courseRepository.findByInstructorIdAndCourseStatus(instructorId, courseStatus, pageable);
        }
        return attachProgress(userId, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCoursesForStudent(Long studentId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Page<Course> page = courseRepository.findByCourseStatusIn(STUDENT_VISIBLE_STATUSES, pageable);
        return attachCounts(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseView> listCoursesForStudentWithProgress(Long studentId, Long userId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        validateProgressUserId(userId);
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Page<Course> page = courseRepository.findByCourseStatusIn(STUDENT_VISIBLE_STATUSES, pageable);
        return attachProgress(userId, page);
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

    private void validateProgressUserId(Long userId) {
        if (userId == null) {
            throw new CourseValidationException(ErrorMessages.COURSE_USER_ID_MANDATORY);
        }
    }

    private Page<CourseView> attachCounts(Page<Course> courses) {
        Map<Long, Integer> totalModulesByCourseId = activeModuleCountsByCourse(courses.getContent());
        return courses.map(course -> new CourseView(
                course,
                null,
                null,
                null,
                totalModulesByCourseId.getOrDefault(course.getId(), 0)
        ));
    }

    private Page<CourseView> attachProgress(Long userId, Page<Course> courses) {
        Map<Long, Integer> totalModulesByCourseId = activeModuleCountsByCourse(courses.getContent());
        List<Long> courseIds = courses.getContent().stream().map(Course::getId).filter(Objects::nonNull).toList();
        Map<Long, Enrollment> enrollmentByCourseId = new HashMap<>();
        if (!courseIds.isEmpty()) {
            enrollmentRepository.findByUserIdAndCourse_IdIn(userId, courseIds)
                    .forEach(enrollment -> enrollmentByCourseId.put(enrollment.getCourse().getId(), enrollment));
        }

        return courses.map(course -> {
            Integer totalModuleCount = totalModulesByCourseId.getOrDefault(course.getId(), 0);
            Enrollment enrollment = enrollmentByCourseId.get(course.getId());
            if (enrollment == null) {
                return new CourseView(course, userId, null, null, totalModuleCount);
            }
            int completedModules = completedModuleCount(userId, course.getId());
            boolean completed = enrollment.getCourseCompletionStatus() == CourseCompletionStatus.COMPLETE;
            return new CourseView(course, userId, completed, completedModules, totalModuleCount);
        });
    }

    private Map<Long, Integer> activeModuleCountsByCourse(List<Course> courses) {
        List<Long> courseIds = courses.stream().map(Course::getId).filter(Objects::nonNull).toList();
        Map<Long, Integer> counts = new HashMap<>();
        if (courseIds.isEmpty()) {
            return counts;
        }
        List<Module> activeModules = moduleRepository.findByCourse_IdInAndIsActiveTrue(courseIds);
        for (Module module : activeModules) {
            if (module.getCourse() == null || module.getCourse().getId() == null) {
                continue;
            }
            counts.merge(module.getCourse().getId(), 1, Integer::sum);
        }
        return counts;
    }

    private int countActiveModules(Long courseId) {
        if (courseId == null) {
            return 0;
        }
        return (int) moduleRepository.findByCourse_IdInAndIsActiveTrue(List.of(courseId)).stream()
                .filter(module -> module.getCourse() != null)
                .filter(module -> courseId.equals(module.getCourse().getId()))
                .count();
    }

    private int completedModuleCount(Long userId, Long courseId) {
        List<Lesson> activeLessons = lessonRepository.findByModule_Course_Id(courseId).stream()
                .filter(lesson -> Boolean.TRUE.equals(lesson.getIsActive()))
                .toList();

        Map<Long, Integer> totalLessonsByModuleId = new HashMap<>();
        for (Lesson lesson : activeLessons) {
            totalLessonsByModuleId.merge(lesson.getModule().getId(), 1, Integer::sum);
        }

        Map<Long, Set<Long>> finishedLessonsByModuleId = new HashMap<>();
        List<Progress> progressRecords = progressRepository.findByUserIdAndLesson_Module_Course_Id(userId, courseId);
        for (Progress progress : progressRecords) {
            Lesson lesson = progress.getLesson();
            if (progress.getLessonStatus() != LessonStatus.FINISHED || !Boolean.TRUE.equals(lesson.getIsActive())) {
                continue;
            }
            finishedLessonsByModuleId.computeIfAbsent(lesson.getModule().getId(), ignored -> new HashSet<>())
                    .add(lesson.getId());
        }

        int completed = 0;
        for (Map.Entry<Long, Integer> moduleEntry : totalLessonsByModuleId.entrySet()) {
            int totalLessons = moduleEntry.getValue();
            int finished = finishedLessonsByModuleId.getOrDefault(moduleEntry.getKey(), Set.of()).size();
            if (totalLessons > 0 && finished == totalLessons) {
                completed++;
            }
        }
        return completed;
    }

    private Pageable buildPageable(Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        int safePage = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safeSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage - 1, safeSize, Sort.by(direction, safeSortBy));
    }
}
