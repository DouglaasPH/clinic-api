package com.douglaasph.clinic_api.controllers.dto.patient;

import com.douglaasph.clinic_api.controllers.dto.user.UserDto;

public record RegisterPatientDto(
        UserDto user,
        PatientDto patient
) {
}
