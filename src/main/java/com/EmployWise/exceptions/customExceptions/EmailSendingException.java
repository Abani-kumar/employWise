package com.EmployWise.exceptions.customExceptions;

import com.EmployWise.exceptions.EmployeeException;

public class EmailSendingException extends EmployeeException{
    public EmailSendingException(String recipient, String reason) {
        super("Failed to send email to " + recipient + ": " + reason);
    }
    
    public EmailSendingException(String recipient, Throwable cause) {
        super("Failed to send email to " + recipient, cause);
    }
}
