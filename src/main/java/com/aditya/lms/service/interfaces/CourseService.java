package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.Course;
import org.springframework.data.domain.Page;

public interface CourseService {

    Course createCourse(Course course);

    Course getCourse(Long courseId);

    Page<Course> listCourses(Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Course updateCourse(Long courseId, Course course);

    void deleteCourse(Long courseId);
}
