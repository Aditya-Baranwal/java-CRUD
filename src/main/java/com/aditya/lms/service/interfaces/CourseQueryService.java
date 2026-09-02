package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.Course;
import com.aditya.lms.enums.CourseStatus;
import org.springframework.data.domain.Page;

public interface CourseQueryService {

    Page<Course> listCoursesForAdmin(Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder);

    Page<Course> listCoursesForInstructor(Long instructorId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder);

    Page<Course> listCoursesForStudent(Long studentId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);
}
