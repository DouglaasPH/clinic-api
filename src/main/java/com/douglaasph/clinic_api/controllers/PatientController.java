package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.services.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/patient")
@Tag(name = "Patient", description = "Endpoints for managing patients of the clinic")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // AUTHORIZATION: ADMIN or DOCTOR
    @Operation(summary = "Find all patients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All patients found")
    })
    @GetMapping
    public ResponseEntity<List<Patient>> findAll () {
        List<Patient> response = patientService.findAll();
        return ResponseEntity.ok().body(response);
    }

    // AUTHORIZATION: ADMIN or THE PATIENT THEMSELVES
    // Business role: Admins have access; patients only have access to their own data.
    @Operation(summary = "Find patient by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #authentication.name == @patientService.findById(#id).user.email")
    public ResponseEntity<Patient> findById (@PathVariable Long id, Authentication authentication) {
        Patient response = patientService.findById(id);

        return ResponseEntity.ok().body(response);
    }
}
