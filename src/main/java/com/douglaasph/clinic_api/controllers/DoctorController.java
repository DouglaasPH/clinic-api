package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.RegisterDoctorDto;
import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.models.entities.enums.Specialties;
import com.douglaasph.clinic_api.services.DoctorService;
import com.douglaasph.clinic_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final UserService userService;

    public DoctorController(DoctorService doctorService, UserService userService) {
        this.doctorService = doctorService;
        this.userService = userService;
    }

    @Operation(summary = "Register doctor", description = "Register doctor and valid data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered doctor with success"),
            @ApiResponse(responseCode = "400", description = "Invalid data (validation failure)")
    })
    @PostMapping
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

    @Operation(summary = "Find all by name or speciality or both")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All doctors found")
    })
    @GetMapping
    public ResponseEntity<List<Doctor>> findAllByNameOrSpecialty (@RequestParam(required = false) String name, @RequestParam(required = false) Specialties specialty) {
        List<Doctor> response = doctorService.findAll(name, specialty);
        return ResponseEntity.ok().body(response);
    }

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
