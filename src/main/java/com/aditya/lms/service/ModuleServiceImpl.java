package com.aditya.lms.service;

import com.aditya.lms.dto.ModuleView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.ModuleConflictException;
import com.aditya.lms.exception.ModuleForbiddenException;
import com.aditya.lms.exception.ModuleNotFoundException;
import com.aditya.lms.exception.ModuleValidationException;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.repository.EnrollmentRepository;
import com.aditya.lms.repository.LessonRepository;
import com.aditya.lms.repository.ModuleRepository;
import com.aditya.lms.repository.ProgressRepository;
import com.aditya.lms.service.interfaces.ModuleService;
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
import java.util.function.Function;

/**
 * Business logic for Module lifecycle, following the rules documented in
 * docs/decisions/module.decisions.md.
 * Role-based access is expressed as separate methods per caller role (admin/instructor/student)
 * rather than a runtime role parameter, so unauthorized actions (e.g. student mutating a module)
 * are caught at compile time by simply not exposing such a method.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    // Module create/update/delete (including the isActive toggle) is only allowed while
    // the owning course is in one of these states.
    private static final Set<CourseStatus> MUTABLE_COURSE_STATUSES = Set.of(
            CourseStatus.DRAFT,
            CourseStatus.READY_TO_PUBLISH
    );

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;

    @Override
    @Transactional
    public Module createModuleAsAdmin(Module module, Long adminId) {
        requireId(adminId);
        return createModule(module, adminId, null);
    }

    @Override
    @Transactional
    public Module createModuleAsInstructor(Module module, Long instructorId) {
        requireId(instructorId);
        return createModule(module, instructorId, instructorId);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleView getModule(Long moduleId) {
        Module module = moduleRepository.findByIdAndIsActiveTrue(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
        return new ModuleView(module, null, null, null, countActiveLessons(module));
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleView getModuleWithProgress(Long moduleId, Long userId) {
        validateProgressUserId(userId);
        Module module = moduleRepository.findByIdAndIsActiveTrue(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
        Long courseId = module.getCourse().getId();
        boolean enrolled = enrollmentRepository.existsByUserIdAndCourse_Id(userId, courseId);
        return buildModuleView(module, userId, enrolled);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleView> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        Course course = getCourse(courseId);
        Page<Module> modules = listModulesPage(course, pageNo, pageSize, active, sortBy, sortOrder, true);
        return attachCounts(course, modules);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleView> listModulesWithProgress(Long courseId, Long userId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        validateProgressUserId(userId);
        Course course = getCourse(courseId);
        Page<Module> modules = listModulesPage(course, pageNo, pageSize, active, sortBy, sortOrder, true);
        return attachProgress(course, modules, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleView> listModulesForInstructor(Long courseId, Long instructorId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        requireId(instructorId);
        Course course = getCourse(courseId);
        Page<Module> modules = listModulesPage(course, pageNo, pageSize, active, sortBy, sortOrder, true);
        return attachCounts(course, modules);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleView> listModulesForInstructorWithProgress(Long courseId, Long instructorId, Long userId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        requireId(instructorId);
        validateProgressUserId(userId);
        Course course = getCourse(courseId);
        Page<Module> modules = listModulesPage(course, pageNo, pageSize, active, sortBy, sortOrder, true);
        return attachProgress(course, modules, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleView> listModulesForStudent(Long courseId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        Course course = getCourse(courseId);
        Page<Module> modules = listModulesPage(course, pageNo, pageSize, Boolean.TRUE, sortBy, sortOrder, false);
        return attachCounts(course, modules);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleView> listModulesForStudentWithProgress(Long courseId, Long userId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        validateProgressUserId(userId);
        Course course = getCourse(courseId);
        Page<Module> modules = listModulesPage(course, pageNo, pageSize, Boolean.TRUE, sortBy, sortOrder, false);
        return attachProgress(course, modules, userId);
    }

    @Override
    @Transactional
    public Module updateModuleAsAdmin(Long moduleId, Module module, Long adminId) {
        requireId(adminId);
        return updateModule(moduleId, module, adminId, null);
    }

    @Override
    @Transactional
    public Module updateModuleAsInstructor(Long moduleId, Module module, Long instructorId) {
        requireId(instructorId);
        return updateModule(moduleId, module, instructorId, instructorId);
    }

    @Override
    @Transactional
    public void deleteModuleAsAdmin(Long moduleId, Long adminId) {
        requireId(adminId);
        deleteModule(moduleId, adminId, null);
    }

    @Override
    @Transactional
    public void deleteModuleAsInstructor(Long moduleId, Long instructorId) {
        requireId(instructorId);
        deleteModule(moduleId, instructorId, instructorId);
    }

    /**
     * @param requesterId          id to stamp on createdBy
     * @param instructorOwnerCheck non-null when the caller is an instructor; used to verify course ownership
     */
    private Module createModule(Module module, Long requesterId, Long instructorOwnerCheck) {
        validateModuleForCreate(module);

        Course course = courseRepository.findById(module.getCourse().getId())
                .orElseThrow(() -> new ModuleConflictException(ErrorMessages.moduleCourseNotFound(module.getCourse().getId())));

        if (instructorOwnerCheck != null) {
            authorizeInstructorOwnership(course, instructorOwnerCheck);
        }
        validateCourseMutable(course.getCourseStatus(), ErrorMessages::moduleCourseStateCreateBlocked);

        if (moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrue(course.getId(), module.getSequence())) {
            throw new ModuleConflictException(ErrorMessages.MODULE_DUPLICATE_SEQUENCE);
        }

        if (module.getIsActive() == null) {
            module.setIsActive(Boolean.TRUE);
        }
        module.setCourse(course);
        module.setCreatedBy(requesterId);

        Module created = moduleRepository.save(module);
        log.info("Module created successfully moduleId={}, courseId={}, requesterId={}", created.getId(), course.getId(), requesterId);
        return created;
    }

    private Page<Module> listModulesPage(Course course, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder, boolean staffView) {
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        Long courseId = course.getId();

        boolean canSeeAllRegardlessOfActive = staffView && course.getCourseStatus() == CourseStatus.DRAFT;
        if (canSeeAllRegardlessOfActive) {
            return moduleRepository.findByCourse_Id(courseId, pageable);
        }

        boolean activeFilter = active == null || active;
        return moduleRepository.findByCourse_IdAndIsActive(courseId, activeFilter, pageable);
    }

    private Course getCourse(Long courseId) {
        if (courseId == null) {
            throw new ModuleValidationException(ErrorMessages.MODULE_COURSE_ID_MANDATORY);
        }
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ModuleConflictException(ErrorMessages.moduleCourseNotFound(courseId)));
    }

    private Page<ModuleView> attachCounts(Course course, Page<Module> modules) {
        Map<Long, Integer> totalLessonsByModuleId = activeLessonCountsByModule(course.getId());
        return modules.map(module -> new ModuleView(
                module,
                null,
                null,
                null,
                totalLessonsByModuleId.getOrDefault(module.getId(), 0)
        ));
    }

    private Page<ModuleView> attachProgress(Course course, Page<Module> modules, Long userId) {
        Map<Long, Integer> totalLessonsByModuleId = activeLessonCountsByModule(course.getId());
        boolean enrolled = enrollmentRepository.existsByUserIdAndCourse_Id(userId, course.getId());
        if (!enrolled) {
            return modules.map(module -> new ModuleView(
                    module,
                    userId,
                    null,
                    null,
                    totalLessonsByModuleId.getOrDefault(module.getId(), 0)
            ));
        }

        Map<Long, Set<Long>> completedLessonIdsByModuleId = new HashMap<>();

        List<Progress> progressRecords = progressRepository.findByUserIdAndLesson_Module_Course_Id(userId, course.getId());
        for (Progress progress : progressRecords) {
            Lesson lesson = progress.getLesson();
            if (!Boolean.TRUE.equals(lesson.getIsActive()) || progress.getLessonStatus() != LessonStatus.FINISHED) {
                continue;
            }
            completedLessonIdsByModuleId
                    .computeIfAbsent(lesson.getModule().getId(), ignored -> new HashSet<>())
                    .add(lesson.getId());
        }

        return modules.map(module -> buildModuleView(module, userId, totalLessonsByModuleId, completedLessonIdsByModuleId));
    }

    private ModuleView buildModuleView(Module module, Long userId, boolean enrolled) {
        int totalLessonCount = countActiveLessons(module);
        if (!enrolled) {
            return new ModuleView(module, userId, null, null, totalLessonCount);
        }
        int completedLessonCount = (int) progressRepository.findByUserIdAndLesson_Module_Id(userId, module.getId()).stream()
                .filter(progress -> Boolean.TRUE.equals(progress.getLesson().getIsActive()))
                .filter(progress -> progress.getLessonStatus() == LessonStatus.FINISHED)
                .map(progress -> progress.getLesson().getId())
                .distinct()
                .count();
        return new ModuleView(
                module,
                userId,
                totalLessonCount > 0 && completedLessonCount == totalLessonCount,
                completedLessonCount,
                totalLessonCount
        );
    }

    private Map<Long, Integer> activeLessonCountsByModule(Long courseId) {
        Map<Long, Integer> totalLessonsByModuleId = new HashMap<>();
        List<Lesson> activeLessons = lessonRepository.findByModule_Course_Id(courseId).stream()
                .filter(lesson -> Boolean.TRUE.equals(lesson.getIsActive()))
                .toList();
        for (Lesson lesson : activeLessons) {
            totalLessonsByModuleId.merge(lesson.getModule().getId(), 1, Integer::sum);
        }
        return totalLessonsByModuleId;
    }

    private int countActiveLessons(Module module) {
        if (module.getLessons() == null) {
            return 0;
        }
        return (int) module.getLessons().stream()
                .filter(lesson -> Boolean.TRUE.equals(lesson.getIsActive()))
                .count();
    }

    private ModuleView buildModuleView(Module module, Long userId, Map<Long, Integer> totalLessonsByModuleId, Map<Long, Set<Long>> completedLessonIdsByModuleId) {
        int totalLessonCount = totalLessonsByModuleId.getOrDefault(module.getId(), 0);
        int completedLessonCount = completedLessonIdsByModuleId.getOrDefault(module.getId(), Set.of()).size();
        return new ModuleView(
                module,
                userId,
                totalLessonCount > 0 && completedLessonCount == totalLessonCount,
                completedLessonCount,
                totalLessonCount
        );
    }

    /**
     * @param requesterId          id to stamp on updatedBy
     * @param instructorOwnerCheck non-null when the caller is an instructor; used to verify course ownership
     */
    private Module updateModule(Long moduleId, Module module, Long requesterId, Long instructorOwnerCheck) {
        Module existing = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
        Course course = existing.getCourse();

        if (instructorOwnerCheck != null) {
            authorizeInstructorOwnership(course, instructorOwnerCheck);
        }
        validateCourseMutable(course.getCourseStatus(), ErrorMessages::moduleCourseStateEditBlocked);
        validateModuleForUpdate(existing, module);

        applyUpdates(existing, module);
        existing.setUpdatedBy(requesterId);

        Module updated = moduleRepository.save(existing);
        log.info("Module updated successfully moduleId={}, requesterId={}", updated.getId(), requesterId);
        return updated;
    }

    private void applyUpdates(Module existing, Module incoming) {
        if (incoming.getTitle() != null) {
            existing.setTitle(incoming.getTitle());
        }
        if (incoming.getDescription() != null) {
            existing.setDescription(incoming.getDescription());
        }
        if (incoming.getSequence() != null) {
            existing.setSequence(incoming.getSequence());
        }
        if (incoming.getIsActive() != null) {
            existing.setIsActive(incoming.getIsActive());
        }
    }

    /**
     * @param requesterId          id to stamp on updatedBy
     * @param instructorOwnerCheck non-null when the caller is an instructor; used to verify course ownership
     */
    private void deleteModule(Long moduleId, Long requesterId, Long instructorOwnerCheck) {
        Module existing = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
        Course course = existing.getCourse();

        if (instructorOwnerCheck != null) {
            authorizeInstructorOwnership(course, instructorOwnerCheck);
        }
        validateCourseMutable(course.getCourseStatus(), ErrorMessages::moduleCourseStateDeleteBlocked);

        if (Boolean.FALSE.equals(existing.getIsActive())) {
            return;
        }

        existing.setIsActive(Boolean.FALSE);
        existing.setUpdatedBy(requesterId);
        moduleRepository.save(existing);
        log.info("Module soft deleted moduleId={}, requesterId={}", moduleId, requesterId);
    }

    private void requireId(Long id) {
        if (id == null) {
            throw new ModuleValidationException(ErrorMessages.MODULE_REQUESTER_ID_MANDATORY);
        }
    }

    private void validateProgressUserId(Long userId) {
        if (userId == null) {
            throw new ModuleValidationException(ErrorMessages.MODULE_USER_ID_MANDATORY);
        }
    }

    /** Instructor may only mutate modules belonging to a course they created. */
    private void authorizeInstructorOwnership(Course course, Long instructorId) {
        if (!Objects.equals(course.getInstructorId(), instructorId)) {
            throw new ModuleForbiddenException(ErrorMessages.moduleInstructorForbidden(course.getId()));
        }
    }

    private void validateCourseMutable(CourseStatus courseStatus, Function<Object, ErrorMessages.Error> errorFactory) {
        if (!MUTABLE_COURSE_STATUSES.contains(courseStatus)) {
            throw new ModuleConflictException(errorFactory.apply(courseStatus));
        }
    }

    private void validateModuleForCreate(Module module) {
        if (module == null) {
            throw new ModuleValidationException(ErrorMessages.MODULE_PAYLOAD_REQUIRED);
        }
        if (module.getCourse() == null || module.getCourse().getId() == null) {
            throw new ModuleValidationException(ErrorMessages.MODULE_COURSE_ID_MANDATORY);
        }
        if (module.getTitle() == null || module.getTitle().isBlank()) {
            throw new ModuleValidationException(ErrorMessages.MODULE_TITLE_MANDATORY);
        }
        if (module.getSequence() == null || module.getSequence() < 1) {
            throw new ModuleValidationException(ErrorMessages.MODULE_SEQUENCE_MANDATORY);
        }
    }

    private void validateModuleForUpdate(Module existing, Module incoming) {
        if (incoming == null) {
            throw new ModuleValidationException(ErrorMessages.MODULE_PAYLOAD_REQUIRED);
        }
        if (incoming.getCourse() != null && incoming.getCourse().getId() != null &&
                !Objects.equals(incoming.getCourse().getId(), existing.getCourse().getId())) {
            throw new ModuleConflictException(ErrorMessages.MODULE_COURSE_UPDATE);
        }
        if (incoming.getTitle() != null && incoming.getTitle().isBlank()) {
            throw new ModuleValidationException(ErrorMessages.MODULE_TITLE_BLANK);
        }
        if (incoming.getSequence() != null && incoming.getSequence() < 1) {
            throw new ModuleValidationException(ErrorMessages.MODULE_SEQUENCE_INVALID);
        }

        Integer effectiveSequence = incoming.getSequence() != null ? incoming.getSequence() : existing.getSequence();
        if (incoming.getSequence() != null && moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrueAndIdNot(
                existing.getCourse().getId(), effectiveSequence, existing.getId())) {
            throw new ModuleConflictException(ErrorMessages.MODULE_DUPLICATE_SEQUENCE);
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
