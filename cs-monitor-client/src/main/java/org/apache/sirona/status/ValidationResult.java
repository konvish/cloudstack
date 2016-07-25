package org.apache.sirona.status;

import org.apache.sirona.status.Status;
/**
 * Created by kong on 2016/1/24.
 */
public class ValidationResult {
    private Status status;
    private String message;
    private String name;

    public ValidationResult(String name, Status status, String message) {
        this.name = name;
        this.status = status;
        this.message = message;
    }

    public String getName() {
        return this.name;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
