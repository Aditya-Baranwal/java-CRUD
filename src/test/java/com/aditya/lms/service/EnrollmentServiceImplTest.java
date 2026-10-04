package com.aditya.lms.service;

import com.aditya.lms.dto.EnrollmentView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.CourseNotFoundException;
import com.aditya.lms.exception.EnrollmentConflictException;
import com.aditya.lms.exception.EnrollmentForbiddenException;
import com.aditya.lms.exception.EnrollmentNotFoundException;
import com.aditya.lms.exception.EnrollmentValidationException;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.repository.EnrollmentRepository;
import com.aditya.lms.repository.LessonRepository;
import com.aditya.lms.repository.ProgressRepository;
import com.aditya.lms.testdata.EnrollmentTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    private static final Long STUDENT_ID = 201L;
    private static final Long OTHER_STUDENT_ID = 202L;
    private static final Long ADMIN_ID = 1L;
    private static final Long COURSE_ID = 1L;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Captor
    private ArgumentCaptor<Enrollment> enrollmentCaptor;

    @Captor
    private ArgumentCaptor<List<Progress>> progressListCaptor;

    private Course publishedOpenCourse;

    @BeforeEach
    void setUp() {
        publishedOpenCourse = EnrollmentTestData.courseWithStatus(CourseStatus.PUBLISHED, Boolean.TRUE);
    }

    // ── createEnrollmentAsStudent ────────────────────────────────────────────

    @Nested
    class CreateEnrollmentAsStudent {

        @Test
        void shouldEnrollStudentWhenCoursePublishedAndOpenForEnrollment() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(publishedOpenCourse));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(false);
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(lessonRepository.findByModule_Course_Id(COURSE_ID)).thenReturn(List.of());

            EnrollmentView view = enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID);

            assertThat(view).isNotNull();
            assertThat(view.enrollment().getIsActive()).isTrue();
            assertThat(view.enrollment().getCourseCompletionStatus()).isEqualTo(CourseCompletionStatus.INCOMPLETE);
            assertThat(view.canEnrolledStudentViewCourseContent()).isTrue();
            assertThat(view.courseAccessMessage()).isEqualTo("Course is open for enrollment.");
            verify(enrollmentRepository).save(enrollmentCaptor.capture());
            assertThat(enrollmentCaptor.getValue().getCourse()).isEqualTo(publishedOpenCourse);
        }

        @Test
        void shouldCreateProgressForEveryCourseLessonOnEnroll() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            Lesson lesson1 = EnrollmentTestData.lesson(10L, publishedOpenCourse);
            Lesson lesson2 = EnrollmentTestData.lesson(11L, publishedOpenCourse);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(publishedOpenCourse));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(false);
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(lessonRepository.findByModule_Course_Id(COURSE_ID)).thenReturn(List.of(lesson1, lesson2));

            enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID);

            verify(progressRepository).saveAll(progressListCaptor.capture());
            assertThat(progressListCaptor.getValue()).hasSize(2);
            assertThat(progressListCaptor.getValue())
                    .allSatisfy(progress -> {
                        assertThat(progress.getUserId()).isEqualTo(STUDENT_ID);
                        assertThat(progress.getLessonStatus()).isEqualTo(LessonStatus.UNSTARTED);
                    });
        }

        @Test
        void shouldThrowForbiddenWhenStudentTriesToEnrollSomeoneElse() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(OTHER_STUDENT_ID, COURSE_ID);

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID))
                    .isInstanceOf(EnrollmentForbiddenException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_SELF_ENROLL_ONLY.message());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenCourseNotPublished() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            Course draftCourse = EnrollmentTestData.courseWithStatus(CourseStatus.DRAFT, Boolean.FALSE);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(draftCourse));

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID))
                    .isInstanceOf(EnrollmentConflictException.class)
                    .hasMessage(ErrorMessages.enrollmentCourseNotPublished(CourseStatus.DRAFT).message());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenCourseClosedForEnrollment() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            Course closedCourse = EnrollmentTestData.courseWithStatus(CourseStatus.PUBLISHED, Boolean.FALSE);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(closedCourse));

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID))
                    .isInstanceOf(EnrollmentConflictException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_COURSE_CLOSED.message());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenAlreadyEnrolled() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(publishedOpenCourse));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(true);

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID))
                    .isInstanceOf(EnrollmentConflictException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_DUPLICATE.message());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowValidationWhenPayloadIsNull() {
            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(null, STUDENT_ID))
                    .isInstanceOf(EnrollmentValidationException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_PAYLOAD_REQUIRED.message());
        }

        @Test
        void shouldThrowValidationWhenUserIdMissing() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(null, COURSE_ID);

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID))
                    .isInstanceOf(EnrollmentValidationException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_USER_ID_MANDATORY.message());
        }

        @Test
        void shouldThrowValidationWhenCourseIdMissing() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, null);

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID))
                    .isInstanceOf(EnrollmentValidationException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_COURSE_ID_MANDATORY.message());
        }

        @Test
        void shouldThrowValidationWhenStudentIdMissing() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, null))
                    .isInstanceOf(EnrollmentValidationException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_REQUESTER_ID_MANDATORY.message());
        }

        @Test
        void shouldThrowCourseNotFoundWhenCourseDoesNotExist() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsStudent(newEnrollment, STUDENT_ID))
                    .isInstanceOf(CourseNotFoundException.class);
        }
    }

    // ── createEnrollmentAsAdmin ──────────────────────────────────────────────

    @Nested
    class CreateEnrollmentAsAdmin {

        @Test
        void shouldThrowConflictWhenCourseClosedForEnrollment() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            Course closedCourse = EnrollmentTestData.courseWithStatus(CourseStatus.PUBLISHED, Boolean.FALSE);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(closedCourse));

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsAdmin(newEnrollment, ADMIN_ID))
                    .isInstanceOf(EnrollmentConflictException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_COURSE_CLOSED.message());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void shouldAllowAdminToEnrollAnyStudentOnBehalf() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(OTHER_STUDENT_ID, COURSE_ID);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(publishedOpenCourse));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(OTHER_STUDENT_ID, COURSE_ID)).thenReturn(false);
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(lessonRepository.findByModule_Course_Id(COURSE_ID)).thenReturn(List.of());

            EnrollmentView view = enrollmentService.createEnrollmentAsAdmin(newEnrollment, ADMIN_ID);

            assertThat(view.enrollment().getUserId()).isEqualTo(OTHER_STUDENT_ID);
        }

        @Test
        void shouldAllowAdminToSelfEnrollWhenCoursePublished() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(ADMIN_ID, COURSE_ID);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(publishedOpenCourse));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(ADMIN_ID, COURSE_ID)).thenReturn(false);
            when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(lessonRepository.findByModule_Course_Id(COURSE_ID)).thenReturn(List.of());

            EnrollmentView view = enrollmentService.createEnrollmentAsAdmin(newEnrollment, ADMIN_ID);

            assertThat(view.enrollment().getUserId()).isEqualTo(ADMIN_ID);
            assertThat(view.enrollment().getIsActive()).isTrue();
        }

        @Test
        void shouldThrowConflictWhenCourseNotPublished() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            Course unpublishedCourse = EnrollmentTestData.courseWithStatus(CourseStatus.UNPUBLISHED, Boolean.FALSE);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(unpublishedCourse));

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsAdmin(newEnrollment, ADMIN_ID))
                    .isInstanceOf(EnrollmentConflictException.class)
                    .hasMessage(ErrorMessages.enrollmentCourseNotPublished(CourseStatus.UNPUBLISHED).message());
        }

        @Test
        void shouldThrowValidationWhenAdminIdMissing() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsAdmin(newEnrollment, null))
                    .isInstanceOf(EnrollmentValidationException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_REQUESTER_ID_MANDATORY.message());
        }

        @Test
        void shouldThrowConflictWhenAlreadyEnrolled() {
            Enrollment newEnrollment = EnrollmentTestData.newUnsavedEnrollment(STUDENT_ID, COURSE_ID);
            when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(publishedOpenCourse));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(true);

            assertThatThrownBy(() -> enrollmentService.createEnrollmentAsAdmin(newEnrollment, ADMIN_ID))
                    .isInstanceOf(EnrollmentConflictException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_DUPLICATE.message());
        }
    }

    // ── getEnrollment ─────────────────────────────────────────────────────────

    @Nested
    class GetEnrollment {

        @Test
        void shouldReturnEnrollmentViewWhenFound() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(enrollment));

            EnrollmentView result = enrollmentService.getEnrollment(1L);

            assertThat(result.enrollment()).isEqualTo(enrollment);
            assertThat(result.canEnrolledStudentViewCourseContent()).isTrue();
            assertThat(result.courseAccessMessage()).isEqualTo("Course is open for enrollment.");
        }

        @Test
        void shouldThrowNotFoundWhenMissing() {
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.getEnrollment(1L))
                    .isInstanceOf(EnrollmentNotFoundException.class)
                    .hasMessage(ErrorMessages.enrollmentNotFound(1L).message());
        }
    }

    // ── listEnrollments ───────────────────────────────────────────────────────

    @Nested
    class ListEnrollments {

        @Test
        void shouldListEnrollmentsByUserIdWhenStatusNotProvided() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            Page<Enrollment> page = new PageImpl<>(List.of(enrollment));
            when(enrollmentRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(page);

            Page<EnrollmentView> result = enrollmentService.listEnrollments(STUDENT_ID, 1, 10, null, "enrolledAt", "desc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).enrollment()).isEqualTo(enrollment);
            verify(enrollmentRepository, never()).findByUserIdAndCourseCompletionStatus(anyLong(), any(), any());
        }

        @Test
        void shouldListEnrollmentsByUserIdAndStatusWhenProvided() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            Page<Enrollment> page = new PageImpl<>(List.of(enrollment));
            when(enrollmentRepository.findByUserIdAndCourseCompletionStatus(anyLong(), any(), any(Pageable.class)))
                    .thenReturn(page);

            Page<EnrollmentView> result = enrollmentService.listEnrollments(
                    STUDENT_ID, 1, 10, CourseCompletionStatus.COMPLETE, "enrolledAt", "asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).enrollment()).isEqualTo(enrollment);
        }

        @Test
        void shouldThrowValidationWhenUserIdMissing() {
            assertThatThrownBy(() -> enrollmentService.listEnrollments(null, 1, 10, null, "enrolledAt", "desc"))
                    .isInstanceOf(EnrollmentValidationException.class)
                    .hasMessage(ErrorMessages.ENROLLMENT_USER_ID_MANDATORY.message());
        }
    }

    // ── cancelEnrollment ──────────────────────────────────────────────────────

    @Nested
    class CancelEnrollment {

        @Test
        void shouldDeleteEnrollmentAndItsProgressRecords() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            Progress progress = EnrollmentTestData.progress(STUDENT_ID, EnrollmentTestData.lesson(10L, publishedOpenCourse), LessonStatus.UNSTARTED);
            when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
            when(progressRepository.findByUserIdAndLesson_Module_Course_Id(STUDENT_ID, COURSE_ID))
                    .thenReturn(List.of(progress));

            enrollmentService.cancelEnrollment(1L);

            verify(progressRepository, times(1)).delete(progress);
            verify(enrollmentRepository).delete(enrollment);
        }

        @Test
        void shouldNoOpWhenEnrollmentAlreadyInactive() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setIsActive(Boolean.FALSE);
            when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

            enrollmentService.cancelEnrollment(1L);

            verify(enrollmentRepository, never()).delete(any());
            verify(progressRepository, never()).findByUserIdAndLesson_Module_Course_Id(anyLong(), anyLong());
        }

        @Test
        void shouldThrowNotFoundWhenEnrollmentMissing() {
            when(enrollmentRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.cancelEnrollment(1L))
                    .isInstanceOf(EnrollmentNotFoundException.class);
        }
    }

    // ── refreshCompletionStatus ───────────────────────────────────────────────

    @Nested
    class RefreshCompletionStatus {

        @Test
        void shouldMarkCompleteWhenAllLessonsFinished() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourseCompletionStatus(CourseCompletionStatus.INCOMPLETE);
            Lesson lesson1 = EnrollmentTestData.lesson(10L, publishedOpenCourse);
            Lesson lesson2 = EnrollmentTestData.lesson(11L, publishedOpenCourse);
            when(enrollmentRepository.findByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(Optional.of(enrollment));
            when(progressRepository.findByUserIdAndLesson_Module_Course_Id(STUDENT_ID, COURSE_ID)).thenReturn(List.of(
                    EnrollmentTestData.progress(STUDENT_ID, lesson1, LessonStatus.FINISHED),
                    EnrollmentTestData.progress(STUDENT_ID, lesson2, LessonStatus.FINISHED)
            ));

            enrollmentService.refreshCompletionStatus(STUDENT_ID, COURSE_ID);

            verify(enrollmentRepository).save(enrollmentCaptor.capture());
            assertThat(enrollmentCaptor.getValue().getCourseCompletionStatus()).isEqualTo(CourseCompletionStatus.COMPLETE);
        }

        @Test
        void shouldMarkIncompleteWhenAnyLessonNotFinished() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourseCompletionStatus(CourseCompletionStatus.COMPLETE);
            Lesson lesson1 = EnrollmentTestData.lesson(10L, publishedOpenCourse);
            Lesson lesson2 = EnrollmentTestData.lesson(11L, publishedOpenCourse);
            when(enrollmentRepository.findByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(Optional.of(enrollment));
            when(progressRepository.findByUserIdAndLesson_Module_Course_Id(STUDENT_ID, COURSE_ID)).thenReturn(List.of(
                    EnrollmentTestData.progress(STUDENT_ID, lesson1, LessonStatus.FINISHED),
                    EnrollmentTestData.progress(STUDENT_ID, lesson2, LessonStatus.STARTED)
            ));

            enrollmentService.refreshCompletionStatus(STUDENT_ID, COURSE_ID);

            verify(enrollmentRepository).save(enrollmentCaptor.capture());
            assertThat(enrollmentCaptor.getValue().getCourseCompletionStatus()).isEqualTo(CourseCompletionStatus.INCOMPLETE);
        }

        @Test
        void shouldNotSaveWhenStatusUnchanged() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourseCompletionStatus(CourseCompletionStatus.COMPLETE);
            Lesson lesson1 = EnrollmentTestData.lesson(10L, publishedOpenCourse);
            when(enrollmentRepository.findByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(Optional.of(enrollment));
            when(progressRepository.findByUserIdAndLesson_Module_Course_Id(STUDENT_ID, COURSE_ID)).thenReturn(List.of(
                    EnrollmentTestData.progress(STUDENT_ID, lesson1, LessonStatus.FINISHED)
            ));

            enrollmentService.refreshCompletionStatus(STUDENT_ID, COURSE_ID);

            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void shouldNoOpWhenEnrollmentNotFound() {
            when(enrollmentRepository.findByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(Optional.empty());

            enrollmentService.refreshCompletionStatus(STUDENT_ID, COURSE_ID);

            verify(progressRepository, never()).findByUserIdAndLesson_Module_Course_Id(anyLong(), anyLong());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        void shouldNoOpWhenNoProgressRecordsExist() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            when(enrollmentRepository.findByUserIdAndCourse_Id(STUDENT_ID, COURSE_ID)).thenReturn(Optional.of(enrollment));
            when(progressRepository.findByUserIdAndLesson_Module_Course_Id(STUDENT_ID, COURSE_ID)).thenReturn(List.of());

            enrollmentService.refreshCompletionStatus(STUDENT_ID, COURSE_ID);

            verify(enrollmentRepository, never()).save(any());
        }
    }

    // ── derived fields exposed via EnrollmentView (canEnrolledStudentViewCourseContent / courseAccessMessage) ──

    @Nested
    class DerivedFieldsOnEnrollmentView {

        @Test
        void shouldReturnViewableTrueAndOpenMessageWhenCoursePublishedAndOpen() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourse(publishedOpenCourse);
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(enrollment));

            EnrollmentView view = enrollmentService.getEnrollment(1L);

            assertThat(view.canEnrolledStudentViewCourseContent()).isTrue();
            assertThat(view.courseAccessMessage()).isEqualTo("Course is open for enrollment.");
        }

        @Test
        void shouldReturnViewableFalseWhenCourseNotPublished() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.DRAFT, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(enrollment));

            EnrollmentView view = enrollmentService.getEnrollment(1L);

            assertThat(view.canEnrolledStudentViewCourseContent()).isFalse();
            assertThat(view.courseAccessMessage()).isNull();
        }

        @Test
        void shouldReturnClosedMessageWhenPublishedAndCanEnrollmentFalse() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.PUBLISHED, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(enrollment));

            EnrollmentView view = enrollmentService.getEnrollment(1L);

            assertThat(view.courseAccessMessage()).isEqualTo("Course is closed for enrollment.");
        }

        @Test
        void shouldReturnSoonRemovedMessageForPlannedAndReadyToUnpublish() {
            Enrollment plannedEnrollment = EnrollmentTestData.activeEnrollment();
            plannedEnrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.PLANNED_TO_UNPUBLISH, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(plannedEnrollment));
            assertThat(enrollmentService.getEnrollment(1L).courseAccessMessage()).isEqualTo("Course will be soon removed.");

            Enrollment readyEnrollment = EnrollmentTestData.activeEnrollment();
            readyEnrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.READY_TO_UNPUBLISH, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(2L)).thenReturn(Optional.of(readyEnrollment));
            assertThat(enrollmentService.getEnrollment(2L).courseAccessMessage()).isEqualTo("Course will be soon removed.");
        }

        @Test
        void shouldReturnRemovedByInstructorMessageForUnpublished() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.UNPUBLISHED, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(enrollment));

            assertThat(enrollmentService.getEnrollment(1L).courseAccessMessage()).isEqualTo("Course is removed by instructor.");
        }

        @Test
        void shouldReturnRemovedMessageForManualUnpublished() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.MANUAL_UNPUBLISHED, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(enrollment));

            assertThat(enrollmentService.getEnrollment(1L).courseAccessMessage()).isEqualTo("Course is removed.");
        }

        @Test
        void shouldReturnNullMessageForDraftAndReadyToPublish() {
            Enrollment draftEnrollment = EnrollmentTestData.activeEnrollment();
            draftEnrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.DRAFT, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(draftEnrollment));
            assertThat(enrollmentService.getEnrollment(1L).courseAccessMessage()).isNull();

            Enrollment readyEnrollment = EnrollmentTestData.activeEnrollment();
            readyEnrollment.setCourse(EnrollmentTestData.courseWithStatus(CourseStatus.READY_TO_PUBLISH, Boolean.FALSE));
            when(enrollmentRepository.findByIdAndIsActiveTrue(2L)).thenReturn(Optional.of(readyEnrollment));
            assertThat(enrollmentService.getEnrollment(2L).courseAccessMessage()).isNull();
        }
    }
}
