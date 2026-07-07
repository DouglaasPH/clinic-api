package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.RegisterPatientDto;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.services.PatientService;
import com.douglaasph.clinic_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/patient")
@Tag(name = "Patient", description = "Endpoints for managing patients of the clinic")
public class PatientController {
    private final PatientService patientService;
    private final UserService userService;

    public PatientController(PatientService patientService, UserService userService) {
        this.patientService = patientService;
        this.userService = userService;
    }

    @Operation(summary = "Register patient", description = "Register patient and valid data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered patient with success"),
            @ApiResponse(responseCode = "400", description = "Invalid data (validation failure)")
    })
    @PostMapping
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

    @Operation(summary = "Find all patients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All patients found")
    })
    @GetMapping
    public ResponseEntity<List<Patient>> findAll () {
        List<Patient> response = patientService.findAll();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Find patient by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Patient> findById (@PathVariable Long id) {
        Patient response = patientService.findById(id);
        return ResponseEntity.ok().body(response);
    }
}
