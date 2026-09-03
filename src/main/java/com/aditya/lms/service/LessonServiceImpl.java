package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.LessonConflictException;
import com.aditya.lms.exception.LessonForbiddenException;
import com.aditya.lms.exception.LessonNotFoundException;
import com.aditya.lms.exception.LessonValidationException;
import com.aditya.lms.exception.ModuleNotFoundException;
import com.aditya.lms.repository.LessonRepository;
import com.aditya.lms.repository.ModuleRepository;
import com.aditya.lms.service.interfaces.LessonService;
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
 * Business logic for Lesson lifecycle, following the rules documented in
 * docs/decisions/module.decisions.md (applied one level deeper via Module -> Course).
 * Role-based access is expressed as separate methods per caller role (admin/instructor/student)
 * rather than a runtime role parameter, so unauthorized actions (e.g. student mutating a lesson)
 * are caught at compile time by simply not exposing such a method.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    // Lesson create/update/delete is only allowed while the owning course is in one of these states.
    private static final Set<CourseStatus> MUTABLE_COURSE_STATUSES = Set.of(
            CourseStatus.DRAFT,
            CourseStatus.READY_TO_PUBLISH
    );

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    @Override
    @Transactional
    public Lesson createLessonAsAdmin(Lesson lesson, Long adminId) {
        requireId(adminId);
        return createLesson(lesson, adminId, null);
    }

    @Override
    @Transactional
    public Lesson createLessonAsInstructor(Lesson lesson, Long instructorId) {
        requireId(instructorId);
        return createLesson(lesson, instructorId, instructorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson getLesson(Long lessonId) {
        return lessonRepository.findByIdAndIsActiveTrue(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Lesson> listLessonsForAdmin(Long moduleId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return listLessons(moduleId, pageNo, pageSize, active, sortBy, sortOrder, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Lesson> listLessonsForInstructor(Long moduleId, Long instructorId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        requireId(instructorId);
        return listLessons(moduleId, pageNo, pageSize, active, sortBy, sortOrder, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Lesson> listLessonsForStudent(Long moduleId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        return listLessons(moduleId, pageNo, pageSize, Boolean.TRUE, sortBy, sortOrder, false);
    }

    @Override
    @Transactional
    public Lesson updateLessonAsAdmin(Long lessonId, Lesson lesson, Long adminId) {
        requireId(adminId);
        return updateLesson(lessonId, lesson, adminId, null);
    }

    @Override
    @Transactional
    public Lesson updateLessonAsInstructor(Long lessonId, Lesson lesson, Long instructorId) {
        requireId(instructorId);
        return updateLesson(lessonId, lesson, instructorId, instructorId);
    }

    @Override
    @Transactional
    public void deleteLessonAsAdmin(Long lessonId, Long adminId) {
        requireId(adminId);
        deleteLesson(lessonId, adminId, null);
    }

    @Override
    @Transactional
    public void deleteLessonAsInstructor(Long lessonId, Long instructorId) {
        requireId(instructorId);
        deleteLesson(lessonId, instructorId, instructorId);
    }

    /**
     * @param requesterId          id to stamp on createdBy/updatedBy
     * @param instructorOwnerCheck non-null when the caller is an instructor; used to verify course ownership
     */
    private Lesson createLesson(Lesson lesson, Long requesterId, Long instructorOwnerCheck) {
        validateLessonForCreate(lesson);

        Module module = moduleRepository.findById(lesson.getModule().getId())
                .orElseThrow(() -> new ModuleNotFoundException(lesson.getModule().getId()));
        Course course = module.getCourse();

        if (instructorOwnerCheck != null) {
            authorizeInstructorOwnership(course, instructorOwnerCheck);
        }
        validateCourseMutable(course.getCourseStatus(), ErrorMessages::lessonCourseStateCreateBlocked);

        if (lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrue(module.getId(), lesson.getSequence())) {
            throw new LessonConflictException(ErrorMessages.LESSON_DUPLICATE_SEQUENCE);
        }

        if (lesson.getIsActive() == null) {
            lesson.setIsActive(Boolean.TRUE);
        }
        lesson.setModule(module);
        lesson.setCreatedBy(requesterId);

        Lesson created = lessonRepository.save(lesson);
        log.info("Lesson created successfully lessonId={}, moduleId={}, requesterId={}", created.getId(), module.getId(), requesterId);
        return created;
    }

    private Page<Lesson> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder, boolean staffView) {
        if (moduleId == null) {
            throw new LessonValidationException(ErrorMessages.LESSON_MODULE_ID_MANDATORY);
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));

        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);

        boolean canSeeAllRegardlessOfActive = staffView && module.getCourse().getCourseStatus() == CourseStatus.DRAFT;
        if (canSeeAllRegardlessOfActive) {
            return lessonRepository.findByModule_Id(moduleId, pageable);
        }

        boolean activeFilter = active == null || active;
        return lessonRepository.findByModule_IdAndIsActive(moduleId, activeFilter, pageable);
    }

    /**
     * @param requesterId          id to stamp on updatedBy
     * @param instructorOwnerCheck non-null when the caller is an instructor; used to verify course ownership
     */
    private Lesson updateLesson(Long lessonId, Lesson lesson, Long requesterId, Long instructorOwnerCheck) {
        Lesson existing = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
        Course course = existing.getModule().getCourse();

        if (instructorOwnerCheck != null) {
            authorizeInstructorOwnership(course, instructorOwnerCheck);
        }
        validateCourseMutable(course.getCourseStatus(), ErrorMessages::lessonCourseStateEditBlocked);
        validateLessonForUpdate(existing, lesson);

        if (lesson.getContentType() != null) {
            existing.setContentType(lesson.getContentType());
        }
        if (lesson.getContentLink() != null) {
            existing.setContentLink(lesson.getContentLink());
        }
        if (lesson.getSequence() != null) {
            existing.setSequence(lesson.getSequence());
        }
        if (lesson.getIsActive() != null) {
            existing.setIsActive(lesson.getIsActive());
        }
        existing.setUpdatedBy(requesterId);

        Lesson updated = lessonRepository.save(existing);
        log.info("Lesson updated successfully lessonId={}, requesterId={}", updated.getId(), requesterId);
        return updated;
    }

    /**
     * @param requesterId          id to stamp on updatedBy
     * @param instructorOwnerCheck non-null when the caller is an instructor; used to verify course ownership
     */
    private void deleteLesson(Long lessonId, Long requesterId, Long instructorOwnerCheck) {
        Lesson existing = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
        Course course = existing.getModule().getCourse();

        if (instructorOwnerCheck != null) {
            authorizeInstructorOwnership(course, instructorOwnerCheck);
        }
        validateCourseMutable(course.getCourseStatus(), ErrorMessages::lessonCourseStateDeleteBlocked);

        if (Boolean.FALSE.equals(existing.getIsActive())) {
            return;
        }

        existing.setIsActive(Boolean.FALSE);
        existing.setUpdatedBy(requesterId);
        lessonRepository.save(existing);
        log.info("Lesson soft deleted lessonId={}, requesterId={}", lessonId, requesterId);
    }

    private void requireId(Long id) {
        if (id == null) {
            throw new LessonValidationException(ErrorMessages.LESSON_REQUESTER_ID_MANDATORY);
        }
    }

    /** Instructor may only mutate lessons belonging to a course they created. */
    private void authorizeInstructorOwnership(Course course, Long instructorId) {
        if (!Objects.equals(course.getInstructorId(), instructorId)) {
            throw new LessonForbiddenException(ErrorMessages.lessonInstructorForbidden(course.getId()));
        }
    }

    private void validateCourseMutable(CourseStatus courseStatus, Function<Object, ErrorMessages.Error> errorFactory) {
        if (!MUTABLE_COURSE_STATUSES.contains(courseStatus)) {
            throw new LessonConflictException(errorFactory.apply(courseStatus));
        }
    }

    private void validateLessonForCreate(Lesson lesson) {
        if (lesson == null) {
            throw new LessonValidationException(ErrorMessages.LESSON_PAYLOAD_REQUIRED);
        }
        if (lesson.getModule() == null || lesson.getModule().getId() == null) {
            throw new LessonValidationException(ErrorMessages.LESSON_MODULE_ID_MANDATORY);
        }
        if (lesson.getContentType() == null) {
            throw new LessonValidationException(ErrorMessages.LESSON_CONTENT_TYPE_MANDATORY);
        }
        if (lesson.getContentLink() == null || lesson.getContentLink().isBlank()) {
            throw new LessonValidationException(ErrorMessages.LESSON_CONTENT_LINK_MANDATORY);
        }
        if (lesson.getSequence() == null || lesson.getSequence() < 1) {
            throw new LessonValidationException(ErrorMessages.LESSON_SEQUENCE_MANDATORY);
        }
    }

    private void validateLessonForUpdate(Lesson existing, Lesson incoming) {
        if (incoming == null) {
            throw new LessonValidationException(ErrorMessages.LESSON_PAYLOAD_REQUIRED);
        }
        if (incoming.getModule() != null && incoming.getModule().getId() != null &&
                !Objects.equals(incoming.getModule().getId(), existing.getModule().getId())) {
            throw new LessonConflictException(ErrorMessages.LESSON_MODULE_UPDATE);
        }
        if (incoming.getContentLink() != null && incoming.getContentLink().isBlank()) {
            throw new LessonValidationException(ErrorMessages.LESSON_CONTENT_LINK_BLANK);
        }
        if (incoming.getSequence() != null && incoming.getSequence() < 1) {
            throw new LessonValidationException(ErrorMessages.LESSON_SEQUENCE_INVALID);
        }

        Integer effectiveSequence = incoming.getSequence() != null ? incoming.getSequence() : existing.getSequence();
        if (incoming.getSequence() != null && lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrueAndIdNot(
                existing.getModule().getId(), effectiveSequence, existing.getId())) {
            throw new LessonConflictException(ErrorMessages.LESSON_DUPLICATE_SEQUENCE);
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
