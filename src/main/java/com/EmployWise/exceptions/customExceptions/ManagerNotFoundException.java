package com.EmployWise.exceptions.customExceptions;

import com.EmployWise.exceptions.EmployeeException;

public class ManagerNotFoundException extends EmployeeException{
    public ManagerNotFoundException(String employeeId, int level) {
        super("No manager found at level " + level + " for employee ID: " + employeeId);
    }
}
