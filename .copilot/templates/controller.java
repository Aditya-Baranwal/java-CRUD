package ${basePackage}.controller;

import ${basePackage}.dto.request.${Entity}CreateRequest;
import ${basePackage}.dto.request.${Entity}UpdateRequest;
import ${basePackage}.dto.response.${Entity}Response;
import ${basePackage}.service.${Entity}Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/${resource}")
@RequiredArgsConstructor
@Validated
public class ${Entity}Controller implements ${Entity}Api{

    private final ${Entity}Service ${entity}Service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ${Entity}Response create(
            @Valid @RequestBody ${Entity}CreateRequest request) {

        return ${entity}Service.create(request);
    }

    @GetMapping("/{id}")
    public ${Entity}Response getById(
            @PathVariable Long id) {

        return ${entity}Service.getById(id);
    }

    @GetMapping
    public Page<${Entity}Response> getAll(
            Pageable pageable) {

        return ${entity}Service.getAll(pageable);
    }

    @PutMapping("/{id}")
    public ${Entity}Response update(
            @PathVariable Long id,
            @Valid @RequestBody ${Entity}UpdateRequest request) {

        return ${entity}Service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id) {

        ${entity}Service.delete(id);
    }
}