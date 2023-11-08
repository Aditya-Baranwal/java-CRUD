package com.hrms.repositories;

import com.hrms.dao.DepartmentDao;
import com.hrms.dao.DesignationDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignationJpaRepository extends JpaRepository<DesignationDao, Long> {
}
