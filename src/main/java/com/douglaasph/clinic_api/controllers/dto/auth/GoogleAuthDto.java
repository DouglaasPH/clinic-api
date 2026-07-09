package com.douglaasph.clinic_api.controllers.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthDto(
        @NotBlank
        String googleToken
) {}
