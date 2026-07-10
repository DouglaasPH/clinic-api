package com.douglaasph.clinic_api.controllers.dto.appointment;

import jakarta.validation.constraints.NotBlank;

public record ReviewDto(
        @NotBlank(message = "The employee_id cannot be blank.")
        String finalDoctorDiagnosis
) {}