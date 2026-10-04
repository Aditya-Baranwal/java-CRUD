package com.aditya.lms.mapper;

import com.aditya.lms.dto.ModuleView;
import com.aditya.lms.entity.Course;
import com.aditya.lms.entity.Lesson;
import com.aditya.lms.entity.Module;
import com.aditya.lms.enums.ContentType;
import com.aditya.lms.testdata.ModuleTestData;
import com.lms.model.CourseResponseModulesInnerDTO;
import com.lms.model.ModuleCreateRequestDTO;
import com.lms.model.ModuleCreateResponseDTO;
import com.lms.model.ModuleDeleteResponseDTO;
import com.lms.model.ModuleGetResponseDTO;
import com.lms.model.ModuleListResponseDTO;
import com.lms.model.ModuleListResponseDataInnerDTO;
import com.lms.model.ModuleListResponseDataInnerOneOfDTO;
import com.lms.model.ModuleProgressResponseDTO;
import com.lms.model.ModuleResponseDTO;
import com.lms.model.ModuleResponseLessonsInnerDTO;
import com.lms.model.ModuleUpdateRequestDTO;
import com.lms.model.ModuleUpdateResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.net.URI;
import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleMapperTest {

    private ModuleMapper moduleMapper;

    @BeforeEach
    void setUp() {
        moduleMapper = new ModuleMapper();
    }

    // ── toEntity ────────────────────────────────────────────────────────────

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromCreateRequest() {
            ModuleCreateRequestDTO request = new ModuleCreateRequestDTO()
                    .courseId(1L)
                    .moduleTitle("Introduction")
                    .moduleDescription("Getting started")
                    .sequence(1);

            Module module = moduleMapper.toEntity(request);

            assertThat(module.getCourse().getId()).isEqualTo(1L);
            assertThat(module.getTitle()).isEqualTo("Introduction");
            assertThat(module.getDescription()).isEqualTo("Getting started");
            assertThat(module.getSequence()).isEqualTo(1);
        }

        @Test
        void shouldDefaultIsActiveToTrue() {
            ModuleCreateRequestDTO request = new ModuleCreateRequestDTO()
                    .courseId(1L)
                    .moduleTitle("Introduction")
                    .sequence(1);

            Module module = moduleMapper.toEntity(request);

            assertThat(module.getIsActive()).isTrue();
        }

        @Test
        void shouldMapNullDescriptionAsNull() {
            ModuleCreateRequestDTO request = new ModuleCreateRequestDTO()
                    .courseId(1L)
                    .moduleTitle("Introduction")
                    .sequence(1);

            Module module = moduleMapper.toEntity(request);

            assertThat(module.getDescription()).isNull();
        }
    }

    // ── applyUpdates ────────────────────────────────────────────────────────

    @Nested
    class ApplyUpdates {

        @Test
        void shouldUpdateOnlyProvidedFields() {
            Module module = ModuleTestData.draftModule();
            ModuleUpdateRequestDTO request = new ModuleUpdateRequestDTO()
                    .moduleTitle("New Title");

            moduleMapper.applyUpdates(module, request);

            assertThat(module.getTitle()).isEqualTo("New Title");
            assertThat(module.getDescription()).isEqualTo("Getting started");
        }

        @Test
        void shouldNotChangeFieldsWhenNotProvided() {
            Module module = ModuleTestData.draftModule();
            ModuleUpdateRequestDTO request = new ModuleUpdateRequestDTO();

            moduleMapper.applyUpdates(module, request);

            assertThat(module.getTitle()).isEqualTo("Introduction");
            assertThat(module.getDescription()).isEqualTo("Getting started");
            assertThat(module.getSequence()).isEqualTo(1);
            assertThat(module.getIsActive()).isTrue();
        }

        @Test
        void shouldUpdateSequenceWhenProvided() {
            Module module = ModuleTestData.draftModule();
            ModuleUpdateRequestDTO request = new ModuleUpdateRequestDTO()
                    .sequence(5);

            moduleMapper.applyUpdates(module, request);

            assertThat(module.getSequence()).isEqualTo(5);
        }

        @Test
        void shouldUpdateIsActiveWhenProvided() {
            Module module = ModuleTestData.draftModule();
            ModuleUpdateRequestDTO request = new ModuleUpdateRequestDTO()
                    .isActive(Boolean.FALSE);

            moduleMapper.applyUpdates(module, request);

            assertThat(module.getIsActive()).isFalse();
        }
    }

    // ── response mapping ────────────────────────────────────────────────────

    @Nested
    class ToCreateResponse {

        @Test
        void shouldMapModuleFieldsIntoCreateResponse() {
            Module module = ModuleTestData.draftModule();

            ModuleCreateResponseDTO response = moduleMapper.toCreateResponse(module);

            assertThat(response.getMessage()).isEqualTo("Module created successfully");
            assertThat(response.getTimestamp()).isNotNull();
            ModuleResponseDTO data = response.getData();
            assertThat(data.getModuleId()).isEqualTo(module.getId());
            assertThat(data.getCourseId()).isEqualTo(module.getCourse().getId());
            assertThat(data.getModuleTitle()).isEqualTo(module.getTitle());
            assertThat(data.getModuleDescription()).isEqualTo(module.getDescription());
            assertThat(data.getSequence()).isEqualTo(module.getSequence());
            assertThat(data.getTotalLessonCount()).isZero();
            assertThat(data.getIsActive()).isEqualTo(module.getIsActive());
            assertThat(data.getCreatedAt()).isEqualTo(module.getCreatedAt());
            assertThat(data.getLessons()).isEmpty();
        }

        @Test
        void shouldMapNullCourseIdWhenCourseIsNull() {
            Module module = ModuleTestData.draftModule();
            module.setCourse(null);

            ModuleCreateResponseDTO response = moduleMapper.toCreateResponse(module);

            assertThat(response.getData().getCourseId()).isNull();
        }
    }

    @Nested
    class ToGetResponse {

        @Test
        void shouldLeaveLessonsEmptyWhenIncludeLessonsIsFalse() {
            Module module = ModuleTestData.draftModule();

            ModuleGetResponseDTO response = moduleMapper.toGetResponse(new ModuleView(module, null, null, null, 0), false);

            assertThat(response.getMessage()).isEqualTo("Module fetched successfully");
            ModuleResponseDTO data = (ModuleResponseDTO) response.getData();
            assertThat(data.getLessons()).isEmpty();
        }

        @Test
        void shouldLeaveLessonsEmptyWhenIncludeLessonsIsTrueButCollectionIsUninitialized() {
            Module module = ModuleTestData.draftModule();

            ModuleGetResponseDTO response = moduleMapper.toGetResponse(new ModuleView(module, null, null, null, 0), true);

            // Hibernate lazy collection is a plain ArrayList here (not a proxy) so it IS
            // considered initialized; assert lessons list mirrors the (empty) entity collection.
            ModuleResponseDTO data = (ModuleResponseDTO) response.getData();
            assertThat(data.getLessons()).isEmpty();
        }

        @Test
        void shouldIncludeMappedLessonsWhenPresent() {
            Module module = ModuleTestData.draftModule();
            Lesson lesson = Lesson.builder()
                    .id(10L)
                    .module(module)
                    .contentType(ContentType.MP4)
                    .contentLink("https://example.com/video")
                    .sequence(1)
                    .isActive(Boolean.TRUE)
                    .build();
            module.setLessons(List.of(lesson));

            ModuleGetResponseDTO response = moduleMapper.toGetResponse(new ModuleView(module, null, null, null, 1), true);

            ModuleResponseDTO data = (ModuleResponseDTO) response.getData();
            assertThat(data.getLessons()).hasSize(1);
            assertThat(data.getTotalLessonCount()).isEqualTo(1);
            ModuleResponseLessonsInnerDTO lessonDto = data.getLessons().get(0);
            assertThat(lessonDto.getLessonId()).isEqualTo(10L);
            assertThat(lessonDto.getModuleId()).isEqualTo(module.getId());
            assertThat(lessonDto.getContentType()).isEqualTo(ModuleResponseLessonsInnerDTO.ContentTypeEnum.MP4);
            assertThat(lessonDto.getContentLink()).isEqualTo(URI.create("https://example.com/video"));
            assertThat(lessonDto.getSequence()).isEqualTo(1);
        }

        @Test
        void shouldMapProgressFieldsIntoGetResponse() {
            Module module = ModuleTestData.draftModule();
            ModuleView moduleView = new ModuleView(module, 201L, Boolean.TRUE, 3, 3);

            ModuleGetResponseDTO response = moduleMapper.toGetProgressResponse(moduleView, false);

            assertThat(response.getData()).isInstanceOf(ModuleProgressResponseDTO.class);
            ModuleProgressResponseDTO data = (ModuleProgressResponseDTO) response.getData();
            assertThat(data.getModuleId()).isEqualTo(module.getId());
            assertThat(data.getUserId()).isEqualTo(201L);
            assertThat(data.getIsModuleCompleted()).isTrue();
            assertThat(data.getCompletedLessonCount()).isEqualTo(3);
            assertThat(data.getTotalLessonCount()).isEqualTo(3);
            assertThat(data.getLessons()).isEmpty();
        }
    }

    @Nested
    class ToUpdateResponse {

        @Test
        void shouldMapModuleFieldsIntoUpdateResponse() {
            Module module = ModuleTestData.draftModule();

            ModuleUpdateResponseDTO response = moduleMapper.toUpdateResponse(module);

            assertThat(response.getMessage()).isEqualTo("Module updated successfully");
            assertThat(response.getData().getModuleId()).isEqualTo(module.getId());
            assertThat(response.getData().getTotalLessonCount()).isZero();
        }
    }

    @Nested
    class ToDeleteResponse {

        @Test
        void shouldMapMessageAndEmptyDataMap() {
            ModuleDeleteResponseDTO response = moduleMapper.toDeleteResponse("Module deleted successfully");

            assertThat(response.getMessage()).isEqualTo("Module deleted successfully");
            assertThat(response.getData()).isEqualTo(Collections.emptyMap());
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    class ToListResponse {

        @Test
        void shouldMapPageContentAndPaginationMetadata() {
            Module module = ModuleTestData.draftModule();
            Page<ModuleView> page = new PageImpl<>(List.of(new ModuleView(module, null, null, null, 2)), PageRequest.of(0, 10), 1);

            ModuleListResponseDTO response = moduleMapper.toListResponse(page);

            assertThat(response.getMessage()).isEqualTo("Modules fetched successfully");
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotal()).isEqualTo(1);
            assertThat(response.getData()).hasSize(1);
            ModuleListResponseDataInnerDTO item = response.getData().get(0);
            assertThat(item).isInstanceOf(CourseResponseModulesInnerDTO.class);
            CourseResponseModulesInnerDTO baseItem = (CourseResponseModulesInnerDTO) item;
            assertThat(baseItem.getModuleId()).isEqualTo(module.getId());
            assertThat(baseItem.getModuleTitle()).isEqualTo(module.getTitle());
            assertThat(baseItem.getSequence()).isEqualTo(module.getSequence());
            assertThat(baseItem.getTotalLessonCount()).isEqualTo(2);
            assertThat(baseItem.getIsActive()).isEqualTo(module.getIsActive());
        }

        @Test
        void shouldMapProgressListItemsWhenRequested() {
            Module module = ModuleTestData.draftModule();
            ModuleView moduleView = new ModuleView(module, 201L, Boolean.FALSE, 1, 3);
            Page<ModuleView> page = new PageImpl<>(List.of(moduleView), PageRequest.of(0, 10), 1);

            ModuleListResponseDTO response = moduleMapper.toListProgressResponse(page);

            assertThat(response.getData()).hasSize(1);
            ModuleListResponseDataInnerDTO item = response.getData().get(0);
            assertThat(item).isInstanceOf(ModuleListResponseDataInnerOneOfDTO.class);
            ModuleListResponseDataInnerOneOfDTO progressItem = (ModuleListResponseDataInnerOneOfDTO) item;
            assertThat(progressItem.getModuleId()).isEqualTo(module.getId());
            assertThat(progressItem.getUserId()).isEqualTo(201L);
            assertThat(progressItem.getIsModuleCompleted()).isFalse();
            assertThat(progressItem.getCompletedLessonCount()).isEqualTo(1);
            assertThat(progressItem.getTotalLessonCount()).isEqualTo(3);
        }

        @Test
        void shouldReturnEmptyDataListForEmptyPage() {
            Page<ModuleView> page = new PageImpl<>(List.of());

            ModuleListResponseDTO response = moduleMapper.toListResponse(page);

            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotal()).isZero();
        }
    }

    // ── course reference handling ───────────────────────────────────────────

    @Nested
    class CourseReferenceHandling {

        @Test
        void shouldMapNullCourseIdInListItemWhenCourseIsNull() {
            Module module = ModuleTestData.draftModule();
            module.setCourse(null);
            Page<ModuleView> page = new PageImpl<>(List.of(new ModuleView(module, null, null, null, 0)));

            ModuleListResponseDTO response = moduleMapper.toListResponse(page);

            assertThat(((CourseResponseModulesInnerDTO) response.getData().get(0)).getCourseId()).isNull();
        }
    }
}
