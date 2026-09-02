package com.aditya.lms.service.interfaces;

import com.aditya.lms.entity.Module;
import org.springframework.data.domain.Page;

public interface ModuleService {

    Module createModule(Module module);

    Module getModule(Long moduleId);

    Page<Module> listModules(Long courseId, Integer pageNo, Integer pageSize, Boolean active, String sortBy, String sortOrder);

    Module updateModule(Long moduleId, Module module);

    void deleteModule(Long moduleId);
}
