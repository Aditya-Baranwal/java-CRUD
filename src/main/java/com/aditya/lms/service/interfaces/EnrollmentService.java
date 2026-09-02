package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.enums.CourseCompletionStatus;
import org.springframework.data.domain.Page;

public interface EnrollmentService {

    Enrollment createEnrollment(Enrollment enrollment);

    Enrollment getEnrollment(Long enrollmentId);

    Page<Enrollment> listEnrollments(Long userId, Integer pageNo, Integer pageSize, CourseCompletionStatus status, String sortBy, String sortOrder);

    void cancelEnrollment(Long enrollmentId);
}
