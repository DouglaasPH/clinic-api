package com.douglaasph.clinic_api.controllers.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginUserDto(
        @NotBlank(message = "The email cannot be blank.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "The password cannot be blank.")
        String password
) {}
