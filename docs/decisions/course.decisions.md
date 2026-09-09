## Business rule 

- admin and instructor can create a course.
- admin can update any course.
- instructor can update only those courses whin m nbvwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww32mch he/she has created.
- student cannot create or update any course.
- course can be made open or closed for enrollment only in published state.
- `can_enrollment` flag indicates whether students are allowed to enroll in the course.
- student can list courses in published and planned_to_unpublish state
- instructor can only list or view courses which he/she owns
- once course is published, planned_to_unpublish, ready_to_unpublish only course_status and can_enrollment is editable by admin and instructor.
- in unpublished and manual_unpublished state, course is not editable by admin and instructor.

## Course status and can_enrollment flag rules

| course_status        | can_enrollment | role_which_can_access_it                                    | can_student_enroll |
|----------------------|----------------|-------------------------------------------------------------|--------------------|
| DRAFT                | -              | ADMIN, INSTRUCTOR                                           | No                 |
| READY_TO_PUBLISH     | -              | ADMIN, INSTRUCTOR                                           | No                 |
| PUBLISHED            | true           | ADMIN, INSTRUCTOR, STUDENT (ALL)                            | yes                |
| PUBLISHED            | false          | ADMIN, INSTRUCTOR, STUDENT (only student who have enrolled) | No                 |
| PLANNED_TO_UNPUBLISH | -              | ADMIN, INSTRUCTOR, STUDENT (only student who have enrolled) | No                 |
| READY_TO_UNPUBLISH   | -              | ADMIN, INSTRUCTOR                                           | No                 |
| UNPUBLISHED          | -              | ADMIN, INSTRUCTOR                                           | No                 |
| MANUAL_UNPUBLISHED   | -              | ADMIN, INSTRUCTOR                                           | No                 |  

## Course status transition rules:

| current_state        | next_possible_state                                          | is_terminal_state | 
|----------------------|--------------------------------------------------------------|-------------------|
| DRAFT                | READY_TO_PUBLISH, PUBLISHED                                  | No                |
| READY_TO_PUBLISH     | PUBLISHED, DRAFT                                             | No                |
| PUBLISHED            | READY_TO_UNPUBLISH, PLANNED_TO_UNPUBLISH, MANUAL_UNPUBLISHED | No                |
| PLANNED_TO_UNPUBLISH | READY_TO_UNPUBLISH, PUBLISHED, MANUAL_UNPUBLISHED            | No                |
| READY_TO_UNPUBLISH   | UNPUBLISHED, MANUAL_UNPUBLISHED                              | No                |
| UNPUBLISHED          | -                                                            | Yes               |
| MANUAL_UNPUBLISHED   | -                                                            | Yes               |
