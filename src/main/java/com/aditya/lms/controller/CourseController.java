package com.aditya.lms.controller;

import com.lms.api.CoursesApi;
import com.lms.model.CreateCourseRequestDTO;
import com.lms.model.CreateCourseResponseDTO;
import com.lms.model.DeleteCourseResponseDTO;
import com.lms.model.GetCourseResponseDTO;
import com.lms.model.ListCourseResponseDTO;
import com.lms.model.UpdateCourseRequestDTO;
import com.lms.model.UpdateCourseResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CourseController implements CoursesApi {

    @Override
    public ResponseEntity<CreateCourseResponseDTO> createCourse(CreateCourseRequestDTO createCourseRequestDTO) {
        return null;
    }

    @Override
    public ResponseEntity<DeleteCourseResponseDTO> deleteCourse(Long courseId) {
        return null;
    }

    @Override
    public ResponseEntity<GetCourseResponseDTO> getCourse(Long courseId, Boolean includeModules) {
        return null;
    }

    @Override
    public ResponseEntity<ListCourseResponseDTO> listCourses(Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ResponseEntity<UpdateCourseResponseDTO> updateCourse(Long courseId, UpdateCourseRequestDTO updateCourseRequestDTO) {
        return null;
    }

}
