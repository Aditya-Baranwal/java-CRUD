## Business Rule

- student can enroll only itself in any course
- instructor cannot enroll itself or anyone in any course
- admin can enroll itself in any course
- admin can enroll a student in a course.
- instructor cannot enroll any student in a course.
- enrollment can be done in course when course is in PUBLISHED state and `can_enroll` is true for course.
- course enrollment can be marked as complete only when the lessons of each module of course are in `FINISHED` state for that user.
- once course goes into UNPUBLISHED or MANUAL_UNPUBLISHED state, enrolled student cannot access content of course.
- Based  on `course_status` and `can_enroll` flag, to more fields can be added in enrollment related dto as follows

| course_status        | can_enrollment | can_enrolled_student_view_course_content | message                          |
|----------------------|----------------|------------------------------------------|----------------------------------|
| DRAFT                | -              | No                                       | -                                |
| READY_TO_PUBLISH     | -              | No                                       | -                                |
| PUBLISHED            | true           | Yes                                      | Course is open for enrollment.   |
| PUBLISHED            | false          | Yes                                      | Course is closed for enrollment. |
| PLANNED_TO_UNPUBLISH | -              | No                                       | Course will be soon removed.     |
| READY_TO_UNPUBLISH   | -              | No                                       | Course will be soon removed.     |
| UNPUBLISHED          | -              | No                                       | Course is removed by instructor. |
| MANUAL_UNPUBLISHED   | -              | No                                       | Course is removed.               |

## progress creation on enrollment:
- when ever a enrollment in a course is done, progress for all lessons of all modules of the course should be created for that user with `NOT_STARTED` state.

## emrollment status transition rules: