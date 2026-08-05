package ${basePackage}.service.impl;

import ${basePackage}.dto.request.${Entity}CreateRequest;
import ${basePackage}.dto.request.${Entity}SearchRequest;
import ${basePackage}.dto.request.${Entity}UpdateRequest;
import ${basePackage}.dto.response.${Entity}Response;
import ${basePackage}.entity.${Entity};
import ${basePackage}.exception.ResourceNotFoundException;
import ${basePackage}.mapper.${Entity}Mapper;
import ${basePackage}.repository.${Entity}Repository;
import ${basePackage}.service.${Entity}Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ${Entity}ServiceImpl implements ${Entity}Service {

private final ${Entity}Repository repository;
private final ${Entity}Mapper mapper;

@Override
public ${Entity}Response create(
        ${Entity}CreateRequest request) {

        log.info("Creating ${entity}");

        ${Entity} entity = mapper.toEntity(request);

        entity = repository.save(entity);

        return mapper.toResponse(entity);
        }

@Override
@Transactional(readOnly = true)
public ${Entity}Response getById(
        Long id) {

        ${Entity} entity = repository.findById(id)
        .orElseThrow(() ->
        new ResourceNotFoundException(
        "${Entity} not found with id: " + id));

        return mapper.toResponse(entity);
        }

@Override
@Transactional(readOnly = true)
public Page<${Entity}Response> getAll(
        Pageable pageable) {

        return repository.findAll(pageable)
        .map(mapper::toResponse);
        }

@Override
@Transactional(readOnly = true)
public Page<${Entity}Response> search(
        ${Entity}SearchRequest request,
        Pageable pageable) {

        // TODO
        // Build Specification / Criteria / QueryDSL
        // based on SearchRequest

        return repository.findAll(pageable)
        .map(mapper::toResponse);
        }

@Override
public ${Entity}Response update(
        Long id,
        ${Entity}UpdateRequest request) {

        ${Entity} entity = repository.findById(id)
        .orElseThrow(() ->
        new ResourceNotFoundException(
        "${Entity} not found with id: " + id));

        mapper.update(request, entity);

        entity = repository.save(entity);

        return mapper.toResponse(entity);
        }

@Override
public void delete(
        Long id) {

        ${Entity} entity = repository.findById(id)
        .orElseThrow(() ->
        new ResourceNotFoundException(
        "${Entity} not found with id: " + id));

        repository.delete(entity);
        }

        }