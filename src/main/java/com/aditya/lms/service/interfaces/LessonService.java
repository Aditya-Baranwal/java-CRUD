package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.Lesson;
import org.springframework.data.domain.Page;

public interface LessonService {

    Lesson createLesson(Lesson lesson);

    Lesson getLesson(Long lessonId);

    Page<Lesson> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Long userId, Boolean active, String sortBy, String sortOrder);

    Lesson updateLesson(Long lessonId, Lesson lesson);

    void deleteLesson(Long lessonId);
}
