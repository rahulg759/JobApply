package com.naukri.automation.exception;

public class AutomationException extends RuntimeException {

    public AutomationException(String message) {
        super(message);
    }

    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AutomationException wrap(String message, Throwable cause) {
        return new AutomationException(message, cause);
    }
}
