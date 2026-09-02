package com.aditya.lms.repository;

import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.enums.CourseCompletionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findByIdAndIsActiveTrue(Long id);

    boolean existsByUserIdAndCourse_Id(Long userId, Long courseId);

    @EntityGraph(attributePaths = "course")
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "course")
    Page<Enrollment> findByUserIdAndCourseCompletionStatus(Long userId, CourseCompletionStatus status, Pageable pageable);

    Optional<Enrollment> findByUserIdAndCourse_Id(Long userId, Long courseId);
}
