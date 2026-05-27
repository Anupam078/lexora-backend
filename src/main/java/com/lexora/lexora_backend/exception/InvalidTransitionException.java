package com.lexora.lexora_backend.exception;

public class InvalidTransitionException extends RuntimeException {

    public InvalidTransitionException(String from, String to) {
        super("Transition from " + from + " to " + to + " is not allowed");
    }
}