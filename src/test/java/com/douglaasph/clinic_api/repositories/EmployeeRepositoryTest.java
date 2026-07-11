package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.controllers.dto.employee.EmployeeDto;
import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestEntityFactory.class)
class EmployeeRepositoryTest {
    @Autowired
    EmployeeRepository repository;

    @Autowired
    TestEntityFactory testEntityFactory;

    @Test
    @DisplayName("Should return a list containing the employee when filtering by matching name and position")
    void findByOptionalFiltersCase1() {
        UserDto userDto = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        Employee employee = this.testEntityFactory.createEmployee(userDto, employeeDto);

        List<Employee> employeeList = this.repository.findByOptionalFilters("Douglas Phelipe", 2);

        assertThat(employeeList)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(employee);
    }

    @Test
    @DisplayName("Should return an empty list when no employees match the given name and position filters")
    void findByOptionalFiltersCase2() {
        List<Employee> employeeList = this.repository.findByOptionalFilters("Douglas Phelipe", 2);

        assertThat(employeeList).isEmpty();
    }
}