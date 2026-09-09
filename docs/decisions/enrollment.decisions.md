## Business Rule

- student can enroll in a course
- instructor cannot enroll in a course
- admin cannot enroll in a course
- student can enroll in a course only when the course is in PUBLISHED state and `can_enroll` is true for course.
- admin can enroll a student in a course only when the course is in PUBLISHED state.
- instructor cannot enroll a student in a course.
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
| UNPUBLISHED          | -              | No                                       | Course is removed by instructor. |
| MANUAL_UNPUBLISHED   | -              | No                                       | Course is removed.               |