package com.douglaasph.clinic_api.services.dto;

import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.models.entities.XRayReport;

public record AppointmentDetailsDto(Employee employee, Patient patient, Appointment appointment, XRayReport xRayReport) {}
