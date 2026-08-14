package com.aditya.lms.controller;

import com.lms.api.EnrollmentsApi;
import com.lms.model.CreateEnrollmentRequestDTO;
import com.lms.model.CreateEnrollmentResponseDTO;
import com.lms.model.DeleteEnrollmentResponseDTO;
import com.lms.model.GetEnrollmentResponseDTO;
import com.lms.model.ListEnrollmentResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController implements EnrollmentsApi {

    @Override
    public ResponseEntity<DeleteEnrollmentResponseDTO> cancelEnrollment(Long enrollmentId) {
        return null;
    }

    @Override
    public ResponseEntity<CreateEnrollmentResponseDTO> createEnrollment(CreateEnrollmentRequestDTO createEnrollmentRequestDTO) {
        return null;
    }

    @Override
    public ResponseEntity<GetEnrollmentResponseDTO> getEnrollment(Long enrollmentId) {
        return null;
    }

    @Override
    public ResponseEntity<ListEnrollmentResponseDTO> listEnrollments(Long userId, Integer pageNo, Integer pageSize, String courseStatus, String sortBy, String sortOrder) {
        return null;
    }

}
