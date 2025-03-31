package com.EmployWise.controllers;

import com.EmployWise.models.Employee;
import com.EmployWise.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor

public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<Employee> addEmployee(@Valid @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.addEmployee(employee));
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Employee>> getPaginatedEmployees(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "employeeName") String sortBy
    ) {
        return ResponseEntity.ok(employeeService.getEmployeesWithPagination(page, size, sortBy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
        @PathVariable String id, 
        @Valid @RequestBody Employee employeeDetails
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Employee with ID " + id + " deleted successfully.");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{employeeId}/manager")
    public ResponseEntity<Employee> getNthLevelManager(
        @PathVariable String employeeId, 
        @RequestParam int level
    ) {
        return ResponseEntity.ok(employeeService.getNthLevelManager(employeeId, level));
    }
}
