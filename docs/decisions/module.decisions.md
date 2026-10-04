## Business rule 

- admin can create a module for any course
- instructor can create a module for a course which he/she has created
- student cannot create a module
- admin can update any module
- instructor can update only those modules which he/she has created
- student cannot update any module
- admin can delete any module
- instructor can delete only those modules which he/she has created
- student cannot delete any module
- `is_active` flag indicates soft delete for the module.
- module can be made active or inactive, only when course is in DRAFT, READY_TO_PUBLISH state.
- module cannot be created once the course is in PUBLISHED or other higher state by instructor as well as admin.
- module cannot be edited once the course is in PUBLISHED or other higher state by instructor as well as admin.
- module cannot be deleted once the course is in PUBLISHED or other higher state by instructor as well as admin.
- module is going to be listed by courseId for student, instructor and admin.
- instructor and admin can see all modules for a course, irrespective of the `is_active` flag, when course is in DRAFT state.

## tracking module completion
- For student & admin roles 
* if enrolled in the course, then module is considered completed if all lessons of the module are completed.
* while listing modules for a course, add module completion status. If they are enrolled in the course.
* while fetching module details, add module completion status. If they are enrolled in the course.
