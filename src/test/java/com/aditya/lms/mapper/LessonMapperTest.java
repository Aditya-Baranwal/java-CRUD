package com.aditya.lms.mapper;

import com.aditya.lms.dto.LessonView;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.enums.ContentType;
import com.aditya.lms.enums.LessonStatus;
import com.aditya.lms.testdata.LessonTestData;
import com.lms.model.LessonCreateRequestDTO;
import com.lms.model.LessonCreateResponseDTO;
import com.lms.model.LessonDeleteResponseDTO;
import com.lms.model.LessonGetResponseDTO;
import com.lms.model.LessonListResponseDTO;
import com.lms.model.LessonListResponseDataInnerDTO;
import com.lms.model.LessonListResponseDataInnerOneOfDTO;
import com.lms.model.LessonProgressResponseDTO;
import com.lms.model.LessonResponseDTO;
import com.lms.model.LessonUpdateRequestDTO;
import com.lms.model.LessonUpdateResponseDTO;
import com.lms.model.ModuleResponseLessonsInnerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LessonMapperTest {

    private LessonMapper lessonMapper;

    @BeforeEach
    void setUp() {
        lessonMapper = new LessonMapper();
    }

    // ── toEntity ────────────────────────────────────────────────────────────

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromCreateRequest() {
            LessonCreateRequestDTO request = new LessonCreateRequestDTO()
                    .moduleId(1L)
                    .contentType(LessonCreateRequestDTO.ContentTypeEnum.MP4)
                    .contentLink(URI.create("https://example.com/lesson.mp4"))
                    .sequence(1);

            Lesson lesson = lessonMapper.toEntity(request);

            assertThat(lesson.getModule().getId()).isEqualTo(1L);
            assertThat(lesson.getContentType()).isEqualTo(ContentType.MP4);
            assertThat(lesson.getContentLink()).isEqualTo("https://example.com/lesson.mp4");
            assertThat(lesson.getSequence()).isEqualTo(1);
        }

        @Test
        void shouldDefaultIsActiveToTrue() {
            LessonCreateRequestDTO request = new LessonCreateRequestDTO()
                    .moduleId(1L)
                    .contentType(LessonCreateRequestDTO.ContentTypeEnum.PDF)
                    .contentLink(URI.create("https://example.com/lesson.pdf"))
                    .sequence(1);

            Lesson lesson = lessonMapper.toEntity(request);

            assertThat(lesson.getIsActive()).isTrue();
        }
    }

    // ── applyUpdates ────────────────────────────────────────────────────────

    @Nested
    class ApplyUpdates {

        @Test
        void shouldUpdateOnlyProvidedFields() {
            Lesson lesson = LessonTestData.draftLesson();
            LessonUpdateRequestDTO request = new LessonUpdateRequestDTO()
                    .contentLink(URI.create("https://example.com/updated.mp4"));

            lessonMapper.applyUpdates(lesson, request);

            assertThat(lesson.getContentLink()).isEqualTo("https://example.com/updated.mp4");
            assertThat(lesson.getContentType()).isEqualTo(ContentType.MP4);
        }

        @Test
        void shouldNotChangeFieldsWhenNotProvided() {
            Lesson lesson = LessonTestData.draftLesson();
            LessonUpdateRequestDTO request = new LessonUpdateRequestDTO();

            lessonMapper.applyUpdates(lesson, request);

            assertThat(lesson.getContentType()).isEqualTo(ContentType.MP4);
            assertThat(lesson.getContentLink()).isEqualTo("https://example.com/lesson.mp4");
            assertThat(lesson.getSequence()).isEqualTo(1);
            assertThat(lesson.getIsActive()).isTrue();
        }

        @Test
        void shouldUpdateContentTypeWhenProvided() {
            Lesson lesson = LessonTestData.draftLesson();
            LessonUpdateRequestDTO request = new LessonUpdateRequestDTO()
                    .contentType(LessonUpdateRequestDTO.ContentTypeEnum.PDF);

            lessonMapper.applyUpdates(lesson, request);

            assertThat(lesson.getContentType()).isEqualTo(ContentType.PDF);
        }

        @Test
        void shouldUpdateSequenceWhenProvided() {
            Lesson lesson = LessonTestData.draftLesson();
            LessonUpdateRequestDTO request = new LessonUpdateRequestDTO()
                    .sequence(5);

            lessonMapper.applyUpdates(lesson, request);

            assertThat(lesson.getSequence()).isEqualTo(5);
        }

        @Test
        void shouldUpdateIsActiveWhenProvided() {
            Lesson lesson = LessonTestData.draftLesson();
            LessonUpdateRequestDTO request = new LessonUpdateRequestDTO()
                    .isActive(Boolean.FALSE);

            lessonMapper.applyUpdates(lesson, request);

            assertThat(lesson.getIsActive()).isFalse();
        }
    }

    // ── response mapping ────────────────────────────────────────────────────

    @Nested
    class ToCreateResponse {

        @Test
        void shouldMapLessonFieldsIntoCreateResponse() {
            Lesson lesson = LessonTestData.draftLesson();

            LessonCreateResponseDTO response = lessonMapper.toCreateResponse(lesson);

            assertThat(response.getMessage()).isEqualTo("Lesson created successfully");
            assertThat(response.getTimestamp()).isNotNull();
            LessonResponseDTO data = response.getData();
            assertThat(data.getLessonId()).isEqualTo(lesson.getId());
            assertThat(data.getModuleId()).isEqualTo(lesson.getModule().getId());
            assertThat(data.getContentType()).isEqualTo(LessonResponseDTO.ContentTypeEnum.MP4);
            assertThat(data.getContentLink()).isEqualTo(URI.create(lesson.getContentLink()));
            assertThat(data.getSequence()).isEqualTo(lesson.getSequence());
            assertThat(data.getIsActive()).isEqualTo(lesson.getIsActive());
            assertThat(data.getCreatedAt()).isEqualTo(lesson.getCreatedAt());
        }

        @Test
        void shouldMapNullModuleIdWhenModuleIsNull() {
            Lesson lesson = LessonTestData.draftLesson();
            lesson.setModule(null);

            LessonCreateResponseDTO response = lessonMapper.toCreateResponse(lesson);

            assertThat(response.getData().getModuleId()).isNull();
        }

        @Test
        void shouldMapNullContentTypeAndLinkWhenNull() {
            Lesson lesson = LessonTestData.draftLesson();
            lesson.setContentType(null);
            lesson.setContentLink(null);

            LessonCreateResponseDTO response = lessonMapper.toCreateResponse(lesson);

            assertThat(response.getData().getContentType()).isNull();
            assertThat(response.getData().getContentLink()).isNull();
        }
    }

    @Nested
    class ToGetResponse {

        @Test
        void shouldMapLessonFieldsIntoGetResponse() {
            Lesson lesson = LessonTestData.draftLesson();

            LessonGetResponseDTO response = lessonMapper.toGetResponse(lesson);

            assertThat(response.getMessage()).isEqualTo("Lesson fetched successfully");
            assertThat(response.getData()).isInstanceOf(LessonResponseDTO.class);
            LessonResponseDTO data = (LessonResponseDTO) response.getData();
            assertThat(data.getLessonId()).isEqualTo(lesson.getId());
        }

        @Test
        void shouldMapLessonProgressFieldsIntoGetResponse() {
            Lesson lesson = LessonTestData.draftLesson();
            LessonView lessonView = new LessonView(
                    lesson,
                    201L,
                    301L,
                    Boolean.TRUE,
                    LessonStatus.FINISHED,
                    lesson.getCreatedAt(),
                    lesson.getCreatedAt().plusDays(1)
            );

            LessonGetResponseDTO response = lessonMapper.toGetProgressResponse(lessonView);

            assertThat(response.getData()).isInstanceOf(LessonProgressResponseDTO.class);
            LessonProgressResponseDTO data = (LessonProgressResponseDTO) response.getData();
            assertThat(data.getLessonId()).isEqualTo(lesson.getId());
            assertThat(data.getUserId()).isEqualTo(201L);
            assertThat(data.getProgressId()).isEqualTo(301L);
            assertThat(data.getIsLessonCompleted()).isTrue();
            assertThat(data.getLessonStatus()).isEqualTo(LessonProgressResponseDTO.LessonStatusEnum.FINISHED);
        }
    }

    @Nested
    class ToUpdateResponse {

        @Test
        void shouldMapLessonFieldsIntoUpdateResponse() {
            Lesson lesson = LessonTestData.draftLesson();

            LessonUpdateResponseDTO response = lessonMapper.toUpdateResponse(lesson);

            assertThat(response.getMessage()).isEqualTo("Lesson updated successfully");
            assertThat(response.getData().getLessonId()).isEqualTo(lesson.getId());
        }
    }

    @Nested
    class ToDeleteResponse {

        @Test
        void shouldMapMessageAndEmptyDataMap() {
            LessonDeleteResponseDTO response = lessonMapper.toDeleteResponse("Lesson deleted successfully");

            assertThat(response.getMessage()).isEqualTo("Lesson deleted successfully");
            assertThat(response.getData()).isEqualTo(Collections.emptyMap());
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    class ToListResponse {

        @Test
        void shouldMapPageContentAndPaginationMetadata() {
            Lesson lesson = LessonTestData.draftLesson();
            Page<Lesson> page = new PageImpl<>(List.of(lesson), PageRequest.of(0, 10), 1);

            LessonListResponseDTO response = lessonMapper.toListResponse(page);

            assertThat(response.getMessage()).isEqualTo("Lessons fetched successfully");
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotal()).isEqualTo(1);
            assertThat(response.getData()).hasSize(1);
            LessonListResponseDataInnerDTO item = response.getData().get(0);
            assertThat(item).isInstanceOf(ModuleResponseLessonsInnerDTO.class);
            ModuleResponseLessonsInnerDTO baseItem = (ModuleResponseLessonsInnerDTO) item;
            assertThat(baseItem.getLessonId()).isEqualTo(lesson.getId());
            assertThat(baseItem.getModuleId()).isEqualTo(lesson.getModule().getId());
            assertThat(baseItem.getContentType()).isEqualTo(ModuleResponseLessonsInnerDTO.ContentTypeEnum.MP4);
            assertThat(baseItem.getSequence()).isEqualTo(lesson.getSequence());
            assertThat(baseItem.getIsActive()).isEqualTo(lesson.getIsActive());
        }

        @Test
        void shouldMapProgressListItemsWhenRequested() {
            Lesson lesson = LessonTestData.draftLesson();
            LessonView lessonView = new LessonView(
                    lesson,
                    201L,
                    301L,
                    Boolean.FALSE,
                    LessonStatus.STARTED,
                    lesson.getCreatedAt(),
                    null
            );
            Page<LessonView> page = new PageImpl<>(List.of(lessonView), PageRequest.of(0, 10), 1);

            LessonListResponseDTO response = lessonMapper.toListProgressResponse(page);

            assertThat(response.getData()).hasSize(1);
            LessonListResponseDataInnerDTO item = response.getData().get(0);
            assertThat(item).isInstanceOf(LessonListResponseDataInnerOneOfDTO.class);
            LessonListResponseDataInnerOneOfDTO progressItem = (LessonListResponseDataInnerOneOfDTO) item;
            assertThat(progressItem.getLessonId()).isEqualTo(lesson.getId());
            assertThat(progressItem.getUserId()).isEqualTo(201L);
            assertThat(progressItem.getProgressId()).isEqualTo(301L);
            assertThat(progressItem.getIsLessonCompleted()).isFalse();
            assertThat(progressItem.getLessonStatus()).isEqualTo(LessonListResponseDataInnerOneOfDTO.LessonStatusEnum.STARTED);
        }

        @Test
        void shouldReturnEmptyDataListForEmptyPage() {
            Page<Lesson> page = new PageImpl<>(List.of());

            LessonListResponseDTO response = lessonMapper.toListResponse(page);

            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotal()).isZero();
        }
    }

    // ── module reference handling ───────────────────────────────────────────

    @Nested
    class ModuleReferenceHandling {

        @Test
        void shouldMapNullModuleIdInListItemWhenModuleIsNull() {
            Lesson lesson = LessonTestData.draftLesson();
            lesson.setModule(null);
            Page<Lesson> page = new PageImpl<>(List.of(lesson));

            LessonListResponseDTO response = lessonMapper.toListResponse(page);

            assertThat(((ModuleResponseLessonsInnerDTO) response.getData().get(0)).getModuleId()).isNull();
        }
    }
}
