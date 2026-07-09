package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.repositories.EmployeeRepository;
import com.douglaasph.clinic_api.exceptions.DatabaseException;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> findAll(String name, Position position) {
        return repository.findByOptionalFilters(name, position == null ? null : position.getCode());
    }

    public Employee findById(Long id) {
        Optional<Employee> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException((id)));
    }

    public Employee insert(Employee obj) {
        try {
            return repository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
