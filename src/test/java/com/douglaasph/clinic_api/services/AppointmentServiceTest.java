package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.appointment.CreateAppointmentDto;
import com.douglaasph.clinic_api.exceptions.AppointmentConflictException;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentType;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.repositories.*;
import com.douglaasph.clinic_api.services.dto.AppointmentDetailsDto;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {
    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private XRayReportRepository xRayReportRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should create and return an appointment successfully when employee is found and position is compatible")
    void insertCase1() throws BadRequestException {
        CreateAppointmentDto dto = createAppointmentDto();
        AppointmentDetailsDto appointmentDetailsDto = appointmentDetails();
        Employee employee = appointmentDetailsDto.employee();

        Mockito.when(employeeRepository.findById(dto.employee_id())).thenReturn(Optional.of(employee));

        Appointment savedAppointment = new Appointment(100L, employee, null, dto.dateHour(), AppointmentStatus.AVAILABLE, dto.type());
        Mockito.when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        Appointment result = appointmentService.insert(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1, result.getAppointmentStatus());
        assertEquals(dto.type(), result.getType());
        assertNull(result.getPatient());

        Mockito.verify(employeeRepository, Mockito.times(1)).findById(dto.employee_id());
        Mockito.verify(appointmentRepository, Mockito.times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee does not exist")
    void insertCase2() {
        CreateAppointmentDto dto = createAppointmentDto();

        Mockito.when(employeeRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.insert(dto);
        });

        assertEquals("Employee not found with id: '1'", exception.getMessage());

        Mockito.verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should throw BadRequestException when employee position is incompatible with appointment type")
    void insertCase3() {
        LocalDateTime now = LocalDateTime.now();
        CreateAppointmentDto incompatibleDto = new CreateAppointmentDto(1L, now, AppointmentType.EXAM_CAPTURE);

        User user = new User(2L, "Mirella Karla", "example2@gmail.com", "1234", Roles.EMPLOYEE);
        Employee incompatibleEmployee = new Employee(1L, "12345678913", Position.DOCTOR, user);

        Mockito.when(employeeRepository.findById(any(Long.class))).thenReturn(Optional.of(incompatibleEmployee));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.insert(incompatibleDto);
        });

        assertEquals("Employee's position incompatible with the type of inquiry.", exception.getMessage());

        Mockito.verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should book an appointment successfully when slot is available and requirements are met")
    void bookCase1() throws AppointmentConflictException {
        String email = "douglas@example.com";
        Long appointmentId = 1L;

        User user = new User(1L, "Douglas", email, "123", Roles.PATIENT);
        Patient patient = new Patient(10L, "12345678912", "81999999999", user);
        user.setPatient(patient);

        Employee doctor = new Employee(1L, "123456", Position.DOCTOR, new User());
        Appointment appointment = new Appointment(appointmentId, doctor, null, LocalDateTime.now(), AppointmentStatus.AVAILABLE, AppointmentType.EXAM_CAPTURE);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        Mockito.when(patientRepository.getReferenceById(10L)).thenReturn(patient);
        Mockito.when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.book(appointmentId, email);

        assertNotNull(result);
        assertEquals(patient, result.getPatient());
        Mockito.verify(appointmentRepository, Mockito.times(1)).save(appointment);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when scheduling user is not found")
    void bookCase2() {
        String email = "notfound@example.com";
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            appointmentService.book(1L, email);
        });

        Mockito.verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when appointment slot does not exist")
    void bookCase3() {
        String email = "douglas@example.com";
        User user = new User(1L, "Douglas", email, "123", Roles.PATIENT);
        Patient patient = new Patient(10L, "12345678912", "81999999999", user);
        user.setPatient(patient);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.book(999L, email);
        });
    }

    @Test
    @DisplayName("Should throw AppointmentConflictException when slot already has a patient assigned")
    void bookCase4() {
        String email = "douglas@example.com";
        Long appointmentId = 1L;

        User user = new User(1L, "Douglas", email, "123", Roles.PATIENT);
        Patient patient = new Patient(10L, "12345678912", "81999999999", user);
        user.setPatient(patient);

        Patient anotherPatient = new Patient(11L, "12345678911", "81988888888", new User());
        Employee doctor = new Employee(1L, "123456", Position.DOCTOR, new User());

        Appointment occupiedAppointment = new Appointment(appointmentId, doctor, anotherPatient, LocalDateTime.now(), AppointmentStatus.AVAILABLE, AppointmentType.EXAM_CAPTURE);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(occupiedAppointment));

        AppointmentConflictException exception = assertThrows(AppointmentConflictException.class, () -> {
            appointmentService.book(appointmentId, email);
        });

        assertEquals("This time slot is already reserved by another patient.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw AppointmentConflictException when slot status is not AVAILABLE")
    void bookCase5() {
        String email = "douglas@example.com";
        Long appointmentId = 1L;

        User user = new User(1L, "Douglas", email, "123", Roles.PATIENT);
        Patient patient = new Patient(10L, "12345678912", "81999999999", user);
        user.setPatient(patient);

        Employee doctor = new Employee(1L, "123456", Position.DOCTOR, new User());
        Appointment unvailableAppointment = new Appointment(appointmentId, doctor, null, LocalDateTime.now(), AppointmentStatus.CANCELED, AppointmentType.EXAM_CAPTURE);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(unvailableAppointment));

        AppointmentConflictException exception = assertThrows(AppointmentConflictException.class, () -> {
            appointmentService.book(appointmentId, email);
        });

        assertEquals("This time slot is not available for scheduling.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw AppointmentConflictException when booking a REPORT_REVIEW without a processed report")
    void bookCase6() {
        String email = "douglas@example.com";
        Long appointmentId = 1L;

        User user = new User(1L, "Douglas", email, "123", Roles.PATIENT);
        Patient patient = new Patient(10L, "12345678912", "81999999999", user);
        user.setPatient(patient);

        Employee doctor = new Employee(1L, "123456", Position.DOCTOR, new User());
        Appointment reviewAppointment = new Appointment(appointmentId, doctor, null, LocalDateTime.now(), AppointmentStatus.AVAILABLE, AppointmentType.REPORT_REVIEW);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(reviewAppointment));

        Mockito.when(xRayReportRepository.existsByAppointment_Patient_IdAndProcessingStatusNot(
                Mockito.eq(10L), Mockito.anyInt()
        )).thenReturn(false);

        AppointmentConflictException exception = assertThrows(AppointmentConflictException.class, () -> {
            appointmentService.book(appointmentId, email);
        });

        assertEquals("It is not possible to schedule a follow-up appointment without an X-ray exam in the system.", exception.getMessage());
    }

    @Test
    @DisplayName("Should cancel appointment successfully when user owns it and notice is greater than 24 hours")
    void cancelCase1() {
        String email = "douglas@example.com";
        Long appointmentId = 1L;
        Long userId = 1L;

        User user = new User(userId, "Douglas", email, "123", Roles.PATIENT);
        Patient patient = new Patient(10L, "12345678912", "81999999999", user);
        user.setPatient(patient);

        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);
        Employee doctor = new Employee(1L, "123456", Position.DOCTOR, new User());
        Appointment appointment = new Appointment(appointmentId, doctor, patient, futureDate, AppointmentStatus.SCHEDULED, AppointmentType.EXAM_CAPTURE);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        Mockito.when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.cancel(appointmentId, email);

        assertNotNull(result);
        assertEquals(AppointmentStatus.CANCELED, result.getStatus());
        Mockito.verify(appointmentRepository, Mockito.times(1)).save(appointment);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when appointment id does not exist")
    void cancelCase2() {
        String email = "douglas@example.com";
        Long appointmentId = 999L;

        User user = new User(1L, "Douglas", email, "123", Roles.PATIENT);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.cancel(appointmentId, email);
        });

        Mockito.verify(appointmentRepository, Mockito.never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when cancelling with less than 24 hours notice")
    void cancelCase3() {
        String email = "douglas@example.com";
        Long appointmentId = 1L;
        Long userId = 1L;

        User user = new User(userId, "Douglas", email, "123", Roles.PATIENT);
        Patient patient = new Patient(10L, "12345678912", "81999999999", user);
        user.setPatient(patient);

        LocalDateTime tooCloseDate = LocalDateTime.now().plusHours(5);
        Employee doctor = new Employee(1L, "123456", Position.DOCTOR, new User());
        Appointment appointment = new Appointment(appointmentId, doctor, patient, tooCloseDate, AppointmentStatus.SCHEDULED, AppointmentType.EXAM_CAPTURE);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.cancel(appointmentId, email);
        });

        assertEquals("The appointment can only be cancelled with 24 hours' notice.", exception.getMessage());
        Mockito.verify(appointmentRepository, Mockito.never()).save(any(Appointment.class));
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

    public CreateAppointmentDto createAppointmentDto() {
        return new CreateAppointmentDto(1L, LocalDateTime.now(), AppointmentType.REPORT_REVIEW);
    }

}