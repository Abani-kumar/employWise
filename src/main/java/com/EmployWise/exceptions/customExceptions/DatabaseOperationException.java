package com.EmployWise.exceptions.customExceptions;

import com.EmployWise.exceptions.EmployeeException;

public class DatabaseOperationException extends EmployeeException{
    public DatabaseOperationException(String operation, String reason) {
        super("Database operation '" + operation + "' failed: " + reason);
    }
    
    public DatabaseOperationException(String operation, Throwable cause) {
        super("Database operation '" + operation + "' failed", cause);
    }
}
