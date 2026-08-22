package com.aditya.lms.controller;

import com.lms.api.LessonsApi;
import com.lms.model.LessonCreateRequestDTO;
import com.lms.model.LessonCreateResponseDTO;
import com.lms.model.LessonDeleteResponseDTO;
import com.lms.model.LessonGetResponseDTO;
import com.lms.model.LessonListResponseDTO;
import com.lms.model.LessonUpdateRequestDTO;
import com.lms.model.LessonUpdateResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LessonController implements LessonsApi {

    @Override
    public ResponseEntity<LessonCreateResponseDTO> createLesson(LessonCreateRequestDTO lessonCreateRequestDTO) {
        return null;
    }

    @Override
    public ResponseEntity<LessonDeleteResponseDTO> deleteLesson(Long lessonId) {
        return null;
    }

    @Override
    public ResponseEntity<LessonGetResponseDTO> getLesson(Long lessonId) {
        return null;
    }

    @Override
    public ResponseEntity<LessonListResponseDTO> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Long userId, Boolean active, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ResponseEntity<LessonUpdateResponseDTO> updateLesson(Long lessonId, LessonUpdateRequestDTO lessonUpdateRequestDTO) {
        return null;
    }
}
