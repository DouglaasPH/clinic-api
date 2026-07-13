package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.employee.EmployeeDto;
import com.douglaasph.clinic_api.controllers.dto.employee.RegisterEmployeeDto;
import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.exceptions.DatabaseException;
import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.repositories.EmployeeRepository;
import com.douglaasph.clinic_api.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should register employee successfully when data is valid")
    void registerCase1() {
        UserDto userDto = new UserDto("Mirella Karla", "example2@gmail.com", "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        RegisterEmployeeDto registerEmployeeDto = new RegisterEmployeeDto(userDto, employeeDto);

        Mockito.doReturn("mocked-encode").when(encoder).encode("1234");

        User user = new User(null, userDto.name(), userDto.email(), "mocked-encode", Roles.EMPLOYEE);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        Employee employee = new Employee(null, employeeDto.licenseNumber(), employeeDto.position(), user);
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee response = employeeService.register(registerEmployeeDto);

        assertNotNull(response);
        assertEquals(employee.getLicenseNumber(), response.getLicenseNumber());
        assertEquals(employee.getPosition(), response.getPosition());
        assertEquals(employee.getUser().getEmail(), response.getUser().getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verify(employeeRepository, Mockito.times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DatabaseException when email or document already exists")
    void registerCase2() {
        UserDto userDto = new UserDto("Mirella Karla", "example2@gmail.com", "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        RegisterEmployeeDto registerEmployeeDto = new RegisterEmployeeDto(userDto, employeeDto);

        Mockito.doReturn("mocked-encode").when(encoder).encode("1234");

        Mockito.when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        DatabaseException exception = assertThrows(DatabaseException.class, () -> {
            employeeService.register(registerEmployeeDto);
        });

        assertEquals("Registration error: Email or document may already exist.", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verifyNoInteractions(employeeRepository);
    }
}