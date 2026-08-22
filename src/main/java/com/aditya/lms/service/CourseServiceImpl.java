package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.exception.CourseConflictException;
import com.aditya.lms.exception.CourseNotFoundException;
import com.aditya.lms.exception.CourseValidationException;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.service.interfaces.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Course createCourse(Course course) {
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new CourseValidationException("courseTitle is mandatory");
        }

        if (course.getInstructorId() == null) {
            throw new CourseValidationException("instructorId is mandatory");
        }

        if (course.getIsActive() == null) {
            course.setIsActive(Boolean.TRUE);
        }

        boolean duplicateExists = courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndIsActiveTrue(
                course.getTitle(), course.getInstructorId());
        if (duplicateExists) {
            throw new CourseConflictException("Active course with the same title already exists for this instructor");
        }

        Course created = courseRepository.save(course);
        log.info("Course created successfully courseId={}, instructorId={}", created.getId(), created.getInstructorId());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourse(Long courseId) {
        return courseRepository.findByIdAndIsActiveTrue(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> listCourses(Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        Pageable pageable = buildPageable(pageNo, pageSize, sortBy, sortOrder);
        boolean activeFilter = active == null || active;
        return courseRepository.findByIsActive(activeFilter, pageable);
    }

    @Override
    @Transactional
    public Course updateCourse(Long courseId, Course course) {
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getInstructorId() != null && !course.getInstructorId().equals(existingCourse.getInstructorId())) {
            throw new CourseConflictException("Course instructor cannot be updated");
        }

        String effectiveTitle = course.getTitle() != null ? course.getTitle() : existingCourse.getTitle();

        if (effectiveTitle != null) {
            boolean duplicateExists = courseRepository
                    .existsByTitleIgnoreCaseAndInstructorIdAndIsActiveTrueAndIdNot(
                            effectiveTitle,
                            existingCourse.getInstructorId(),
                            existingCourse.getId()
                    );
            if (duplicateExists) {
                throw new CourseConflictException("Active course with the same title already exists for this instructor");
            }
        }

        if (course.getTitle() != null) {
            existingCourse.setTitle(course.getTitle());
        }
        if (course.getDescription() != null) {
            existingCourse.setDescription(course.getDescription());
        }
        if (course.getTags() != null) {
            existingCourse.setTags(course.getTags());
        }
        if (course.getIsActive() != null) {
            existingCourse.setIsActive(course.getIsActive());
        }
        if (course.getUpdatedBy() != null) {
            existingCourse.setUpdatedBy(course.getUpdatedBy());
        }

        Course updated = courseRepository.save(existingCourse);
        log.info("Course updated successfully courseId={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Course existing = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (Boolean.FALSE.equals(existing.getIsActive())) {
            return;
        }

        existing.setIsActive(Boolean.FALSE);
        courseRepository.save(existing);
        log.info("Course soft deleted courseId={}", courseId);
    }

    private Pageable buildPageable(Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        int safePage = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safeSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage - 1, safeSize, Sort.by(direction, safeSortBy));
    }
}
