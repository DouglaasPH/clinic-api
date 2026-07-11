package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.appointment.CreateAppointmentDto;
import com.douglaasph.clinic_api.exceptions.AppointmentConflictException;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/appointment")
@Tag(name = "Appointment", description = "Endpoints for managing appointment of the clinic")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    // AUTHORIZATION: ADMIN
    @Operation(summary = "Insert appointment", description = "Valid data and add appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inserted appointment with success"),
            @ApiResponse(responseCode = "400", description = "Invalid data (validation failure)")
    })
    @PostMapping
    public ResponseEntity<Appointment> insert(@RequestBody @Valid CreateAppointmentDto dto) throws BadRequestException {
            Appointment appointmentResponse = appointmentService.insert(dto);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appointmentResponse.getId()).toUri();
            return ResponseEntity.created(uri).body(appointmentResponse);
    }

    // AUTHORIZATION: PATIENT AND ADMIN
    @Operation(summary = "Find all available appointments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All inserted appointments")
    })
    @GetMapping("/available")
    public ResponseEntity<List<Appointment>> findAllAvailable() {
        return ResponseEntity.ok().body(appointmentService.findAllAvailable());
    }

    // AUTHORIZATION: ANY ROLE (authenticated only)
    // Business Rule: Admins can fetch all records, while patients and employees can only fetch records linked to them.
    @Operation(summary = "Find all appointments linked to the user ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All inserted appointments")
    })
    @GetMapping
    public ResponseEntity<List<Appointment>> findAll(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
        return ResponseEntity.ok().body(appointmentService.findAll(authentication.getName(), isAdmin));
    }

    // AUTHORIZATION: PATIENT
    @Operation(summary = "Book appointment", description = "Book appointment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booked appointment with success"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PutMapping("/{appointmentId}/book")
    public ResponseEntity<Appointment> book(@PathVariable Long appointmentId, Authentication authentication) throws AppointmentConflictException {
        Appointment response = appointmentService.book(appointmentId, authentication.getName());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    // AUTHORIZATION: ADMIN or PATIENT
    @Operation(summary = "Cancel appointment", description = "Cancel appointment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Canceled appointment with success"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long appointmentId, Authentication authentication) {
        return ResponseEntity.ok().body(appointmentService.cancel(appointmentId, authentication.getName()));
    }

    // AUTHORIZATION: EMPLOYEE (ONLY TECHNICAL)
    @Operation(summary = "Start exam capture process", description = "Validates the appointment and generates a secure upload URL for the X-Ray image.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Process started successfully. Upload URL generated."),
            @ApiResponse(responseCode = "404", description = "Appointment not found"),
            @ApiResponse(responseCode = "409", description = "Business rule violation (e.g., appointment not assigned to you, or status is not BOOKED)")
    })
    @PostMapping("/{appointmentId}/request-upload")
    @PreAuthorize("@securityUtils.isEmployeeTechnical(authentication)")
    public ResponseEntity<Map<String, String>> startExam(
            @PathVariable Long appointmentId,
            Authentication authentication
    ) throws AppointmentConflictException {
        String uploadUrl = appointmentService.startExamCapture(appointmentId, authentication.getName());
        return ResponseEntity.ok(Map.of("uploadUrl", uploadUrl));
    }
}
