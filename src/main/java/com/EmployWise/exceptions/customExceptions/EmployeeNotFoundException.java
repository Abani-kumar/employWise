package com.EmployWise.exceptions.customExceptions;

import com.EmployWise.exceptions.EmployeeException;

public class EmployeeNotFoundException extends EmployeeException{
    public EmployeeNotFoundException(String id) {
        super("Employee not found with ID: " + id);
    }
}
