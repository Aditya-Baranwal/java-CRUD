package com.hrms.dao;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity(name = "employee_daily_attendances")
@Data
public class EmployeeDailyAttendanceDao {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    EmployeeDao employee;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_date", nullable = false)
    private Date modifyDate;

    @PrePersist
    protected void onCreate() {
        createDate = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        modifyDate = new Date();
    }
}
