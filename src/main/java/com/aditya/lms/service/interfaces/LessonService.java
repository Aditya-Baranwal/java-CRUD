package com.aditya.lms.service.interfaces;

import com.aditya.lms.dto.LessonView;
import com.aditya.lms.entity.Lesson;
import org.springframework.data.domain.Page;

public interface LessonService {

    Lesson createLessonAsAdmin(Lesson lesson, Long adminId);

    Lesson createLessonAsInstructor(Lesson lesson, Long instructorId);

    Lesson getLesson(Long lessonId);

    LessonView getLessonWithProgress(Long lessonId, Long userId);

    Page<Lesson> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<LessonView> listLessonsWithProgress(Long moduleId, Long userId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<Lesson> listLessonsForInstructor(Long moduleId, Long instructorId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<LessonView> listLessonsForInstructorWithProgress(Long moduleId, Long instructorId, Long userId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<Lesson> listLessonsForStudent(Long moduleId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);

    Page<LessonView> listLessonsForStudentWithProgress(Long moduleId, Long userId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);

    Lesson updateLessonAsAdmin(Long lessonId, Lesson lesson, Long adminId);

    Lesson updateLessonAsInstructor(Long lessonId, Lesson lesson, Long instructorId);

    void deleteLessonAsAdmin(Long lessonId, Long adminId);

    void deleteLessonAsInstructor(Long lessonId, Long instructorId);
}
