package com.aditya.lms.controller;

import com.aditya.lms.entity.Course;
import com.aditya.lms.mapper.CourseMapper;
import com.aditya.lms.service.interfaces.CourseService;
import com.lms.api.CoursesApi;
import com.lms.model.CourseCreateRequestDTO;
import com.lms.model.CourseCreateResponseDTO;
import com.lms.model.CourseDeleteResponseDTO;
import com.lms.model.CourseGetResponseDTO;
import com.lms.model.CourseListResponseDTO;
import com.lms.model.CourseUpdateRequestDTO;
import com.lms.model.CourseUpdateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourseController implements CoursesApi {

    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @Override
    public ResponseEntity<CourseCreateResponseDTO> createCourse(CourseCreateRequestDTO courseCreateRequestDTO) {
        Course created = courseService.createCourse(courseMapper.toEntity(courseCreateRequestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(courseMapper.toCreateResponse(created));
    }

    @Override
    public ResponseEntity<CourseDeleteResponseDTO> deleteCourse(Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(courseMapper.toDeleteResponse("Course deleted successfully"));
    }

    @Override
    public ResponseEntity<CourseGetResponseDTO> getCourse(Long courseId, Boolean includeModules) {
        Course course = courseService.getCourse(courseId);
        return ResponseEntity.ok(courseMapper.toGetResponse(course, Boolean.TRUE.equals(includeModules)));
    }

    @Override
    public ResponseEntity<CourseListResponseDTO> listCourses(Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return ResponseEntity.ok(
                courseMapper.toListResponse(courseService.listCourses(pageNo, pageSize, active, sortBy, sortOrder)));
    }

    @Override
    public ResponseEntity<CourseUpdateResponseDTO> updateCourse(Long courseId, CourseUpdateRequestDTO courseUpdateRequestDTO) {
        Course courseToUpdate = new Course();
        courseMapper.applyUpdates(courseToUpdate, courseUpdateRequestDTO);
        Course updated = courseService.updateCourse(courseId, courseToUpdate);
        return ResponseEntity.ok(courseMapper.toUpdateResponse(updated));
    }

}
