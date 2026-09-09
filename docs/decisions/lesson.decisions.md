## Business Rule

- admin can create a lesson for any module
- instructor can create a lesson for a module which he/she has created
- student cannot create a lesson
- admin can update any lesson
- instructor can update only those lessons which he/she has created
- student cannot update any lesson
- admin can delete any lesson
- instructor can delete only those lessons which he/she has created
- student cannot delete any lesson
- `is_active` flag indicates soft delete for the lesson.
- lesson can be made active or inactive, only when Course is in DRAFT, READY_TO_PUBLISH state.
- lesson cannot be created once the Course is in PUBLISHED or other higher state by instructor as well as admin.
- lesson cannot be edited once the Course is in PUBLISHED or other higher state by instructor as well as admin.
- lesson cannot be deleted once the Course is in PUBLISHED or other higher state by instructor as well as admin.
- lesson is going to be listed by moduleId for student, instructor and admin.
- instructor and admin can see all lessons for a module, irrespective of the `is_active` flag, when Course is in DRAFT state.
- lesson can be created only when the course is in DRAFT, READY_TO_PUBLISH state.
