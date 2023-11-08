package com.hrms.controllers;

import com.hrms.dao.EmployeeDao;
import com.hrms.domain.EmployeeDomain;
import com.hrms.dto.resquest.EmployeeRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    EmployeeDomain employeeDomain;

    @GetMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmployeeDao get(@PathVariable("id") String id) {
        try {
            List<EmployeeDao> result = this.employeeDomain.read(Long.parseLong(id), null, null);
            if(result.size() == 0) return null;
            return result.get(0);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping(value="", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<EmployeeDao> getAll(@RequestParam("page") String pageNumber, @RequestParam("pageSize") String pageSize) {
        try {
            List<EmployeeDao> result = this.employeeDomain.read(null, Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmployeeDao post(@RequestBody EmployeeRequestDto employee) {
        try {
            EmployeeDao employee1 = this.employeeDomain.create(employee);
            return employee1;
        } catch (Exception e) {
            throw e;
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public EmployeeDao put(@RequestBody EmployeeRequestDto employee, @PathVariable("id") String id) {
        try {
            EmployeeDao updatedEmployee = this.employeeDomain.update(Long.parseLong(id), employee);
            return updatedEmployee;
        } catch (Exception e) {
            throw e;
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long delete(@PathVariable("id") Long id) {
        try {
            Long employeeId = this.employeeDomain.delete(id);
            return employeeId;
        } catch (Exception e) {
            throw e;
        }
    }

}
