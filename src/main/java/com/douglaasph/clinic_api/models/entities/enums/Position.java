package com.douglaasph.clinic_api.models.entities.enums;

public enum Position {
    TECHNICAL(1),
    DOCTOR(2);

    private int code;

    private Position(int code) { this.code = code; }

    public int getCode() { return code; }

    public static Position valueOf(int code) {
        for (Position value : Position.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid Position code");
    }
}
