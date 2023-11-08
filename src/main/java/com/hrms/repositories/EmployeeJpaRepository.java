package com.hrms.repositories;

import com.hrms.dao.EmployeeDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeJpaRepository extends JpaRepository<EmployeeDao, Long> {
    /**
     * flush()
     * saveAndFlush()
     * saveAllAndFlush()
     * deleteAllInBatch()
     * deleteAllByIdInBatch()
     * deleteAllInBatch()
     * getOne()
     * getById()
     * getReferenceById()
     * findOne()
     * findAll(entityQuery)
     * findAll(entityQuery, sort)
     * findAll(entityQuery, pageable)
     * count()
     * exists()
     * findBy()
     * save()
     * saveAll()
     * findById()
     * existsById()
     * findAll()
     * findAllById()
     * count()
     * deleteById()
     * delete()
     * deleteAllById()
     * deleteAll()
     * deleteAll()
     * findAll(sort)
     * findAll(pageable)
     */

    @Query(
            value = "select email_id from employees where id in :Ids",
            nativeQuery = true
    )
    List<String> findAllById(List<Long> Ids);
}