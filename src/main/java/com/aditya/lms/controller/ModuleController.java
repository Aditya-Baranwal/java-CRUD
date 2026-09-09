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

    // TODO: replace with the authenticated caller's id once a security layer is introduced;
    // wired to the admin-variant service methods as a stopgap since all roles are not yet distinguishable.
    private static final Long TEMP_REQUESTER_ID = 0L;

    private final ModuleService moduleService;
    private final ModuleMapper moduleMapper;

    @Override
    public ResponseEntity<ModuleCreateResponseDTO> createModule(ModuleCreateRequestDTO moduleCreateRequestDTO) {
        Module created = moduleService.createModuleAsAdmin(moduleMapper.toEntity(moduleCreateRequestDTO), TEMP_REQUESTER_ID);
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleMapper.toCreateResponse(created));
    }

    @Override
    public ResponseEntity<ModuleDeleteResponseDTO> deleteModule(Long moduleId) {
        moduleService.deleteModuleAsAdmin(moduleId, TEMP_REQUESTER_ID);
        return ResponseEntity.ok(moduleMapper.toDeleteResponse("Module deleted successfully"));
    }

    @Override
    public ResponseEntity<ModuleGetResponseDTO> getModule(Long moduleId, Boolean includeLessons) {
        Module module = moduleService.getModule(moduleId);
        return ResponseEntity.ok(moduleMapper.toGetResponse(module, Boolean.TRUE.equals(includeLessons)));
    }

    @Override
    public ResponseEntity<ModuleListResponseDTO> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return ResponseEntity.ok(moduleMapper.toListResponse(moduleService.listModulesForAdmin(courseId, pageNo, pageSize, active, sortBy, sortOrder)));
    }

    @Override
    public ResponseEntity<ModuleUpdateResponseDTO> updateModule(Long moduleId, ModuleUpdateRequestDTO moduleUpdateRequestDTO) {
        Module moduleToUpdate = new Module();
        moduleMapper.applyUpdates(moduleToUpdate, moduleUpdateRequestDTO);
        Module updated = moduleService.updateModuleAsAdmin(moduleId, moduleToUpdate, TEMP_REQUESTER_ID);
        return ResponseEntity.ok(moduleMapper.toUpdateResponse(updated));
    }
}
