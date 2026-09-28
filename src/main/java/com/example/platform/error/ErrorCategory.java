package com.example.platform.error;

/** The three error categories. See docs/standards/error-handling-standard.md. */
public enum ErrorCategory {
    /** Infrastructure or dependency failure. Retryable when the operation is idempotent. */
    SYSTEM('S', true),
    /** A domain rule rejected the request. Never retried. */
    BUSINESS('B', false),
    /** This project's own logic, input contract or configuration is wrong. Never retried. */
    PROJECT('P', false);

    private final char letter;
    private final boolean retryable;

    ErrorCategory(char letter, boolean retryable) {
        this.letter = letter;
        this.retryable = retryable;
    }

    public char letter() { return letter; }
    public boolean retryable() { return retryable; }

    public static ErrorCategory fromCode(String code) {
        char c = code.split("-")[1].charAt(0);
        for (ErrorCategory cat : values()) {
            if (cat.letter == c) return cat;
        }
        throw new IllegalArgumentException("Unknown category letter in " + code);
    }
}
