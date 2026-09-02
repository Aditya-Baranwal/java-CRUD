package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.LessonStatus;
import org.springframework.data.domain.Page;

public interface ProgressService {

    Progress getProgress(Long progressId);

    Page<Progress> listProgress(Long userId, Integer pageNo, Integer pageSize, Long courseId, Long moduleId, LessonStatus lessonStatus, String sortBy, String sortOrder);

    Progress updateProgress(Long progressId, Progress progress);
}
