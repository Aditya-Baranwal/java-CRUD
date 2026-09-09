package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.ModuleConflictException;
import com.aditya.lms.exception.ModuleForbiddenException;
import com.aditya.lms.exception.ModuleNotFoundException;
import com.aditya.lms.exception.ModuleValidationException;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.repository.ModuleRepository;
import com.aditya.lms.service.interfaces.ModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

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
    public Module getModule(Long moduleId) {
        return moduleRepository.findByIdAndIsActiveTrue(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Module> listModulesForAdmin(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return listModules(courseId, pageNo, pageSize, active, sortBy, sortOrder, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Module> listModulesForInstructor(Long courseId, Long instructorId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        requireId(instructorId);
        return listModules(courseId, pageNo, pageSize, active, sortBy, sortOrder, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Module> listModulesForStudent(Long courseId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        return listModules(courseId, pageNo, pageSize, Boolean.TRUE, sortBy, sortOrder, false);
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

    private Page<Module> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder, boolean staffView) {
        if (courseId == null) {
            throw new ModuleValidationException(ErrorMessages.MODULE_COURSE_ID_MANDATORY);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ModuleConflictException(ErrorMessages.moduleCourseNotFound(courseId)));

        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);

        boolean canSeeAllRegardlessOfActive = staffView && course.getCourseStatus() == CourseStatus.DRAFT;
        if (canSeeAllRegardlessOfActive) {
            return moduleRepository.findByCourse_Id(courseId, pageable);
        }

        boolean activeFilter = active == null || active;
        return moduleRepository.findByCourse_IdAndIsActive(courseId, activeFilter, pageable);
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
