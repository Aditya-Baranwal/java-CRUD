package com.aditya.lms.dto;

import com.aditya.lms.entity.Course;

public record CourseView(
        Course course,
        Long userId,
        Boolean isCourseCompleted,
        Integer completedModuleCount,
        Integer totalModuleCount) {
}
