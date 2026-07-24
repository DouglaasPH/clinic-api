package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.appointment.CreateAppointmentDto;
import com.douglaasph.clinic_api.exceptions.AppointmentConflictException;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentType;
import com.douglaasph.clinic_api.models.entities.enums.ProcessingStatus;
import com.douglaasph.clinic_api.repositories.*;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private XRayReportRepository xRayReportRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private XRayReportService xRayReportService;

    @Autowired
    private UserRepository userRepository;

    // Business Rule: Admins can fetch all records, while patients and doctors can only fetch records linked to them.
    public List<Appointment> findAll(String loggedEmail, boolean isAdmin) {
        if (isAdmin) {
            return appointmentRepository.findAll();
        } else {
            return appointmentRepository.findByPatientOrEmployeeEmail(loggedEmail);
        }
    }

    public List<Appointment> findAllAvailable() {
        return appointmentRepository.findByAppointmentStatusAndDateHourAfter(1, LocalDateTime.now());
    }



    @Transactional
    public Appointment insert(CreateAppointmentDto dto) throws BadRequestException {
        Employee employee = employeeRepository.findById(dto.employee_id()).orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.employee_id()));

        // TECHNICAL CODE IS 1 === EXAM_CAPTURE CODE IS 1
        // DOCTOR CODE IS 2 === REPORT_REVIEW CODE IS 2
        if (dto.type().getCode() != employee.getPosition().getCode()) {
            throw new BadRequestException("Employee's position incompatible with the type of inquiry.");
        }

        Appointment appointment = new Appointment(null, employee, null, dto.dateHour(), AppointmentStatus.AVAILABLE, dto.type());
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment book(Long appointmentId, String email) throws AppointmentConflictException {
        Long loggedPatientId = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(("user not found")))
                .getPatient()
                .getId();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (appointment.getPatient() != null) {
            throw new AppointmentConflictException("This time slot is already reserved by another patient.");
        }
        if (appointment.getStatus() != AppointmentStatus.AVAILABLE) {
            throw new AppointmentConflictException("This time slot is not available for scheduling.");
        }

        if (appointment.getType() == AppointmentType.REPORT_REVIEW) {
            boolean hasProcessedReport = xRayReportRepository.existsByAppointment_Patient_IdAndProcessingStatusNot(loggedPatientId, ProcessingStatus.VALIDATED_BY_DOCTOR.getCode());
            if (!hasProcessedReport) {
                throw new AppointmentConflictException("It is not possible to schedule a follow-up appointment without an X-ray exam in the system.");
            }
        }

        Patient patient = patientRepository.getReferenceById(loggedPatientId);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return appointmentRepository.save(appointment);
    }

    // Business rule: you can only cancel up to 24 hours before your scheduled appointment..
    @Transactional
    public Appointment cancel(Long appointmentId, String email) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));


        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(appointment.getDateHour().minusHours(24))) {
            throw new IllegalArgumentException("The appointment can only be cancelled with 24 hours' notice.");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public String startExamCapture(Long appointmentId, String email) throws AppointmentConflictException {
        Long loggedEmployeeId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
                .getPatient()
                .getId();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (appointment.getEmployee() == null || !appointment.getEmployee().getId().equals(loggedEmployeeId)) {
            throw new AppointmentConflictException("This appointment is not assigned to you.");
        }

        if (appointment.getType() != AppointmentType.EXAM_CAPTURE) {
            throw new AppointmentConflictException("This appointment scheduling is not intended for the collection of test samples.");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppointmentConflictException("The scheduled task must have the status SCHEDULED in order to be started.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return xRayReportService.createReportAndGenerateUploadUrl(appointment);
    }
}
