package com.aditya.lms.mapper;

import com.aditya.lms.dto.ModuleView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.ContentType;
import com.lms.model.CourseResponseModulesInnerDTO;
import com.lms.model.ModuleCreateRequestDTO;
import com.lms.model.ModuleCreateResponseDTO;
import com.lms.model.ModuleDeleteResponseDTO;
import com.lms.model.ModuleGetResponseDTO;
import com.lms.model.ModuleListResponseDTO;
import com.lms.model.ModuleListResponseDataInnerDTO;
import com.lms.model.ModuleListResponseDataInnerOneOfDTO;
import com.lms.model.ModuleProgressResponseDTO;
import com.lms.model.ModuleResponseDTO;
import com.lms.model.ModuleResponseLessonsInnerDTO;
import com.lms.model.ModuleUpdateRequestDTO;
import com.lms.model.ModuleUpdateResponseDTO;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class ModuleMapper {

    public Module toEntity(ModuleCreateRequestDTO request) {
        Module module = new Module();
        Course course = new Course();
        course.setId(request.getCourseId());
        module.setCourse(course);
        module.setTitle(request.getModuleTitle());
        module.setDescription(request.getModuleDescription());
        module.setSequence(request.getSequence());
        module.setIsActive(Boolean.TRUE);
        return module;
    }

    public void applyUpdates(Module module, ModuleUpdateRequestDTO request) {
        if (request.getModuleTitle() != null) {
            module.setTitle(request.getModuleTitle());
        }
        if (request.getModuleDescription() != null) {
            module.setDescription(request.getModuleDescription());
        }
        if (request.getSequence() != null) {
            module.setSequence(request.getSequence());
        }
        if (request.getIsActive() != null) {
            module.setIsActive(request.getIsActive());
        }
    }

    public ModuleCreateResponseDTO toCreateResponse(Module module) {
        return new ModuleCreateResponseDTO()
                .message("Module created successfully")
                .data(toResponse(new ModuleView(module, null, null, null, 0), false))
                .timestamp(OffsetDateTime.now());
    }

    public ModuleGetResponseDTO toGetResponse(ModuleView moduleView, boolean includeLessons) {
        return new ModuleGetResponseDTO()
                .message("Module fetched successfully")
                .data(toResponse(moduleView, includeLessons))
                .timestamp(OffsetDateTime.now());
    }

    public ModuleGetResponseDTO toGetProgressResponse(ModuleView moduleView, boolean includeLessons) {
        return new ModuleGetResponseDTO()
                .message("Module fetched successfully")
                .data(toProgressResponse(moduleView, includeLessons))
                .timestamp(OffsetDateTime.now());
    }

    public ModuleUpdateResponseDTO toUpdateResponse(Module module) {
        return new ModuleUpdateResponseDTO()
                .message("Module updated successfully")
                .data(toResponse(new ModuleView(module, null, null, null, 0), false))
                .timestamp(OffsetDateTime.now());
    }

    public ModuleDeleteResponseDTO toDeleteResponse(String message) {
        return new ModuleDeleteResponseDTO()
                .message(message)
                .data(Collections.emptyMap())
                .timestamp(OffsetDateTime.now());
    }

    public ModuleListResponseDTO toListResponse(Page<ModuleView> page) {
        return new ModuleListResponseDTO()
                .message("Modules fetched successfully")
                .data(page.getContent().stream().map(this::toListItem).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    public ModuleListResponseDTO toListProgressResponse(Page<ModuleView> page) {
        return new ModuleListResponseDTO()
                .message("Modules fetched successfully")
                .data(page.getContent().stream().map(this::toProgressListItem).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    private ModuleResponseDTO toResponse(ModuleView moduleView, boolean includeLessons) {
        Module module = moduleView.module();
        ModuleResponseDTO response = new ModuleResponseDTO()
                .moduleId(module.getId())
                .courseId(module.getCourse() == null ? null : module.getCourse().getId())
                .moduleTitle(module.getTitle())
                .moduleDescription(module.getDescription())
                .sequence(module.getSequence())
                .totalLessonCount(moduleView.totalLessonCount())
                .isActive(module.getIsActive())
                .createdAt(module.getCreatedAt());

        if (!includeLessons || module.getLessons() == null || !Hibernate.isInitialized(module.getLessons())) {
            response.setLessons(Collections.emptyList());
            return response;
        }

        response.setLessons(module.getLessons().stream().map(this::toLessonInner).toList());
        return response;
    }

    private ModuleProgressResponseDTO toProgressResponse(ModuleView moduleView, boolean includeLessons) {
        Module module = moduleView.module();
        ModuleProgressResponseDTO response = new ModuleProgressResponseDTO()
                .moduleId(module.getId())
                .courseId(module.getCourse() == null ? null : module.getCourse().getId())
                .moduleTitle(module.getTitle())
                .moduleDescription(module.getDescription())
                .sequence(module.getSequence())
                .userId(moduleView.userId())
                .isModuleCompleted(moduleView.isModuleCompleted())
                .completedLessonCount(moduleView.completedLessonCount())
                .totalLessonCount(moduleView.totalLessonCount())
                .isActive(module.getIsActive())
                .createdAt(module.getCreatedAt());

        if (!includeLessons || module.getLessons() == null || !Hibernate.isInitialized(module.getLessons())) {
            response.setLessons(Collections.emptyList());
            return response;
        }

        response.setLessons(module.getLessons().stream().map(this::toLessonInner).toList());
        return response;
    }

    private ModuleListResponseDataInnerDTO toListItem(ModuleView moduleView) {
        Module module = moduleView.module();
        return new CourseResponseModulesInnerDTO()
                .moduleId(module.getId())
                .courseId(module.getCourse() == null ? null : module.getCourse().getId())
                .moduleTitle(module.getTitle())
                .moduleDescription(module.getDescription())
                .sequence(module.getSequence())
                .totalLessonCount(moduleView.totalLessonCount())
                .isActive(module.getIsActive())
                .createdAt(module.getCreatedAt());
    }

    private ModuleListResponseDataInnerDTO toProgressListItem(ModuleView moduleView) {
        Module module = moduleView.module();
        return new ModuleListResponseDataInnerOneOfDTO()
                .moduleId(module.getId())
                .courseId(module.getCourse() == null ? null : module.getCourse().getId())
                .moduleTitle(module.getTitle())
                .moduleDescription(module.getDescription())
                .sequence(module.getSequence())
                .isActive(module.getIsActive())
                .createdAt(module.getCreatedAt())
                .userId(moduleView.userId())
                .isModuleCompleted(moduleView.isModuleCompleted())
                .completedLessonCount(moduleView.completedLessonCount())
                .totalLessonCount(moduleView.totalLessonCount());
    }

    private ModuleResponseLessonsInnerDTO toLessonInner(Lesson lesson) {
        return new ModuleResponseLessonsInnerDTO()
                .lessonId(lesson.getId())
                .moduleId(lesson.getModule() == null ? null : lesson.getModule().getId())
                .contentType(lesson.getContentType() == null ? null : ModuleResponseLessonsInnerDTO.ContentTypeEnum.valueOf(lesson.getContentType().name()))
                .contentLink(lesson.getContentLink() == null ? null : URI.create(lesson.getContentLink()))
                .sequence(lesson.getSequence())
                .isActive(lesson.getIsActive())
                .createdAt(lesson.getCreatedAt());
    }
}
