package com.douglaasph.clinic_api.util;

import com.douglaasph.clinic_api.controllers.dto.appointment.CreateAppointmentDto;
import com.douglaasph.clinic_api.controllers.dto.employee.EmployeeDto;
import com.douglaasph.clinic_api.controllers.dto.patient.PatientDto;
import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.ProcessingStatus;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@TestComponent
public class TestEntityFactory {
    @Autowired
    private EntityManager entityManager;

    public User createUser(UserDto dataUser, Roles role) {
        User newUser = new User(null, dataUser.name(), dataUser.email(), dataUser.password(), role);
        this.entityManager.persist(newUser);
        return newUser;
    }

    public Patient createPatient(UserDto dataUser, PatientDto dataPatient) {
        User newUser = createUser(dataUser, Roles.PATIENT);

        Patient newPatient = new Patient(null, dataPatient.cpf(), dataPatient.phone(), newUser);
        this.entityManager.persist(newPatient);

        return newPatient;
    }

    public Employee createEmployee(UserDto dataUser, EmployeeDto dataEmployee) {
        User newUser = createUser(dataUser, Roles.EMPLOYEE);

        Employee newEmployee = new Employee(null, dataEmployee.licenseNumber(), dataEmployee.position(), newUser);
        this.entityManager.persist(newEmployee);
        return newEmployee;
    }

    public Appointment createAppointment(Patient patient, Employee employee, CreateAppointmentDto data) {
        Appointment newAppointment = new Appointment(null, employee, patient, data.dateHour(), AppointmentStatus.AVAILABLE, data.type());
        this.entityManager.persist(newAppointment);
        return newAppointment;
    }

    public XRayReport createXRayReport(Appointment appointment, boolean releasedToPatient) {
        String s3Key = "exams/" + UUID.randomUUID() + ".png";
        XRayReport report = new XRayReport();
        report.setAppointment(appointment);
        report.setS3Key(s3Key);
        report.setProcessingStatus(ProcessingStatus.AWAITING_AI.getCode());
        report.setReleasedToPatient(releasedToPatient);
        this.entityManager.persist(report);
        return  report;
    }

    public RefreshToken createRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken(null, token, user, Instant.now().plus(1, ChronoUnit.DAYS));
        this.entityManager.persist(refreshToken);
        return refreshToken;
    }
}
