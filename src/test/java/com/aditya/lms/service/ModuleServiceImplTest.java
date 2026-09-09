package com.aditya.lms.service;

import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.ModuleConflictException;
import com.aditya.lms.exception.ModuleForbiddenException;
import com.aditya.lms.exception.ModuleNotFoundException;
import com.aditya.lms.exception.ModuleValidationException;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.repository.ModuleRepository;
import com.aditya.lms.testdata.ModuleTestData;
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
class ModuleServiceImplTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long INSTRUCTOR_ID = 101L;
    private static final Long OTHER_INSTRUCTOR_ID = 202L;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ModuleServiceImpl moduleService;

    @Captor
    private ArgumentCaptor<Module> moduleCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Course draftCourse;
    private Module draftModule;

    @BeforeEach
    void setUp() {
        draftCourse = ModuleTestData.courseWithStatus(CourseStatus.DRAFT);
        draftModule = ModuleTestData.draftModule();
    }

    // ── createModuleAsAdmin / createModuleAsInstructor ──────────────────────

    @Nested
    class CreateModule {

        @Test
        void shouldCreateModuleAsAdmin() {
            Module newModule = ModuleTestData.newUnsavedModule();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrue(1L, newModule.getSequence()))
                    .thenReturn(false);
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module created = moduleService.createModuleAsAdmin(newModule, ADMIN_ID);

            assertThat(created).isNotNull();
            assertThat(created.getIsActive()).isTrue();
            verify(moduleRepository).save(moduleCaptor.capture());
            assertThat(moduleCaptor.getValue().getCreatedBy()).isEqualTo(ADMIN_ID);
            assertThat(moduleCaptor.getValue().getCourse()).isEqualTo(draftCourse);
        }

        @Test
        void shouldCreateModuleAsInstructorWhenOwnerMatches() {
            Module newModule = ModuleTestData.newUnsavedModule();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrue(1L, newModule.getSequence()))
                    .thenReturn(false);
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module created = moduleService.createModuleAsInstructor(newModule, INSTRUCTOR_ID);

            assertThat(created).isNotNull();
            verify(moduleRepository).save(moduleCaptor.capture());
            assertThat(moduleCaptor.getValue().getCreatedBy()).isEqualTo(INSTRUCTOR_ID);
        }

        @Test
        void shouldThrowForbiddenWhenInstructorDoesNotOwnCourse() {
            Module newModule = ModuleTestData.newUnsavedModule();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));

            assertThatThrownBy(() -> moduleService.createModuleAsInstructor(newModule, OTHER_INSTRUCTOR_ID))
                    .isInstanceOf(ModuleForbiddenException.class)
                    .hasMessage(ErrorMessages.moduleInstructorForbidden(1L).message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenAdminIdIsNull() {
            Module newModule = ModuleTestData.newUnsavedModule();

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, null))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_REQUESTER_ID_MANDATORY.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenPayloadIsNull() {
            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(null, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_PAYLOAD_REQUIRED.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenCourseIdIsMissing() {
            Module newModule = ModuleTestData.newUnsavedModule();
            newModule.setCourse(null);

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_COURSE_ID_MANDATORY.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenTitleIsBlank() {
            Module newModule = ModuleTestData.newUnsavedModule();
            newModule.setTitle("   ");

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_TITLE_MANDATORY.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenSequenceIsNull() {
            Module newModule = ModuleTestData.newUnsavedModule();
            newModule.setSequence(null);

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_SEQUENCE_MANDATORY.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenSequenceIsLessThanOne() {
            Module newModule = ModuleTestData.newUnsavedModule();
            newModule.setSequence(0);

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_SEQUENCE_MANDATORY.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenCourseDoesNotExist() {
            Module newModule = ModuleTestData.newUnsavedModule();
            when(courseRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, ADMIN_ID))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.moduleCourseNotFound(1L).message());
            verify(moduleRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"PUBLISHED", "READY_TO_UNPUBLISH", "PLANNED_TO_UNPUBLISH", "UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenCourseIsNotMutable(CourseStatus status) {
            Module newModule = ModuleTestData.newUnsavedModule();
            Course course = ModuleTestData.courseWithStatus(status);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, ADMIN_ID))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.moduleCourseStateCreateBlocked(status).message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldAllowCreateWhenCourseIsReadyToPublish() {
            Module newModule = ModuleTestData.newUnsavedModule();
            Course course = ModuleTestData.courseWithStatus(CourseStatus.READY_TO_PUBLISH);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrue(1L, newModule.getSequence()))
                    .thenReturn(false);
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module created = moduleService.createModuleAsAdmin(newModule, ADMIN_ID);

            assertThat(created).isNotNull();
        }

        @Test
        void shouldThrowConflictWhenDuplicateActiveSequenceExists() {
            Module newModule = ModuleTestData.newUnsavedModule();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrue(1L, newModule.getSequence()))
                    .thenReturn(true);

            assertThatThrownBy(() -> moduleService.createModuleAsAdmin(newModule, ADMIN_ID))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.MODULE_DUPLICATE_SEQUENCE.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldDefaultIsActiveToTrueWhenNotProvided() {
            Module newModule = ModuleTestData.newUnsavedModule();
            newModule.setIsActive(null);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrue(1L, newModule.getSequence()))
                    .thenReturn(false);
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module created = moduleService.createModuleAsAdmin(newModule, ADMIN_ID);

            assertThat(created.getIsActive()).isTrue();
        }
    }

    // ── getModule ────────────────────────────────────────────────────────────

    @Nested
    class GetModule {

        @Test
        void shouldReturnModuleWhenFound() {
            when(moduleRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(draftModule));

            Module result = moduleService.getModule(1L);

            assertThat(result).isEqualTo(draftModule);
        }

        @Test
        void shouldThrowNotFoundWhenModuleDoesNotExist() {
            when(moduleRepository.findByIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> moduleService.getModule(99L))
                    .isInstanceOf(ModuleNotFoundException.class)
                    .hasMessage(ErrorMessages.moduleNotFound(99L).message());
        }
    }

    // ── listModulesForAdmin / ForInstructor / ForStudent ────────────────────

    @Nested
    class ListModules {

        @Test
        void shouldThrowWhenCourseIdIsNullForAdmin() {
            assertThatThrownBy(() -> moduleService.listModulesForAdmin(null, 1, 10, null, null, null))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_COURSE_ID_MANDATORY.message());
        }

        @Test
        void shouldThrowConflictWhenCourseDoesNotExist() {
            when(courseRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> moduleService.listModulesForAdmin(1L, 1, 10, null, null, null))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.moduleCourseNotFound(1L).message());
        }

        @Test
        void shouldReturnAllModulesRegardlessOfActiveFlagForAdminWhenCourseIsDraft() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.findByCourse_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of(draftModule)));

            Page<Module> result = moduleService.listModulesForAdmin(1L, 1, 10, null, null, null);

            assertThat(result.getContent()).containsExactly(draftModule);
            verify(moduleRepository).findByCourse_Id(eq(1L), any(Pageable.class));
            verify(moduleRepository, never()).findByCourse_IdAndIsActive(anyLong(), any(), any());
        }

        @Test
        void shouldApplyActiveFilterForAdminWhenCourseIsNotDraft() {
            Course publishedCourse = ModuleTestData.courseWithStatus(CourseStatus.PUBLISHED);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
            when(moduleRepository.findByCourse_IdAndIsActive(eq(1L), eq(true), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of(draftModule)));

            Page<Module> result = moduleService.listModulesForAdmin(1L, 1, 10, null, null, null);

            assertThat(result.getContent()).containsExactly(draftModule);
            verify(moduleRepository).findByCourse_IdAndIsActive(eq(1L), eq(true), any(Pageable.class));
        }

        @Test
        void shouldApplyExplicitActiveFilterWhenProvided() {
            Course publishedCourse = ModuleTestData.courseWithStatus(CourseStatus.PUBLISHED);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
            when(moduleRepository.findByCourse_IdAndIsActive(eq(1L), eq(false), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of()));

            moduleService.listModulesForAdmin(1L, 1, 10, Boolean.FALSE, null, null);

            verify(moduleRepository).findByCourse_IdAndIsActive(eq(1L), eq(false), any(Pageable.class));
        }

        @Test
        void shouldThrowWhenInstructorIdIsNullForInstructorListing() {
            assertThatThrownBy(() -> moduleService.listModulesForInstructor(1L, null, 1, 10, null, null, null))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_REQUESTER_ID_MANDATORY.message());
        }

        @Test
        void shouldReturnAllModulesForInstructorWhenCourseIsDraft() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.findByCourse_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of(draftModule)));

            Page<Module> result = moduleService.listModulesForInstructor(1L, INSTRUCTOR_ID, 1, 10, null, null, null);

            assertThat(result.getContent()).containsExactly(draftModule);
        }

        @Test
        void shouldOnlyReturnActiveModulesForStudent() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.findByCourse_IdAndIsActive(eq(1L), eq(true), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of(draftModule)));

            Page<Module> result = moduleService.listModulesForStudent(1L, 1, 10, null, null);

            assertThat(result.getContent()).containsExactly(draftModule);
            verify(moduleRepository).findByCourse_IdAndIsActive(eq(1L), eq(true), any(Pageable.class));
            verify(moduleRepository, never()).findByCourse_Id(anyLong(), any());
        }

        @Test
        void shouldApplyDefaultPaginationAndSortingWhenParametersAreNull() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.findByCourse_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of()));

            moduleService.listModulesForAdmin(1L, null, null, null, null, null);

            verify(moduleRepository).findByCourse_Id(eq(1L), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        void shouldCapPageSizeAtOneHundred() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.findByCourse_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of()));

            moduleService.listModulesForAdmin(1L, 1, 500, null, null, null);

            verify(moduleRepository).findByCourse_Id(eq(1L), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        void shouldApplyAscendingSortWhenRequested() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            when(moduleRepository.findByCourse_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(java.util.List.of()));

            moduleService.listModulesForAdmin(1L, 2, 20, null, "title", "asc");

            verify(moduleRepository).findByCourse_Id(eq(1L), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    // ── updateModuleAsAdmin / updateModuleAsInstructor ──────────────────────

    @Nested
    class UpdateModule {

        @Test
        void shouldUpdateEditableFieldsAsAdmin() {
            Module existing = ModuleTestData.draftModule();
            Module incoming = Module.builder().title("Updated Title").description("Updated description").build();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module updated = moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID);

            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getDescription()).isEqualTo("Updated description");
            verify(moduleRepository).save(moduleCaptor.capture());
            assertThat(moduleCaptor.getValue().getUpdatedBy()).isEqualTo(ADMIN_ID);
        }

        @Test
        void shouldUpdateAsInstructorWhenOwnerMatches() {
            Module existing = ModuleTestData.draftModule();
            Module incoming = Module.builder().title("Updated Title").build();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module updated = moduleService.updateModuleAsInstructor(1L, incoming, INSTRUCTOR_ID);

            assertThat(updated.getTitle()).isEqualTo("Updated Title");
        }

        @Test
        void shouldThrowForbiddenWhenInstructorDoesNotOwnCourse() {
            Module existing = ModuleTestData.draftModule();
            Module incoming = Module.builder().title("Updated Title").build();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> moduleService.updateModuleAsInstructor(1L, incoming, OTHER_INSTRUCTOR_ID))
                    .isInstanceOf(ModuleForbiddenException.class)
                    .hasMessage(ErrorMessages.moduleInstructorForbidden(1L).message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenModuleDoesNotExist() {
            when(moduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(99L, Module.builder().build(), ADMIN_ID))
                    .isInstanceOf(ModuleNotFoundException.class);
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenIncomingPayloadIsNull() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));

            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(1L, null, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_PAYLOAD_REQUIRED.message());
        }

        @Test
        void shouldThrowWhenAdminIdIsNull() {
            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(1L, Module.builder().build(), null))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_REQUESTER_ID_MANDATORY.message());
            verify(moduleRepository, never()).findById(anyLong());
        }

        @Test
        void shouldThrowConflictWhenCourseIdIsChanged() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            Module incoming = Module.builder().course(Course.builder().id(999L).build()).build();

            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.MODULE_COURSE_UPDATE.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldAllowSameCourseIdOnUpdate() {
            Module existing = ModuleTestData.draftModule();
            Module incoming = Module.builder().course(Course.builder().id(existing.getCourse().getId()).build()).build();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module updated = moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID);

            assertThat(updated.getCourse().getId()).isEqualTo(existing.getCourse().getId());
        }

        @Test
        void shouldThrowWhenTitleIsBlank() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            Module incoming = Module.builder().title("   ").build();

            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_TITLE_BLANK.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenSequenceIsLessThanOne() {
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(draftModule));
            Module incoming = Module.builder().sequence(0).build();

            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_SEQUENCE_INVALID.message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenDuplicateSequenceExistsForAnotherModule() {
            Module existing = ModuleTestData.draftModule();
            Module incoming = Module.builder().sequence(2).build();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(moduleRepository.existsByCourse_IdAndSequenceAndIsActiveTrueAndIdNot(
                    existing.getCourse().getId(), 2, existing.getId())).thenReturn(true);

            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.MODULE_DUPLICATE_SEQUENCE.message());
            verify(moduleRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"PUBLISHED", "READY_TO_UNPUBLISH", "PLANNED_TO_UNPUBLISH", "UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenCourseIsNotMutable(CourseStatus status) {
            Module existing = ModuleTestData.moduleWithCourseStatus(status);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            Module incoming = Module.builder().title("New Title").build();

            assertThatThrownBy(() -> moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.moduleCourseStateEditBlocked(status).message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldToggleIsActiveWhenProvided() {
            Module existing = ModuleTestData.draftModule();
            Module incoming = Module.builder().isActive(Boolean.FALSE).build();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Module updated = moduleService.updateModuleAsAdmin(1L, incoming, ADMIN_ID);

            assertThat(updated.getIsActive()).isFalse();
        }
    }

    // ── deleteModuleAsAdmin / deleteModuleAsInstructor ──────────────────────

    @Nested
    class DeleteModule {

        @Test
        void shouldSoftDeleteModuleAsAdmin() {
            Module existing = ModuleTestData.draftModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            moduleService.deleteModuleAsAdmin(1L, ADMIN_ID);

            verify(moduleRepository).save(moduleCaptor.capture());
            assertThat(moduleCaptor.getValue().getIsActive()).isFalse();
            assertThat(moduleCaptor.getValue().getUpdatedBy()).isEqualTo(ADMIN_ID);
        }

        @Test
        void shouldSoftDeleteAsInstructorWhenOwnerMatches() {
            Module existing = ModuleTestData.draftModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> invocation.getArgument(0));

            moduleService.deleteModuleAsInstructor(1L, INSTRUCTOR_ID);

            verify(moduleRepository).save(moduleCaptor.capture());
            assertThat(moduleCaptor.getValue().getIsActive()).isFalse();
        }

        @Test
        void shouldThrowForbiddenWhenInstructorDoesNotOwnCourse() {
            Module existing = ModuleTestData.draftModule();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> moduleService.deleteModuleAsInstructor(1L, OTHER_INSTRUCTOR_ID))
                    .isInstanceOf(ModuleForbiddenException.class)
                    .hasMessage(ErrorMessages.moduleInstructorForbidden(1L).message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenModuleDoesNotExist() {
            when(moduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> moduleService.deleteModuleAsAdmin(99L, ADMIN_ID))
                    .isInstanceOf(ModuleNotFoundException.class);
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldBeIdempotentWhenModuleAlreadyInactive() {
            Module existing = ModuleTestData.defaultModuleBuilder().isActive(Boolean.FALSE).build();
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));

            moduleService.deleteModuleAsAdmin(1L, ADMIN_ID);

            verify(moduleRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"PUBLISHED", "READY_TO_UNPUBLISH", "PLANNED_TO_UNPUBLISH", "UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenCourseIsNotMutable(CourseStatus status) {
            Module existing = ModuleTestData.moduleWithCourseStatus(status);
            when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> moduleService.deleteModuleAsAdmin(1L, ADMIN_ID))
                    .isInstanceOf(ModuleConflictException.class)
                    .hasMessage(ErrorMessages.moduleCourseStateDeleteBlocked(status).message());
            verify(moduleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenAdminIdIsNull() {
            assertThatThrownBy(() -> moduleService.deleteModuleAsAdmin(1L, null))
                    .isInstanceOf(ModuleValidationException.class)
                    .hasMessage(ErrorMessages.MODULE_REQUESTER_ID_MANDATORY.message());
            verify(moduleRepository, never()).findById(anyLong());
        }
    }
}
