package com.douglaasph.clinic_api.controllers.dto.appointment;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import jakarta.validation.constraints.NotBlank;

public record UpdateDiagnosisAppointmentDto(
        @NotBlank(message = "The diagnosis cannot be blank.")
        String diagnosis,

        @NotBlank(message = "The status cannot be blank.")
        AppointmentStatus status
) {}
