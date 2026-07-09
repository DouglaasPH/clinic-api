package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.enums.Specialties;
import com.douglaasph.clinic_api.services.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/doctor")
@Tag(name = "Doctor", description = "Endpoints for managing doctors of the clinic")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // AUTHORIZATION: ADMIN or PATIENT
    @Operation(summary = "Find all by name or speciality or both")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All doctors found")
    })
    @GetMapping
    public ResponseEntity<List<Doctor>> findAllByNameOrSpecialty (@RequestParam(required = false) String name, @RequestParam(required = false) Specialties specialty) {
        List<Doctor> response = doctorService.findAll(name, specialty);
        return ResponseEntity.ok().body(response);
    }

    // AUTHORIZATION: ANY ROLE (authenticated only)
    @Operation(summary = "Find doctor by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> findById (@PathVariable Long id) {
        Doctor response = doctorService.findById(id);
        return ResponseEntity.ok().body(response);
    }
}
