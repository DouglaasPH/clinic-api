package com.douglaasph.clinic_api.controllers.dto.appointment;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateAppointmentDto(
        @NotBlank(message = "The employee_id cannot be blank.")
        Long employee_id,

        @NotBlank(message = "The date and hour cannot be blank.")
        LocalDateTime dateHour,

        @NotBlank(message = "The type cannot be blank.")
        AppointmentType type
) {}
