package com.leave.management.system.exceptions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExceptionObject {
    private int status;
    private String message;

    public ExceptionObject(String message,int status) {
        this.message = message;
        this.status = status;
    }
}