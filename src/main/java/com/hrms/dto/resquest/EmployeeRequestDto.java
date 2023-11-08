package com.hrms.dto.resquest;

import com.hrms.dao.DepartmentDao;
import com.hrms.dao.DesignationDao;
import com.hrms.dao.EmployeeDao;
import lombok.Data;

import java.util.Date;

@Data
public class EmployeeRequestDto {
    Date dob;
    String name;
    String contactNumber;
    String emailId;
    String designationId;
    String departmentId;
    String managerId;
    Double salary;
    Date dateOfJoining;

    public EmployeeDao toDao(DepartmentDao department, DesignationDao designation, EmployeeDao manager) {
        EmployeeDao employeeDao = new EmployeeDao();
        employeeDao.setDob(this.dob);
        employeeDao.setName(this.name);
        employeeDao.setContactNumber(this.contactNumber);
        employeeDao.setEmailId(this.emailId);
        employeeDao.setDesignation(designation);
        employeeDao.setDepartment(department);
        employeeDao.setManager(manager);
        employeeDao.setSalary(this.salary);
        employeeDao.setDateOfJoining(this.dateOfJoining);
        return employeeDao;
    }
}
