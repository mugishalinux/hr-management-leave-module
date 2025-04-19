package com.leave.management.system.exceptions;

import lombok.Data;

@Data
public class Error {
    private String message;
    private String status;

    public Error(String message, String status) {
        this.message = message;
        this.status = status;
    }
}
