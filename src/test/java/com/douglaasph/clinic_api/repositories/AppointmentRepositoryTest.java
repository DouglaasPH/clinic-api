package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.controllers.dto.appointment.CreateAppointmentDto;
import com.douglaasph.clinic_api.controllers.dto.employee.EmployeeDto;
import com.douglaasph.clinic_api.controllers.dto.patient.PatientDto;
import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentType;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestEntityFactory.class)
class AppointmentRepositoryTest {
    @Autowired
    AppointmentRepository repository;

    @Autowired
    TestEntityFactory testEntityFactory;

    @Test
    @DisplayName("Should return a list containing the appointment when searching by a matching patient or employee email")
    void findByPatientOrEmployeeEmailCase1() {
        String emailPatient = "example@gmail.com";
        UserDto userDto = new UserDto("Douglas Phelipe", emailPatient, "1234");
        PatientDto patientDto = new PatientDto("12345678912", "81900000000");
        Patient patient = this.testEntityFactory.createPatient(userDto, patientDto);

        String emailEmployee = "example2@gmail.com";
        UserDto userDto2 = new UserDto("Mirella Karla", emailEmployee, "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        Employee employee = this.testEntityFactory.createEmployee(userDto2, employeeDto);

        CreateAppointmentDto createAppointmentDto = new CreateAppointmentDto(employee.getId(), LocalDateTime.now(), AppointmentType.REPORT_REVIEW);
        Appointment appointment = this.testEntityFactory.createAppointment(patient, employee, createAppointmentDto);

        List<Appointment> byPatientEmailList = this.repository.findByPatientOrEmployeeEmail(emailPatient);
        List<Appointment> byEmployeeEmailList = this.repository.findByPatientOrEmployeeEmail(emailEmployee);

        assertThat(byPatientEmailList)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(appointment);

        assertThat(byEmployeeEmailList)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(appointment);
    }

    @Test
    @DisplayName("Should return empty lists when no appointments match the given patient or employee emails")
    void findByPatientOrEmployeeEmailCase2() {
        String emailPatient = "example@gmail.com";
        String emailEmployee = "example2@gmail.com";

        List<Appointment> byPatientEmailList = this.repository.findByPatientOrEmployeeEmail(emailPatient);
        List<Appointment> byEmployeeEmailList = this.repository.findByPatientOrEmployeeEmail(emailEmployee);

        assertThat(byPatientEmailList).isEmpty();

        assertThat(byEmployeeEmailList).isEmpty();
    }
}