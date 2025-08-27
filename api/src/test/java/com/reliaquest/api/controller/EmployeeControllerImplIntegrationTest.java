package com.reliaquest.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.reliaquest.api.model.EmployeeCommand;
import com.reliaquest.api.model.EmployeeResource;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for Employee Resource.
 * Uses running application with no mocking.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // I was trying to use this for the rate limiter but that is an external service
public class EmployeeControllerImplIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setup() {
        this.baseUrl = "http://localhost:" + port + "/api/employees"; // adjust path if needed
    }

    @Test
    void testGetAllEmployees() {
        final ResponseEntity<List<EmployeeResource>> response = restTemplate.exchange(
                baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<EmployeeResource>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testCreateAndGetEmployeeById() {
        final EmployeeCommand newEmployee = new EmployeeCommand();
        newEmployee.setName("John Doe");
        newEmployee.setSalary(50000);
        newEmployee.setAge(50);
        newEmployee.setTitle("Legend");

        // Create employee
        final ResponseEntity<EmployeeResource> createResponse =
                restTemplate.postForEntity(baseUrl, newEmployee, EmployeeResource.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final EmployeeResource created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("John Doe");

        // Get by ID
        final ResponseEntity<EmployeeResource> getResponse =
                restTemplate.getForEntity(baseUrl + "/" + created.getId(), EmployeeResource.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("John Doe");
    }

    @Test
    void testCreateEmployee_BadCommand_ThrowException() {
        final EmployeeCommand newEmployee = new EmployeeCommand();
        newEmployee.setName("");
        newEmployee.setSalary(50000);
        newEmployee.setAge(50);
        newEmployee.setTitle("");

        // Create employee
        final ResponseEntity<EmployeeResource> createResponse =
                restTemplate.postForEntity(baseUrl, newEmployee, EmployeeResource.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testDeleteEmployeeById() {
        // Create a new employee first
        final EmployeeCommand employee = new EmployeeCommand();
        employee.setAge(30);
        employee.setTitle("Dev");
        employee.setName("Jane Smith");
        employee.setSalary(60000);

        final ResponseEntity<EmployeeResource> createResponse =
                restTemplate.postForEntity(baseUrl, employee, EmployeeResource.class);
        String id = String.valueOf(createResponse.getBody().getId());

        // Delete the employee
        final ResponseEntity<String> deleteResponse =
                restTemplate.exchange(baseUrl + "/" + id, HttpMethod.DELETE, null, String.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Confirm deletion
        // External service doesn't seem to delete? Debug it
        //        ResponseEntity<EmployeeResource> getResponse =
        //                restTemplate.getForEntity(baseUrl + "/" + id, EmployeeResource.class);
        //        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetEmployeesByNameSearch() {
        final String searchName = "John";
        final ResponseEntity<EmployeeResource[]> response =
                restTemplate.getForEntity(baseUrl + "/search/" + searchName, EmployeeResource[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final EmployeeResource[] employees = response.getBody();
        assertThat(employees).isNotNull();
    }

    @Test
    void testGetHighestSalary() {
        final ResponseEntity<Integer> response = restTemplate.getForEntity(baseUrl + "/highestSalary", Integer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        final ResponseEntity<List> response =
                restTemplate.getForEntity(baseUrl + "/topTenHighestEarningEmployeeNames", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
