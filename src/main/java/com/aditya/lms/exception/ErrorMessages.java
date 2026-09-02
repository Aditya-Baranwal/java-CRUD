package com.aditya.lms.exception;

public final class ErrorMessages {

    private ErrorMessages() {}

    public record Error(String code, String message) {}

    // ── Course ──────────────────────────────────────────────────────────────

    public static final Error COURSE_PAYLOAD_REQUIRED          = new Error("COURSE_001", "Course payload is required");
    public static final Error COURSE_TITLE_MANDATORY           = new Error("COURSE_002", "courseTitle is mandatory");
    public static final Error COURSE_INSTRUCTOR_ID_MANDATORY   = new Error("COURSE_003", "instructorId is mandatory");
    public static final Error COURSE_INSTRUCTOR_UPDATE         = new Error("COURSE_004", "Course instructor cannot be updated");
    public static final Error COURSE_DUPLICATE_TITLE           = new Error("COURSE_005", "Active course with the same title already exists for this instructor");
    public static final Error COURSE_CREATE_DRAFT_ONLY         = new Error("COURSE_006", "Course can only be created in DRAFT state");
    public static final Error COURSE_CAN_ENROLLMENT_PUBLISHED_ONLY  = new Error("COURSE_007", "can_enrollment can only be true when course_status is PUBLISHED");

    public static Error courseNotFound(Long courseId) {
        return new Error("COURSE_404", "Course not found for id: " + courseId);
    }

    public static Error courseTerminalTransition(Object status) {
        return new Error("COURSE_008", "Course in terminal state " + status + " cannot be transitioned");
    }

    public static Error courseInvalidTransition(Object from, Object to) {
        return new Error("COURSE_009", "Course status transition from " + from + " to " + to + " is not allowed");
    }

    public static Error courseTerminalEdit(Object status) {
        return new Error("COURSE_010", "Course in " + status + " state cannot be edited");
    }

    public static Error courseTerminalDelete(Object status) {
        return new Error("COURSE_011", "Course in " + status + " state cannot be deleted");
    }

    public static Error courseLimitedEdit(Object status) {
        return new Error("COURSE_012", "Only courseStatus and canEnrollment are editable when course is in " + status + " state");
    }
}
