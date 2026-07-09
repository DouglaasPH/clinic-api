package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.models.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByName(String name);

    List<Employee> findByPosition(Integer position);
}
