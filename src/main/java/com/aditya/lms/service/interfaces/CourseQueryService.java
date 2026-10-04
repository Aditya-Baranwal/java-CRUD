package com.aditya.lms.service.interfaces;

import com.aditya.lms.dto.CourseView;
import com.aditya.lms.enums.CourseStatus;
import org.springframework.data.domain.Page;

public interface CourseQueryService {

    Page<CourseView> listCoursesForAdmin(Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder);

    Page<CourseView> listCoursesForAdminWithProgress(Long userId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder);

    Page<CourseView> listCoursesForInstructor(Long instructorId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder);

    Page<CourseView> listCoursesForInstructorWithProgress(Long instructorId, Long userId, Integer pageNo, Integer pageSize, CourseStatus courseStatus, String sortBy, String sortOrder);

    Page<CourseView> listCoursesForStudent(Long studentId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);

    Page<CourseView> listCoursesForStudentWithProgress(Long studentId, Long userId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);
}
