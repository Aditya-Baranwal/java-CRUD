package com.aditya.lms.controller;

import com.lms.api.EnrollmentsApi;
import com.lms.model.EnrollmentCreateRequestDTO;
import com.lms.model.EnrollmentCreateResponseDTO;
import com.lms.model.EnrollmentDeleteResponseDTO;
import com.lms.model.EnrollmentGetResponseDTO;
import com.lms.model.EnrollmentListResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController implements EnrollmentsApi {

    @Override
    public ResponseEntity<EnrollmentDeleteResponseDTO> cancelEnrollment(Long enrollmentId) {
        return null;
    }

    @Override
    public ResponseEntity<EnrollmentCreateResponseDTO> createEnrollment(EnrollmentCreateRequestDTO enrollmentCreateRequestDTO) {
        return null;
    }

    @Override
    public ResponseEntity<EnrollmentGetResponseDTO> getEnrollment(Long enrollmentId) {
        return null;
    }

    @Override
    public ResponseEntity<EnrollmentListResponseDTO> listEnrollments(Long userId, Integer pageNo, Integer pageSize, String courseCompletionStatus, String sortBy, String sortOrder) {
        return null;
    }

}
