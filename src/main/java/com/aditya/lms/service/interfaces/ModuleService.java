package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.Module;
import org.springframework.data.domain.Page;

public interface ModuleService {

    Module createModuleAsAdmin(Module module, Long adminId);

    Module createModuleAsInstructor(Module module, Long instructorId);

    Module getModule(Long moduleId);

    Page<Module> listModulesForAdmin(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<Module> listModulesForInstructor(Long courseId, Long instructorId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Page<Module> listModulesForStudent(Long courseId, Integer pageNo, Integer pageSize, String sortBy, String sortOrder);

    Module updateModuleAsAdmin(Long moduleId, Module module, Long adminId);

    Module updateModuleAsInstructor(Long moduleId, Module module, Long instructorId);

    void deleteModuleAsAdmin(Long moduleId, Long adminId);

    void deleteModuleAsInstructor(Long moduleId, Long instructorId);
}
