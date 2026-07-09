package com.douglaasph.clinic_api.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthDto(
        @NotBlank
        String googleToken
) {}
