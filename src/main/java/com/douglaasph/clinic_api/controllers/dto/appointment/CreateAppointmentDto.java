package com.douglaasph.clinic_api.controllers.dto.appointment;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAppointmentDto(
        @NotNull(message = "The employee_id cannot be null.")
        Long employee_id,

        @NotNull(message = "The date and hour cannot be null.")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime dateHour,

        @NotNull(message = "The type cannot be null.")
        AppointmentType type
) {}
