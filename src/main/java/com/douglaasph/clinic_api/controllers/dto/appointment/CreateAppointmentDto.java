package com.douglaasph.clinic_api.controllers.dto.appointment;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateAppointmentDto(
        @NotBlank(message = "The doctor_id cannot be blank.")
        Long doctor_id,

        @NotBlank(message = "The patient_id cannot be blank.")
        Long patient_id,

        @NotBlank(message = "The date and hour cannot be blank.")
        LocalDateTime dateHour,

        @NotBlank(message = "The status cannot be blank.")
        AppointmentStatus status
) {}
