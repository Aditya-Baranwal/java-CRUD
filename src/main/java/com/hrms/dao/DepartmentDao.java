package com.hrms.dao;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity (name = "departments")
@Data
public class DepartmentDao {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 30, nullable = false)
    String name;

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
