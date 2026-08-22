package com.aditya.lms.controller;

import com.lms.api.ProgressApi;
import com.lms.model.ProgressGetResponseDTO;
import com.lms.model.ProgressListResponseDTO;
import com.lms.model.ProgressUpdateRequestDTO;
import com.lms.model.ProgressUpdateResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProgressController implements ProgressApi {

    @Override
    public ResponseEntity<ProgressGetResponseDTO> getProgress(Long progressId) {
        return null;
    }

    @Override
    public ResponseEntity<ProgressListResponseDTO> listProgress(Long userId, Integer pageNo, Integer pageSize, Long courseId, Long moduleId, String lessonStatus, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ResponseEntity<ProgressUpdateResponseDTO> updateProgress(Long progressId, ProgressUpdateRequestDTO progressUpdateRequestDTO) {
        return null;
    }
}
