```
package ${basePackage}.controller;

import ${basePackage}.${Entity}Api;
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

@RestController
@RequestMapping("/api/v1/${resource}")
@RequiredArgsConstructor
@Validated
public class ${Entity}Controller implements ${Entity}Api{

    private final ${Entity}Service ${entity}Service;

    // IMPLEMENT ${Entity}Api METHODS HERE
}
```