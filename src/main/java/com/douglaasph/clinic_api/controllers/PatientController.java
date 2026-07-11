package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.patient.CompletePatientSocialDto;
import com.douglaasph.clinic_api.controllers.dto.patient.RegisterPatientDto;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.services.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/patient")
@Tag(name = "Patient", description = "Endpoints for managing patients of the clinic")
public class PatientController {
    @Autowired
    private PatientService patientService;

    // AUTHORIZATION: ANYONE WITHOUT REGISTER
    @Operation(summary = "Register patient", description = "Register patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered patient with success"),
            @ApiResponse(responseCode = "400", description = "Invalid data (validation failure)")
    })
    @PostMapping("/register")
    public ResponseEntity<Patient> register (@RequestBody @Valid RegisterPatientDto dto) {
        Patient patientResponse = patientService.register(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(patientResponse.getId()).toUri();
        return ResponseEntity.created(uri).body(patientResponse);
    }

    // AUTHORIZATION: ANYONE
    @PostMapping("/register/google")
    @Operation(summary = "Complete Google self-registration", description = "Completes the patient registration by validating the Google token.")
    public ResponseEntity<LoginResponseDto> completeGooglePatientRegister(@RequestBody @Valid CompletePatientSocialDto dto) {
        LoginResponseDto response = patientService.completeGooglePatientRegister(dto);
        return ResponseEntity.ok(response);
    }

    // AUTHORIZATION: ADMIN or EMPLOYEE (ONLY DOCTOR)
    @Operation(summary = "Find all patients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All patients found")
    })
    @GetMapping
    @PreAuthorize("@securityUtils.isEmployeeDoctor(authentication)")
    public ResponseEntity<List<Patient>> findAll () {
        return ResponseEntity.ok().body(patientService.findAll());
    }

    // AUTHORIZATION: ADMIN or THE PATIENT THEMSELVES
    // Business role: Admins have access; patients only have access to their own data.
    @Operation(summary = "Find patient by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.isAdminOrPatientThemSelves(authentication, #id)")
    public ResponseEntity<Patient> findById (@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok().body(patientService.findById(id));
    }
}
