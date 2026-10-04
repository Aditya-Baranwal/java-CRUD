## Business Rule

- progress helps to track the lesson completion status for a user enrolled in a course.
- user enrolled in a course can update the lesson status only for lessons belonging to that course.
- user with role 'ADMIN' can update the lesson status for any user enrolled in a course.
- user with role 'INSTRUCTOR' can not update the lesson status for any user enrolled in a course.

## enrollment and progress tracking
- when a user updates the status of a lesson, system should check if the user is enrolled in the course to which the lesson belongs.
- when a user updated the status of a lesson, system should check if user has completed all lessons of the course. If yes, then mark the enrollment as completed for the user.