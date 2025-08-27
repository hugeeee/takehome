package com.reliaquest.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.reliaquest.api.controller.impl.EmployeeControllerImpl;
import com.reliaquest.api.model.EmployeeCommand;
import com.reliaquest.api.model.EmployeeResource;
import com.reliaquest.api.service.EmployeeService;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for EmployeeControllerImpl
 */
class EmployeeControllerImplTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeControllerImpl employeeController;

    private UUID id1;
    private UUID id2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
    }

    @Test
    void getAllEmployees_success() {
        List<EmployeeResource> employees = Arrays.asList(
                new EmployeeResource(id1, "Alice", 100000, 1000, "Dev", "alice@mail.com"),
                new EmployeeResource(id2, "Bob", 10000, 2000, "sales", "bob@temp.com"));

        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<EmployeeResource>> response = employeeController.getAllEmployees();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getAllEmployees_exception() {
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<List<EmployeeResource>> response = employeeController.getAllEmployees();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getEmployeesByNameSearch_success() {
        final String name = "Alice";
        final int salary = 100000;
        final int age = 31;
        final String title = "Sales";
        final String email = "alice@cool.com";
        when(employeeService.searchEmployeesByName("Ali"))
                .thenReturn(Collections.singletonList(new EmployeeResource(id1, name, salary, 31, title, email)));

        ResponseEntity<List<EmployeeResource>> response = employeeController.getEmployeesByNameSearch("Ali");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting("name").containsExactly(name);
        assertThat(response.getBody()).extracting("salary").containsExactly(salary);
        assertThat(response.getBody()).extracting("age").containsExactly(age);
        assertThat(response.getBody()).extracting("title").containsExactly(title);
        assertThat(response.getBody()).extracting("email").containsExactly(email);
    }

    @Test
    void getEmployeeById_found() {
        EmployeeResource emp = new EmployeeResource(id1, "Alice", 99999, 31, "Dev", "alice@fake.com");
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(emp));

        ResponseEntity<EmployeeResource> response = employeeController.getEmployeeById("1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo("Alice");
    }

    @Test
    void getEmployeeById_notFound() {
        when(employeeService.getEmployeeById("99")).thenReturn(Optional.empty());

        ResponseEntity<EmployeeResource> response = employeeController.getEmployeeById("99");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getHighestSalary_success() {
        when(employeeService.getHighestSalary()).thenReturn(Optional.of(5000));

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5000);
    }

    @Test
    void getHighestSalary_notFound() {
        when(employeeService.getHighestSalary()).thenReturn(Optional.empty());

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_success() {
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(Arrays.asList("Alice", "Bob"));

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("Alice", "Bob");
    }

    @Test
    void createEmployee_success() {
        final String charlie = "Charlie";
        final Integer salary = 99999;
        final String title = "Dev";
        final int age = 28;
        EmployeeCommand cmd = new EmployeeCommand(charlie, salary, age, title);
        EmployeeResource resource = new EmployeeResource(id1, charlie, salary, age, title, "charlie@fun.com");

        when(employeeService.createEmployee(cmd)).thenReturn(resource);

        ResponseEntity<EmployeeResource> response = employeeController.createEmployee(cmd);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo(charlie);
        assertThat(response.getBody().getSalary()).isEqualTo(salary);
        assertThat(response.getBody().getTitle()).isEqualTo(title);
        assertThat(response.getBody().getAge()).isEqualTo(age);
    }

    @Test
    void createEmployee_badRequest() {
        EmployeeCommand cmd = new EmployeeCommand("Charlie", 99999, 28, "Dev");

        when(employeeService.createEmployee(cmd)).thenThrow(new IllegalArgumentException("Invalid input"));

        ResponseEntity<EmployeeResource> response = employeeController.createEmployee(cmd);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteEmployeeById_success() {
        when(employeeService.deleteEmployeeById("1")).thenReturn(Optional.of("Alice"));

        ResponseEntity<String> response = employeeController.deleteEmployeeById("1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Alice");
    }

    @Test
    void deleteEmployeeById_notFound() {
        when(employeeService.deleteEmployeeById("99")).thenReturn(Optional.empty());

        ResponseEntity<String> response = employeeController.deleteEmployeeById("99");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteEmployeeById_badRequest() {
        when(employeeService.deleteEmployeeById("bad")).thenThrow(new IllegalArgumentException("Invalid ID"));

        ResponseEntity<String> response = employeeController.deleteEmployeeById("bad");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
