package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.config.aws.StorageGateway;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.models.entities.enums.*;
import com.douglaasph.clinic_api.repositories.AppointmentRepository;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.douglaasph.clinic_api.repositories.XRayReportRepository;
import com.douglaasph.clinic_api.services.dto.AppointmentDetailsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class XRayReportServiceTest {
    @Mock
    private XRayReportRepository xRayReportRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageGateway storageGateway;

    @InjectMocks
    private XRayReportService xRayReportService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should successfully create XRayReport, generate presigned URL and send notification")
    void createReportAndGenerateUploadUrl() {
        AppointmentDetailsDto dto = appointmentDetails();

        XRayReport report = new XRayReport(100L,
                "exams/any-generated-uuid.png",
                ProcessingStatus.PROCESSED_BY_IA.getCode(),
                null,
                null,
                false,
                dto.appointment());

        Mockito.when(xRayReportRepository.save(any(XRayReport.class))).thenReturn(report);
        Mockito.when(storageGateway.generatePresignedUploadUrl(any(String.class))).thenReturn("mocked-pressigned-url");

        String response = xRayReportService.createReportAndGenerateUploadUrl(dto.appointment());

        assertNotNull(response);
        assertEquals("mocked-pressigned-url", response);

        Mockito.verify(xRayReportRepository, Mockito.times(1)).save(any(XRayReport.class));
        Mockito.verify(storageGateway, Mockito.times(1)).generatePresignedUploadUrl(any(String.class));
    }

    @Test
    @DisplayName("Should throw an exception and not contact gateways when database persistence fails")
    void createReportAndGenerateUploadUrlCase2() {
        AppointmentDetailsDto dto = appointmentDetails();

        Appointment appointment = new Appointment(1L, dto.employee(), dto.patient(), LocalDateTime.now(), AppointmentStatus.SCHEDULED, AppointmentType.REPORT_REVIEW);

        Mockito.when(xRayReportRepository.save(any(XRayReport.class)))
                .thenThrow(new RuntimeException("Database connection failure"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            xRayReportService.createReportAndGenerateUploadUrl(appointment);
        });

        assertEquals("Database connection failure", exception.getMessage());

        Mockito.verifyNoInteractions(storageGateway);
    }

    @Test
    @DisplayName("Should successfully update diagnosis and status when doctor reviews the report")
    void reviewDoctorCase1() {
        AppointmentDetailsDto dto = appointmentDetails();

        Mockito.when(appointmentRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(dto.appointment()));
        Mockito.when(xRayReportRepository.findByAppointmentId(any(Long.class))).thenReturn(Optional.ofNullable(dto.xRayReport()));

        XRayReport response = xRayReportService.reviewDoctor(dto.appointment().getId(),
                "mocked-final-doctor-diagnosis");

        assertNotNull(response);
        assertEquals("mocked-final-doctor-diagnosis", response.getFinalMedicalDiagnosis());
        assertEquals(4, response.getProcessingStatus());
        assertEquals(4, response.getAppointment().getAppointmentStatus());
        assertTrue(response.isReleasedToPatient());

        Mockito.verify(appointmentRepository, Mockito.times(1)).save(any(Appointment.class));
        Mockito.verify(xRayReportRepository, Mockito.times(1)).save(any(XRayReport.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when appointment does not exist")
    void reviewDoctorCase2() {
        Long nonExistentId = 1L;
        String diagnosis = "Valid diagnosis";

        Mockito.when(appointmentRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            xRayReportService.reviewDoctor(nonExistentId, diagnosis);
        });

        Mockito.verify(xRayReportRepository, Mockito.never()).findByAppointmentId(any());
        Mockito.verify(appointmentRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when XRayReport does not exist for the appointment")
    void reviewDoctorCase3() {
        Long appointmentId = 1L;
        String diagnosis = "Valid diagnosis";
        Appointment mockAppointment = appointmentDetails().appointment();
        mockAppointment.setId(appointmentId);

        Mockito.when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(mockAppointment));

        Mockito.when(xRayReportRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            xRayReportService.reviewDoctor(appointmentId, diagnosis);
        });

        Mockito.verify(appointmentRepository, Mockito.never()).save(any());
        Mockito.verify(xRayReportRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Should return a list of XRayReports when found by patient email")
    void findAllByPatientIdAndReleasedToPatientTrueCase1() {
        AppointmentDetailsDto dto = appointmentDetails();
        String userEmail = dto.appointment().getPatient().getUser().getEmail();

        Mockito.when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(dto.appointment().getPatient().getUser()));
        Mockito.when(xRayReportRepository.findAllByAppointment_Patient_IdAndReleasedToPatientTrue(any(Long.class))).thenReturn(List.of(dto.xRayReport()));

        List<XRayReport> response = xRayReportService.findAllByPatientIdAndReleasedToPatientTrue(userEmail);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(dto.xRayReport().getId(), response.get(0).getId());

        Mockito.verify(xRayReportRepository, Mockito.times(1))
                .findAllByAppointment_Patient_IdAndReleasedToPatientTrue(any(Long.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient user is not found by email")
    void findAllByPatientIdAndReleasedToPatientTrueCase2() {
        String userEmail = appointmentDetails().appointment().getPatient().getUser().getEmail();

        Mockito.when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        Mockito.when(userRepository.findById(any(Long.class)))
                .thenThrow(new ResourceNotFoundException("User", "id", appointmentDetails().appointment().getPatient().getUser().getId()));

        RuntimeException exception = assertThrows(ResourceNotFoundException.class, () -> {
            xRayReportService.findAllByPatientIdAndReleasedToPatientTrue(userEmail);
        });

        assertEquals("User not found with email: 'example@gmail.com'", exception.getMessage());

        Mockito.verifyNoInteractions(xRayReportRepository);
    }

    AppointmentDetailsDto appointmentDetails() {
        User user1 = new User(1L, "Douglas Phelipe", "example@gmail.com", "1234", Roles.PATIENT);
        Patient patient = new Patient(1L, "12345678912", "81900000000", user1);

        User user2 = new User(2L, "Mirella Karla", "example2@gmail.com", "1234", Roles.EMPLOYEE);
        Employee employee = new Employee(1L, "12345678913", Position.DOCTOR, user2);

        Appointment appointment = new Appointment(1L, employee, patient, LocalDateTime.now(), AppointmentStatus.SCHEDULED, AppointmentType.REPORT_REVIEW);

        XRayReport xRayReport = new XRayReport(1L, "mocked-s3-key", 3, null, null, false, appointment);

        return new AppointmentDetailsDto(employee, patient, appointment, xRayReport);
    }
}