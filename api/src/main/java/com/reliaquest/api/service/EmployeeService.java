package com.reliaquest.api.service;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeCommand;
import com.reliaquest.api.model.EmployeeResource;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service class to manage interactions with Mock Server API
 */
@Service
@Slf4j
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final String BASE_URL = "http://localhost:8112/api/v1/employee";

    public EmployeeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Gets all the employees
     * @return a list of all the employees
     */
    public List<EmployeeResource> getAllEmployees() {
        try {
            log.info("Fetching all employees from external API");

            ResponseEntity<ApiResponse<EmployeeResource[]>> response =
                    restTemplate.exchange(BASE_URL, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse<EmployeeResource[]> apiResponse = response.getBody();

                if (apiResponse.isSuccessful() && apiResponse.getData() != null) {
                    return Arrays.asList(apiResponse.getData());
                } else {
                    log.warn("API returned error: {}", apiResponse.getError());
                    return Collections.emptyList();
                }
            } else {
                log.warn("Failed to fetch employees, HTTP status: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error fetching all employees from external API: ", e);
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    /**
     * Gets a list of employees whose names partially match the provided searchString
     * @param searchString names to search for - string can be a substring of the name
     * @return a list of matching employees with name that matches search string
     */
    public List<EmployeeResource> searchEmployeesByName(final String searchString) {
        try {
            log.info("Searching employees by name: {}", searchString);

            if (searchString == null || searchString.trim().isEmpty()) {
                return Collections.emptyList();
            }

            final List<EmployeeResource> allEmployees = getAllEmployees();

            return allEmployees.stream()
                    .filter(employee -> {
                        String name = normalize(employee.getName());
                        String search = normalize(searchString);
                        return !name.isEmpty() && name.contains(search);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching employees by name: {}", searchString, e);
            throw new RuntimeException("Failed to search employees by name", e);
        }
    }

    /**
     * Gets the employee with the provided id if it exists
     * @param id id of the employee
     * @return optional of employee
     */
    public Optional<EmployeeResource> getEmployeeById(final String id) {
        try {
            log.info("Fetching employee by ID: {}", id);

            if (id == null || id.trim().isEmpty()) {
                return Optional.empty();
            }

            ResponseEntity<ApiResponse<EmployeeResource>> response = restTemplate.exchange(
                    BASE_URL + "/" + id.trim(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse<EmployeeResource> apiResponse = response.getBody();

                if (apiResponse.isSuccessful() && apiResponse.getData() != null) {
                    return Optional.of(apiResponse.getData());
                } else {
                    log.warn("API returned error for employee ID {}: {}", id, apiResponse.getError());
                    return Optional.empty();
                }
            } else {
                log.warn("Employee not found with ID: {}, HTTP status: {}", id, response.getStatusCode());
                return Optional.empty();
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.info("Employee not found with ID: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching employee by ID: {}", id, e);
            throw new RuntimeException("Failed to fetch employee by ID", e);
        }
    }

    /**
     * Gets the highest salary if there is one
     * @return optional of highest salary
     */
    public Optional<Integer> getHighestSalary() {
        try {
            log.info("Calculating highest salary of employees");

            final List<EmployeeResource> allEmployees = getAllEmployees();

            return allEmployees.stream()
                    .map(EmployeeResource::getSalary)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo);
        } catch (Exception e) {
            log.error("Error calculating highest salary: ", e);
            throw new RuntimeException("Failed to calculate highest salary", e);
        }
    }

    /**
     * Gets the top ten highest earning employees
     * @return a list of max 10 employees
     */
    public List<String> getTopTenHighestEarningEmployeeNames() {
        try {
            log.info("Fetching top ten highest earning employee names");

            final List<EmployeeResource> allEmployees = getAllEmployees();

            return allEmployees.stream()
                    .filter(employee -> employee.getSalary() != null
                            && employee.getName() != null
                            && !employee.getName().trim().isEmpty())
                    .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                    .limit(10)
                    .map(EmployeeResource::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching top ten highest earning employees: ", e);
            throw new RuntimeException("Failed to fetch top earning employees", e);
        }
    }

    /**
     * Creates an EmployeeResource from the provided EmployeeCommand payload
     * @param employeeCommand payload with employee details
     * @return created Employee
     */
    public EmployeeResource createEmployee(final EmployeeCommand employeeCommand) {
        try {
            log.info("Creating new employee: {}", employeeCommand);

            if (employeeCommand == null) {
                throw new IllegalArgumentException("Employee input cannot be null");
            }

            // Validate input
            validateEmployeeCommand(employeeCommand);

            // Convert EmployeeInput to the format expected by the external API
            final Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", employeeCommand.getName().trim());
            requestBody.put("salary", employeeCommand.getSalary());
            requestBody.put("age", employeeCommand.getAge());
            requestBody.put("title", employeeCommand.getTitle());

            ResponseEntity<ApiResponse<EmployeeResource>> response = restTemplate.exchange(
                    BASE_URL, HttpMethod.POST, new HttpEntity<>(requestBody), new ParameterizedTypeReference<>() {});

            // Should verify with mock service what the expected response code is
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse<EmployeeResource> apiResponse = response.getBody();

                if (apiResponse.isSuccessful() && apiResponse.getData() != null) {
                    log.info(
                            "Employee created successfully with ID: {}",
                            apiResponse.getData().getId());
                    return apiResponse.getData();
                } else {
                    log.error("API returned error when creating employee: {}", apiResponse.getError());
                    throw new RuntimeException("Failed to create employee: " + apiResponse.getError());
                }
            } else {
                log.error("Failed to create employee, HTTP status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to create employee");
            }
        } catch (Exception e) {
            log.error("Error creating employee: ", e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    /**
     * Deletes the employee with the provided id
     * @param id id of the employee to delete
     * @return an empty optional if no delete happened, otherwise optional with deleted employee name
     */
    public Optional<String> deleteEmployeeById(final String id) {
        log.info("Deleting employee by ID: {}", id);

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }

        try {
            return getEmployeeById(id.trim()).map(employee -> {
                restTemplate.delete(BASE_URL + "/" + id.trim());
                log.info("Employee deleted successfully with ID: {}", id);
                return employee.getName();
            });

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee not found for deletion with ID: {}", id);
            return Optional.empty();
        }
    }

    /**
     * NOTE TO DEV: I would have preferred to do these validations as annotations
     * Validates the payload
     * @param employeeCommand the employee payload
     */
    private void validateEmployeeCommand(final EmployeeCommand employeeCommand) {
        if (employeeCommand.getName() == null
                || employeeCommand.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        if (employeeCommand.getSalary() == null || employeeCommand.getSalary() < 0) {
            throw new IllegalArgumentException("Employee salary must be a positive number");
        }

        if (employeeCommand.getAge() == null || employeeCommand.getAge() < 16 || employeeCommand.getAge() > 75) {
            throw new IllegalArgumentException("Employee age must be between 16 and 100");
        }

        if (employeeCommand.getTitle() == null || employeeCommand.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee title is required");
        }
    }

    /**
     * Strip white space and punctuation
     * @param input string to normalize
     * @return normalized string
     */
    private String normalize(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("\\s+", "") // remove all spaces/tabs
                .replaceAll("[^a-z0-9]", ""); // strip punctuation, keep only letters & digits
    }
}
