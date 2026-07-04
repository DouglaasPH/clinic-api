package com.douglaasph.clinic_api.models.entities.enums;

public enum Specialties {
    CARDIOLOGIA(1),
    PEDIATRIA(2),
    DERMATOLOGIA(3),
    ORTOPEDIA(4),
    GINECOLOGIA(5),
    OFTALMOLOGIA(6),
    OTORRINOLARINGOLOGIA(7),
    PSIQUIATIA(8);

    private int code;

    private Specialties(int code) { this.code = code; }

    public int getCode() { return code; }

    public static Specialties valueOf(int code) {
        for (Specialties value : Specialties.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid Specialties code");
    }
}
