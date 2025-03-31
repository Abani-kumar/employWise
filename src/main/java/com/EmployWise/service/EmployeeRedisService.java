package com.EmployWise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.EmployWise.exceptions.customExceptions.DatabaseOperationException;
import com.EmployWise.exceptions.customExceptions.InvalidEmployeeDataException;
import com.EmployWise.exceptions.customExceptions.ManagerNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmployeeRedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addEmployeeRedis(String employeeId, String managerId) {
        redisTemplate.opsForValue().set(employeeId, managerId);
    }

    public String findNthLevelManager(String employeeId, int n) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee ID cannot be null or empty");
        }

        if (n < 1) {
            throw new InvalidEmployeeDataException("N must be a positive number");
        }

        while (n > 0) {
            String managerId = redisTemplate.opsForValue().get(employeeId);

            if (managerId == null) {
                throw new ManagerNotFoundException(employeeId, n);
            }

            employeeId = managerId;
            n--;
        }
        
        return employeeId;
    }
    
    public void removeEmployeeRedis(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new InvalidEmployeeDataException("Employee ID cannot be null or empty");
        }

        redisTemplate.delete(employeeId);
    }
}
