package com.aditya.lms.service;

import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.ProgressNotFoundException;
import com.aditya.lms.exception.ProgressValidationException;
import com.aditya.lms.repository.ProgressRepository;
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
            throw new ProgressValidationException("userId is mandatory");
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
    public Progress updateProgress(Long progressId, Progress progress) {
        Progress existing = progressRepository.findById(progressId)
                .orElseThrow(() -> new ProgressNotFoundException(progressId));

        if (progress == null) {
            throw new ProgressValidationException("Progress payload is required");
        }

        if (progress.getLessonStatus() != null) {
            existing.setLessonStatus(progress.getLessonStatus());

            if (progress.getLessonStatus() == LessonStatus.STARTED && existing.getStartedAt() == null) {
                existing.setStartedAt(OffsetDateTime.now());
            }

            if (progress.getLessonStatus() == LessonStatus.FINISHED) {
                if (existing.getStartedAt() == null) {
                    existing.setStartedAt(OffsetDateTime.now());
                }
                if (existing.getCompletedAt() == null) {
                    existing.setCompletedAt(OffsetDateTime.now());
                }
            }

            if (progress.getLessonStatus() == LessonStatus.UNSTARTED) {
                existing.setCompletedAt(null);
            }
        }

        if (progress.getStartedAt() != null) {
            existing.setStartedAt(progress.getStartedAt());
        }

        if (progress.getCompletedAt() != null) {
            existing.setCompletedAt(progress.getCompletedAt());
        }

        Progress updated = progressRepository.save(existing);
        log.info("Progress updated successfully progressId={}, lessonStatus={}", updated.getId(), updated.getLessonStatus());
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
