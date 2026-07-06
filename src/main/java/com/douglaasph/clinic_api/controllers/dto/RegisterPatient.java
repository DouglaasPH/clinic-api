package com.douglaasph.clinic_api.controllers.dto;

public record RegisterPatient(
        UserDto user,
        PatientDto patient
) {
}
