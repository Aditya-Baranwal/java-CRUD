package com.aditya.lms.repository;

import com.aditya.lms.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByIdAndIsActiveTrue(Long id);

    Page<Course> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

    Page<Course> findByInstructorIdAndIsActive(Long instructorId, Boolean isActive, Pageable pageable);
}
