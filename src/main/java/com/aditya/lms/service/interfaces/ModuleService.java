package com.aditya.lms.service.interfaces;

import com.aditya.lms.dto.ModuleView;
import com.aditya.lms.entity.Module;
import org.springframework.data.domain.Page;

public interface ModuleService {

    Module createModuleAsAdmin(Module module, Long adminId);

    Module createModuleAsInstructor(Module module, Long instructorId);

    ModuleView getModule(Long moduleId);

    ModuleView getModuleWithProgress(Long moduleId, Long userId);

    Page<ModuleView> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<ModuleView> listModulesWithProgress(Long courseId, Long userId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<ModuleView> listModulesForInstructor(Long courseId, Long instructorId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<ModuleView> listModulesForInstructorWithProgress(Long courseId, Long instructorId, Long userId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<ModuleView> listModulesForStudent(Long courseId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);

    Page<ModuleView> listModulesForStudentWithProgress(Long courseId, Long userId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);

    Module updateModuleAsAdmin(Long moduleId, Module module, Long adminId);

    Module updateModuleAsInstructor(Long moduleId, Module module, Long instructorId);

    void deleteModuleAsAdmin(Long moduleId, Long adminId);

    void deleteModuleAsInstructor(Long moduleId, Long instructorId);
}
