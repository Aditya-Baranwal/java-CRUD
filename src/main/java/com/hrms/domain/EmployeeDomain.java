package com.hrms.domain;

import com.hrms.dao.DepartmentDao;
import com.hrms.dao.DesignationDao;
import com.hrms.dto.MailDataDto;
import com.hrms.dto.resquest.EmployeeRequestDto;
import com.hrms.repositories.DepartmentJpaRepository;
import com.hrms.repositories.EmployeeJpaRepository;
import com.hrms.services.MailerService;
import com.hrms.dao.EmployeeDailyAttendanceDao;
import com.hrms.dao.EmployeeDao;
import com.hrms.repositories.DesignationJpaRepository;
import com.hrms.repositories.EmployeeDailyAttendanceRepository;
import com.hrms.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeDomain {

    @Autowired
    EmployeeJpaRepository employeeJpaRepository;

    @Autowired
    DepartmentJpaRepository departmentJpaRepository;

    @Autowired
    DesignationJpaRepository designationJpaRepository;

    @Autowired
    MailerService mailerService;

    @Value("spring.mail.username")
    String senderMailId;

    @Autowired
    EmployeeDailyAttendanceRepository employeeDailyAttendanceRepository;

    public EmployeeDao create(EmployeeRequestDto employee) {
        try {
            DepartmentDao department = null;
            DesignationDao designation  = null;
            EmployeeDao manager = null;

            if(employee.getDepartmentId() != null) {
                department = departmentJpaRepository.findById(Long.parseLong(employee.getDepartmentId())).get();
            }

            if(employee.getDesignationId() != null) {
                designation = this.designationJpaRepository.findById(Long.parseLong(employee.getDesignationId())).get();
            }

            if(employee.getManagerId() != null) {
                manager = this.employeeJpaRepository.findById(Long.parseLong(employee.getManagerId())).get();
            }

            EmployeeDao newEmployee = this.employeeJpaRepository.save(employee.toDao(department, designation, manager));
            MailDataDto mailDetails = new MailDataDto(
                   newEmployee.getEmailId(),
                   senderMailId,
                    Constant.ONBOARD_MAIL_TITLE,
                    Constant.ONBOARD_MAIL_BODY
            );
            mailerService.sendMail(mailDetails);
            return newEmployee;

        } catch (Exception e) {
            throw e;
        }
    }

    public List<EmployeeDao> read(Long employeeId, Integer page, Integer pageSize) {
        try {
            if(employeeId == null) {
                Pageable pageDetail = PageRequest.of(page, pageSize);
                Page<EmployeeDao> employees =  this.employeeJpaRepository.findAll(pageDetail);
                return employees.get().toList();
            } else {
                EmployeeDao employee = this.employeeJpaRepository.findById(employeeId).get();
                List<EmployeeDao> employees = new ArrayList<>();
                employees.add(employee);
                return employees;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public EmployeeDao update(Long id, EmployeeRequestDto employee) {
        try {
            DepartmentDao department = null;
            DesignationDao designation = null;
            EmployeeDao manager = null;
            if(employee.getDepartmentId() != null) {
                department = this.departmentJpaRepository.findById(Long.parseLong(employee.getDepartmentId())).get();
            }

            if(employee.getDesignationId() != null) {
                designation = this.designationJpaRepository.findById(Long.parseLong(employee.getDesignationId())).get();
            }

            if(employee.getManagerId() != null) {
                manager = this.employeeJpaRepository.findById(Long.parseLong(employee.getManagerId())).get();
            }

            return  this.employeeJpaRepository.save(employee.toDao(department, designation, manager));
        } catch (Exception e) {
            throw e;
        }
    }

    public Long delete(Long employeeId) {
        try {
            this.employeeJpaRepository.deleteById(employeeId);
            return employeeId;
        } catch (Exception e) {
            throw e;
        }
    }

    @Scheduled(cron = "0 * 20 * * *")
    public void sendLogoutReminder() {
        try {
            HashMap<Long, Integer> countMap = new HashMap<>();
            int page = 0, pageSize = 50;
            while(true) {
                Pageable pageable = PageRequest.of(page, pageSize);
                Long time = new Date().getTime();
                Date today = new Date(time - time % (24 * 60 * 60 * 1000));
                Page<EmployeeDailyAttendanceDao> todaysLog =  employeeDailyAttendanceRepository.findAllForDay(today, pageable);
                if(todaysLog.get().count() == 0) break;
                todaysLog.get().toList().forEach(log -> {
                    Long employeeId = log.getEmployee().getId();
                    if(countMap.containsKey(employeeId)) {
                        if(log.getAction().equalsIgnoreCase("login")) {
                            countMap.put(employeeId, countMap.get(employeeId)+1);
                        } else {
                            countMap.put(employeeId, countMap.get(employeeId)-1);
                        }
                    } else {
                        if(log.getAction().equalsIgnoreCase("login")) {
                            countMap.put(employeeId, 1);
                        } else {
                            countMap.put(employeeId, -1);
                        }
                    }
                });
                ++page;
            }
            List<Long> employeeToLogout = countMap.entrySet().stream()
                    .filter(entry -> entry.getValue() != 0)
                    .map(Map.Entry::getKey).toList();

            List<String> receivingMailId = employeeJpaRepository.findAllById(employeeToLogout);
            receivingMailId.forEach(mailId -> {
                MailDataDto mailDetails = new MailDataDto(
                        mailId,
                        senderMailId,
                        Constant.LOGOUT_REMINDER_MAIL_TITLE,
                        Constant.LOGOUT_REMINDER_MAIL_BODY
                );

                mailerService.sendMail(mailDetails);
            });

            System.out.println("Mail send to :\n" + Arrays.toString(receivingMailId.toArray()));
        } catch (Exception ex) {
            throw ex;
        }
    }
}
