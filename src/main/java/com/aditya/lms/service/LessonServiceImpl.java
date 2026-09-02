package com.aditya.lms.service;

import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.exception.LessonConflictException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    @Override
    @Transactional
    public Lesson createLesson(Lesson lesson) {
        validateLessonForCreate(lesson);

        Module module = moduleRepository.findById(lesson.getModule().getId())
                .orElseThrow(() -> new ModuleNotFoundException(lesson.getModule().getId()));

        if (lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrue(module.getId(), lesson.getSequence())) {
            throw new LessonConflictException("Lesson sequence already exists within the module");
        }

        if (lesson.getIsActive() == null) {
            lesson.setIsActive(Boolean.TRUE);
        }

        lesson.setModule(module);
        Lesson created = lessonRepository.save(lesson);
        log.info("Lesson created successfully lessonId={}, moduleId={}", created.getId(), module.getId());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson getLesson(Long lessonId) {
        return lessonRepository.findByIdAndIsActiveTrue(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Lesson> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Long userId, Boolean active, String sortBy, String sortOrder) {

        if (moduleId == null) {
            throw new LessonValidationException("moduleId is mandatory");
        }

        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        boolean activeFilter = active == null || active;
        return lessonRepository.findByModule_IdAndIsActive(moduleId, activeFilter, pageable);
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long lessonId, Lesson lesson) {
        Lesson existing = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));

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
        if (lesson.getUpdatedBy() != null) {
            existing.setUpdatedBy(lesson.getUpdatedBy());
        }

        Lesson updated = lessonRepository.save(existing);
        log.info("Lesson updated successfully lessonId={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson existing = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));

        if (Boolean.FALSE.equals(existing.getIsActive())) {
            return;
        }

        existing.setIsActive(Boolean.FALSE);
        lessonRepository.save(existing);
        log.info("Lesson soft deleted lessonId={}", lessonId);
    }

    private void validateLessonForCreate(Lesson lesson) {
        if (lesson == null) {
            throw new LessonValidationException("Lesson payload is required");
        }
        if (lesson.getModule() == null || lesson.getModule().getId() == null) {
            throw new LessonValidationException("moduleId is mandatory");
        }
        if (lesson.getContentType() == null) {
            throw new LessonValidationException("contentType is mandatory");
        }
        if (lesson.getContentLink() == null || lesson.getContentLink().isBlank()) {
            throw new LessonValidationException("contentLink is mandatory");
        }
        if (lesson.getSequence() == null || lesson.getSequence() < 1) {
            throw new LessonValidationException("sequence is mandatory and must be >= 1");
        }
    }

    private void validateLessonForUpdate(Lesson existing, Lesson incoming) {
        if (incoming == null) {
            throw new LessonValidationException("Lesson payload is required");
        }
        if (incoming.getModule() != null && incoming.getModule().getId() != null &&
                !Objects.equals(incoming.getModule().getId(), existing.getModule().getId())) {
            throw new LessonConflictException("Lesson module cannot be changed");
        }
        if (incoming.getContentLink() != null && incoming.getContentLink().isBlank()) {
            throw new LessonValidationException("contentLink cannot be blank");
        }
        if (incoming.getSequence() != null && incoming.getSequence() < 1) {
            throw new LessonValidationException("sequence must be >= 1");
        }

        Integer effectiveSequence = incoming.getSequence() != null ? incoming.getSequence() : existing.getSequence();
        if (incoming.getSequence() != null && lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrueAndIdNot(
                existing.getModule().getId(), effectiveSequence, existing.getId())) {
            throw new LessonConflictException("Lesson sequence already exists within the module");
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
