package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.ContentType;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.LessonConflictException;
import com.aditya.lms.exception.LessonForbiddenException;
import com.aditya.lms.exception.LessonNotFoundException;
import com.aditya.lms.exception.LessonValidationException;
import com.aditya.lms.exception.ModuleNotFoundException;
import com.aditya.lms.repository.LessonRepository;
import com.aditya.lms.repository.ModuleRepository;
import com.aditya.lms.testdata.LessonTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long INSTRUCTOR_ID = 101L;
    private static final Long OTHER_INSTRUCTOR_ID = 202L;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @Captor
    private ArgumentCaptor<Lesson> lessonCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Module draftModule;
    private Lesson draftLesson;

    @BeforeEach
    void setUp() {
        draftModule = LessonTestData.moduleWithCourseStatus(CourseStatus.DRAFT);
        draftLesson = LessonTestData.draftLesson();
    }

    // ── createLessonAsAdmin / createLessonAsInstructor ──────────────────────

    @Nested
    class CreateLesson {

        @Test
        void shouldCreateLessonAsAdmin() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrue(1L, newLesson.getSequence()))
                    .thenReturn(false);
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson created = lessonService.createLessonAsAdmin(newLesson, ADMIN_ID);

            assertThat(created).isNotNull();
            assertThat(created.getIsActive()).isTrue();
            verify(lessonRepository).save(lessonCaptor.capture());
            assertThat(lessonCaptor.getValue().getCreatedBy()).isEqualTo(ADMIN_ID);
            assertThat(lessonCaptor.getValue().getModule()).isEqualTo(draftModule);
        }

        @Test
        void shouldCreateLessonAsInstructorWhenOwnerMatches() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrue(1L, newLesson.getSequence()))
                    .thenReturn(false);
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson created = lessonService.createLessonAsInstructor(newLesson, INSTRUCTOR_ID);

            assertThat(created).isNotNull();
            verify(lessonRepository).save(lessonCaptor.capture());
            assertThat(lessonCaptor.getValue().getCreatedBy()).isEqualTo(INSTRUCTOR_ID);
        }

        @Test
        void shouldThrowForbiddenWhenInstructorDoesNotOwnCourse() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));

            assertThatThrownBy(() -> lessonService.createLessonAsInstructor(newLesson, OTHER_INSTRUCTOR_ID))
                    .isInstanceOf(LessonForbiddenException.class)
                    .hasMessage(ErrorMessages.lessonInstructorForbidden(1L).message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenAdminIdIsNull() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, null))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_REQUESTER_ID_MANDATORY.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenPayloadIsNull() {
            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(null, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_PAYLOAD_REQUIRED.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenModuleIdIsMissing() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            newLesson.setModule(null);

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_MODULE_ID_MANDATORY.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenContentTypeIsNull() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            newLesson.setContentType(null);

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_CONTENT_TYPE_MANDATORY.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenContentLinkIsBlank() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            newLesson.setContentLink("   ");

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_CONTENT_LINK_MANDATORY.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenContentLinkIsNull() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            newLesson.setContentLink(null);

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_CONTENT_LINK_MANDATORY.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenSequenceIsNull() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            newLesson.setSequence(null);

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_SEQUENCE_MANDATORY.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenSequenceIsLessThanOne() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            newLesson.setSequence(0);

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_SEQUENCE_MANDATORY.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenModuleDoesNotExist() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            when(moduleRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(ModuleNotFoundException.class);
            verify(lessonRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"PUBLISHED", "READY_TO_UNPUBLISH", "PLANNED_TO_UNPUBLISH", "UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenCourseIsNotMutable(CourseStatus status) {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            Module module = LessonTestData.moduleWithCourseStatus(status);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonConflictException.class)
                    .hasMessage(ErrorMessages.lessonCourseStateCreateBlocked(status).message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldAllowCreateWhenCourseIsReadyToPublish() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            Module module = LessonTestData.moduleWithCourseStatus(CourseStatus.READY_TO_PUBLISH);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
            when(lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrue(1L, newLesson.getSequence()))
                    .thenReturn(false);
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson created = lessonService.createLessonAsAdmin(newLesson, ADMIN_ID);

            assertThat(created).isNotNull();
        }

        @Test
        void shouldThrowConflictWhenDuplicateActiveSequenceExists() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrue(1L, newLesson.getSequence()))
                    .thenReturn(true);

            assertThatThrownBy(() -> lessonService.createLessonAsAdmin(newLesson, ADMIN_ID))
                    .isInstanceOf(LessonConflictException.class)
                    .hasMessage(ErrorMessages.LESSON_DUPLICATE_SEQUENCE.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldDefaultIsActiveToTrueWhenNotProvided() {
            Lesson newLesson = LessonTestData.newUnsavedLesson();
            newLesson.setIsActive(null);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrue(1L, newLesson.getSequence()))
                    .thenReturn(false);
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson created = lessonService.createLessonAsAdmin(newLesson, ADMIN_ID);

            assertThat(created.getIsActive()).isTrue();
        }
    }

    // ── getLesson ────────────────────────────────────────────────────────────

    @Nested
    class GetLesson {

        @Test
        void shouldReturnLessonWhenFound() {
            when(lessonRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(draftLesson));

            Lesson result = lessonService.getLesson(1L);

            assertThat(result).isEqualTo(draftLesson);
        }

        @Test
        void shouldThrowNotFoundWhenLessonDoesNotExist() {
            when(lessonRepository.findByIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.getLesson(99L))
                    .isInstanceOf(LessonNotFoundException.class)
                    .hasMessage(ErrorMessages.lessonNotFound(99L).message());
        }
    }

    // ── listLessonsForAdmin / ForInstructor / ForStudent ────────────────────

    @Nested
    class ListLessons {

        @Test
        void shouldThrowWhenModuleIdIsNullForAdmin() {
            assertThatThrownBy(() -> lessonService.listLessonsForAdmin(null, 1, 10, null, null, null))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_MODULE_ID_MANDATORY.message());
        }

        @Test
        void shouldThrowNotFoundWhenModuleDoesNotExist() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.listLessonsForAdmin(1L, 1, 10, null, null, null))
                    .isInstanceOf(ModuleNotFoundException.class);
        }

        @Test
        void shouldReturnAllLessonsRegardlessOfActiveFlagForAdminWhenCourseIsDraft() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.findByModule_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftLesson)));

            Page<Lesson> result = lessonService.listLessonsForAdmin(1L, 1, 10, null, null, null);

            assertThat(result.getContent()).containsExactly(draftLesson);
            verify(lessonRepository).findByModule_Id(eq(1L), any(Pageable.class));
            verify(lessonRepository, never()).findByModule_IdAndIsActive(anyLong(), any(), any());
        }

        @Test
        void shouldApplyActiveFilterForAdminWhenCourseIsNotDraft() {
            Module publishedModule = LessonTestData.moduleWithCourseStatus(CourseStatus.PUBLISHED);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(publishedModule));
            when(lessonRepository.findByModule_IdAndIsActive(eq(1L), eq(true), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftLesson)));

            Page<Lesson> result = lessonService.listLessonsForAdmin(1L, 1, 10, null, null, null);

            assertThat(result.getContent()).containsExactly(draftLesson);
            verify(lessonRepository).findByModule_IdAndIsActive(eq(1L), eq(true), any(Pageable.class));
        }

        @Test
        void shouldApplyExplicitActiveFilterWhenProvided() {
            Module publishedModule = LessonTestData.moduleWithCourseStatus(CourseStatus.PUBLISHED);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(publishedModule));
            when(lessonRepository.findByModule_IdAndIsActive(eq(1L), eq(false), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            lessonService.listLessonsForAdmin(1L, 1, 10, Boolean.FALSE, null, null);

            verify(lessonRepository).findByModule_IdAndIsActive(eq(1L), eq(false), any(Pageable.class));
        }

        @Test
        void shouldThrowWhenInstructorIdIsNullForInstructorListing() {
            assertThatThrownBy(() -> lessonService.listLessonsForInstructor(1L, null, 1, 10, null, null, null))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_REQUESTER_ID_MANDATORY.message());
        }

        @Test
        void shouldReturnAllLessonsForInstructorWhenCourseIsDraft() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.findByModule_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftLesson)));

            Page<Lesson> result = lessonService.listLessonsForInstructor(1L, INSTRUCTOR_ID, 1, 10, null, null, null);

            assertThat(result.getContent()).containsExactly(draftLesson);
        }

        @Test
        void shouldOnlyReturnActiveLessonsForStudent() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.findByModule_IdAndIsActive(eq(1L), eq(true), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftLesson)));

            Page<Lesson> result = lessonService.listLessonsForStudent(1L, 1, 10, null, null);

            assertThat(result.getContent()).containsExactly(draftLesson);
            verify(lessonRepository).findByModule_IdAndIsActive(eq(1L), eq(true), any(Pageable.class));
            verify(lessonRepository, never()).findByModule_Id(anyLong(), any());
        }

        @Test
        void shouldApplyDefaultPaginationAndSortingWhenParametersAreNull() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.findByModule_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            lessonService.listLessonsForAdmin(1L, null, null, null, null, null);

            verify(lessonRepository).findByModule_Id(eq(1L), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        void shouldCapPageSizeAtOneHundred() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.findByModule_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            lessonService.listLessonsForAdmin(1L, 1, 500, null, null, null);

            verify(lessonRepository).findByModule_Id(eq(1L), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        void shouldApplyAscendingSortWhenRequested() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            when(lessonRepository.findByModule_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            lessonService.listLessonsForAdmin(1L, 2, 20, null, "sequence", "asc");

            verify(lessonRepository).findByModule_Id(eq(1L), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getSort().getOrderFor("sequence").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    // ── updateLessonAsAdmin / updateLessonAsInstructor ──────────────────────

    @Nested
    class UpdateLesson {

        @Test
        void shouldUpdateEditableFieldsAsAdmin() {
            Lesson existing = LessonTestData.draftLesson();
            Lesson incoming = Lesson.builder().contentLink("https://example.com/updated.mp4").build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson updated = lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID);

            assertThat(updated.getContentLink()).isEqualTo("https://example.com/updated.mp4");
            verify(lessonRepository).save(lessonCaptor.capture());
            assertThat(lessonCaptor.getValue().getUpdatedBy()).isEqualTo(ADMIN_ID);
        }

        @Test
        void shouldUpdateAsInstructorWhenOwnerMatches() {
            Lesson existing = LessonTestData.draftLesson();
            Lesson incoming = Lesson.builder().contentLink("https://example.com/updated.mp4").build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson updated = lessonService.updateLessonAsInstructor(1L, incoming, INSTRUCTOR_ID);

            assertThat(updated.getContentLink()).isEqualTo("https://example.com/updated.mp4");
        }

        @Test
        void shouldThrowForbiddenWhenInstructorDoesNotOwnCourse() {
            Lesson existing = LessonTestData.draftLesson();
            Lesson incoming = Lesson.builder().contentLink("https://example.com/updated.mp4").build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> lessonService.updateLessonAsInstructor(1L, incoming, OTHER_INSTRUCTOR_ID))
                    .isInstanceOf(LessonForbiddenException.class)
                    .hasMessage(ErrorMessages.lessonInstructorForbidden(1L).message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenLessonDoesNotExist() {
            when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(99L, Lesson.builder().build(), ADMIN_ID))
                    .isInstanceOf(LessonNotFoundException.class);
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenIncomingPayloadIsNull() {
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(draftLesson));

            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(1L, null, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_PAYLOAD_REQUIRED.message());
        }

        @Test
        void shouldThrowWhenAdminIdIsNull() {
            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(1L, Lesson.builder().build(), null))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_REQUESTER_ID_MANDATORY.message());
            verify(lessonRepository, never()).findById(anyLong());
        }

        @Test
        void shouldThrowConflictWhenModuleIdIsChanged() {
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(draftLesson));
            Lesson incoming = Lesson.builder().module(Module.builder().id(999L).build()).build();

            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(LessonConflictException.class)
                    .hasMessage(ErrorMessages.LESSON_MODULE_UPDATE.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldAllowSameModuleIdOnUpdate() {
            Lesson existing = LessonTestData.draftLesson();
            Lesson incoming = Lesson.builder().module(Module.builder().id(existing.getModule().getId()).build()).build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson updated = lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID);

            assertThat(updated.getModule().getId()).isEqualTo(existing.getModule().getId());
        }

        @Test
        void shouldThrowWhenContentLinkIsBlank() {
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(draftLesson));
            Lesson incoming = Lesson.builder().contentLink("   ").build();

            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_CONTENT_LINK_BLANK.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenSequenceIsLessThanOne() {
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(draftLesson));
            Lesson incoming = Lesson.builder().sequence(0).build();

            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_SEQUENCE_INVALID.message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenDuplicateSequenceExistsForAnotherLesson() {
            Lesson existing = LessonTestData.draftLesson();
            Lesson incoming = Lesson.builder().sequence(2).build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.existsByModule_IdAndSequenceAndIsActiveTrueAndIdNot(
                    existing.getModule().getId(), 2, existing.getId())).thenReturn(true);

            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(LessonConflictException.class)
                    .hasMessage(ErrorMessages.LESSON_DUPLICATE_SEQUENCE.message());
            verify(lessonRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"PUBLISHED", "READY_TO_UNPUBLISH", "PLANNED_TO_UNPUBLISH", "UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenCourseIsNotMutable(CourseStatus status) {
            Lesson existing = LessonTestData.lessonWithCourseStatus(status);
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            Lesson incoming = Lesson.builder().contentLink("https://example.com/new.mp4").build();

            assertThatThrownBy(() -> lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(LessonConflictException.class)
                    .hasMessage(ErrorMessages.lessonCourseStateEditBlocked(status).message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldToggleIsActiveWhenProvided() {
            Lesson existing = LessonTestData.draftLesson();
            Lesson incoming = Lesson.builder().isActive(Boolean.FALSE).build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson updated = lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID);

            assertThat(updated.getIsActive()).isFalse();
        }

        @Test
        void shouldUpdateContentTypeWhenProvided() {
            Lesson existing = LessonTestData.draftLesson();
            Lesson incoming = Lesson.builder().contentType(ContentType.PDF).build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Lesson updated = lessonService.updateLessonAsAdmin(1L, incoming, ADMIN_ID);

            assertThat(updated.getContentType()).isEqualTo(ContentType.PDF);
        }
    }

    // ── deleteLessonAsAdmin / deleteLessonAsInstructor ──────────────────────

    @Nested
    class DeleteLesson {

        @Test
        void shouldSoftDeleteLessonAsAdmin() {
            Lesson existing = LessonTestData.draftLesson();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            lessonService.deleteLessonAsAdmin(1L, ADMIN_ID);

            verify(lessonRepository).save(lessonCaptor.capture());
            assertThat(lessonCaptor.getValue().getIsActive()).isFalse();
            assertThat(lessonCaptor.getValue().getUpdatedBy()).isEqualTo(ADMIN_ID);
        }

        @Test
        void shouldSoftDeleteAsInstructorWhenOwnerMatches() {
            Lesson existing = LessonTestData.draftLesson();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

            lessonService.deleteLessonAsInstructor(1L, INSTRUCTOR_ID);

            verify(lessonRepository).save(lessonCaptor.capture());
            assertThat(lessonCaptor.getValue().getIsActive()).isFalse();
        }

        @Test
        void shouldThrowForbiddenWhenInstructorDoesNotOwnCourse() {
            Lesson existing = LessonTestData.draftLesson();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> lessonService.deleteLessonAsInstructor(1L, OTHER_INSTRUCTOR_ID))
                    .isInstanceOf(LessonForbiddenException.class)
                    .hasMessage(ErrorMessages.lessonInstructorForbidden(1L).message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenLessonDoesNotExist() {
            when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lessonService.deleteLessonAsAdmin(99L, ADMIN_ID))
                    .isInstanceOf(LessonNotFoundException.class);
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldBeIdempotentWhenLessonAlreadyInactive() {
            Lesson existing = LessonTestData.defaultLessonBuilder().isActive(Boolean.FALSE).build();
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));

            lessonService.deleteLessonAsAdmin(1L, ADMIN_ID);

            verify(lessonRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"PUBLISHED", "READY_TO_UNPUBLISH", "PLANNED_TO_UNPUBLISH", "UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenCourseIsNotMutable(CourseStatus status) {
            Lesson existing = LessonTestData.lessonWithCourseStatus(status);
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> lessonService.deleteLessonAsAdmin(1L, ADMIN_ID))
                    .isInstanceOf(LessonConflictException.class)
                    .hasMessage(ErrorMessages.lessonCourseStateDeleteBlocked(status).message());
            verify(lessonRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenAdminIdIsNull() {
            assertThatThrownBy(() -> lessonService.deleteLessonAsAdmin(1L, null))
                    .isInstanceOf(LessonValidationException.class)
                    .hasMessage(ErrorMessages.LESSON_REQUESTER_ID_MANDATORY.message());
            verify(lessonRepository, never()).findById(anyLong());
        }
    }
}
