package com.aditya.lms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aditya.lms.enums.ContentType;

import java.time.OffsetDateTime;

/**
 * JPA Entity representing a Lesson (learning material within a Module).
 * - Table: lesson
 * - Tracks parent Module relationship
 * - Maintains sequence ordering within a module
 * - Stores content type and link
 * - Includes audit fields (createdBy, updatedBy)
 * - Revision tracking enabled via Envers
 */
@Entity
@Table(name = "lesson")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lesson_seq")
    @SequenceGenerator(name = "lesson_seq", sequenceName = "lesson_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(name = "content_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(name = "content_link", nullable = false, length = 255)
    private String contentLink;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

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
