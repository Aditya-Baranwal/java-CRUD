package com.aditya.lms.controller;

import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.mapper.ProgressMapper;
import com.aditya.lms.service.interfaces.ProgressService;
import com.lms.api.ProgressApi;
import com.lms.model.ProgressGetResponseDTO;
import com.lms.model.ProgressListResponseDTO;
import com.lms.model.ProgressUpdateRequestDTO;
import com.lms.model.ProgressUpdateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProgressController implements ProgressApi {

    private final ProgressService progressService;
    private final ProgressMapper progressMapper;

    @Override
    public ResponseEntity<ProgressGetResponseDTO> getProgress(Long progressId) {
        return ResponseEntity.ok(progressMapper.toGetResponse(progressService.getProgress(progressId)));
    }

    @Override
    public ResponseEntity<ProgressListResponseDTO> listProgress(Long userId, Integer pageNo, Integer pageSize, Long courseId, Long moduleId, String lessonStatus, String sortBy, String sortOrder) {
        LessonStatus status = lessonStatus == null ? null : LessonStatus.valueOf(lessonStatus);
        return ResponseEntity.ok(progressMapper.toListResponse(progressService.listProgress(userId, pageNo, pageSize, courseId, moduleId, status, sortBy, sortOrder)));
    }

    @Override
    public ResponseEntity<ProgressUpdateResponseDTO> updateProgress(Long progressId, ProgressUpdateRequestDTO progressUpdateRequestDTO) {
        Progress progress = progressMapper.toEntity(progressUpdateRequestDTO);
        return ResponseEntity.ok(progressMapper.toUpdateResponse(progressService.updateProgress(progressId, progress)));
    }
}
