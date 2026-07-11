package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.employee.RegisterEmployeeDto;
import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.repositories.EmployeeRepository;
import com.douglaasph.clinic_api.exceptions.DatabaseException;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import com.douglaasph.clinic_api.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    public Employee register(RegisterEmployeeDto dto) {
        try {
            User user = new User(null,
                    dto.user().name(),
                    dto.user().email(),
                    encoder.encode(dto.user().password()),
                    Roles.valueOf(2));
            User savedUser = userRepository.save(user);

            Employee employee = new Employee(null,
                    dto.employee().licenseNumber(),
                    dto.employee().position(),
                    savedUser);

            return employeeRepository.save(employee);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Registration error: Email or document may already exist.");
        }
    }

    public List<Employee> findAll(String name, Position position) {
        return employeeRepository.findByOptionalFilters(name, position == null ? null : position.getCode());
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }
}
