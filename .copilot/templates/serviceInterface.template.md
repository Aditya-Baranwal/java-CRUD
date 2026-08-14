```
package ${basePackage}.service;

import ${basePackage}.dto.request.${Entity}CreateRequest;
import ${basePackage}.dto.request.${Entity}SearchRequest;
import ${basePackage}.dto.request.${Entity}UpdateRequest;
import ${basePackage}.dto.response.${Entity}Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for ${Entity}.
 *
 * Responsibilities
 * - Business logic
 * - Validation
 * - Transaction management
 * - Coordination between repositories
 * - Event publishing
 * - External service orchestration
 */
public interface ${Entity}Service {

    /**
     * Create a new resource.
     */
    ${Entity}Response create(
            ${Entity}CreateRequest request
    );

    /**
     * Fetch resource by identifier.
     */
    ${Entity}Response getById(
            Long id
    );

    /**
     * Search resources.
     */
    Page<${Entity}Response> search(
            ${Entity}SearchRequest request,
    Pageable pageable
    );

    /**
     * Fetch all resources.
     */
    Page<${Entity}Response> getAll(
            Pageable pageable
    );

    /**
     * Update an existing resource.
     */
    ${Entity}Response update(
            Long id,
            ${Entity}UpdateRequest request
    );

    /**
     * Delete a resource.
     */
    void delete(
            Long id
    );

}
```