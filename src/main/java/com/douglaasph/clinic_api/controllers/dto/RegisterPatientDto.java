package com.douglaasph.clinic_api.controllers.dto;

public record RegisterPatientDto(
        UserDto user,
        PatientDto patient
) {
}
