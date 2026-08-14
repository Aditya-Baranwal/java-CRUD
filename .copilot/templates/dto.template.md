```
package ${basePackage}.dto.${type};

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ${Entity} ${Type} DTO
 *
 * Generated from project template.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${Entity}${Type} {

/**
 * Primary identifier.
 * Response DTO only.
 */
private Long id;

/**
 * Example business field.
 */
@NotBlank(message = "Name is required")
@Size(max = 100, message = "Name cannot exceed 100 characters")
private String name;

/**
 * Example description.
 */
@Size(max = 500)
private String description;

/**
 * Example numeric field.
 */
@Positive
private Integer quantity;

/**
 * Example monetary field.
 */
@DecimalMin(value = "0.0")
private Double price;

/**
 * Example status.
 */
@NotNull
private ${Entity}Status status;

/**
 * Foreign key reference.
 */
@NotNull
private Long ownerId;

/**
 * Collection of child ids.
 */
private List<Long> childIds;

/**
 * Response metadata.
 */
private LocalDate createdDate;

private LocalDateTime createdAt;

private LocalDateTime updatedAt;

/**
 * Optimistic locking version.
 * Response DTO only.
 */
private Long version;
        }
```