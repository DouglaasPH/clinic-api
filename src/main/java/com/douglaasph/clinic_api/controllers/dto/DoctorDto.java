package com.douglaasph.clinic_api.controllers.dto;

import com.douglaasph.clinic_api.models.entities.enums.Specialties;
import jakarta.validation.constraints.NotBlank;

public record DoctorDto(
        @NotBlank(message = "The crm cannot be blank.")
        String crm,

        @NotBlank(message = "The specialty cannot be blank.")
        Specialties specialty
) {}
