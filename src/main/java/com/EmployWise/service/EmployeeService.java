package com.EmployWise.service;

import com.EmployWise.exceptions.*;
import com.EmployWise.exceptions.customExceptions.DatabaseOperationException;
import com.EmployWise.exceptions.customExceptions.EmailSendingException;
import com.EmployWise.exceptions.customExceptions.EmployeeNotFoundException;
import com.EmployWise.exceptions.customExceptions.InvalidEmployeeDataException;
import com.EmployWise.exceptions.customExceptions.ManagerNotFoundException;
import com.EmployWise.models.Employee;
import com.EmployWise.repository.EmployeeRepository;
import com.EmployWise.validation.EmployeeDataValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    @Value("${spring.mail.username:abanikumar492@gmail.com}")
    private String mailUsername;

    private final EmployeeRepository employeeRepository;
    private final JavaMailSender javaMailSender;
    private final EmployeeDataValidator validator;
    private final EmployeeRedisService employeeRedisService;

    @Transactional
    public Employee addEmployee(Employee employee) {
        try {
            validator.validateEmployee(employee);

            employee.generateId();

            Optional<Employee> existingEmployee = employeeRepository.findAll().stream()
                    .filter(e -> e.getEmail().equals(employee.getEmail()))
                    .findFirst();

            if (existingEmployee.isPresent()) {
                throw new InvalidEmployeeDataException("Email already exists: " + employee.getEmail());
            }

            validator.validateReportingHierarchy(employee);

            Employee savedEmployee = employeeRepository.save(employee);
            log.info("Employee created with ID: {}", savedEmployee.getId());

            if (savedEmployee.getReportsTo() != null && !savedEmployee.getReportsTo().isEmpty()) {
                employeeRedisService.addEmployeeRedis(savedEmployee.getId(), savedEmployee.getReportsTo());

                try {
                    sendManagerNotificationEmail(savedEmployee);
                } catch (EmailSendingException e) {
                    log.warn("Failed to send manager notification email for employee ID: {}", savedEmployee.getId(), e);
                }
            }

            return savedEmployee;
        } catch (Exception e) {
            if (e instanceof EmployeeException) {
                throw e;
            }

            log.error("Error creating employee", e);
            throw new DatabaseOperationException("Failed to create employee", e);
        }
    }

    public List<Employee> getAllEmployees() {
        try {
            return employeeRepository.findAll();
        } catch (Exception e) {
            log.error("Error retrieving all employees", e);
            throw new DatabaseOperationException("Failed to retrieve all employees", e);
        }
    }

    public Page<Employee> getEmployeesWithPagination(int page, int size, String sortBy) {
        try {
            if (page < 0) {
                throw new InvalidEmployeeDataException("Page number cannot be negative");
            }
            if (size <= 0) {
                throw new InvalidEmployeeDataException("Page size must be positive");
            }

            if (sortBy == null || sortBy.trim().isEmpty()) {
                sortBy = "employeeName";
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
            Page<Employee> employeePage = employeeRepository.findAll(pageable);

            if (page > 0 && employeePage.isEmpty()) {
                long totalElements = employeeRepository.count();
                int maxPages = (int) Math.ceil((double) totalElements / size);

                if (maxPages == 0) {
                    log.info("No employees found in the database");
                    return employeePage;
                } else {
                    throw new InvalidEmployeeDataException(
                            "Requested page " + page + " exceeds the maximum available page " + (maxPages - 1));
                }
            }

            return employeePage;
        } catch (EmployeeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving paginated employees", e);
            throw new DatabaseOperationException("Error retrieving paginated employees", e);
        }
    }

    @Transactional
    public Employee updateEmployee(String id, Employee employeeDetails) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new InvalidEmployeeDataException("Employee ID cannot be null or empty");
            }

            Employee existingEmployee = employeeRepository.findById(id)
                    .orElseThrow(() -> new EmployeeNotFoundException(id));

            if (employeeDetails.getEmail() != null &&
                    !employeeDetails.getEmail().equals(existingEmployee.getEmail())) {

                boolean emailExists = employeeRepository.findAll().stream()
                        .anyMatch(e -> e.getEmail().equals(employeeDetails.getEmail()) && !e.getId().equals(id));

                if (emailExists) {
                    throw new InvalidEmployeeDataException("Email already exists: " + employeeDetails.getEmail());
                }
            }

            if (employeeDetails.getEmployeeName() != null) {
                existingEmployee.setEmployeeName(employeeDetails.getEmployeeName());
            }
            if (employeeDetails.getPhoneNumber() != null) {
                existingEmployee.setPhoneNumber(employeeDetails.getPhoneNumber());
            }
            if (employeeDetails.getEmail() != null) {
                existingEmployee.setEmail(employeeDetails.getEmail());
            }

            String oldManager = existingEmployee.getReportsTo();
            String newManager = employeeDetails.getReportsTo();

            if (newManager != null) {
                if (newManager.equals(existingEmployee.getId())) {
                    throw new InvalidEmployeeDataException("Employee cannot report to themselves");
                }

                if (oldManager == null || !oldManager.equals(newManager)) {
                    existingEmployee.setReportsTo(newManager);

                    employeeRedisService.addEmployeeRedis(existingEmployee.getId(), newManager);

                    try {
                        sendManagerNotificationEmail(existingEmployee);
                    } catch (EmailSendingException e) {
                        log.warn("Failed to send manager notification email", e);
                    }
                }
            } else if (employeeDetails.getReportsTo() == null && existingEmployee.getReportsTo() != null) {
                existingEmployee.setReportsTo(null);
                employeeRedisService.removeEmployeeRedis(existingEmployee.getId());
            }

            if (employeeDetails.getProfileImageUrl() != null) {
                existingEmployee.setProfileImageUrl(employeeDetails.getProfileImageUrl());
            }

            validator.validateEmployee(existingEmployee);
            validator.validateReportingHierarchy(existingEmployee);

            Employee updatedEmployee = employeeRepository.save(existingEmployee);
            log.info("Employee updated with ID: {}", updatedEmployee.getId());

            return updatedEmployee;
        } catch (Exception e) {
            if (e instanceof EmployeeException) {
                throw e;
            }
            log.error("Error updating employee with ID: {}", id, e);
            throw new DatabaseOperationException("Failed to update employee", e);
        }
    }

    @Transactional
    public String deleteEmployee(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new InvalidEmployeeDataException("Employee ID cannot be null or empty");
            }

            if (!employeeRepository.existsById(id)) {
                throw new EmployeeNotFoundException(id);
            }

            List<Employee> subordinates = employeeRepository.findAll().stream()
                    .filter(employee -> id.equals(employee.getReportsTo()))
                    .collect(Collectors.toList());

            if (!subordinates.isEmpty()) {
                throw new InvalidEmployeeDataException("Cannot delete employee as they have "
                        + subordinates.size() + " subordinates reporting to them");
            }

            employeeRepository.deleteById(id);
            employeeRedisService.removeEmployeeRedis(id);

            log.info("Employee deleted with ID: {}", id);

            return "Employee with ID " + id + " deleted successfully.";
        } catch (Exception e) {
            if (e instanceof EmployeeException) {
                throw e;
            }
            log.error("Error deleting employee with ID: {}", id, e);
            throw new DatabaseOperationException("Failed to delete employee", e);
        }
    }


    public Employee getNthLevelManager(String employeeId, int level) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                throw new InvalidEmployeeDataException("Employee ID cannot be null or empty");
            }

            if (level < 1) {
                throw new InvalidEmployeeDataException("Level must be a positive number");
            }

            if (!employeeRepository.existsById(employeeId)) {
                throw new EmployeeNotFoundException(employeeId);
            }

            String nthManager = employeeRedisService.findNthLevelManager(employeeId, level);

            if (nthManager == null) {
                throw new ManagerNotFoundException(employeeId, level);
            }

            return employeeRepository.findById(nthManager)
                    .orElseThrow(() -> new EmployeeNotFoundException(nthManager));

        } catch (EmployeeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding nth level manager for employee ID: {}", employeeId, e);
            throw new DatabaseOperationException("Failed to find nth level manager", e);
        }
    }

    private void sendManagerNotificationEmail(Employee newEmployee) {
        try {
            String managerId = newEmployee.getReportsTo();
            if (managerId == null || managerId.isEmpty()) return;

            Employee manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new ManagerNotFoundException(managerId, 1));

            if (manager.getEmail() == null || manager.getEmail().isEmpty()) {
                log.warn("Cannot send email to manager with ID {} because email is not available", managerId);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(manager.getEmail());
            message.setSubject("New Employee Notification");
            message.setText(String.format(
                    "%s will now work under you. Mobile: %s, Email: %s",
                    newEmployee.getEmployeeName(),
                    newEmployee.getPhoneNumber(),
                    newEmployee.getEmail()
            ));

            javaMailSender.send(message);
            log.info("Email sent to manager ID: {} for employee ID: {}", manager.getId(), newEmployee.getId());

        } catch (Exception e) {
            log.error("Error sending email notification", e);
            throw new EmailSendingException("Failed to send email to manager ID: " + newEmployee.getReportsTo(), e);
        }
    }
}