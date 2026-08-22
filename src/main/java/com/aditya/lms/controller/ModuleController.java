package com.aditya.lms.controller;

import com.lms.api.ModulesApi;
import com.lms.model.ModuleCreateRequestDTO;
import com.lms.model.ModuleCreateResponseDTO;
import com.lms.model.ModuleDeleteResponseDTO;
import com.lms.model.ModuleGetResponseDTO;
import com.lms.model.ModuleListResponseDTO;
import com.lms.model.ModuleUpdateRequestDTO;
import com.lms.model.ModuleUpdateResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ModuleController implements ModulesApi {

    @Override
    public ResponseEntity<ModuleCreateResponseDTO> createModule(ModuleCreateRequestDTO moduleCreateRequestDTO) {
        return null;
    }

    @Override
    public ResponseEntity<ModuleDeleteResponseDTO> deleteModule(Long moduleId) {
        return null;
    }

    @Override
    public ResponseEntity<ModuleGetResponseDTO> getModule(Long moduleId, Boolean includeLessons) {
        return null;
    }

    @Override
    public ResponseEntity<ModuleListResponseDTO> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ResponseEntity<ModuleUpdateResponseDTO> updateModule(Long moduleId, ModuleUpdateRequestDTO moduleUpdateRequestDTO) {
        return null;
    }
}
