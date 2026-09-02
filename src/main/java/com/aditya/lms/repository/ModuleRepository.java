package com.aditya.lms.repository;

import com.aditya.lms.entity.Module;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    @EntityGraph(attributePaths = "lessons")
    Optional<Module> findByIdAndIsActiveTrue(Long id);

    boolean existsByCourse_IdAndSequenceAndIsActiveTrue(Long courseId, Integer sequence);

    boolean existsByCourse_IdAndSequenceAndIsActiveTrueAndIdNot(Long courseId, Integer sequence, Long id);

    Page<Module> findByCourse_IdAndIsActive(Long courseId, Boolean isActive, Pageable pageable);

    Page<Module> findByCourse_Id(Long courseId, Pageable pageable);
}
