package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.patient.CompletePatientSocialDto;
import com.douglaasph.clinic_api.controllers.dto.patient.PatientDto;
import com.douglaasph.clinic_api.controllers.dto.patient.RegisterPatientDto;
import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import com.douglaasph.clinic_api.exceptions.TokenException;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.repositories.PatientRepository;
import com.douglaasph.clinic_api.exceptions.DatabaseException;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PatientService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthService authService;

    @Transactional
    public LoginResponseDto register(RegisterPatientDto dto) {
        try {
            User user = new User(null,
                    dto.user().name(),
                    dto.user().email(),
                    encoder.encode(dto.user().password()),
                    Roles.valueOf(3));
            User savedUser = userRepository.save(user);

            Patient patient = new Patient(null,
                    dto.patient().cpf(),
                    dto.patient().phone(),
                    savedUser);

            Patient savedPatient = patientRepository.save(patient);

            String accessToken = jwtService.generateToken(user.getEmail());
            RefreshToken refreshToken = refreshTokenService.insert(user.getEmail());

            return new LoginResponseDto(true, accessToken, refreshToken.getToken());

        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Registration error: Email or document may already exist.");
        }
    }

    @Transactional
    public LoginResponseDto completeGooglePatientRegister(CompletePatientSocialDto dto) {
        try {
            GoogleIdTokenVerifier verifier = authService.getGoogleIdTokenVerifier();

            GoogleIdToken idToken = verifier.verify(dto.googleToken());

            if (idToken == null) {
                throw new TokenException("Google token is invalid or has expired.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (userRepository.existsByEmail(email)) {
                throw new DatabaseException("This email is already registered.");
            }

            UserDto userDto = new UserDto(dto.name(), email, dto.password());
            PatientDto patientDto = new PatientDto(dto.cpf(), dto.phone());
            RegisterPatientDto registerPatientDto = new RegisterPatientDto(userDto, patientDto);

            return register(registerPatientDto);
        } catch (TokenException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing social login", e);
        }
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }
}
