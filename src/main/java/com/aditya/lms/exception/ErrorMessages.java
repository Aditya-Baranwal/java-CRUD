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

    public static final Error COURSE_USER_ID_MANDATORY = new Error("COURSE_013", "userId is mandatory when progress=true");

    // ── Lesson ──────────────────────────────────────────────────────────────

    public static final Error LESSON_PAYLOAD_REQUIRED       = new Error("LESSON_001", "Lesson payload is required");
    public static final Error LESSON_MODULE_ID_MANDATORY    = new Error("LESSON_002", "moduleId is mandatory");
    public static final Error LESSON_CONTENT_TYPE_MANDATORY = new Error("LESSON_003", "contentType is mandatory");
    public static final Error LESSON_CONTENT_LINK_MANDATORY = new Error("LESSON_004", "contentLink is mandatory");
    public static final Error LESSON_SEQUENCE_MANDATORY     = new Error("LESSON_005", "sequence is mandatory and must be >= 1");
    public static final Error LESSON_REQUESTER_ID_MANDATORY = new Error("LESSON_006", "requesterId is mandatory");
    public static final Error LESSON_MODULE_UPDATE          = new Error("LESSON_007", "Lesson module cannot be changed");
    public static final Error LESSON_CONTENT_LINK_BLANK     = new Error("LESSON_008", "contentLink cannot be blank");
    public static final Error LESSON_SEQUENCE_INVALID       = new Error("LESSON_009", "sequence must be >= 1");
    public static final Error LESSON_DUPLICATE_SEQUENCE     = new Error("LESSON_010", "Active lesson with the same sequence already exists within the module");

    public static Error lessonNotFound(Long lessonId) {
        return new Error("LESSON_404", "Lesson not found for id: " + lessonId);
    }

    public static Error lessonInstructorForbidden(Long courseId) {
        return new Error("LESSON_011", "Instructor does not own the course for id: " + courseId);
    }

    public static Error lessonCourseStateCreateBlocked(Object courseStatus) {
        return new Error("LESSON_012", "Lesson cannot be created while course is in " + courseStatus + " state");
    }

    public static Error lessonCourseStateEditBlocked(Object courseStatus) {
        return new Error("LESSON_013", "Lesson cannot be edited while course is in " + courseStatus + " state");
    }

    public static Error lessonCourseStateDeleteBlocked(Object courseStatus) {
        return new Error("LESSON_014", "Lesson cannot be deleted while course is in " + courseStatus + " state");
    }

    public static final Error LESSON_USER_ID_MANDATORY = new Error("LESSON_015", "userId is mandatory when progress=true");

    // ── Module ──────────────────────────────────────────────────────────────

    public static final Error MODULE_PAYLOAD_REQUIRED       = new Error("MODULE_001", "Module payload is required");
    public static final Error MODULE_COURSE_ID_MANDATORY    = new Error("MODULE_002", "courseId is mandatory");
    public static final Error MODULE_TITLE_MANDATORY        = new Error("MODULE_003", "moduleTitle is mandatory");
    public static final Error MODULE_SEQUENCE_MANDATORY     = new Error("MODULE_004", "sequence is mandatory and must be >= 1");
    public static final Error MODULE_REQUESTER_ID_MANDATORY = new Error("MODULE_005", "requesterId is mandatory");
    public static final Error MODULE_COURSE_UPDATE          = new Error("MODULE_006", "Module course cannot be changed");
    public static final Error MODULE_TITLE_BLANK             = new Error("MODULE_007", "moduleTitle cannot be blank");
    public static final Error MODULE_SEQUENCE_INVALID       = new Error("MODULE_008", "sequence must be >= 1");
    public static final Error MODULE_DUPLICATE_SEQUENCE     = new Error("MODULE_009", "Active module with the same sequence already exists within the course");

    public static Error moduleNotFound(Long moduleId) {
        return new Error("MODULE_404", "Module not found for id: " + moduleId);
    }

    public static Error moduleCourseNotFound(Long courseId) {
        return new Error("MODULE_010", "Course not found for module creation, courseId: " + courseId);
    }

    public static Error moduleInstructorForbidden(Long courseId) {
        return new Error("MODULE_011", "Instructor does not own the course for id: " + courseId);
    }

    public static Error moduleCourseStateCreateBlocked(Object courseStatus) {
        return new Error("MODULE_012", "Module cannot be created while course is in " + courseStatus + " state");
    }

    public static Error moduleCourseStateEditBlocked(Object courseStatus) {
        return new Error("MODULE_013", "Module cannot be edited while course is in " + courseStatus + " state");
    }

    public static Error moduleCourseStateDeleteBlocked(Object courseStatus) {
        return new Error("MODULE_014", "Module cannot be deleted while course is in " + courseStatus + " state");
    }

    public static final Error MODULE_USER_ID_MANDATORY = new Error("MODULE_015", "userId is mandatory when progress=true");

    // ── Enrollment ──────────────────────────────────────────────────────────

    public static final Error ENROLLMENT_PAYLOAD_REQUIRED       = new Error("ENROLLMENT_001", "Enrollment payload is required");
    public static final Error ENROLLMENT_USER_ID_MANDATORY      = new Error("ENROLLMENT_002", "userId is mandatory");
    public static final Error ENROLLMENT_COURSE_ID_MANDATORY    = new Error("ENROLLMENT_003", "courseId is mandatory");
    public static final Error ENROLLMENT_REQUESTER_ID_MANDATORY = new Error("ENROLLMENT_004", "requesterId is mandatory");
    public static final Error ENROLLMENT_DUPLICATE              = new Error("ENROLLMENT_005", "User is already enrolled in this course");
    public static final Error ENROLLMENT_COURSE_CLOSED          = new Error("ENROLLMENT_006", "Course enrollment is disabled");
    public static final Error ENROLLMENT_SELF_ENROLL_ONLY       = new Error("ENROLLMENT_007", "Students may only enroll themselves in a course");

    public static Error enrollmentNotFound(Long enrollmentId) {
        return new Error("ENROLLMENT_404", "Enrollment not found for id: " + enrollmentId);
    }

    public static Error enrollmentCourseNotPublished(Object courseStatus) {
        return new Error("ENROLLMENT_008", "Enrollment is not allowed while course is in " + courseStatus + " state");
    }
}
