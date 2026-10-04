package com.aditya.lms.service;

import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.ProgressConflictException;
import com.aditya.lms.exception.ProgressForbiddenException;
import com.aditya.lms.exception.ProgressNotFoundException;
import com.aditya.lms.exception.ProgressValidationException;
import com.aditya.lms.repository.EnrollmentRepository;
import com.aditya.lms.repository.ProgressRepository;
import com.aditya.lms.service.interfaces.EnrollmentService;
import com.aditya.lms.service.interfaces.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    @Override
    @Transactional(readOnly = true)
    public Progress getProgress(Long progressId) {
        return progressRepository.findById(progressId)
                .orElseThrow(() -> new ProgressNotFoundException(progressId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Progress> listProgress(Long userId, Integer pageNo, Integer pageSize,
                                      Long courseId, Long moduleId, LessonStatus lessonStatus,
                                      String sortBy, String sortOrder) {
        if (userId == null) {
            throw new ProgressValidationException(ErrorMessages.PROGRESS_USER_ID_MANDATORY);
        }

        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);

        if (courseId != null && moduleId != null) {
            if (lessonStatus != null) {
                return progressRepository.findByUserIdAndLesson_Module_Course_IdAndLessonStatus(userId, courseId, lessonStatus, pageable);
            }
            return progressRepository.findByUserIdAndLesson_Module_Course_Id(userId, courseId, pageable);
        }

        if (moduleId != null) {
            if (lessonStatus != null) {
                return progressRepository.findByUserIdAndLesson_Module_IdAndLessonStatus(userId, moduleId, lessonStatus, pageable);
            }
            return progressRepository.findByUserIdAndLesson_Module_Id(userId, moduleId, pageable);
        }

        if (courseId != null) {
            if (lessonStatus != null) {
                return progressRepository.findByUserIdAndLesson_Module_Course_IdAndLessonStatus(userId, courseId, lessonStatus, pageable);
            }
            return progressRepository.findByUserIdAndLesson_Module_Course_Id(userId, courseId, pageable);
        }

        if (lessonStatus != null) {
            return progressRepository.findByUserIdAndLessonStatus(userId, lessonStatus, pageable);
        }

        return progressRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public Progress updateProgressAsStudent(Long progressId, Progress progress, Long studentId) {
        if (studentId == null) {
            throw new ProgressValidationException(ErrorMessages.PROGRESS_REQUESTER_ID_MANDATORY);
        }
        Progress updated = updateProgress(progressId, progress);
        if (!studentId.equals(updated.getUserId())) {
            throw new ProgressForbiddenException(ErrorMessages.PROGRESS_STUDENT_OWN_ONLY);
        }
        return updated;
    }

    @Override
    @Transactional
    public Progress updateProgressAsAdmin(Long progressId, Progress progress, Long adminId) {
        if (adminId == null) {
            throw new ProgressValidationException(ErrorMessages.PROGRESS_REQUESTER_ID_MANDATORY);
        }
        return updateProgress(progressId, progress);
    }

    private Progress updateProgress(Long progressId, Progress progress) {
        Progress existing = progressRepository.findById(progressId)
                .orElseThrow(() -> new ProgressNotFoundException(progressId));

        if (progress == null) {
            throw new ProgressValidationException(ErrorMessages.PROGRESS_PAYLOAD_REQUIRED);
        }

        Long courseId = existing.getLesson().getModule().getCourse().getId();
        boolean enrolled = enrollmentRepository.existsByUserIdAndCourse_Id(existing.getUserId(), courseId);
        if (!enrolled) {
            throw new ProgressConflictException(ErrorMessages.PROGRESS_USER_NOT_ENROLLED);
        }

        LessonStatus requestedStatus = progress.getLessonStatus();
        if (requestedStatus != null) {
            existing.setLessonStatus(requestedStatus);

            if (requestedStatus == LessonStatus.STARTED && existing.getStartedAt() == null) {
                existing.setStartedAt(OffsetDateTime.now());
            }

            if (requestedStatus == LessonStatus.FINISHED) {
                if (existing.getStartedAt() == null) {
                    existing.setStartedAt(OffsetDateTime.now());
                }
                if (existing.getCompletedAt() == null) {
                    existing.setCompletedAt(OffsetDateTime.now());
                }
            }

            if (requestedStatus == LessonStatus.UNSTARTED) {
                existing.setCompletedAt(null);
            }
        }

        Progress updated = progressRepository.save(existing);
        log.info("Progress updated successfully progressId={}, lessonStatus={}", updated.getId(), updated.getLessonStatus());
        enrollmentService.refreshCompletionStatus(updated.getUserId(), courseId);
        return updated;
    }

    private Pageable buildPageable(Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        int safePage = pageNo == null || pageNo < 1 ? 0 : pageNo - 1;
        int safeSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));
    }
}
