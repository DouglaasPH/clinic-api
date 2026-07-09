package com.douglaasph.clinic_api.controllers.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        @NotBlank(message = "The name cannot be blank.")
        String name,

        @NotBlank(message = "The email cannot be blank.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "The password cannot be blank.")
        String password
) {}
