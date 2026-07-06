package com.douglaasph.clinic_api.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatientDto(
        @NotBlank(message = "The cpf cannot be blank.")
        @Size(max = 11, min = 11, message = "CPF must contain 11 digits. Example: 00000000000")
        String cpf,

        @NotBlank(message = "The phone cannot be blank.")
        @Size(max = 13, min = 13, message = "Phone must contain 13 digits. Example: 5581900000000")
        String phone
) {}
