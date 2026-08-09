package com.aditya.lms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * JPA Entity representing a Course.
 * - Table: course
 * - Uses sequence-based id generation (PostgreSQL)
 * - Stores tags as JSONB
 * - Includes audit fields (createdBy, updatedBy)
 */
@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_seq")
    @SequenceGenerator(name = "course_seq", sequenceName = "course_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String title;

    @Column(name = "description", length = 200)
    @Size(max = 200)
    private String description;

    /**
     * Stored as JSONB in PostgreSQL. Example: ["java","spring"]
     */
    @Column(name = "tags", columnDefinition = "jsonb default '[]'")
    private String tags;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Version
    @Column(name = "version")
    private Long version;
}
