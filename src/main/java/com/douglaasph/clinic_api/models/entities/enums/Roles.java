package com.douglaasph.clinic_api.models.entities.enums;

public enum Roles {
    ADMIN(1),
    DOCTOR(2),
    PATIENT(3);

    private int code;

    private Roles(int code) { this.code = code; }

    public int getCode() { return code; }

    public static Roles valueOf(int code) {
        for (Roles value : Roles.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid Roles code");
    }
}
