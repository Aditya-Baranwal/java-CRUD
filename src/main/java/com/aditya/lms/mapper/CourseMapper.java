package com.aditya.lms.mapper;

import com.aditya.lms.dto.CourseView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.enums.CourseStatus;
import com.lms.model.CourseCreateRequestDTO;
import com.lms.model.CourseCreateResponseDTO;
import com.lms.model.CourseDeleteResponseDTO;
import com.lms.model.CourseGetResponseDTO;
import com.lms.model.CourseListResponseDTO;
import com.lms.model.CourseListResponseDataInnerDTO;
import com.lms.model.CourseListResponseDataInnerOneOfDTO;
import com.lms.model.CourseListResponseDataInnerOneOf1DTO;
import com.lms.model.CourseProgressResponseDTO;
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
        if (request.getCanEnrollment() != null) {
            course.setCanEnrollment(request.getCanEnrollment());
        }
    }

    public CourseCreateResponseDTO toCreateResponse(Course course) {
        return new CourseCreateResponseDTO()
                .message("Course created successfully")
                .data(toCourseResponse(new CourseView(course, null, null, null, 0), false))
                .timestamp(OffsetDateTime.now());
    }

    public CourseGetResponseDTO toGetResponse(CourseView courseView, boolean includeModules) {
        return new CourseGetResponseDTO()
                .message("Course fetched successfully")
                .data(toCourseResponse(courseView, includeModules))
                .timestamp(OffsetDateTime.now());
    }

    public CourseGetResponseDTO toGetProgressResponse(CourseView courseView, boolean includeModules) {
        return new CourseGetResponseDTO()
                .message("Course fetched successfully")
                .data(toProgressResponse(courseView, includeModules))
                .timestamp(OffsetDateTime.now());
    }

    public CourseUpdateResponseDTO toUpdateResponse(Course course) {
        return new CourseUpdateResponseDTO()
                .message("Course updated successfully")
                .data(toCourseResponse(new CourseView(course, null, null, null, 0), false))
                .timestamp(OffsetDateTime.now());
    }

    public CourseDeleteResponseDTO toDeleteResponse(String message) {
        return new CourseDeleteResponseDTO()
                .message(message)
                .data(Collections.emptyMap())
                .timestamp(OffsetDateTime.now());
    }

    public CourseListResponseDTO toListResponse(Page<CourseView> page) {
        return new CourseListResponseDTO()
                .message("Courses fetched successfully")
                .data(page.getContent().stream().map(this::toListItemResponse).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    public CourseListResponseDTO toListProgressResponse(Page<CourseView> page) {
        return new CourseListResponseDTO()
                .message("Courses fetched successfully")
                .data(page.getContent().stream().map(this::toProgressListItemResponse).toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .total(Math.toIntExact(page.getTotalElements()))
                .timestamp(OffsetDateTime.now());
    }

    private CourseResponseDTO toCourseResponse(CourseView courseView, boolean includeModules) {
        Course course = courseView.course();
        CourseResponseDTO response = new CourseResponseDTO()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseTags(course.getTags() == null ? List.of() : course.getTags())
                .courseStatus(course.getCourseStatus() == null ? null : toApiStatus(course.getCourseStatus()))
                .instructorId(course.getInstructorId())
                .canEnrollment(course.getCanEnrollment())
                .totalModuleCount(courseView.totalModuleCount())
                .createdAt(course.getCreatedAt());

        if (includeModules) {
            response.setModules(List.of());
        }
        return response;
    }

    private CourseProgressResponseDTO toProgressResponse(CourseView courseView, boolean includeModules) {
        Course course = courseView.course();
        CourseProgressResponseDTO response = new CourseProgressResponseDTO()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseTags(course.getTags() == null ? List.of() : course.getTags())
                .courseStatus(course.getCourseStatus() == null ? null : CourseProgressResponseDTO.CourseStatusEnum.valueOf(course.getCourseStatus().name()))
                .instructorId(course.getInstructorId())
                .canEnrollment(course.getCanEnrollment())
                .userId(courseView.userId())
                .isCourseCompleted(courseView.isCourseCompleted())
                .completedModuleCount(courseView.completedModuleCount())
                .totalModuleCount(courseView.totalModuleCount())
                .createdAt(course.getCreatedAt());

        if (includeModules) {
            response.setModules(List.of());
        }
        return response;
    }

    private CourseListResponseDataInnerDTO toListItemResponse(CourseView courseView) {
        Course course = courseView.course();
        return new CourseListResponseDataInnerOneOfDTO()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseTags(course.getTags() == null ? List.of() : course.getTags())
                .courseStatus(course.getCourseStatus() == null ? null : CourseListResponseDataInnerOneOfDTO.CourseStatusEnum.valueOf(course.getCourseStatus().name()))
                .instructorId(course.getInstructorId())
                .canEnrollment(course.getCanEnrollment())
                .totalModuleCount(courseView.totalModuleCount())
                .createdAt(course.getCreatedAt());
    }

    private CourseListResponseDataInnerDTO toProgressListItemResponse(CourseView courseView) {
        Course course = courseView.course();
        return new CourseListResponseDataInnerOneOf1DTO()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseTags(course.getTags() == null ? List.of() : course.getTags())
                .courseStatus(course.getCourseStatus() == null ? null : CourseListResponseDataInnerOneOf1DTO.CourseStatusEnum.valueOf(course.getCourseStatus().name()))
                .instructorId(course.getInstructorId())
                .canEnrollment(course.getCanEnrollment())
                .userId(courseView.userId())
                .isCourseCompleted(courseView.isCourseCompleted())
                .completedModuleCount(courseView.completedModuleCount())
                .totalModuleCount(courseView.totalModuleCount())
                .createdAt(course.getCreatedAt());
    }

    private CourseStatus toDomainStatus(CourseUpdateRequestDTO.CourseStatusEnum status) {
        if (status == null) {
            return null;
        }
        return CourseStatus.valueOf(status.name());
    }

    public CourseStatus toDomainStatus(String courseStatus) {
        if (courseStatus == null || courseStatus.isBlank()) {
            return null;
        }
        return CourseStatus.valueOf(courseStatus.toUpperCase());
    }

    private CourseResponseDTO.CourseStatusEnum toApiStatus(CourseStatus status) {
        if (status == null) {
            return null;
        }
        return CourseResponseDTO.CourseStatusEnum.valueOf(status.name());
    }

}
