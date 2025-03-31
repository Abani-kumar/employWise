package com.EmployWise.exceptions.customExceptions;

import com.EmployWise.exceptions.EmployeeException;

public class InvalidEmployeeDataException extends EmployeeException{
    public InvalidEmployeeDataException(String message) {
        super(message);
    }
}
