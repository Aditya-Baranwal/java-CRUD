package com.hrms.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity(name = "designations")
@Data
@NoArgsConstructor
public class DesignationDao {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 30, nullable = false)
    String tittle;

    @Column(name = "salary_band", length = 30, nullable = false)
    String SalaryBand;

    @NonNull
    @Column(name = "min_salary")
    Double minSalary;

    @NonNull
    @Column(name = "max_salary")
    Double maxSalary;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer level;

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
