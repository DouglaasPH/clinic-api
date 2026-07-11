package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.appointment.ReviewDto;
import com.douglaasph.clinic_api.models.entities.XRayReport;
import com.douglaasph.clinic_api.services.XRayReportService;
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
@RequestMapping(value = "/x-ray")
@Tag(name = "X-Ray", description = "Endpoints for managing and retrieving X-Ray exam reports")
public class XRayReportController {
    private final XRayReportService xRayReportService;

    public XRayReportController(XRayReportService xRayReportService) {
        this.xRayReportService = xRayReportService;
    }

    // AUTHORIZATION: EMPLOYEE (ONLY DOCTOR)
    @Operation(
            summary = "Update diagnosis report",
            description = "Allows the assigned doctor to provide or update the final medical diagnosis for an X-Ray exam linked to a specific appointment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Diagnosis updated and saved successfully."),
            @ApiResponse(responseCode = "404", description = "Appointment or X-Ray report not found."),
            @ApiResponse(responseCode = "409", description = "Business rule violation: The appointment is either not assigned to you, or its current status does not allow editing.")
    })
    @PutMapping("/{appointmentId}/review")
    @PreAuthorize("@securityUtils.isEmployeeDoctor(authentication)")
    public ResponseEntity<XRayReport> review(
            @PathVariable Long appointmentId,
            @RequestBody ReviewDto dto
    ) {
        return ResponseEntity.ok().body(xRayReportService.reviewDoctor(appointmentId, dto.finalDoctorDiagnosis()));
    }


    // AUTHORIZATION: PATIENT
    @Operation(
            summary = "Find pending X-Ray exams for patient",
            description = "Retrieves all X-Ray reports belonging to the authenticated patient that have NOT yet been released for public view (where releasedToPatient is false)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of pending X-Ray reports retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Patient is not authenticated or token is invalid."),
            @ApiResponse(responseCode = "404", description = "Patient profile not found for the authenticated user.")
    })
    @GetMapping
    public ResponseEntity<List<XRayReport>> findAll(Authentication authentication) {
        return ResponseEntity.ok().body(xRayReportService.findAllByPatientIdAndReleasedToPatientTrue(authentication.getName()));
    }
}
