package com.hrms.dao;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity(name = "courses")
@Data
public class CourseDao {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 50, nullable = false)
    String title;

    @Column(length = 500, nullable = false)
    String description;

    @Column(name = "duration_in_days", nullable = false)
    Integer durationInDays;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    Set<EmployeeDao> employees;
}
