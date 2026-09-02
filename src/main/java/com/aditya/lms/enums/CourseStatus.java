package com.aditya.lms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseStatus {

    DRAFT(false),
    READY_TO_PUBLISH(false),
    PUBLISHED(false),
    PLANNED_TO_UNPUBLISH(false),
    READY_TO_UNPUBLISH(false),
    UNPUBLISHED(true),
    MANUAL_UNPUBLISHED(true);

    private final boolean terminal;
}
