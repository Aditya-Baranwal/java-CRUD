package com.hrms.repositories;

import com.hrms.dao.EmployeeDailyAttendanceDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface EmployeeDailyAttendanceRepository extends JpaRepository<EmployeeDailyAttendanceDao, Long> {

    @Query(
    value = "select * from employee_daily_attendances eda where create_date > :createdAt order by create_date",
    nativeQuery = true)
    Page<EmployeeDailyAttendanceDao> findAllForDay(Date createdAt, Pageable pageable);

}
