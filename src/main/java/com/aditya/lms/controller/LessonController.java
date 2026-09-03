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

    // TODO: replace with the authenticated caller's id once a security layer is introduced;
    // wired to the admin-variant service methods as a stopgap since all roles are not yet distinguishable.
    private static final Long TEMP_REQUESTER_ID = 0L;

    private final LessonService lessonService;
    private final LessonMapper lessonMapper;

    @Override
    public ResponseEntity<LessonCreateResponseDTO> createLesson(LessonCreateRequestDTO lessonCreateRequestDTO) {
        Lesson created = lessonService.createLessonAsAdmin(lessonMapper.toEntity(lessonCreateRequestDTO), TEMP_REQUESTER_ID);
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonMapper.toCreateResponse(created));
    }

    @Override
    public ResponseEntity<LessonDeleteResponseDTO> deleteLesson(Long lessonId) {
        lessonService.deleteLessonAsAdmin(lessonId, TEMP_REQUESTER_ID);
        return ResponseEntity.ok(lessonMapper.toDeleteResponse("Lesson deleted successfully"));
    }

    @Override
    public ResponseEntity<LessonGetResponseDTO> getLesson(Long lessonId) {
        Lesson lesson = lessonService.getLesson(lessonId);
        return ResponseEntity.ok(lessonMapper.toGetResponse(lesson));
    }

    @Override
    public ResponseEntity<LessonListResponseDTO> listLessons(Long moduleId, Integer pageNo, Integer pageSize, Long userId, Boolean active, String sortBy, String sortOrder) {
        return ResponseEntity.ok(lessonMapper.toListResponse(lessonService.listLessonsForAdmin(moduleId, pageNo, pageSize, active, sortBy, sortOrder)));
    }

    @Override
    public ResponseEntity<LessonUpdateResponseDTO> updateLesson(Long lessonId, LessonUpdateRequestDTO lessonUpdateRequestDTO) {
        Lesson lessonToUpdate = new Lesson();
        lessonMapper.applyUpdates(lessonToUpdate, lessonUpdateRequestDTO);
        Lesson updated = lessonService.updateLessonAsAdmin(lessonId, lessonToUpdate, TEMP_REQUESTER_ID);
        return ResponseEntity.ok(lessonMapper.toUpdateResponse(updated));
    }
}
