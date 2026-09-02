package com.aditya.lms.controller;

import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.mapper.EnrollmentMapper;
import com.aditya.lms.service.interfaces.EnrollmentService;
import com.lms.api.EnrollmentsApi;
import com.lms.model.EnrollmentCreateRequestDTO;
import com.lms.model.EnrollmentCreateResponseDTO;
import com.lms.model.EnrollmentDeleteResponseDTO;
import com.lms.model.EnrollmentGetResponseDTO;
import com.lms.model.EnrollmentListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EnrollmentController implements EnrollmentsApi {

    private final EnrollmentService enrollmentService;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public ResponseEntity<EnrollmentDeleteResponseDTO> cancelEnrollment(Long enrollmentId) {
        enrollmentService.cancelEnrollment(enrollmentId);
        return ResponseEntity.ok(enrollmentMapper.toDeleteResponse("Enrollment cancelled successfully"));
    }

    @Override
    public ResponseEntity<EnrollmentCreateResponseDTO> createEnrollment(EnrollmentCreateRequestDTO enrollmentCreateRequestDTO) {
        Enrollment created = enrollmentService.createEnrollment(enrollmentMapper.toEntity(enrollmentCreateRequestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentMapper.toCreateResponse(created));
    }

    @Override
    public ResponseEntity<EnrollmentGetResponseDTO> getEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentService.getEnrollment(enrollmentId);
        return ResponseEntity.ok(enrollmentMapper.toGetResponse(enrollment));
    }

    @Override
    public ResponseEntity<EnrollmentListResponseDTO> listEnrollments(Long userId, Integer pageNo, Integer pageSize, String courseCompletionStatus, String sortBy, String sortOrder) {
        CourseCompletionStatus status = courseCompletionStatus == null ? null : CourseCompletionStatus.valueOf(courseCompletionStatus);
        return ResponseEntity.ok(enrollmentMapper.toListResponse(enrollmentService.listEnrollments(userId, pageNo, pageSize, status, sortBy, sortOrder)));
    }

}
