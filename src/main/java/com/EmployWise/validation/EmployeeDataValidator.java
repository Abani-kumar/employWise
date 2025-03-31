package com.EmployWise.validation;

import com.EmployWise.exceptions.customExceptions.InvalidEmployeeDataException;
import com.EmployWise.models.Employee;
import com.EmployWise.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class EmployeeDataValidator {
    private final EmployeeRepository employeeRepository;

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[0-9]{10,15}$");


    public void validateEmployee(Employee employee) {
        List<String> errors = new ArrayList<>();


        if (employee.getEmployeeName() != null && employee.getEmployeeName().trim().isEmpty()) {
            errors.add("Employee name cannot be empty");
        }
        if (employee.getEmail() != null) {
            if (!EMAIL_PATTERN.matcher(employee.getEmail()).matches()) {
                errors.add("Invalid email format");
            } else {
                Optional<Employee> existingEmployee = employeeRepository.findByEmail(employee.getEmail());
                System.out.println("existingEmployee: " + existingEmployee);
                System.out.println(employee.getId());
                if (existingEmployee.isPresent() && !existingEmployee.get().getId().equals(employee.getId()) ) {
                    errors.add("Email already in use by another employee");
                }
            }
        }
        if (employee.getPhoneNumber() != null) {
            if (!PHONE_PATTERN.matcher(employee.getPhoneNumber()).matches()) {
                errors.add("Invalid phone number format");
            }
        }

        if (employee.getReportsTo() != null && !employee.getReportsTo().trim().isEmpty()) {
            Optional<Employee> manager = employeeRepository.findById(employee.getReportsTo());
            if (manager.isEmpty()) {
                errors.add("Manager with ID " + employee.getReportsTo() + " does not exist");
            }
            if (employee.getId() != null && employee.getId().equals(employee.getReportsTo())) {
                errors.add("Employee cannot report to themselves");
            }
        }

        // Validate profile image URL only if provided
        if (employee.getProfileImageUrl() != null && !employee.getProfileImageUrl().trim().isEmpty()) {
            try {
                new java.net.URL(employee.getProfileImageUrl());
            } catch (Exception e) {
                errors.add("Invalid profile image URL");
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidEmployeeDataException(String.join("; ", errors));
        }
    }


    public void validateReportingHierarchy(Employee employee) {
        if (employee.getReportsTo() == null || employee.getId() == null) {
            return;
        }
        
        String managerId = employee.getReportsTo();
        List<String> managerChain = new ArrayList<>();
        
        while (managerId != null && !managerId.isEmpty()) {
            if (managerId.equals(employee.getId()) || managerChain.contains(managerId)) {
                throw new InvalidEmployeeDataException("Circular reporting relationship detected");
            }
            
            managerChain.add(managerId);
            
            Optional<Employee> manager = employeeRepository.findById(managerId);
            if (manager.isEmpty()) {
                break;
            }
            
            managerId = manager.get().getReportsTo();
        }
    }
}
