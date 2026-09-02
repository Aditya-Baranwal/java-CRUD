package com.aditya.lms.repository;

import com.aditya.lms.entity.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @EntityGraph(attributePaths = "module")
    Optional<Lesson> findByIdAndIsActiveTrue(Long id);

    boolean existsByModule_IdAndSequenceAndIsActiveTrue(Long moduleId, Integer sequence);

    boolean existsByModule_IdAndSequenceAndIsActiveTrueAndIdNot(Long moduleId, Integer sequence, Long id);

    Page<Lesson> findByModule_IdAndIsActive(Long moduleId, Boolean isActive, Pageable pageable);

    Page<Lesson> findByModule_Id(Long moduleId, Pageable pageable);

    List<Lesson> findByModule_Course_Id(Long courseId);
}
