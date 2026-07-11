package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.controllers.dto.appointment.CreateAppointmentDto;
import com.douglaasph.clinic_api.controllers.dto.employee.EmployeeDto;
import com.douglaasph.clinic_api.controllers.dto.patient.PatientDto;
import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.models.entities.enums.*;
import com.douglaasph.clinic_api.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestEntityFactory.class)
class XRayReportRepositoryTest {
    @Autowired
    XRayReportRepository xRayReportRepository;

    @Autowired
    private TestEntityFactory testEntityFactory;

    @Test
    @DisplayName("Should return true when a report exists for the patient with a different processing status")
    void existsByAppointment_Patient_IdAndProcessingStatusNotCase1() {
        UserDto userDto = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        PatientDto patientDto = new PatientDto("12345678912", "81900000000");
        Patient patient = this.testEntityFactory.createPatient(userDto, patientDto);

        UserDto userDto2 = new UserDto("Mirella Karla", "example2@gmail.com", "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        Employee employee = this.testEntityFactory.createEmployee(userDto2, employeeDto);

        CreateAppointmentDto createAppointmentDto = new CreateAppointmentDto(employee.getId(), LocalDateTime.now(), AppointmentType.REPORT_REVIEW);

        Appointment appointment = this.testEntityFactory.createAppointment(patient, employee, createAppointmentDto);
        this.testEntityFactory.createXRayReport(appointment, false);

        boolean result = this.xRayReportRepository.existsByAppointment_Patient_IdAndProcessingStatusNot(patient.getId(),ProcessingStatus.PROCESSED_BY_IA.getCode());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when no reports exist for the given patient")
    void existsByAppointment_Patient_IdAndProcessingStatusNotCase2() {
        boolean result = this.xRayReportRepository.existsByAppointment_Patient_IdAndProcessingStatusNot(1L,ProcessingStatus.PROCESSED_BY_IA.getCode());

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should successfully return the report when searching by its linked appointment ID")
    void findByAppointmentIdCase3() {
        UserDto userDto = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        PatientDto patientDto = new PatientDto("12345678912", "81900000000");
        Patient patient = this.testEntityFactory.createPatient(userDto, patientDto);

        UserDto userDto2 = new UserDto("Mirella Karla", "example2@gmail.com", "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        Employee employee = this.testEntityFactory.createEmployee(userDto2, employeeDto);

        CreateAppointmentDto createAppointmentDto = new CreateAppointmentDto(employee.getId(), LocalDateTime.now(), AppointmentType.REPORT_REVIEW);

        Appointment appointment = this.testEntityFactory.createAppointment(patient, employee, createAppointmentDto);
        this.testEntityFactory.createXRayReport(appointment, false);

        Optional<XRayReport> result = this.xRayReportRepository.findByAppointmentId(appointment.getId());

        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should return an empty Optional when searching by a non-existent appointment ID")
    void findByAppointmentIdCase4() {
        Optional<XRayReport> result = this.xRayReportRepository.findByAppointmentId(1L);

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should list reports when they belong to the patient and have been released")
    void findAllByAppointment_Patient_IdAndReleasedToPatientTrueCase5() {
        UserDto userDto = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        PatientDto patientDto = new PatientDto("12345678912", "81900000000");
        Patient patient = this.testEntityFactory.createPatient(userDto, patientDto);

        UserDto userDto2 = new UserDto("Mirella Karla", "example2@gmail.com", "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        Employee employee = this.testEntityFactory.createEmployee(userDto2, employeeDto);

        CreateAppointmentDto createAppointmentDto = new CreateAppointmentDto(employee.getId(), LocalDateTime.now(), AppointmentType.REPORT_REVIEW);

        Appointment appointment = this.testEntityFactory.createAppointment(patient, employee, createAppointmentDto);
        this.testEntityFactory.createXRayReport(appointment, true);

        List<XRayReport> result = this.xRayReportRepository.findAllByAppointment_Patient_IdAndReleasedToPatientTrue(appointment.getId());

        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Should return an empty list when the patient's report has not been released yet")
    void findAllByAppointment_Patient_IdAndReleasedToPatientTrueCase6() {
        UserDto userDto = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        PatientDto patientDto = new PatientDto("12345678912", "81900000000");
        Patient patient = this.testEntityFactory.createPatient(userDto, patientDto);

        UserDto userDto2 = new UserDto("Mirella Karla", "example2@gmail.com", "1234");
        EmployeeDto employeeDto = new EmployeeDto("12345678913", Position.DOCTOR);
        Employee employee = this.testEntityFactory.createEmployee(userDto2, employeeDto);

        CreateAppointmentDto createAppointmentDto = new CreateAppointmentDto(employee.getId(), LocalDateTime.now(), AppointmentType.REPORT_REVIEW);

        Appointment appointment = this.testEntityFactory.createAppointment(patient, employee, createAppointmentDto);
        this.testEntityFactory.createXRayReport(appointment, false);

        List<XRayReport> result = this.xRayReportRepository.findAllByAppointment_Patient_IdAndReleasedToPatientTrue(appointment.getId());

        assertThat(result.isEmpty()).isTrue();
    }
}