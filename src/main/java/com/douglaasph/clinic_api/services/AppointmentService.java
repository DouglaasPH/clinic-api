package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.appointment.CreateAppointmentDto;
import com.douglaasph.clinic_api.exceptions.AppointmentConflictException;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentType;
import com.douglaasph.clinic_api.models.entities.enums.ProcessingStatus;
import com.douglaasph.clinic_api.repositories.*;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final XRayReportRepository xRayReportRepository;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;
    private final XRayReportService xRayReportService;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, XRayReportRepository xRayReportRepository, PatientRepository patientRepository, EmployeeRepository employeeRepository, XRayReportService xRayReportService, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.xRayReportRepository = xRayReportRepository;
        this.patientRepository = patientRepository;
        this.employeeRepository = employeeRepository;
        this.xRayReportService = xRayReportService;
        this.userRepository = userRepository;
    }

    // Business Rule: Admins can fetch all records, while patients and doctors can only fetch records linked to them.
    public List<Appointment> findAll(Authentication authentication) {
        String loggedEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        if (isAdmin) {
            return appointmentRepository.findAll();
        } else {
            return appointmentRepository.findByPatientOrEmployeeEmail(loggedEmail);
        }
    }

    public List<Appointment> findAllAvailable() {
        return appointmentRepository.findByStatusAndDateHourAfter(AppointmentStatus.AVAILABLE, LocalDateTime.now());
    }



    public Appointment insert(CreateAppointmentDto dto) throws BadRequestException {
        Employee employee = employeeRepository.findById(dto.employee_id()).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if ((dto.type().getCode() == 1 && employee.getPosition().getCode() == 2) ||
                (dto.type().getCode() == 2 && employee.getPosition().getCode() == 1)) {
            throw new BadRequestException("Employee's position incompatible with the type of inquiry.");
        }

        Appointment appointment = new Appointment(null, employee, null, dto.dateHour(), AppointmentStatus.AVAILABLE, dto.type());
        return appointmentRepository.save(appointment);
    }

    public Appointment book(Long AppointmentId, Authentication authentication) throws AppointmentConflictException {
        Long loggedPatientId = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(("user not found")))
                .getPatient()
                .getId();

        Appointment appointment = appointmentRepository.findById(AppointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(AppointmentId));

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

    // Business rule: a patient can only cancel up to 24 hours before the scheduled appointment, but an admin can cancel at any time.
    public Appointment cancel(Long appointmentId, String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();

        try {
            Appointment appointment = appointmentRepository.getReferenceById(appointmentId);

            if (Objects.equals(appointment.getPatient().getId(), userId)) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(appointment.getDateHour().minusHours(24))) {
                    throw new IllegalArgumentException("The appointment can only be cancelled with 24 hours' notice.");
                }
            }

            appointment.setStatus(AppointmentStatus.CANCELED);
            return appointmentRepository.save(appointment);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(appointmentId);
        }
    }

    @Transactional
    public String startExamCapture(Long appointmentId, String email) throws AppointmentConflictException {
        Long loggedEmployeeId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(appointmentId));

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
