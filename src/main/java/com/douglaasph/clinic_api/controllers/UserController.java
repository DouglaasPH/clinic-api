package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.auth.GoogleAuthDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginUserDto;
import com.douglaasph.clinic_api.controllers.dto.doctor.RegisterDoctorDto;
import com.douglaasph.clinic_api.controllers.dto.patient.CompletePatientSocialDto;
import com.douglaasph.clinic_api.controllers.dto.patient.RegisterPatientDto;
import com.douglaasph.clinic_api.exceptions.TokenException;
import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.services.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${google.client.id}")
    private String googleClientId;

    public UserController(UserService userService, DoctorService doctorService, PatientService patientService, JWTService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // AUTHORIZATION: ANYONE
    @Operation(summary = "Login", description = "Login ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Succesfully"),
            @ApiResponse(responseCode = "400", description = "Email or password invalid")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (@RequestBody @Valid LoginUserDto user) {
        boolean isValid = userService.verify(user);

        if (!isValid) {
            throw new BadCredentialsException("Email or password invalid.");
        }

        String accessToken = jwtService.generateToken(user.email());
        RefreshToken refreshToken = refreshTokenService.insert(user.email());

        return ResponseEntity.ok(new LoginResponseDto(true, accessToken, refreshToken.getToken()));
    }

    // AUTHORIZATION: ANYONE
    @Operation(summary = "Refresh token", description = "Refresh token ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Succesfully"),
            @ApiResponse(responseCode = "400", description = "Token invalid or expired")
    })
    @PostMapping("/refresh/{refreshToken}")
    public ResponseEntity<LoginResponseDto> refresh (@PathVariable String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Business rule: If the refresh token is valid, generate a NEW 5-minute access token.
                    String newAccessToken = jwtService.generateToken(user.getEmail());
                    return ResponseEntity.ok(new LoginResponseDto(true, newAccessToken, refreshToken));
                })
                .orElseThrow(() -> new TokenException("Refresh Token invalid."));
    }

    // AUTHORIZATION: ADMIN
    @Operation(summary = "Register doctor", description = "Register doctor and valid data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered doctor with success"),
            @ApiResponse(responseCode = "400", description = "Invalid data (validation failure)")
    })
    @PostMapping("/register/doctor")
    public ResponseEntity<Doctor> register (@RequestBody @Valid RegisterDoctorDto dto) {
        try {
            User user = new User(null, dto.user().name(), dto.user().email(), dto.user().password(), Roles.valueOf(2));
            User userResponse = userService.insert(user);
            Doctor doctor = new Doctor(null, dto.doctor().getCrm(), dto.doctor().getSpecialty(), userResponse);
            Doctor doctorResponse = doctorService.insert(doctor);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(doctorResponse.getId()).toUri();
            return ResponseEntity.created(uri).body(doctorResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body((Doctor) Map.of("error", e.getMessage()));
        }
    }

    // AUTHORIZATION: ANYONE WITHOUT REGISTER
    @Operation(summary = "Register patient", description = "Register patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered patient with success"),
            @ApiResponse(responseCode = "400", description = "Invalid data (validation failure)")
    })
    @PostMapping("/register/patient")
    public ResponseEntity<Patient> register (@RequestBody @Valid RegisterPatientDto dto) {
        try {
            User user = new User(null, dto.user().name(), dto.user().email(), dto.user().password(), Roles.valueOf(3));
            User userResponse = userService.insert(user);
            Patient patient = new Patient(null, dto.patient().cpf(), dto.patient().phone(), userResponse);
            Patient patientResponse = patientService.insert(patient);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(patientResponse.getId()).toUri();
            return ResponseEntity.created(uri).body(patientResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body((Patient) Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/patient/google")
    @Operation(summary = "Complete Google self-registration", description = "Completes the patient registration by validating the Google token.")
    public ResponseEntity<LoginResponseDto> completeGooglePatientRegister(@RequestBody @Valid CompletePatientSocialDto dto) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            // 2. Valida o token recebido do frontend
            GoogleIdToken idToken = verifier.verify(dto.googleToken());

            if (idToken == null) {
                throw new TokenException("Google token is invalid or has expired.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (userService.existsByEmail(email)) {
                throw new TokenException("This email is already registered.");
            }


            User user = new User(null, dto.name(), email, dto.password(), Roles.valueOf(3));
            User userResponse = userService.insert(user);

            Patient patient = new Patient(null, dto.cpf(), dto.phone(), userResponse);
            patientService.insert(patient);

            String accessToken = jwtService.generateToken(userResponse.getEmail());
            RefreshToken refreshToken = refreshTokenService.insert(userResponse.getEmail());

            return ResponseEntity.ok(new LoginResponseDto(true, accessToken, refreshToken.getToken()));

        } catch (TokenException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing social login", e);
        }
    }


    @PostMapping("/auth/google/check")
    @Operation(summary = "Check Google User", description = "Checks if the Google user is already registered in the system.")
    public ResponseEntity<?> checkGoogleUser(@RequestBody @Valid GoogleAuthDto dto) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(dto.googleToken());

            if (idToken == null) {
                throw new TokenException("Google token is invalid or has expired.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (userService.existsByEmail(email)) {
                User user = userService.findByEmail(email);
                String accessToken = jwtService.generateToken(user.getEmail());
                RefreshToken refreshToken = refreshTokenService.insert(user.getEmail());
                return ResponseEntity.ok().body(new LoginResponseDto(true, accessToken, refreshToken.getToken()));
            }

            Map<String, Object> registerSuggestion = new HashMap<>();
            registerSuggestion.put("registered", false);
            registerSuggestion.put("email", email);
            registerSuggestion.put("name", payload.get("name"));

            return ResponseEntity.ok().body(registerSuggestion);

        } catch (TokenException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing social login", e);
        }
    }
}
