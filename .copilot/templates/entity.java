package ${basePackage}.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ${Entity} Entity
 *
 * Represents the ${entity} table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "${tableName}",
        indexes = {
                @Index(name = "idx_${tableName}_status", columnList = "status"),
                @Index(name = "idx_${tableName}_created_at", columnList = "created_at")
        }
)
public class ${Entity} {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

/**
 * Example business field.
 */
@Column(nullable = false, length = 100)
private String name;

/**
 * Optional description.
 */
@Column(length = 500)
private String description;

/**
 * Example status enum.
 */
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 30)
private ${Entity}Status status;

/**
 * Example numeric field.
 */
@Column(nullable = false)
private Integer quantity;

/**
 * Example monetary value.
 */
@Column(nullable = false)
private Double price;

/**
 * Many entities belong to one owner.
 */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(
        name = "owner_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_${tableName}_owner")
)
private User owner;

/**
 * Optimistic locking.
 */
@Version
private Long version;

/**
 * Audit fields.
 */
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;

@Column(nullable = false)
private LocalDateTime updatedAt;

@PrePersist
public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        }

@PreUpdate
public void preUpdate() {
        updatedAt = LocalDateTime.now();
        }
        }