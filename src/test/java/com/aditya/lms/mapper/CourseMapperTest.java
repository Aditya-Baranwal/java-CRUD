package com.aditya.lms.mapper;

import com.aditya.lms.entity.Course;
import com.aditya.lms.enums.CourseStatus;
import com.aditya.lms.testdata.CourseTestData;
import com.lms.model.CourseCreateRequestDTO;
import com.lms.model.CourseCreateResponseDTO;
import com.lms.model.CourseDeleteResponseDTO;
import com.lms.model.CourseGetResponseDTO;
import com.lms.model.CourseListResponseDTO;
import com.lms.model.CourseListResponseDataInnerDTO;
import com.lms.model.CourseResponseDTO;
import com.lms.model.CourseUpdateRequestDTO;
import com.lms.model.CourseUpdateResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseMapperTest {

    private CourseMapper courseMapper;

    @BeforeEach
    void setUp() {
        courseMapper = new CourseMapper();
    }

    // ── toEntity ────────────────────────────────────────────────────────────

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromCreateRequest() {
            CourseCreateRequestDTO request = new CourseCreateRequestDTO()
                    .courseTitle("Java Spring Boot")
                    .courseDescription("Complete Spring Boot course")
                    .courseTags(List.of("java", "spring"))
                    .instructorId(101L);

            Course course = courseMapper.toEntity(request);

            assertThat(course.getTitle()).isEqualTo("Java Spring Boot");
            assertThat(course.getDescription()).isEqualTo("Complete Spring Boot course");
            assertThat(course.getTags()).containsExactly("java", "spring");
            assertThat(course.getInstructorId()).isEqualTo(101L);
        }

        @Test
        void shouldDefaultTagsToEmptyListWhenNull() {
            CourseCreateRequestDTO request = new CourseCreateRequestDTO()
                    .courseTitle("Java Spring Boot")
                    .instructorId(101L)
                    .courseTags(null);

            Course course = courseMapper.toEntity(request);

            assertThat(course.getTags()).isEmpty();
        }

        @Test
        void shouldMapNullDescriptionAsNull() {
            CourseCreateRequestDTO request = new CourseCreateRequestDTO()
                    .courseTitle("Java Spring Boot")
                    .instructorId(101L);

            Course course = courseMapper.toEntity(request);

            assertThat(course.getDescription()).isNull();
        }
    }

    // ── applyUpdates ────────────────────────────────────────────────────────

    @Nested
    class ApplyUpdates {

        @Test
        void shouldUpdateOnlyProvidedFields() {
            Course course = CourseTestData.draftCourse();
            CourseUpdateRequestDTO request = new CourseUpdateRequestDTO()
                    .courseTitle("New Title");

            courseMapper.applyUpdates(course, request);

            assertThat(course.getTitle()).isEqualTo("New Title");
            assertThat(course.getDescription()).isEqualTo("Complete Spring Boot course");
        }

        @Test
        void shouldNotChangeTitleDescriptionStatusOrCanEnrollmentWhenNotProvided() {
            Course course = CourseTestData.draftCourse();
            CourseUpdateRequestDTO request = new CourseUpdateRequestDTO();

            courseMapper.applyUpdates(course, request);

            assertThat(course.getTitle()).isEqualTo("Java Spring Boot");
            assertThat(course.getDescription()).isEqualTo("Complete Spring Boot course");
            assertThat(course.getCourseStatus()).isEqualTo(CourseStatus.DRAFT);
            assertThat(course.getCanEnrollment()).isFalse();
        }

        @Test
        void shouldMapCourseStatusFromApiEnumToDomainEnum() {
            Course course = CourseTestData.draftCourse();
            CourseUpdateRequestDTO request = new CourseUpdateRequestDTO()
                    .courseStatus(CourseUpdateRequestDTO.CourseStatusEnum.PUBLISHED);

            courseMapper.applyUpdates(course, request);

            assertThat(course.getCourseStatus()).isEqualTo(CourseStatus.PUBLISHED);
        }

        @Test
        void shouldUpdateCanEnrollmentWhenProvided() {
            Course course = CourseTestData.draftCourse();
            CourseUpdateRequestDTO request = new CourseUpdateRequestDTO()
                    .canEnrollment(Boolean.TRUE);

            courseMapper.applyUpdates(course, request);

            assertThat(course.getCanEnrollment()).isTrue();
        }

        @Test
        void shouldUpdateTagsWhenProvided() {
            Course course = CourseTestData.draftCourse();
            CourseUpdateRequestDTO request = new CourseUpdateRequestDTO()
                    .courseTags(List.of("updated-tag"));

            courseMapper.applyUpdates(course, request);

            assertThat(course.getTags()).containsExactly("updated-tag");
        }
    }

    // ── response mapping ────────────────────────────────────────────────────

    @Nested
    class ToCreateResponse {

        @Test
        void shouldMapCourseFieldsIntoCreateResponse() {
            Course course = CourseTestData.draftCourse();

            CourseCreateResponseDTO response = courseMapper.toCreateResponse(course);

            assertThat(response.getMessage()).isEqualTo("Course created successfully");
            assertThat(response.getTimestamp()).isNotNull();
            CourseResponseDTO data = response.getData();
            assertThat(data.getCourseId()).isEqualTo(course.getId());
            assertThat(data.getCourseTitle()).isEqualTo(course.getTitle());
            assertThat(data.getCourseDescription()).isEqualTo(course.getDescription());
            assertThat(data.getCourseTags()).containsExactlyElementsOf(course.getTags());
            assertThat(data.getCourseStatus()).isEqualTo(CourseResponseDTO.CourseStatusEnum.DRAFT);
            assertThat(data.getInstructorId()).isEqualTo(course.getInstructorId());
            assertThat(data.getCanEnrollment()).isEqualTo(course.getCanEnrollment());
            assertThat(data.getCreatedAt()).isEqualTo(course.getCreatedAt());
            assertThat(data.getModules()).isEmpty();
        }
    }

    @Nested
    class ToGetResponse {

        @Test
        void shouldLeaveModulesEmptyWhenIncludeModulesIsFalse() {
            Course course = CourseTestData.draftCourse();

            CourseGetResponseDTO response = courseMapper.toGetResponse(course, false);

            assertThat(response.getMessage()).isEqualTo("Course fetched successfully");
            assertThat(response.getData().getModules()).isEmpty();
        }

        @Test
        void shouldIncludeEmptyModulesListWhenIncludeModulesIsTrue() {
            Course course = CourseTestData.draftCourse();

            CourseGetResponseDTO response = courseMapper.toGetResponse(course, true);

            assertThat(response.getData().getModules()).isEmpty();
        }

        @Test
        void shouldMapNullCourseStatusAsNull() {
            Course course = CourseTestData.draftCourse();
            course.setCourseStatus(null);

            CourseGetResponseDTO response = courseMapper.toGetResponse(course, false);

            assertThat(response.getData().getCourseStatus()).isNull();
        }

        @Test
        void shouldDefaultTagsToEmptyListWhenCourseTagsAreNull() {
            Course course = CourseTestData.draftCourse();
            course.setTags(null);

            CourseGetResponseDTO response = courseMapper.toGetResponse(course, false);

            assertThat(response.getData().getCourseTags()).isEmpty();
        }
    }

    @Nested
    class ToUpdateResponse {

        @Test
        void shouldMapCourseFieldsIntoUpdateResponse() {
            Course course = CourseTestData.draftCourse();

            CourseUpdateResponseDTO response = courseMapper.toUpdateResponse(course);

            assertThat(response.getMessage()).isEqualTo("Course updated successfully");
            assertThat(response.getData().getCourseId()).isEqualTo(course.getId());
        }
    }

    @Nested
    class ToDeleteResponse {

        @Test
        void shouldMapMessageAndEmptyDataMap() {
            CourseDeleteResponseDTO response = courseMapper.toDeleteResponse("Course enrollment closed successfully");

            assertThat(response.getMessage()).isEqualTo("Course enrollment closed successfully");
            assertThat(response.getData()).isEqualTo(Collections.emptyMap());
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    class ToListResponse {

        @Test
        void shouldMapPageContentAndPaginationMetadata() {
            Course course = CourseTestData.draftCourse();
            Page<Course> page = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);

            CourseListResponseDTO response = courseMapper.toListResponse(page);

            assertThat(response.getMessage()).isEqualTo("Courses fetched successfully");
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotal()).isEqualTo(1);
            assertThat(response.getData()).hasSize(1);
            CourseListResponseDataInnerDTO item = response.getData().get(0);
            assertThat(item.getCourseId()).isEqualTo(course.getId());
            assertThat(item.getCourseTitle()).isEqualTo(course.getTitle());
            assertThat(item.getCourseStatus()).isEqualTo(CourseListResponseDataInnerDTO.CourseStatusEnum.DRAFT);
            assertThat(item.getCanEnrollment()).isEqualTo(course.getCanEnrollment());
        }

        @Test
        void shouldReturnEmptyDataListForEmptyPage() {
            Page<Course> page = new PageImpl<>(List.of());

            CourseListResponseDTO response = courseMapper.toListResponse(page);

            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotal()).isZero();
        }
    }

    // ── status conversion ───────────────────────────────────────────────────

    @Nested
    class ToDomainStatus {

        @Test
        void shouldConvertValidStringToDomainStatus() {
            CourseStatus status = courseMapper.toDomainStatus("published");

            assertThat(status).isEqualTo(CourseStatus.PUBLISHED);
        }

        @Test
        void shouldReturnNullWhenStringStatusIsNull() {
            assertThat(courseMapper.toDomainStatus((String) null)).isNull();
        }

        @Test
        void shouldReturnNullWhenStringStatusIsBlank() {
            assertThat(courseMapper.toDomainStatus("  ")).isNull();
        }

        @Test
        void shouldThrowWhenStringStatusIsInvalid() {
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> courseMapper.toDomainStatus("NOT_A_STATUS"));
        }
    }
}
