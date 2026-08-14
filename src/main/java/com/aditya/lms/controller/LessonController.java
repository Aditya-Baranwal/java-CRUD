package com.aditya.lms.controller;

import com.lms.api.LessonsApi;
import com.lms.model.CreateLessonRequestDTO;
import com.lms.model.CreateLessonResponseDTO;
import com.lms.model.DeleteLessonResponseDTO;
import com.lms.model.GetLessonResponseDTO;
import com.lms.model.ListLessonResponseDTO;
import com.lms.model.UpdateLessonRequestDTO;
import com.lms.model.UpdateLessonResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LessonController implements LessonsApi {

    @Override
    public ResponseEntity<CreateLessonResponseDTO> createLesson(CreateLessonRequestDTO createLessonRequestDTO) {
        return null;
    }

    @Override
    public ResponseEntity<DeleteLessonResponseDTO> deleteLesson(Long lessonId) {
        return null;
    }

    @Override
    public ResponseEntity<GetLessonResponseDTO> getLesson(Long lessonId) {
        return null;
    }

    @Override
    public ResponseEntity<ListLessonResponseDTO> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Long userId, Boolean active, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ResponseEntity<UpdateLessonResponseDTO> updateLesson(Long lessonId, UpdateLessonRequestDTO updateLessonRequestDTO) {
        return null;
    }
}
