package com.aditya.lms.repository;

import com.aditya.lms.entity.Course;
import com.aditya.lms.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByIdAndIsActiveTrue(Long id);

    boolean existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrue(String title, Long instructorId);

    boolean existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(String title, Long instructorId, Long id);

    Page<Course> findByCourseStatus(CourseStatus courseStatus, Pageable pageable);

    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

    Page<Course> findByInstructorIdAndCourseStatus(Long instructorId, CourseStatus courseStatus, Pageable pageable);

    Page<Course> findByCourseStatusIn(Set<CourseStatus> statuses, Pageable pageable);

}
