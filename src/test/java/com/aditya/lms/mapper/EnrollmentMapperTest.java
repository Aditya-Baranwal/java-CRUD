package com.aditya.lms.mapper;

import com.aditya.lms.dto.EnrollmentView;
import com.aditya.lms.entity.Enrollment;
import com.aditya.lms.enums.CourseCompletionStatus;
import com.aditya.lms.testdata.EnrollmentTestData;
import com.lms.model.EnrollmentCreateRequestDTO;
import com.lms.model.EnrollmentCreateResponseDTO;
import com.lms.model.EnrollmentDeleteResponseDTO;
import com.lms.model.EnrollmentGetResponseDTO;
import com.lms.model.EnrollmentListResponseDTO;
import com.lms.model.EnrollmentResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentMapperTest {

    private EnrollmentMapper enrollmentMapper;

    @BeforeEach
    void setUp() {
        enrollmentMapper = new EnrollmentMapper();
    }

    // ── toEntity ────────────────────────────────────────────────────────────

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromCreateRequest() {
            EnrollmentCreateRequestDTO request = new EnrollmentCreateRequestDTO()
                    .userId(201L)
                    .courseId(1L);

            Enrollment enrollment = enrollmentMapper.toEntity(request);

            assertThat(enrollment.getUserId()).isEqualTo(201L);
            assertThat(enrollment.getCourse().getId()).isEqualTo(1L);
        }

        @Test
        void shouldDefaultCourseCompletionStatusToIncomplete() {
            EnrollmentCreateRequestDTO request = new EnrollmentCreateRequestDTO()
                    .userId(201L)
                    .courseId(1L);

            Enrollment enrollment = enrollmentMapper.toEntity(request);

            assertThat(enrollment.getCourseCompletionStatus()).isEqualTo(CourseCompletionStatus.INCOMPLETE);
        }

        @Test
        void shouldDefaultIsActiveToTrue() {
            EnrollmentCreateRequestDTO request = new EnrollmentCreateRequestDTO()
                    .userId(201L)
                    .courseId(1L);

            Enrollment enrollment = enrollmentMapper.toEntity(request);

            assertThat(enrollment.getIsActive()).isTrue();
        }
    }

    // ── toCreateResponse / toGetResponse ─────────────────────────────────────

    @Nested
    class ToCreateResponse {

        @Test
        void shouldMapEnrollmentFieldsIntoCreateResponse() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            EnrollmentView view = new EnrollmentView(enrollment, true, "Course is open for enrollment.");

            EnrollmentCreateResponseDTO response = enrollmentMapper.toCreateResponse(view);

            assertThat(response.getMessage()).isEqualTo("Enrollment created successfully");
            assertThat(response.getTimestamp()).isNotNull();
            EnrollmentResponseDTO data = response.getData();
            assertThat(data.getId()).isEqualTo(enrollment.getId());
            assertThat(data.getUserId()).isEqualTo(enrollment.getUserId());
            assertThat(data.getCourseId()).isEqualTo(enrollment.getCourse().getId());
            assertThat(data.getCourseTitle()).isEqualTo(enrollment.getCourse().getTitle());
            assertThat(data.getCourseCompletionStatus().name()).isEqualTo(enrollment.getCourseCompletionStatus().name());
            assertThat(data.getEnrolledAt()).isEqualTo(enrollment.getEnrolledAt());
            assertThat(data.getCanEnrolledStudentViewCourseContent()).isTrue();
            assertThat(data.getCourseAccessMessage()).isEqualTo("Course is open for enrollment.");
        }

        @Test
        void shouldMapNullCourseFieldsWhenCourseIsNull() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            enrollment.setCourse(null);
            EnrollmentView view = new EnrollmentView(enrollment, false, null);

            EnrollmentCreateResponseDTO response = enrollmentMapper.toCreateResponse(view);

            assertThat(response.getData().getCourseId()).isNull();
            assertThat(response.getData().getCourseTitle()).isNull();
            assertThat(response.getData().getCourseAccessMessage()).isNull();
        }

        @Test
        void shouldSetCanViewCourseContentFalseWhenViewSaysFalse() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            EnrollmentView view = new EnrollmentView(enrollment, false, "Course is closed for enrollment.");

            EnrollmentCreateResponseDTO response = enrollmentMapper.toCreateResponse(view);

            assertThat(response.getData().getCanEnrolledStudentViewCourseContent()).isFalse();
        }
    }

    @Nested
    class ToGetResponse {

        @Test
        void shouldMapEnrollmentFieldsIntoGetResponse() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            EnrollmentView view = new EnrollmentView(enrollment, true, "Course is open for enrollment.");

            EnrollmentGetResponseDTO response = enrollmentMapper.toGetResponse(view);

            assertThat(response.getMessage()).isEqualTo("Enrollment fetched successfully");
            assertThat(response.getData().getId()).isEqualTo(enrollment.getId());
            assertThat(response.getData().getCourseAccessMessage()).isEqualTo("Course is open for enrollment.");
        }
    }

    @Nested
    class ToDeleteResponse {

        @Test
        void shouldMapMessageAndEmptyDataMap() {
            EnrollmentDeleteResponseDTO response = enrollmentMapper.toDeleteResponse("Enrollment cancelled successfully");

            assertThat(response.getMessage()).isEqualTo("Enrollment cancelled successfully");
            assertThat(response.getData()).isEqualTo(Collections.emptyMap());
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    class ToListResponse {

        @Test
        void shouldMapPageContentAndPaginationMetadata() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            EnrollmentView view = new EnrollmentView(enrollment, true, "Course is open for enrollment.");
            Page<EnrollmentView> page = new PageImpl<>(List.of(view), PageRequest.of(0, 10), 1);

            EnrollmentListResponseDTO response = enrollmentMapper.toListResponse(page);

            assertThat(response.getMessage()).isEqualTo("Enrollments fetched successfully");
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotal()).isEqualTo(1);
            assertThat(response.getData()).hasSize(1);
            EnrollmentResponseDTO item = response.getData().get(0);
            assertThat(item.getId()).isEqualTo(enrollment.getId());
            assertThat(item.getCanEnrolledStudentViewCourseContent()).isTrue();
        }

        @Test
        void shouldReturnEmptyDataListForEmptyPage() {
            Page<EnrollmentView> page = new PageImpl<>(List.of());

            EnrollmentListResponseDTO response = enrollmentMapper.toListResponse(page);

            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotal()).isZero();
        }

        @Test
        void shouldReflectPerItemDerivedFieldsFromEachView() {
            Enrollment enrollment = EnrollmentTestData.activeEnrollment();
            EnrollmentView view = new EnrollmentView(enrollment, false, null);
            Page<EnrollmentView> page = new PageImpl<>(List.of(view));

            EnrollmentListResponseDTO response = enrollmentMapper.toListResponse(page);

            assertThat(response.getData().get(0).getCanEnrolledStudentViewCourseContent()).isFalse();
            assertThat(response.getData().get(0).getCourseAccessMessage()).isNull();
        }
    }
}
