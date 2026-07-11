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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
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
