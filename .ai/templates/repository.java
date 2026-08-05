package ${basePackage}.repository;

import ${basePackage}.entity.${Entity};
import ${basePackage}.entity.${Entity}Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ${Entity}.
 *
 * Responsibilities
 * - CRUD operations
 * - Query methods
 * - Pagination
 * - Specifications
 * - Optimized fetching
 *
 * Must NOT contain business logic.
 */
public interface ${Entity}Repository
        extends JpaRepository<${Entity}, Long>,
        JpaSpecificationExecutor<${Entity}> {

        /**
         * Find by primary key and active status.
         */
        Optional<${Entity}> findByIdAndStatus(
        Long id,
        ${Entity}Status status
        );

        /**
         * Check existence.
         */
        boolean existsByName(
        String name
        );

        /**
         * Count records by status.
         */
        long countByStatus(
        ${Entity}Status status
        );

        /**
         * Paginated search by status.
         */
        Page<${Entity}> findByStatus(
        ${Entity}Status status,
        Pageable pageable
        );

        /**
         * Recent records.
         */
        List<${Entity}> findTop10ByOrderByCreatedAtDesc();

        /**
         * Records created after timestamp.
         */
        List<${Entity}> findByCreatedAtAfter(
        LocalDateTime createdAfter
        );

/**
 * Fetch related entities to avoid N+1.
 */
@EntityGraph(attributePaths = {
        "owner"
})
    Optional<${Entity}> findWithOwnerById(
        Long id
        );

/**
 * Example JPQL query.
 */
@Query("""
            SELECT e
            FROM ${Entity} e
            WHERE e.status = :status
              AND LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<${Entity}> search(
@Param("keyword") String keyword,
@Param("status") ${Entity}Status status,
        Pageable pageable
        );

}