package com.douglaasph.clinic_api.models.entities.enums;

public enum AppointmentStatus {
    AVAILABLE(1),
    SCHEDULED(2),
    CANCELED(3),
    COMPLETED(4);

    private int code;

    private AppointmentStatus(int code) { this.code = code; }

    public int getCode() { return code; }

    public static AppointmentStatus valueOf(int code) {
        for (AppointmentStatus value : AppointmentStatus.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid Status code");
    }
}
