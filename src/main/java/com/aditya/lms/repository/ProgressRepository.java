package com.aditya.lms.repository;

import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.LessonStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    Optional<Progress> findById(Long id);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    Page<Progress> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    Page<Progress> findByUserIdAndLessonStatus(Long userId, LessonStatus lessonStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    Page<Progress> findByUserIdAndLesson_Module_Id(Long userId, Long moduleId, Pageable pageable);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    Page<Progress> findByUserIdAndLesson_Module_Course_Id(Long userId, Long courseId, Pageable pageable);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    Page<Progress> findByUserIdAndLesson_Module_IdAndLessonStatus(Long userId, Long moduleId, LessonStatus lessonStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    Page<Progress> findByUserIdAndLesson_Module_Course_IdAndLessonStatus(Long userId, Long courseId, LessonStatus lessonStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    List<Progress> findByUserIdAndLesson_Module_Course_Id(Long userId, Long courseId);

    @EntityGraph(attributePaths = {"lesson", "lesson.module", "lesson.module.course"})
    List<Progress> findByUserIdAndLesson_Id(Long userId, Long lessonId);
}
