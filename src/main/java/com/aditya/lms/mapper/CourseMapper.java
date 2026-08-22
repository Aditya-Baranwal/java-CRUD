package com.aditya.lms.mapper;

import com.aditya.lms.entity.Course;
import com.aditya.lms.enums.CourseStatus;
import com.lms.model.CourseCreateRequestDTO;
import com.lms.model.CourseCreateResponseDTO;
import com.lms.model.CourseDeleteResponseDTO;
import com.lms.model.CourseGetResponseDTO;
import com.lms.model.CourseListResponseDTO;
import com.lms.model.CourseListResponseDataInnerDTO;
import com.lms.model.CourseResponseDTO;
import com.lms.model.CourseUpdateRequestDTO;
import com.lms.model.CourseUpdateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    public Course toEntity(CourseCreateRequestDTO request) {
        Course course = new Course();
        course.setTitle(request.getCourseTitle());
        course.setDescription(request.getCourseDescription());
        course.setTags(request.getCourseTags() == null ? List.of() : request.getCourseTags());
        course.setInstructorId(request.getInstructorId());
        course.setIsActive(Boolean.TRUE);
        course.setCourseStatus(request.getCourseStatus() == null ? CourseStatus.UNPUBLISHED : toDomainStatus(request.getCourseStatus()));
        return course;
    }

    public void applyUpdates(Course course, CourseUpdateRequestDTO request) {
        if (request.getCourseTitle() != null) {
            course.setTitle(request.getCourseTitle());
        }
        if (request.getCourseDescription() != null) {
            course.setDescription(request.getCourseDescription());
        }
        if (request.getCourseTags() != null) {
            course.setTags(request.getCourseTags());
        }
        if (request.getCourseStatus() != null) {
            course.setCourseStatus(toDomainStatus(request.getCourseStatus()));
        }
        if (request.getIsActive() != null) {
            course.setIsActive(request.getIsActive());
        }
    }

    public CourseCreateResponseDTO toCreateResponse(Course course) {
        return new CourseCreateResponseDTO()
                .message("Course created successfully")
                .data(toCourseResponse(course, false))
                .timestamp(OffsetDateTime.now());
    }

    public CourseGetResponseDTO toGetResponse(Course course, boolean includeModules) {
        return new CourseGetResponseDTO()
                .message("Course fetched successfully")
                .data(toCourseResponse(course, includeModules))
                .timestamp(OffsetDateTime.now());
    }

    public CourseUpdateResponseDTO toUpdateResponse(Course course) {
        return new CourseUpdateResponseDTO()
                .message("Course updated successfully")
                .data(toCourseResponse(course, false))
                .timestamp(OffsetDateTime.now());
    }

    public CourseDeleteResponseDTO toDeleteResponse(String message) {
        return new CourseDeleteResponseDTO()
                .message(message)
                .data(Collections.emptyMap())
                .timestamp(OffsetDateTime.now());
    }

    public CourseListResponseDTO toListResponse(Page<Course> page) {
        return new CourseListResponseDTO()
                .message("Courses fetched successfully")
                .data(page.getContent().stream().map(this::toListItemResponse).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    private CourseResponseDTO toCourseResponse(Course course, boolean includeModules) {
        CourseResponseDTO response = new CourseResponseDTO()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseTags(course.getTags() == null ? List.of() : course.getTags())
                .courseStatus(course.getCourseStatus() == null ? null : toApiStatus(course.getCourseStatus()))
                .instructorId(course.getInstructorId())
                .isActive(course.getIsActive())
                .createdAt(course.getCreatedAt());

        if (includeModules) {
            response.setModules(List.of());
        }
        return response;
    }

    private CourseListResponseDataInnerDTO toListItemResponse(Course course) {
        return new CourseListResponseDataInnerDTO()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseTags(course.getTags() == null ? List.of() : course.getTags())
                .courseStatus(course.getCourseStatus() == null ? null : toListApiStatus(course.getCourseStatus()))
                .instructorId(course.getInstructorId())
                .isActive(course.getIsActive())
                .createdAt(course.getCreatedAt());
    }

    private CourseStatus toDomainStatus(CourseCreateRequestDTO.CourseStatusEnum status) {
        if (status == null) {
            return CourseStatus.UNPUBLISHED;
        }
        return CourseStatus.valueOf(status.name());
    }

    private CourseStatus toDomainStatus(CourseUpdateRequestDTO.CourseStatusEnum status) {
        if (status == null) {
            return CourseStatus.UNPUBLISHED;
        }
        return CourseStatus.valueOf(status.name());
    }

    private CourseResponseDTO.CourseStatusEnum toApiStatus(CourseStatus status) {
        if (status == null) {
            return null;
        }
        return CourseResponseDTO.CourseStatusEnum.valueOf(status.name());
    }

    private CourseListResponseDataInnerDTO.CourseStatusEnum toListApiStatus(CourseStatus status) {
        if (status == null) {
            return null;
        }
        return CourseListResponseDataInnerDTO.CourseStatusEnum.valueOf(status.name());
    }
}
