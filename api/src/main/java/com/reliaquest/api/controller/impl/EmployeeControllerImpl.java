package com.reliaquest.api.controller.impl;

import com.reliaquest.api.controller.IEmployeeController;
import com.reliaquest.api.model.EmployeeCommand;
import com.reliaquest.api.model.EmployeeResource;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Employee Resource
 */
@RestController
@RequestMapping("api/employees")
@Slf4j
public class EmployeeControllerImpl implements IEmployeeController<EmployeeResource, EmployeeCommand> {

    private final EmployeeService employeeService;

    EmployeeControllerImpl(final EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<EmployeeResource>> getAllEmployees() {
        try {
            log.info("Controller: Getting all employees");
            final List<EmployeeResource> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Controller: Error getting all employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<EmployeeResource>> getEmployeesByNameSearch(@PathVariable final String searchString) {
        try {
            log.info("Controller: Searching employees by name: {}", searchString);
            final List<EmployeeResource> employees = employeeService.searchEmployeesByName(searchString);
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Controller: Error searching employees by name", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<EmployeeResource> getEmployeeById(@PathVariable final String id) {
        try {
            log.info("Controller: Getting employee by ID: {}", id);
            final Optional<EmployeeResource> employee = employeeService.getEmployeeById(id);
            return employee.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Controller: Error getting employee by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        try {
            log.info("Controller: Getting highest salary");
            Optional<Integer> highestSalary = employeeService.getHighestSalary();
            return highestSalary
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Controller: Error getting highest salary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        try {
            log.info("Controller: Getting top ten highest earning employee names");
            List<String> names = employeeService.getTopTenHighestEarningEmployeeNames();
            return ResponseEntity.ok(names);
        } catch (Exception e) {
            log.error("Controller: Error getting top earning employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<EmployeeResource> createEmployee(@RequestBody final EmployeeCommand employeeInput) {
        try {
            log.info("Controller: Creating employee");
            final EmployeeResource createdEmployee = employeeService.createEmployee(employeeInput);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (IllegalArgumentException e) {
            log.warn("Controller: Invalid employee input: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Controller: Error creating employee", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable final String id) {
        try {
            log.info("Controller: Deleting employee by ID: {}", id);

            return employeeService
                    .deleteEmployeeById(id)
                    .map(ResponseEntity::ok) // employee found + deleted
                    .orElseGet(() -> ResponseEntity.notFound().build()); // not found

        } catch (IllegalArgumentException e) {
            log.warn("Controller: Invalid employee ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Controller: Error deleting employee", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
