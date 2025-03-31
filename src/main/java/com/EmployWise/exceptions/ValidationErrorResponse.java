package com.EmployWise.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse extends ErrorResponse{
    private Map<String, String> fieldErrors;
    
    public ValidationErrorResponse(int status, String code, String message, LocalDateTime timestamp, 
                                  Map<String, String> fieldErrors) {
        super(status, code, message, timestamp);
        this.fieldErrors = fieldErrors;
    }
}
