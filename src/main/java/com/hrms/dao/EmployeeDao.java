package com.hrms.dao;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.Set;

@Data
@Entity(name = "employees")
@NoArgsConstructor
public class EmployeeDao {
     @Id
     @Column(updatable = false, nullable = false)
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

     @Column(nullable = false)
     Date dob;

     @Column(length = 100, nullable = false)
     String name;

     @Column(name = "contact_number", length = 10, nullable = true)
     String contactNumber;

     @Column(name = "email_id", nullable = false)
     String emailId;

     @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @JoinColumn(name = "designation_id", referencedColumnName = "id")
     DesignationDao designation;

     @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @JoinColumn(name="department_id", referencedColumnName="id")
     DepartmentDao department;

     @ManyToOne(cascade= CascadeType.ALL, fetch = FetchType.LAZY)
     @JoinColumn(name="manager_id", referencedColumnName="id")
     EmployeeDao manager;

     @ManyToMany(fetch = FetchType.LAZY)
     @JoinTable(
             name = "employee_course_mapping",
             joinColumns = @JoinColumn(name = "employee_id"),
             inverseJoinColumns = @JoinColumn(name = "course_id"))
     Set<CourseDao> courses;

     @Column(name = "salary", nullable = false)
     Double salary;

     @Column(name = "date_of_joining", nullable = false)
     Date dateOfJoining;

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
