package com.douglaasph.clinic_api.controllers.dto.admin;

public record DashboardAdminMetricsDto(Integer numberOfExamsPerformed, Integer availableAppointmentSlots, Integer totalNumberOfPatients) {}
