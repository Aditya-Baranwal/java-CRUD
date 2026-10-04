package com.aditya.lms.controller;

import com.aditya.lms.entity.Progress;
import com.aditya.lms.mapper.ProgressMapper;
import com.aditya.lms.service.interfaces.ProgressService;
import com.lms.api.ProgressApi;
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

    // TODO: replace with the authenticated caller's id/role once a security layer exists.
    private static final Long TEMP_REQUESTER_ID = 0L;

    private final ProgressService progressService;
    private final ProgressMapper progressMapper;

    @Override
    public ResponseEntity<ProgressUpdateResponseDTO> updateProgress(Long progressId, ProgressUpdateRequestDTO progressUpdateRequestDTO) {
        Progress progress = progressMapper.toEntity(progressUpdateRequestDTO);
        return ResponseEntity.ok(progressMapper.toUpdateResponse(progressService.updateProgressAsAdmin(progressId, progress, TEMP_REQUESTER_ID)));
    }
}
