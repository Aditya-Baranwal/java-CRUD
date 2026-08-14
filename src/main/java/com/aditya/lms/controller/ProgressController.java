package com.aditya.lms.controller;

import com.lms.api.ProgressApi;
import com.lms.model.GetProgressResponseDTO;
import com.lms.model.ListProgressResponseDTO;
import com.lms.model.UpdateProgressRequestDTO;
import com.lms.model.UpdateProgressResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProgressController implements ProgressApi {

    @Override
    public ResponseEntity<GetProgressResponseDTO> getProgress(Long progressId) {
        return null;
    }

    @Override
    public ResponseEntity<ListProgressResponseDTO> listProgress(Long userId, Integer pageNo, Integer pageSize, Long courseId, Long moduleId, String lessonStatus, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ResponseEntity<UpdateProgressResponseDTO> updateProgress(Long progressId, UpdateProgressRequestDTO updateProgressRequestDTO) {
        return null;
    }
}
