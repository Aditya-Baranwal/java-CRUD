package com.aditya.lms.controller;

import com.lms.api.ModulesApi;
import com.lms.model.CreateModuleRequestDTO;
import com.lms.model.CreateModuleResponseDTO;
import com.lms.model.DeleteModuleResponseDTO;
import com.lms.model.GetModuleResponseDTO;
import com.lms.model.ListModuleResponseDTO;
import com.lms.model.UpdateModuleRequestDTO;
import com.lms.model.UpdateModuleResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ModuleController implements ModulesApi {

    @Override
    public ResponseEntity<CreateModuleResponseDTO> createModule(CreateModuleRequestDTO createModuleRequestDTO) {
        return null;
    }

    @Override
    public ResponseEntity<DeleteModuleResponseDTO> deleteModule(Long moduleId) {
        return null;
    }

    @Override
    public ResponseEntity<GetModuleResponseDTO> getModule(Long moduleId, Boolean includeLessons) {
        return null;
    }

    @Override
    public ResponseEntity<ListModuleResponseDTO> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public ResponseEntity<UpdateModuleResponseDTO> updateModule(Long moduleId, UpdateModuleRequestDTO updateModuleRequestDTO) {
        return null;
    }
}
