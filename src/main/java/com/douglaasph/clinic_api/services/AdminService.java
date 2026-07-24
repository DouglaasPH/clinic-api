package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.admin.AppointmentManagementAdminDto;
import com.douglaasph.clinic_api.controllers.dto.admin.DashboardAdminMetricsDto;
import com.douglaasph.clinic_api.controllers.dto.admin.EmployeesManagementMetricsDto;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AdminService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private XRayReportRepository xRayReportRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    public DashboardAdminMetricsDto dashboardMetrics() {
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        LocalDate today = LocalDate.now(zoneId);
        Instant startOfDay = today.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = today.atTime(23, 59, 59, 999_999_999).atZone(zoneId).toInstant();

        Integer numberOfExamsPerformed = this.xRayReportRepository.findByCreatedAtBetween(startOfDay, endOfDay).size();
        Integer availableAppointmentSlots = this.appointmentRepository.findByAppointmentStatusAndDateHourAfter(1, LocalDateTime.now()).size();
        Integer totalNumberOfPatients = this.patientRepository.findAll().size();

        return new DashboardAdminMetricsDto(numberOfExamsPerformed, availableAppointmentSlots, totalNumberOfPatients);
    }

    public EmployeesManagementMetricsDto employeesManagementMetrics() {
        Integer numberOfEmployees = this.employeeRepository.findAll().size();
        Integer numberOfDoctors = this.employeeRepository.findByOptionalFilters(null, 2).size();
        Integer numberOfTechnicians = this.employeeRepository.findByOptionalFilters(null, 1).size();

        return new EmployeesManagementMetricsDto(numberOfEmployees, numberOfDoctors, numberOfTechnicians);
    }

    public Page<User> findAllEmployeesForManagementWithPagination(String name, Position position, int page) {
        PageRequest pageable = PageRequest.of(page, 4);
        Integer positionCode = (position == null) ? null : position.getCode();
        return userRepository.findEmployeesWithFilters(name, positionCode, pageable);
    }

    public Page<AppointmentManagementAdminDto> findAllAppointmentsForManagementWithPagination(AppointmentStatus status, Integer page) {
        PageRequest pageable = PageRequest.of(page, 4);
        Integer statusCode = (status == null) ? null : status.getCode();
        return appointmentRepository.findAllAppointmentsManagement(statusCode, pageable);
    }
}
