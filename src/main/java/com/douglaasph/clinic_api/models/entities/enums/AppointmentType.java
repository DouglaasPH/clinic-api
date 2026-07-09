package com.douglaasph.clinic_api.models.entities.enums;

public enum AppointmentType {
    EXAM_CAPTURE(1), // Scheduling an appointment with the technician for the X-ray
    REPORT_REVIEW(2); // Appointment with the doctor to review the report and provide feedback

    private int code;

    private AppointmentType(int code) { this.code = code; }

    public int getCode() { return code; }

    public static AppointmentType valueOf(int code) {
        for (AppointmentType value : AppointmentType.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid appointment type code");
    }
}
