package com.aditya.lms.mapper;

import com.aditya.lms.dto.LessonView;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.ContentType;
import com.lms.model.LessonCreateRequestDTO;
import com.lms.model.LessonCreateResponseDTO;
import com.lms.model.LessonDeleteResponseDTO;
import com.lms.model.LessonGetResponseDTO;
import com.lms.model.LessonListResponseDTO;
import com.lms.model.LessonListResponseDataInnerDTO;
import com.lms.model.LessonListResponseDataInnerOneOfDTO;
import com.lms.model.LessonProgressResponseDTO;
import com.lms.model.LessonResponseDTO;
import com.lms.model.LessonUpdateRequestDTO;
import com.lms.model.LessonUpdateResponseDTO;
import com.lms.model.ModuleResponseLessonsInnerDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;

@Component
public class LessonMapper {

    public Lesson toEntity(LessonCreateRequestDTO request) {
        Lesson lesson = new Lesson();
        Module module = new Module();
        module.setId(request.getModuleId());
        lesson.setModule(module);
        lesson.setContentType(ContentType.valueOf(request.getContentType().name()));
        lesson.setContentLink(request.getContentLink().toString());
        lesson.setSequence(request.getSequence());
        lesson.setIsActive(Boolean.TRUE);
        return lesson;
    }

    public void applyUpdates(Lesson lesson, LessonUpdateRequestDTO request) {
        if (request.getContentType() != null) {
            lesson.setContentType(ContentType.valueOf(request.getContentType().name()));
        }
        if (request.getContentLink() != null) {
            lesson.setContentLink(request.getContentLink().toString());
        }
        if (request.getSequence() != null) {
            lesson.setSequence(request.getSequence());
        }
        if (request.getIsActive() != null) {
            lesson.setIsActive(request.getIsActive());
        }
    }

    public LessonCreateResponseDTO toCreateResponse(Lesson lesson) {
        return new LessonCreateResponseDTO()
                .message("Lesson created successfully")
                .data(toResponse(lesson))
                .timestamp(OffsetDateTime.now());
    }

    public LessonGetResponseDTO toGetResponse(Lesson lesson) {
        return new LessonGetResponseDTO()
                .message("Lesson fetched successfully")
                .data(toResponse(lesson))
                .timestamp(OffsetDateTime.now());
    }

    public LessonGetResponseDTO toGetProgressResponse(LessonView lessonView) {
        return new LessonGetResponseDTO()
                .message("Lesson fetched successfully")
                .data(toProgressResponse(lessonView))
                .timestamp(OffsetDateTime.now());
    }

    public LessonUpdateResponseDTO toUpdateResponse(Lesson lesson) {
        return new LessonUpdateResponseDTO()
                .message("Lesson updated successfully")
                .data(toResponse(lesson))
                .timestamp(OffsetDateTime.now());
    }

    public LessonDeleteResponseDTO toDeleteResponse(String message) {
        return new LessonDeleteResponseDTO()
                .message(message)
                .data(Collections.emptyMap())
                .timestamp(OffsetDateTime.now());
    }

    public LessonListResponseDTO toListResponse(Page<Lesson> page) {
        return new LessonListResponseDTO()
                .message("Lessons fetched successfully")
                .data(page.getContent().stream().map(this::toListItem).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    public LessonListResponseDTO toListProgressResponse(Page<LessonView> page) {
        return new LessonListResponseDTO()
                .message("Lessons fetched successfully")
                .data(page.getContent().stream().map(this::toProgressListItem).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    private LessonResponseDTO toResponse(Lesson lesson) {
        return new LessonResponseDTO()
                .lessonId(lesson.getId())
                .moduleId(lesson.getModule() == null ? null : lesson.getModule().getId())
                .contentType(lesson.getContentType() == null ? null : LessonResponseDTO.ContentTypeEnum.valueOf(lesson.getContentType().name()))
                .contentLink(lesson.getContentLink() == null ? null : URI.create(lesson.getContentLink()))
                .sequence(lesson.getSequence())
                .isActive(lesson.getIsActive())
                .createdAt(lesson.getCreatedAt());
    }

    private LessonProgressResponseDTO toProgressResponse(LessonView lessonView) {
        Lesson lesson = lessonView.lesson();
        return new LessonProgressResponseDTO()
                .lessonId(lesson.getId())
                .moduleId(lesson.getModule() == null ? null : lesson.getModule().getId())
                .contentType(lesson.getContentType() == null ? null : LessonProgressResponseDTO.ContentTypeEnum.valueOf(lesson.getContentType().name()))
                .contentLink(lesson.getContentLink() == null ? null : URI.create(lesson.getContentLink()))
                .sequence(lesson.getSequence())
                .userId(lessonView.userId())
                .progressId(lessonView.progressId())
                .lessonStatus(lessonView.progressStatus() == null ? null : LessonProgressResponseDTO.LessonStatusEnum.valueOf(lessonView.progressStatus().name()))
                .isLessonCompleted(lessonView.isLessonCompleted())
                .progressStartedAt(lessonView.progressStartedAt())
                .progressCompletedAt(lessonView.progressCompletedAt())
                .isActive(lesson.getIsActive())
                .createdAt(lesson.getCreatedAt());
    }

    private LessonListResponseDataInnerDTO toListItem(Lesson lesson) {
        return new ModuleResponseLessonsInnerDTO()
                .lessonId(lesson.getId())
                .moduleId(lesson.getModule() == null ? null : lesson.getModule().getId())
                .contentType(lesson.getContentType() == null ? null : ModuleResponseLessonsInnerDTO.ContentTypeEnum.valueOf(lesson.getContentType().name()))
                .contentLink(lesson.getContentLink() == null ? null : URI.create(lesson.getContentLink()))
                .sequence(lesson.getSequence())
                .isActive(lesson.getIsActive())
                .createdAt(lesson.getCreatedAt());
    }

    private LessonListResponseDataInnerDTO toProgressListItem(LessonView lessonView) {
        Lesson lesson = lessonView.lesson();
        return new LessonListResponseDataInnerOneOfDTO()
                .lessonId(lesson.getId())
                .moduleId(lesson.getModule() == null ? null : lesson.getModule().getId())
                .contentType(lesson.getContentType() == null ? null : LessonListResponseDataInnerOneOfDTO.ContentTypeEnum.valueOf(lesson.getContentType().name()))
                .contentLink(lesson.getContentLink() == null ? null : URI.create(lesson.getContentLink()))
                .sequence(lesson.getSequence())
                .isActive(lesson.getIsActive())
                .createdAt(lesson.getCreatedAt())
                .userId(lessonView.userId())
                .progressId(lessonView.progressId())
                .lessonStatus(lessonView.progressStatus() == null ? null : LessonListResponseDataInnerOneOfDTO.LessonStatusEnum.valueOf(lessonView.progressStatus().name()))
                .isLessonCompleted(lessonView.isLessonCompleted())
                .progressStartedAt(lessonView.progressStartedAt())
                .progressCompletedAt(lessonView.progressCompletedAt());
    }
}
