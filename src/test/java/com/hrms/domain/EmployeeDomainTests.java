package com.hrms.domain;

import com.hrms.dao.DepartmentDao;
import com.hrms.dao.DesignationDao;
import com.hrms.dao.EmployeeDao;
import com.hrms.dto.MailDataDto;
import com.hrms.dto.resquest.EmployeeRequestDto;
import com.hrms.repositories.DepartmentJpaRepository;
import com.hrms.repositories.DesignationJpaRepository;
import com.hrms.repositories.EmployeeJpaRepository;
import com.hrms.services.MailerService;
import com.hrms.util.Constant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;

/**
 * @ExtendWith
 * @InjectMocks
 * @Mock
 * @BeforeAll
 * @BeforeEach
 * @AfterEach
 * @AfterAll
 * @Nested
 * @DisplayName
 * @Test
 * @Disabled
 * @DisplayName
 * @TestPropertySource
 * @SpringbootTest - The @SpringBootTest annotation is useful when we need to bootstrap the entire
 * container. The annotation works by creating the ApplicationContext that will be utilized in our tests
 * @TestConfiguration -
 * @Import - Configuration classes annotated with @TestConfiguration are excluded from component scanning,
 * therefore we need to import it explicitly in every test where we want to @Autowire it
 * @DataJpaTest
 * @WebMvcTest
 */


// @TestPropertySource(properties = {"spring.mail.username=baranwal.adi@gmail.com"})
@ExtendWith(MockitoExtension.class)
public class EmployeeDomainTests {

    @InjectMocks
    EmployeeDomain employeeDomain;

    // dependecies
    @Mock
    static EmployeeJpaRepository employeeJpaRepositoryMock;
    @Mock
    static DepartmentJpaRepository departmentJpaRepositoryMock;
    @Mock
    static DesignationJpaRepository designationJpaRepositoryMock;
    @Mock
    static MailerService mailerServiceMock;
    @Mock
    static EmployeeRequestDto employeeRequestDtoMock;

    // data required
    static EmployeeRequestDto employeeRequestDto;
    static MailDataDto mailDataDto;
    static EmployeeDao employeeDao;
    static EmployeeDao createdEmployeeDao;

    @BeforeAll
    public static void intialSetup() {
        // injects @value
        // ReflectionTestUtils.setField(EmployeeDomain.class, "spring.mail.username", "baranwal.adi@gmail.com");

        employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setDob(new Date());
        employeeRequestDto.setName("Adiitya Baranwal");
        employeeRequestDto.setContactNumber("9702025345");
        employeeRequestDto.setEmailId("baranwal.adi@gmail.com");
        employeeRequestDto.setSalary(1000.0);
        employeeRequestDto.setDateOfJoining(new Date());

        mailDataDto = new MailDataDto(
                employeeRequestDto.getEmailId(),
                "baranwal.adi@gmail.com",
                Constant.ONBOARD_MAIL_TITLE,
                Constant.ONBOARD_MAIL_BODY
        );

        employeeDao = new EmployeeDao();
        employeeDao.setDob(employeeRequestDto.getDob());
        employeeDao.setName(employeeRequestDto.getName());
        employeeDao.setContactNumber(employeeRequestDto.getContactNumber());
        employeeDao.setEmailId(employeeRequestDto.getEmailId());
        employeeDao.setDesignation(null);
        employeeDao.setDepartment(null);
        employeeDao.setManager(null);
        employeeDao.setSalary(employeeRequestDto.getSalary());
        employeeDao.setDateOfJoining(employeeRequestDto.getDateOfJoining());

        createdEmployeeDao = new EmployeeDao();
        createdEmployeeDao.setId(1L);
        createdEmployeeDao.setDob(employeeRequestDto.getDob());
        createdEmployeeDao.setName(employeeRequestDto.getName());
        createdEmployeeDao.setContactNumber(employeeRequestDto.getContactNumber());
        createdEmployeeDao.setEmailId(employeeRequestDto.getEmailId());
        createdEmployeeDao.setDesignation(null);
        createdEmployeeDao.setDepartment(null);
        createdEmployeeDao.setManager(null);
        createdEmployeeDao.setCourses(null);
        createdEmployeeDao.setSalary(employeeRequestDto.getSalary());
        createdEmployeeDao.setDateOfJoining(employeeRequestDto.getDateOfJoining());
        createdEmployeeDao.setCreateDate(new Date());
        createdEmployeeDao.setModifyDate(new Date());
    }

    @Nested
    @DisplayName("employee create method")
    class CreateEmployeeMethod {

        @Test
        @DisplayName("successful creation of employee without - department, designation, manager")
        public void testCreate_1() throws Exception {
            doNothing().when(mailerServiceMock).sendMail(isA(MailDataDto.class));
            Mockito.when(employeeJpaRepositoryMock.save(employeeDao)).thenReturn(createdEmployeeDao);
            EmployeeDao emp = employeeDomain.create(employeeRequestDto);
            assertEquals(emp, createdEmployeeDao);
        }

        @Test
        @Disabled
        @DisplayName("successful creation of employee with - department, designation, manager")
        public void testCreate_2() throws Exception {
            Mockito.when(departmentJpaRepositoryMock.findById(null)).thenReturn(Optional.of(new DepartmentDao()));
            Mockito.when(designationJpaRepositoryMock.findById(null)).thenReturn(Optional.of(new DesignationDao()));
            Mockito.when(employeeJpaRepositoryMock.findById(null)).thenReturn(Optional.of(new EmployeeDao()));
            doNothing().when(mailerServiceMock).sendMail(isA(MailDataDto.class));
//        Mockito.when(employeeRequestDtoMock.toDao(null, null, null)).thenReturn(employeeDao);
//        PowerMockito.whenNew(MailDataDto.class).withAnyArguments().thenReturn(mailDataDto);
            Mockito.when(employeeJpaRepositoryMock.save(employeeDao)).thenReturn(createdEmployeeDao);
            EmployeeDao emp = employeeDomain.create(employeeRequestDto);
            assertEquals(emp, createdEmployeeDao);
        }
    }



}
