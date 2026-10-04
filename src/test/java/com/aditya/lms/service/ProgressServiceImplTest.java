package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.ContentType;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.ProgressConflictException;
import com.aditya.lms.exception.ProgressForbiddenException;
import com.aditya.lms.exception.ProgressNotFoundException;
import com.aditya.lms.exception.ProgressValidationException;
import com.aditya.lms.repository.EnrollmentRepository;
import com.aditya.lms.repository.ProgressRepository;
import com.aditya.lms.service.interfaces.EnrollmentService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {

    private static final Long PROGRESS_ID = 10L;
    private static final Long COURSE_ID = 1L;
    private static final Long USER_ID = 201L;
    private static final Long OTHER_USER_ID = 202L;
    private static final Long ADMIN_ID = 1L;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private ProgressServiceImpl progressService;

    @Captor
    private ArgumentCaptor<Progress> progressCaptor;

    @Nested
    class UpdateProgressAsStudent {

        @Test
        void shouldUpdateOwnProgressWhenUserIsEnrolled() {
            Progress existing = existingProgress(USER_ID, LessonStatus.UNSTARTED);
            Progress incoming = Progress.builder().lessonStatus(LessonStatus.STARTED).build();
            when(progressRepository.findById(PROGRESS_ID)).thenReturn(Optional.of(existing));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(USER_ID, COURSE_ID)).thenReturn(true);
            when(progressRepository.save(any(Progress.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Progress updated = progressService.updateProgressAsStudent(PROGRESS_ID, incoming, USER_ID);

            assertThat(updated.getLessonStatus()).isEqualTo(LessonStatus.STARTED);
            assertThat(updated.getStartedAt()).isNotNull();
            verify(enrollmentService).refreshCompletionStatus(USER_ID, COURSE_ID);
        }

        @Test
        void shouldThrowForbiddenWhenStudentUpdatesOtherUserProgress() {
            Progress existing = existingProgress(USER_ID, LessonStatus.UNSTARTED);
            Progress incoming = Progress.builder().lessonStatus(LessonStatus.STARTED).build();
            when(progressRepository.findById(PROGRESS_ID)).thenReturn(Optional.of(existing));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(USER_ID, COURSE_ID)).thenReturn(true);
            when(progressRepository.save(any(Progress.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertThatThrownBy(() -> progressService.updateProgressAsStudent(PROGRESS_ID, incoming, OTHER_USER_ID))
                    .isInstanceOf(ProgressForbiddenException.class)
                    .hasMessage(ErrorMessages.PROGRESS_STUDENT_OWN_ONLY.message());
        }

        @Test
        void shouldThrowConflictWhenProgressUserNotEnrolledInCourse() {
            Progress existing = existingProgress(USER_ID, LessonStatus.UNSTARTED);
            Progress incoming = Progress.builder().lessonStatus(LessonStatus.STARTED).build();
            when(progressRepository.findById(PROGRESS_ID)).thenReturn(Optional.of(existing));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(USER_ID, COURSE_ID)).thenReturn(false);

            assertThatThrownBy(() -> progressService.updateProgressAsStudent(PROGRESS_ID, incoming, USER_ID))
                    .isInstanceOf(ProgressConflictException.class)
                    .hasMessage(ErrorMessages.PROGRESS_USER_NOT_ENROLLED.message());
            verify(progressRepository, never()).save(any());
        }

        @Test
        void shouldThrowValidationWhenStudentIdMissing() {
            assertThatThrownBy(() -> progressService.updateProgressAsStudent(PROGRESS_ID, Progress.builder().build(), null))
                    .isInstanceOf(ProgressValidationException.class)
                    .hasMessage(ErrorMessages.PROGRESS_REQUESTER_ID_MANDATORY.message());
        }
    }

    @Nested
    class UpdateProgressAsAdmin {

        @Test
        void shouldAllowAdminToUpdateAnyUserProgressWhenEnrolled() {
            Progress existing = existingProgress(USER_ID, LessonStatus.STARTED);
            Progress incoming = Progress.builder().lessonStatus(LessonStatus.FINISHED).build();
            when(progressRepository.findById(PROGRESS_ID)).thenReturn(Optional.of(existing));
            when(enrollmentRepository.existsByUserIdAndCourse_Id(USER_ID, COURSE_ID)).thenReturn(true);
            when(progressRepository.save(any(Progress.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Progress updated = progressService.updateProgressAsAdmin(PROGRESS_ID, incoming, ADMIN_ID);

            assertThat(updated.getLessonStatus()).isEqualTo(LessonStatus.FINISHED);
            assertThat(updated.getCompletedAt()).isNotNull();
            verify(progressRepository).save(progressCaptor.capture());
            assertThat(progressCaptor.getValue().getUserId()).isEqualTo(USER_ID);
            verify(enrollmentService).refreshCompletionStatus(USER_ID, COURSE_ID);
        }

        @Test
        void shouldThrowValidationWhenAdminIdMissing() {
            assertThatThrownBy(() -> progressService.updateProgressAsAdmin(PROGRESS_ID, Progress.builder().build(), null))
                    .isInstanceOf(ProgressValidationException.class)
                    .hasMessage(ErrorMessages.PROGRESS_REQUESTER_ID_MANDATORY.message());
        }
    }

    @Nested
    class CommonUpdateValidation {

        @Test
        void shouldThrowNotFoundWhenProgressMissing() {
            when(progressRepository.findById(PROGRESS_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> progressService.updateProgressAsAdmin(PROGRESS_ID, Progress.builder().build(), ADMIN_ID))
                    .isInstanceOf(ProgressNotFoundException.class);
        }

        @Test
        void shouldThrowValidationWhenPayloadMissing() {
            Progress existing = existingProgress(USER_ID, LessonStatus.UNSTARTED);
            when(progressRepository.findById(PROGRESS_ID)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> progressService.updateProgressAsAdmin(PROGRESS_ID, null, ADMIN_ID))
                    .isInstanceOf(ProgressValidationException.class)
                    .hasMessage(ErrorMessages.PROGRESS_PAYLOAD_REQUIRED.message());
            verify(progressRepository, never()).save(any());
        }
    }

    private Progress existingProgress(Long userId, LessonStatus status) {
        Course course = Course.builder().id(COURSE_ID).build();
        Module module = Module.builder().id(20L).course(course).build();
        Lesson lesson = Lesson.builder()
                .id(30L)
                .module(module)
                .contentType(ContentType.MP4)
                .contentLink("https://example.com/video")
                .sequence(1)
                .isActive(Boolean.TRUE)
                .build();
        return Progress.builder()
                .id(PROGRESS_ID)
                .userId(userId)
                .lesson(lesson)
                .lessonStatus(status)
                .build();
    }
}
