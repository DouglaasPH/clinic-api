package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.patient.CompletePatientSocialDto;
import com.douglaasph.clinic_api.controllers.dto.patient.PatientDto;
import com.douglaasph.clinic_api.controllers.dto.patient.RegisterPatientDto;
import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.exceptions.DatabaseException;
import com.douglaasph.clinic_api.exceptions.TokenException;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.repositories.PatientRepository;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class PatientServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private PatientService patientService;

    @BeforeEach
    void setup() { MockitoAnnotations.initMocks(this); }

    @Test
    @DisplayName("Should register patient successfully when data is valid")
    void registerCase1() {
        UserDto userDto = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        PatientDto patientDto = new PatientDto("12345678912", "81900000000");
        RegisterPatientDto registerPatientDto = new RegisterPatientDto(userDto, patientDto);

        Mockito.doReturn("mocked-encode").when(encoder).encode("1234");

        User user = new User(null, userDto.name(), userDto.email(), "mocked-encode", Roles.PATIENT);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        Patient patient = new Patient(null, patientDto.cpf(), patientDto.phone(), user);
        Mockito.when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient response = patientService.register(registerPatientDto);

        assertNotNull(response);
        assertEquals(patient.getCpf(), response.getCpf());
        assertEquals(patient.getPhone(), response.getPhone());
        assertEquals(patient.getUser().getEmail(), response.getUser().getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verify(patientRepository, Mockito.times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should throw DatabaseException when email or document already exists")
    void registerCase2() {
        UserDto userDto = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        PatientDto patientDto = new PatientDto("12345678912", "81900000000");
        RegisterPatientDto registerPatientDto = new RegisterPatientDto(userDto, patientDto);

        Mockito.doReturn("mocked-encode").when(encoder).encode("1234");

        Mockito.when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        DatabaseException exception = assertThrows(DatabaseException.class, () -> {
            patientService.register(registerPatientDto);
        });

        assertEquals("Registration error: Email or document may already exist.", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verifyNoInteractions(patientRepository);
    }

    @Test
    @DisplayName("Should authenticate existing Google patient successfully")
    void completeGooglePatientRegisterCase1() throws GeneralSecurityException, IOException {
        CompletePatientSocialDto completePatientSocialDto = new CompletePatientSocialDto(
                "mocked-google-token",
                "Douglas Phelipe",
                "1234",
                "12345678912",
                "1234567890123"
        );

        GoogleIdTokenVerifier verifierMock = Mockito.mock(GoogleIdTokenVerifier.class);
        GoogleIdToken idTokenMock = Mockito.mock(GoogleIdToken.class);
        GoogleIdToken.Payload payloadMock = Mockito.mock(GoogleIdToken.Payload.class);

        Mockito.when(verifierMock.verify(Mockito.anyString())).thenReturn(idTokenMock);
        Mockito.when(idTokenMock.getPayload()).thenReturn(payloadMock);
        Mockito.when(payloadMock.getEmail()).thenReturn("patient@gmail.com");

        Mockito.doReturn(verifierMock).when(authService).getGoogleIdTokenVerifier();

        Mockito.when(userRepository.existsByEmail("patient@gmail.com")).thenReturn(false);

        Mockito.when(jwtService.generateToken("patient@gmail.com")).thenReturn("mocked-access-token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mocked-refresh-token");
        Mockito.when(refreshTokenService.insert("patient@gmail.com")).thenReturn(mockRefreshToken);

        LoginResponseDto result = patientService.completeGooglePatientRegister(completePatientSocialDto);

        assertNotNull(result);
        assertTrue(result.registered());

        assertEquals("mocked-access-token", result.accessToken());
        assertEquals("mocked-refresh-token", result.refreshToken());

        Mockito.verify(jwtService, Mockito.times(1)).generateToken("patient@gmail.com");
        Mockito.verify(refreshTokenService, Mockito.times(1)).insert("patient@gmail.com");
    }

    @Test
    @DisplayName("Should throw TokenException when Google token is invalid or expired")
    void completeGooglePatientRegisterCase2() throws GeneralSecurityException, IOException {
        CompletePatientSocialDto completePatientSocialDto = new CompletePatientSocialDto(
                "mocked-google-token",
                "Douglas Phelipe",
                "1234",
                "12345678912",
                "1234567890123"
        );

        GoogleIdTokenVerifier verifierMock = Mockito.mock(GoogleIdTokenVerifier.class);
        Mockito.when(verifierMock.verify(Mockito.anyString())).thenReturn(null);

        Mockito.doReturn(verifierMock).when(authService).getGoogleIdTokenVerifier();

        assertThrows(TokenException.class, () -> patientService.completeGooglePatientRegister(completePatientSocialDto));

        Mockito.verifyNoInteractions(userRepository);
        Mockito.verifyNoInteractions(jwtService);
        Mockito.verifyNoInteractions(refreshTokenService);
    }

    @Test
    @DisplayName("Should throw DatabaseException when email is already registered")
    void completeGooglePatientRegisterCase3() throws GeneralSecurityException, IOException {
        CompletePatientSocialDto completePatientSocialDto = new CompletePatientSocialDto(
                "mocked-google-token",
                "Douglas Phelipe",
                "1234",
                "12345678912",
                "1234567890123"
        );

        GoogleIdTokenVerifier verifierMock = Mockito.mock(GoogleIdTokenVerifier.class);
        GoogleIdToken idTokenMock = Mockito.mock(GoogleIdToken.class);
        GoogleIdToken.Payload payloadMock = Mockito.mock(GoogleIdToken.Payload.class);

        Mockito.when(verifierMock.verify(Mockito.anyString())).thenReturn(idTokenMock);
        Mockito.when(idTokenMock.getPayload()).thenReturn(payloadMock);
        Mockito.when(payloadMock.getEmail()).thenReturn("patient@gmail.com");

        Mockito.doReturn(verifierMock).when(authService).getGoogleIdTokenVerifier();

        Mockito.when(userRepository.existsByEmail("patient@gmail.com")).thenReturn(true);

        DatabaseException exception = assertThrows(DatabaseException.class, () -> {
            patientService.completeGooglePatientRegister(completePatientSocialDto);
        });

        assertEquals("This email is already registered.", exception.getMessage());

        Mockito.verifyNoInteractions(jwtService);
        Mockito.verifyNoInteractions(refreshTokenService);
    }
}