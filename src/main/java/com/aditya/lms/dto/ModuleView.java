package com.aditya.lms.dto;

import com.aditya.lms.entity.Module;

/**
 * Service-layer read model wrapping a {@link Module} together with module-completion fields
 * derived for a requested user.
 */
public record ModuleView(
        Module module,
        Long userId,
        Boolean isModuleCompleted,
        Integer completedLessonCount,
        Integer totalLessonCount) {
}
