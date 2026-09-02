package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Module;
import com.aditya.lms.exception.ModuleConflictException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Module createModule(Module module) {
        validateModuleForCreate(module);

        Course course = courseRepository.findById(module.getCourse().getId())
                .orElseThrow(() -> new ModuleConflictException("Course not found for module creation"));

        if (moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrue(course.getId(), module.getSequence())) {
            throw new ModuleConflictException("Module sequence already exists within the course");
        }

        if (module.getIsActive() == null) {
            module.setIsActive(Boolean.TRUE);
        }

        module.setCourse(course);
        Module created = moduleRepository.save(module);
        log.info("Module created successfully moduleId={}, courseId={}", created.getId(), course.getId());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Module getModule(Long moduleId) {
        return moduleRepository.findByIdAndIsActiveTrue(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Module> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        if (courseId == null) {
            throw new ModuleValidationException("courseId is mandatory");
        }

        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        boolean activeFilter = active == null || active;
        return moduleRepository.findByCourse_IdAndIsActive(courseId, activeFilter, pageable);
    }

    @Override
    @Transactional
    public Module updateModule(Long moduleId, Module module) {
        Module existing = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));

        validateModuleForUpdate(existing, module);

        if (module.getTitle() != null) {
            existing.setTitle(module.getTitle());
        }
        if (module.getDescription() != null) {
            existing.setDescription(module.getDescription());
        }
        if (module.getSequence() != null) {
            existing.setSequence(module.getSequence());
        }
        if (module.getIsActive() != null) {
            existing.setIsActive(module.getIsActive());
        }
        if (module.getUpdatedBy() != null) {
            existing.setUpdatedBy(module.getUpdatedBy());
        }

        Module updated = moduleRepository.save(existing);
        log.info("Module updated successfully moduleId={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteModule(Long moduleId) {
        Module existing = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));

        if (Boolean.FALSE.equals(existing.getIsActive())) {
            return;
        }

        existing.setIsActive(Boolean.FALSE);
        moduleRepository.save(existing);
        log.info("Module soft deleted moduleId={}", moduleId);
    }

    private void validateModuleForCreate(Module module) {
        if (module == null) {
            throw new ModuleValidationException("Module payload is required");
        }
        if (module.getCourse() == null || module.getCourse().getId() == null) {
            throw new ModuleValidationException("courseId is mandatory");
        }
        if (module.getTitle() == null || module.getTitle().isBlank()) {
            throw new ModuleValidationException("moduleTitle is mandatory");
        }
        if (module.getSequence() == null || module.getSequence() < 1) {
            throw new ModuleValidationException("sequence is mandatory and must be >= 1");
        }
    }

    private void validateModuleForUpdate(Module existing, Module incoming) {
        if (incoming == null) {
            throw new ModuleValidationException("Module payload is required");
        }
        if (incoming.getCourse() != null && incoming.getCourse().getId() != null &&
                !Objects.equals(incoming.getCourse().getId(), existing.getCourse().getId())) {
            throw new ModuleConflictException("Module course cannot be changed");
        }
        if (incoming.getTitle() != null && incoming.getTitle().isBlank()) {
            throw new ModuleValidationException("moduleTitle cannot be blank");
        }
        if (incoming.getSequence() != null && incoming.getSequence() < 1) {
            throw new ModuleValidationException("sequence must be >= 1");
        }

        Integer effectiveSequence = incoming.getSequence() != null ? incoming.getSequence() : existing.getSequence();
        if (incoming.getSequence() != null && moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrueAndIdNot(
                existing.getCourse().getId(), effectiveSequence, existing.getId())) {
            throw new ModuleConflictException("Module sequence already exists within the course");
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
