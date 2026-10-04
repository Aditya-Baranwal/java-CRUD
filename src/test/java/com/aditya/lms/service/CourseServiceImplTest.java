package com.aditya.lms.service;

import com.aditya.lms.dto.CourseView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.entity.Progress;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.exception.CourseConflictException;
import com.aditya.lms.exception.CourseNotFoundException;
import com.aditya.lms.exception.CourseValidationException;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.repository.CourseRepository;
import com.aditya.lms.repository.EnrollmentRepository;
import com.aditya.lms.repository.LessonRepository;
import com.aditya.lms.repository.ModuleRepository;
import com.aditya.lms.repository.ProgressRepository;
import com.aditya.lms.testdata.CourseTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Captor
    private ArgumentCaptor<Course> courseCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Course draftCourse;

    @BeforeEach
    void setUp() {
        draftCourse = CourseTestData.draftCourse();
        lenient().when(moduleRepository.findByCourse_IdInAndIsActiveTrue(anyList())).thenReturn(List.of());
    }

    // ── createCourse ────────────────────────────────────────────────────────

    @Nested
    class CreateCourse {

        @Test
        void shouldCreateCourseWithDraftStatusAndCanEnrollmentFalse() {
            Course newCourse = CourseTestData.newUnsavedCourse();
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrue(
                    newCourse.getTitle(), newCourse.getInstructorId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course created = courseService.createCourse(newCourse);

            assertThat(created).isNotNull();
            assertThat(created.getCourseStatus()).isEqualTo(CourseStatus.DRAFT);
            assertThat(created.getCanEnrollment()).isFalse();
            verify(courseRepository).save(courseCaptor.capture());
            assertThat(courseCaptor.getValue().getCourseStatus()).isEqualTo(CourseStatus.DRAFT);
            assertThat(courseCaptor.getValue().getCanEnrollment()).isFalse();
        }

        @Test
        void shouldForceDraftStatusEvenWhenIncomingStatusIsDifferent() {
            Course newCourse = CourseTestData.newUnsavedCourse();
            newCourse.setCourseStatus(CourseStatus.PUBLISHED);
            newCourse.setCanEnrollment(Boolean.TRUE);
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrue(
                    newCourse.getTitle(), newCourse.getInstructorId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course created = courseService.createCourse(newCourse);

            assertThat(created.getCourseStatus()).isEqualTo(CourseStatus.DRAFT);
            assertThat(created.getCanEnrollment()).isFalse();
        }

        @Test
        void shouldThrowWhenPayloadIsNull() {
            assertThatThrownBy(() -> courseService.createCourse(null))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.COURSE_PAYLOAD_REQUIRED.message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenTitleIsBlank() {
            Course course = CourseTestData.newUnsavedCourse();
            course.setTitle("   ");

            assertThatThrownBy(() -> courseService.createCourse(course))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.COURSE_TITLE_MANDATORY.message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenTitleIsNull() {
            Course course = CourseTestData.newUnsavedCourse();
            course.setTitle(null);

            assertThatThrownBy(() -> courseService.createCourse(course))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.COURSE_TITLE_MANDATORY.message());
        }

        @Test
        void shouldThrowWhenInstructorIdIsNull() {
            Course course = CourseTestData.newUnsavedCourse();
            course.setInstructorId(null);

            assertThatThrownBy(() -> courseService.createCourse(course))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.COURSE_INSTRUCTOR_ID_MANDATORY.message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenDuplicateActiveTitleExistsForInstructor() {
            Course course = CourseTestData.newUnsavedCourse();
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrue(
                    course.getTitle(), course.getInstructorId())).thenReturn(true);

            assertThatThrownBy(() -> courseService.createCourse(course))
                    .isInstanceOf(CourseConflictException.class)
                    .hasMessage(ErrorMessages.COURSE_DUPLICATE_TITLE.message());
            verify(courseRepository, never()).save(any());
        }
    }

    // ── getCourse ───────────────────────────────────────────────────────────

    @Nested
    class GetCourse {

        @Test
        void shouldReturnCourseWhenFound() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));

            CourseView result = courseService.getCourse(1L);

            assertThat(result.course()).isEqualTo(draftCourse);
            assertThat(result.totalModuleCount()).isZero();
        }

        @Test
        void shouldThrowNotFoundWhenCourseDoesNotExist() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourse(99L))
                    .isInstanceOf(CourseNotFoundException.class)
                    .hasMessage(ErrorMessages.courseNotFound(99L).message());
        }
    }

    // ── updateCourse ────────────────────────────────────────────────────────

    @Nested
    class UpdateCourse {

        @Test
        void shouldUpdateEditableFieldsOnDraftCourse() {
            Course existing = CourseTestData.draftCourse();
            Course incoming = Course.builder()
                    .title("Updated Title")
                    .description("Updated description")
                    .tags(List.of("updated"))
                    .build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    "Updated Title", existing.getInstructorId(), existing.getId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course updated = courseService.updateCourse(1L, incoming);

            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getDescription()).isEqualTo("Updated description");
            assertThat(updated.getTags()).containsExactly("updated");
        }

        @Test
        void shouldThrowNotFoundWhenCourseDoesNotExist() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(99L, Course.builder().build()))
                    .isInstanceOf(CourseNotFoundException.class);
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenIncomingPayloadIsNull() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));

            assertThatThrownBy(() -> courseService.updateCourse(1L, null))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.COURSE_PAYLOAD_REQUIRED.message());
        }

        @Test
        void shouldThrowConflictWhenInstructorIdIsChanged() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
            Course incoming = Course.builder().instructorId(999L).build();

            assertThatThrownBy(() -> courseService.updateCourse(1L, incoming))
                    .isInstanceOf(CourseConflictException.class)
                    .hasMessage(ErrorMessages.COURSE_INSTRUCTOR_UPDATE.message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldAllowSameInstructorIdOnUpdate() {
            Course existing = CourseTestData.draftCourse();
            Course incoming = Course.builder().instructorId(existing.getInstructorId()).build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    existing.getTitle(), existing.getInstructorId(), existing.getId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course updated = courseService.updateCourse(1L, incoming);

            assertThat(updated.getInstructorId()).isEqualTo(existing.getInstructorId());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenEditingCourseInTerminalState(CourseStatus terminalStatus) {
            Course existing = CourseTestData.courseWithStatus(terminalStatus, Boolean.FALSE);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            Course incoming = Course.builder().title("New Title").build();

            assertThatThrownBy(() -> courseService.updateCourse(1L, incoming))
                    .isInstanceOf(CourseConflictException.class)
                    .hasMessage(ErrorMessages.courseTerminalEdit(terminalStatus).message());
            verify(courseRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"PUBLISHED", "PLANNED_TO_UNPUBLISH", "READY_TO_UNPUBLISH"})
        void shouldThrowValidationWhenEditingRestrictedFieldsInLimitedEditState(CourseStatus limitedStatus) {
            Course existing = CourseTestData.courseWithStatus(limitedStatus, Boolean.FALSE);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            Course incoming = Course.builder().title("New Title").build();

            assertThatThrownBy(() -> courseService.updateCourse(1L, incoming))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.courseLimitedEdit(limitedStatus).message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldAllowCanEnrollmentEditInLimitedEditState() {
            Course existing = CourseTestData.courseWithStatus(CourseStatus.PUBLISHED, Boolean.TRUE);
            Course incoming = Course.builder().canEnrollment(Boolean.FALSE).build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    existing.getTitle(), existing.getInstructorId(), existing.getId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course updated = courseService.updateCourse(1L, incoming);

            assertThat(updated.getCanEnrollment()).isFalse();
        }

        @Test
        void shouldThrowConflictWhenDuplicateTitleExistsForAnotherCourse() {
            Course existing = CourseTestData.draftCourse();
            Course incoming = Course.builder().title("Duplicate Title").build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    "Duplicate Title", existing.getInstructorId(), existing.getId())).thenReturn(true);

            assertThatThrownBy(() -> courseService.updateCourse(1L, incoming))
                    .isInstanceOf(CourseConflictException.class)
                    .hasMessage(ErrorMessages.COURSE_DUPLICATE_TITLE.message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenTransitioningFromDraftToUnpublished() {
            Course existing = CourseTestData.draftCourse();
            Course incoming = Course.builder().courseStatus(CourseStatus.UNPUBLISHED).build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    existing.getTitle(), existing.getInstructorId(), existing.getId())).thenReturn(false);

            assertThatThrownBy(() -> courseService.updateCourse(1L, incoming))
                    .isInstanceOf(CourseConflictException.class)
                    .hasMessage(ErrorMessages.courseInvalidTransition(CourseStatus.DRAFT, CourseStatus.UNPUBLISHED).message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldAllowValidStatusTransitionFromDraftToReadyToPublish() {
            Course existing = CourseTestData.draftCourse();
            Course incoming = Course.builder().courseStatus(CourseStatus.READY_TO_PUBLISH).build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    existing.getTitle(), existing.getInstructorId(), existing.getId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course updated = courseService.updateCourse(1L, incoming);

            assertThat(updated.getCourseStatus()).isEqualTo(CourseStatus.READY_TO_PUBLISH);
        }

        @Test
        void shouldThrowValidationWhenCanEnrollmentTrueOnNonPublishedStatus() {
            Course existing = CourseTestData.draftCourse();
            Course incoming = Course.builder().canEnrollment(Boolean.TRUE).build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    existing.getTitle(), existing.getInstructorId(), existing.getId())).thenReturn(false);

            assertThatThrownBy(() -> courseService.updateCourse(1L, incoming))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.COURSE_CAN_ENROLLMENT_PUBLISHED_ONLY.message());
            verify(courseRepository, never()).save(any());
        }

        @Test
        void shouldAllowCanEnrollmentTrueWhenTransitioningToPublished() {
            Course existing = CourseTestData.courseWithStatus(CourseStatus.READY_TO_PUBLISH, Boolean.FALSE);
            Course incoming = Course.builder()
                    .courseStatus(CourseStatus.PUBLISHED)
                    .canEnrollment(Boolean.TRUE)
                    .build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    existing.getTitle(), existing.getInstructorId(), existing.getId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course updated = courseService.updateCourse(1L, incoming);

            assertThat(updated.getCourseStatus()).isEqualTo(CourseStatus.PUBLISHED);
            assertThat(updated.getCanEnrollment()).isTrue();
        }

        @Test
        void shouldForceCanEnrollmentFalseWhenResultingStatusIsNotPublished() {
            Course existing = CourseTestData.courseWithStatus(CourseStatus.PUBLISHED, Boolean.TRUE);
            Course incoming = Course.builder()
                    .courseStatus(CourseStatus.READY_TO_UNPUBLISH)
                    .canEnrollment(Boolean.FALSE)
                    .build();
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.existsByTitleIgnoreCaseAndInstructorIdAndCanEnrollmentTrueAndIdNot(
                    existing.getTitle(), existing.getInstructorId(), existing.getId())).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Course updated = courseService.updateCourse(1L, incoming);

            assertThat(updated.getCourseStatus()).isEqualTo(CourseStatus.READY_TO_UNPUBLISH);
            assertThat(updated.getCanEnrollment()).isFalse();
        }
    }

    // ── deleteCourse ────────────────────────────────────────────────────────

    @Nested
    class DeleteCourse {

        @Test
        void shouldSoftDeleteCourseBySettingManualUnpublishedAndCanEnrollmentFalse() {
            Course existing = CourseTestData.courseWithStatus(CourseStatus.PUBLISHED, Boolean.TRUE);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            courseService.deleteCourse(1L);

            verify(courseRepository).save(courseCaptor.capture());
            assertThat(courseCaptor.getValue().getCourseStatus()).isEqualTo(CourseStatus.MANUAL_UNPUBLISHED);
            assertThat(courseCaptor.getValue().getCanEnrollment()).isFalse();
        }

        @Test
        void shouldThrowNotFoundWhenCourseDoesNotExist() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.deleteCourse(99L))
                    .isInstanceOf(CourseNotFoundException.class);
            verify(courseRepository, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(value = CourseStatus.class, names = {"UNPUBLISHED", "MANUAL_UNPUBLISHED"})
        void shouldThrowConflictWhenCourseAlreadyInTerminalState(CourseStatus terminalStatus) {
            Course existing = CourseTestData.courseWithStatus(terminalStatus, Boolean.FALSE);
            when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> courseService.deleteCourse(1L))
                    .isInstanceOf(CourseConflictException.class)
                    .hasMessage(ErrorMessages.courseTerminalDelete(terminalStatus).message());
            verify(courseRepository, never()).save(any());
        }
    }

    // ── listCourses ─────────────────────────────────────────────────────────

    @Nested
    class ListCourses {

        @Test
        void shouldListAllCoursesWhenStatusFilterIsNull() {
            Page<Course> page = new PageImpl<>(List.of(draftCourse));
            when(courseRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<CourseView> result = courseService.listCourses(1, 10, null, null, null);

            assertThat(result.getContent()).extracting(CourseView::course).containsExactly(draftCourse);
            verify(courseRepository).findAll(any(Pageable.class));
            verify(courseRepository, never()).findByCourseStatus(any(), any());
        }

        @Test
        void shouldListCoursesFilteredByStatus() {
            Page<Course> page = new PageImpl<>(List.of(draftCourse));
            when(courseRepository.findByCourseStatus(eq(CourseStatus.PUBLISHED), any(Pageable.class))).thenReturn(page);

            Page<CourseView> result = courseService.listCourses(1, 10, CourseStatus.PUBLISHED, null, null);

            assertThat(result.getContent()).extracting(CourseView::course).containsExactly(draftCourse);
            verify(courseRepository).findByCourseStatus(eq(CourseStatus.PUBLISHED), any(Pageable.class));
        }

        @Test
        void shouldApplyDefaultPaginationAndSortingWhenParametersAreNull() {
            when(courseRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            courseService.listCourses(null, null, null, null, null);

            verify(courseRepository).findAll(pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        void shouldCapPageSizeAtOneHundred() {
            when(courseRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            courseService.listCourses(1, 500, null, null, null);

            verify(courseRepository).findAll(pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        void shouldApplyAscendingSortWhenRequested() {
            when(courseRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            courseService.listCourses(2, 20, null, "title", "asc");

            verify(courseRepository).findAll(pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    class ListCoursesForAdmin {

        @Test
        void shouldListAllCoursesWhenStatusFilterIsNull() {
            when(courseRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(draftCourse)));

            Page<CourseView> result = courseService.listCoursesForAdmin(1, 10, null, null, null);

            assertThat(result.getContent()).extracting(CourseView::course).containsExactly(draftCourse);
            verify(courseRepository).findAll(any(Pageable.class));
        }

        @Test
        void shouldListCoursesFilteredByStatus() {
            when(courseRepository.findByCourseStatus(eq(CourseStatus.DRAFT), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftCourse)));

            Page<CourseView> result = courseService.listCoursesForAdmin(1, 10, CourseStatus.DRAFT, null, null);

            assertThat(result.getContent()).extracting(CourseView::course).containsExactly(draftCourse);
        }
    }

    @Nested
    class ListCoursesForInstructor {

        @Test
        void shouldThrowWhenInstructorIdIsNull() {
            assertThatThrownBy(() -> courseService.listCoursesForInstructor(null, 1, 10, null, null, null))
                    .isInstanceOf(CourseValidationException.class)
                    .hasMessage(ErrorMessages.COURSE_INSTRUCTOR_ID_MANDATORY.message());
        }

        @Test
        void shouldListByInstructorIdWhenStatusFilterIsNull() {
            when(courseRepository.findByInstructorId(eq(101L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftCourse)));

            Page<CourseView> result = courseService.listCoursesForInstructor(101L, 1, 10, null, null, null);

            assertThat(result.getContent()).extracting(CourseView::course).containsExactly(draftCourse);
            verify(courseRepository).findByInstructorId(eq(101L), any(Pageable.class));
            verify(courseRepository, never()).findByInstructorIdAndCourseStatus(anyLong(), any(), any());
        }

        @Test
        void shouldListByInstructorIdAndStatusWhenStatusFilterProvided() {
            when(courseRepository.findByInstructorIdAndCourseStatus(eq(101L), eq(CourseStatus.PUBLISHED), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftCourse)));

            Page<CourseView> result = courseService.listCoursesForInstructor(101L, 1, 10, CourseStatus.PUBLISHED, null, null);

            assertThat(result.getContent()).extracting(CourseView::course).containsExactly(draftCourse);
            verify(courseRepository).findByInstructorIdAndCourseStatus(eq(101L), eq(CourseStatus.PUBLISHED), any(Pageable.class));
        }
    }

    @Nested
    class ListCoursesForStudent {

        @Test
        void shouldListOnlyPublishedAndPlannedToUnpublishCourses() {
            when(courseRepository.findByCourseStatusIn(
                    eq(Set.of(CourseStatus.PUBLISHED, CourseStatus.PLANNED_TO_UNPUBLISH)), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(draftCourse)));

            Page<CourseView> result = courseService.listCoursesForStudent(55L, 1, 10, null, null);

            assertThat(result.getContent()).extracting(CourseView::course).containsExactly(draftCourse);
            verify(courseRepository).findByCourseStatusIn(
                    eq(Set.of(CourseStatus.PUBLISHED, CourseStatus.PLANNED_TO_UNPUBLISH)), any(Pageable.class));
        }

        @Test
        void shouldNotFilterByInstructorOrCanEnrollment() {
            when(courseRepository.findByCourseStatusIn(any(Set.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            courseService.listCoursesForStudent(55L, 1, 10, null, null);

            verify(courseRepository, never()).findByInstructorId(anyLong(), any());
            verify(courseRepository, never()).findAll(any(Pageable.class));
        }

        @Nested
        class CourseProgress {

            @Test
            void shouldRequireUserIdWhenGettingCourseWithProgress() {
                assertThatThrownBy(() -> courseService.getCourseWithProgress(1L, null))
                        .isInstanceOf(CourseValidationException.class)
                        .hasMessage(ErrorMessages.COURSE_USER_ID_MANDATORY.message());
            }

            @Test
            void shouldReturnNullProgressFieldsWhenUserNotEnrolled() {
                when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
                when(enrollmentRepository.findByUserIdAndCourse_Id(201L, 1L)).thenReturn(Optional.empty());

                CourseView view = courseService.getCourseWithProgress(1L, 201L);

                assertThat(view.userId()).isEqualTo(201L);
                assertThat(view.isCourseCompleted()).isNull();
                assertThat(view.completedModuleCount()).isNull();
                assertThat(view.totalModuleCount()).isZero();
            }

            @Test
            void shouldReturnCompletionFromEnrollmentStatusAndModuleCounts() {
                when(courseRepository.findById(1L)).thenReturn(Optional.of(draftCourse));
                Enrollment enrollment = Enrollment.builder()
                        .userId(201L)
                        .course(draftCourse)
                        .courseCompletionStatus(CourseCompletionStatus.COMPLETE)
                        .build();
                when(enrollmentRepository.findByUserIdAndCourse_Id(201L, 1L)).thenReturn(Optional.of(enrollment));

                Module activeModule1 = Module.builder().id(10L).course(draftCourse).isActive(Boolean.TRUE).build();
                Module activeModule2 = Module.builder().id(11L).course(draftCourse).isActive(Boolean.TRUE).build();
                when(moduleRepository.findByCourse_IdInAndIsActiveTrue(List.of(1L))).thenReturn(List.of(activeModule1, activeModule2));

                Lesson m1l1 = Lesson.builder().id(100L).module(activeModule1).isActive(Boolean.TRUE).build();
                Lesson m1l2 = Lesson.builder().id(101L).module(activeModule1).isActive(Boolean.TRUE).build();
                Lesson m2l1 = Lesson.builder().id(102L).module(activeModule2).isActive(Boolean.TRUE).build();
                when(lessonRepository.findByModule_Course_Id(1L)).thenReturn(List.of(m1l1, m1l2, m2l1));

                Progress p1 = Progress.builder().lesson(m1l1).lessonStatus(LessonStatus.FINISHED).build();
                Progress p2 = Progress.builder().lesson(m1l2).lessonStatus(LessonStatus.FINISHED).build();
                Progress p3 = Progress.builder().lesson(m2l1).lessonStatus(LessonStatus.STARTED).build();
                when(progressRepository.findByUserIdAndLesson_Module_Course_Id(201L, 1L)).thenReturn(List.of(p1, p2, p3));

                CourseView view = courseService.getCourseWithProgress(1L, 201L);

                assertThat(view.userId()).isEqualTo(201L);
                assertThat(view.isCourseCompleted()).isTrue();
                assertThat(view.totalModuleCount()).isEqualTo(2);
                assertThat(view.completedModuleCount()).isEqualTo(1);
            }
        }
    }
}
