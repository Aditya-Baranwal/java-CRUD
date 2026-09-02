package com.aditya.lms.controller;

import com.aditya.lms.entity.Module;
import com.aditya.lms.mapper.ModuleMapper;
import com.aditya.lms.service.interfaces.ModuleService;
import com.lms.api.ModulesApi;
import com.lms.model.ModuleCreateRequestDTO;
import com.lms.model.ModuleCreateResponseDTO;
import com.lms.model.ModuleDeleteResponseDTO;
import com.lms.model.ModuleGetResponseDTO;
import com.lms.model.ModuleListResponseDTO;
import com.lms.model.ModuleUpdateRequestDTO;
import com.lms.model.ModuleUpdateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ModuleController implements ModulesApi {

    private final ModuleService moduleService;
    private final ModuleMapper moduleMapper;

    @Override
    public ResponseEntity<ModuleCreateResponseDTO> createModule(ModuleCreateRequestDTO moduleCreateRequestDTO) {
        Module created = moduleService.createModule(moduleMapper.toEntity(moduleCreateRequestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleMapper.toCreateResponse(created));
    }

    @Override
    public ResponseEntity<ModuleDeleteResponseDTO> deleteModule(Long moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.ok(moduleMapper.toDeleteResponse("Module deleted successfully"));
    }

    @Override
    public ResponseEntity<ModuleGetResponseDTO> getModule(Long moduleId, Boolean includeLessons) {
        Module module = moduleService.getModule(moduleId);
        return ResponseEntity.ok(moduleMapper.toGetResponse(module, Boolean.TRUE.equals(includeLessons)));
    }

    @Override
    public ResponseEntity<ModuleListResponseDTO> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return ResponseEntity.ok(moduleMapper.toListResponse(moduleService.listModules(courseId, pageNo, pageSize, active, sortBy, sortOrder)));
    }

    @Override
    public ResponseEntity<ModuleUpdateResponseDTO> updateModule(Long moduleId, ModuleUpdateRequestDTO moduleUpdateRequestDTO) {
        Module moduleToUpdate = new Module();
        moduleMapper.applyUpdates(moduleToUpdate, moduleUpdateRequestDTO);
        Module updated = moduleService.updateModule(moduleId, moduleToUpdate);
        return ResponseEntity.ok(moduleMapper.toUpdateResponse(updated));
    }
}
