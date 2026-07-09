package com.douglaasph.clinic_api.controllers.exceptions;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}
