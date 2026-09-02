package com.aditya.lms.controller;

import com.aditya.lms.entity.Lesson;
import com.aditya.lms.mapper.LessonMapper;
import com.aditya.lms.service.interfaces.LessonService;
import com.lms.api.LessonsApi;
import com.lms.model.LessonCreateRequestDTO;
import com.lms.model.LessonCreateResponseDTO;
import com.lms.model.LessonDeleteResponseDTO;
import com.lms.model.LessonGetResponseDTO;
import com.lms.model.LessonListResponseDTO;
import com.lms.model.LessonUpdateRequestDTO;
import com.lms.model.LessonUpdateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LessonController implements LessonsApi {

    private final LessonService lessonService;
    private final LessonMapper lessonMapper;

    @Override
    public ResponseEntity<LessonCreateResponseDTO> createLesson(LessonCreateRequestDTO lessonCreateRequestDTO) {
        Lesson created = lessonService.createLesson(lessonMapper.toEntity(lessonCreateRequestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonMapper.toCreateResponse(created));
    }

    @Override
    public ResponseEntity<LessonDeleteResponseDTO> deleteLesson(Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(lessonMapper.toDeleteResponse("Lesson deleted successfully"));
    }

    @Override
    public ResponseEntity<LessonGetResponseDTO> getLesson(Long lessonId) {
        Lesson lesson = lessonService.getLesson(lessonId);
        return ResponseEntity.ok(lessonMapper.toGetResponse(lesson));
    }

    @Override
    public ResponseEntity<LessonListResponseDTO> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Long userId, Boolean active, String sortBy, String sortOrder) {
        return ResponseEntity.ok(lessonMapper.toListResponse(lessonService.listLessons(moduleId, pageNo, pageSize, userId, active, sortBy, sortOrder)));
    }

    @Override
    public ResponseEntity<LessonUpdateResponseDTO> updateLesson(Long lessonId, LessonUpdateRequestDTO lessonUpdateRequestDTO) {
        Lesson lessonToUpdate = new Lesson();
        lessonMapper.applyUpdates(lessonToUpdate, lessonUpdateRequestDTO);
        Lesson updated = lessonService.updateLesson(lessonId, lessonToUpdate);
        return ResponseEntity.ok(lessonMapper.toUpdateResponse(updated));
    }
}
