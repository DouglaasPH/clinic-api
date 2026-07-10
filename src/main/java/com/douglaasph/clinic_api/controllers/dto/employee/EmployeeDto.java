package com.douglaasph.clinic_api.controllers.dto.employee;

import com.douglaasph.clinic_api.models.entities.enums.Position;
import jakarta.validation.constraints.NotBlank;

public record EmployeeDto(
        @NotBlank(message = "The license number cannot be blank.")
        String licenseNumber,

        @NotBlank(message = "The position cannot be blank.")
        Position position
) {}
