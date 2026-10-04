package com.aditya.lms.mapper;

import com.aditya.lms.dto.EnrollmentView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.lms.model.EnrollmentCreateRequestDTO;
import com.lms.model.EnrollmentCreateResponseDTO;
import com.lms.model.EnrollmentDeleteResponseDTO;
import com.lms.model.EnrollmentGetResponseDTO;
import com.lms.model.EnrollmentListResponseDTO;
import com.lms.model.EnrollmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;

@Component
public class EnrollmentMapper {

    public Enrollment toEntity(EnrollmentCreateRequestDTO request) {
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(request.getUserId());
        Course course = new Course();
        course.setId(request.getCourseId());
        enrollment.setCourse(course);
        enrollment.setCourseCompletionStatus(CourseCompletionStatus.INCOMPLETE);
        enrollment.setIsActive(Boolean.TRUE);
        return enrollment;
    }

    public EnrollmentCreateResponseDTO toCreateResponse(EnrollmentView view) {
        return new EnrollmentCreateResponseDTO()
                .message("Enrollment created successfully")
                .data(toResponse(view))
                .timestamp(OffsetDateTime.now());
    }

    public EnrollmentGetResponseDTO toGetResponse(EnrollmentView view) {
        return new EnrollmentGetResponseDTO()
                .message("Enrollment fetched successfully")
                .data(toResponse(view))
                .timestamp(OffsetDateTime.now());
    }

    public EnrollmentListResponseDTO toListResponse(Page<EnrollmentView> page) {
        return new EnrollmentListResponseDTO()
                .message("Enrollments fetched successfully")
                .data(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    public EnrollmentDeleteResponseDTO toDeleteResponse(String message) {
        return new EnrollmentDeleteResponseDTO()
                .message(message)
                .data(Collections.emptyMap())
                .timestamp(OffsetDateTime.now());
    }

    private EnrollmentResponseDTO toResponse(EnrollmentView view) {
        Enrollment enrollment = view.enrollment();
        return new EnrollmentResponseDTO()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourse() == null ? null : enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse() == null ? null : enrollment.getCourse().getTitle())
                .courseCompletionStatus(enrollment.getCourseCompletionStatus() == null ? null : EnrollmentResponseDTO.CourseCompletionStatusEnum.valueOf(enrollment.getCourseCompletionStatus().name()))
                .enrolledAt(enrollment.getEnrolledAt())
                .canEnrolledStudentViewCourseContent(view.canEnrolledStudentViewCourseContent())
                .courseAccessMessage(view.courseAccessMessage());
    }
}
