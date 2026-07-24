package com.douglaasph.clinic_api.controllers.dto.admin;

import java.time.LocalDateTime;

public record AppointmentManagementAdminDto(
        Long id,
        LocalDateTime dateHour,
        Integer appointmentStatus, // Código numérico retornado direto pro frontend
        Integer appointmentType,   // Código numérico retornado direto pro frontend
        String employeeName,
        String patientName
) {}
