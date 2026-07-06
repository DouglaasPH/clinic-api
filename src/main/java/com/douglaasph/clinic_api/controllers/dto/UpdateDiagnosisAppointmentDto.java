package com.douglaasph.clinic_api.controllers.dto;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record UpdateDiagnosisAppointmentDto(
        @NotBlank(message = "The diagnosis cannot be blank.")
        String diagnosis,

        @NotBlank(message = "The status cannot be blank.")
        AppointmentStatus status
) {}
