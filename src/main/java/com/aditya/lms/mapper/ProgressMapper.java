package com.aditya.lms.mapper;

import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.LessonStatus;
import com.lms.model.ProgressGetResponseDTO;
import com.lms.model.ProgressListResponseDTO;
import com.lms.model.ProgressResponseDTO;
import com.lms.model.ProgressUpdateRequestDTO;
import com.lms.model.ProgressUpdateResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ProgressMapper {

    public Progress toEntity(ProgressUpdateRequestDTO request) {
        Progress progress = new Progress();
        progress.setLessonStatus(request == null ? null : LessonStatus.valueOf(request.getLessonStatus().name()));
        return progress;
    }

    public ProgressGetResponseDTO toGetResponse(Progress progress) {
        return new ProgressGetResponseDTO()
                .message("Progress fetched successfully")
                .data(toResponse(progress))
                .timestamp(OffsetDateTime.now());
    }

    public ProgressUpdateResponseDTO toUpdateResponse(Progress progress) {
        return new ProgressUpdateResponseDTO()
                .message("Progress updated successfully")
                .data(toResponse(progress))
                .timestamp(OffsetDateTime.now());
    }

    public ProgressListResponseDTO toListResponse(Page<Progress> page) {
        return new ProgressListResponseDTO()
                .message("Progress fetched successfully")
                .data(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    private ProgressResponseDTO toResponse(Progress progress) {
        Lesson lesson = progress.getLesson();
        return new ProgressResponseDTO()
                .progressId(progress.getId())
                .lessonId(lesson == null ? null : lesson.getId())
                .lessonTitle(null)
                .userId(progress.getUserId())
                .moduleId(lesson == null || lesson.getModule() == null ? null : lesson.getModule().getId())
                .courseId(lesson == null || lesson.getModule() == null || lesson.getModule().getCourse() == null ? null : lesson.getModule().getCourse().getId())
                .lessonStatus(progress.getLessonStatus() == null ? null : ProgressResponseDTO.LessonStatusEnum.valueOf(progress.getLessonStatus().name()))
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt());
    }
}
