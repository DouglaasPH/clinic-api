package com.douglaasph.clinic_api.models.entities.enums;

public enum ProcessingStatus {
    AWAITING_AI(1),
    PROCESSED_BY_IA(2),
    AWAITING_VALIDATION_BY_DOCTOR(3),
    VALIDATED_BY_DOCTOR(4);

    private int code;

    private ProcessingStatus(int code) { this.code = code; }

    public int getCode() { return code; }

    public static ProcessingStatus valueOf(int code) {
        for (ProcessingStatus value : ProcessingStatus.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid processing status code");
    }
}
