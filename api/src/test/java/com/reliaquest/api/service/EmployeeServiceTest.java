package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeCommand;
import com.reliaquest.api.model.EmployeeResource;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeResource emp1;
    private EmployeeResource emp2;

    private final int highestSalary = 500000;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emp1 = new EmployeeResource(UUID.randomUUID(), "Alice", highestSalary, 31, "Dev", "alice@mail.com");
        emp2 = new EmployeeResource(UUID.randomUUID(), "Bob", 100000, 33, "QA", "bob@mail.com");
    }

    @Test
    void getAllEmployees_success() {
        ApiResponse<EmployeeResource[]> apiResponse =
                new ApiResponse<>(new EmployeeResource[] {emp1, emp2}, ApiResponse.Status.HANDLED, null);
        ResponseEntity<ApiResponse<EmployeeResource[]>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<EmployeeResource> employees = employeeService.getAllEmployees();

        assertThat(employees).hasSize(2).extracting(EmployeeResource::getName).contains("Alice", "Bob");
    }

    @Test
    void getAllEmployees_apiError() {
        ApiResponse<EmployeeResource[]> apiResponse = new ApiResponse<>(null, ApiResponse.Status.ERROR, "Failed");
        ResponseEntity<ApiResponse<EmployeeResource[]>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<EmployeeResource> employees = employeeService.getAllEmployees();

        assertThat(employees).isEmpty();
    }

    @Test
    void searchEmployeesByName_success() {
        EmployeeService spyService = spy(employeeService);
        doReturn(List.of(emp1, emp2)).when(spyService).getAllEmployees();

        List<EmployeeResource> result = spyService.searchEmployeesByName("ali");

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(EmployeeResource::getName)
                .isEqualTo("Alice");
    }

    @Test
    void searchEmployeesByName_emptySearchString() {
        List<EmployeeResource> result = employeeService.searchEmployeesByName("   ");
        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeeById_found() {
        ApiResponse<EmployeeResource> apiResponse = new ApiResponse<>(emp1, ApiResponse.Status.HANDLED, null);
        ResponseEntity<ApiResponse<EmployeeResource>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        contains(emp1.getId().toString()),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        Optional<EmployeeResource> employee =
                employeeService.getEmployeeById(emp1.getId().toString());

        assertThat(employee).isPresent();
        assertThat(employee.get().getName()).isEqualTo("Alice");
    }

    @Test
    void getEmployeeById_notFound() {
        UUID id = UUID.randomUUID();

        // Make restTemplate.exchange throw 404 Not Found
        when(restTemplate.exchange(
                        contains(id.toString()),
                        eq(HttpMethod.GET),
                        isNull(),
                        ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<EmployeeResource>>>any()))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        Optional<EmployeeResource> result = employeeService.getEmployeeById(id.toString());

        assertTrue(result.isEmpty(), "Expected empty result for non-existing employee ID");
    }

    @Test
    void getHighestSalary_success() {
        EmployeeService spyService = spy(employeeService);
        doReturn(List.of(emp1, emp2)).when(spyService).getAllEmployees();

        Optional<Integer> highest = spyService.getHighestSalary();

        assertThat(highest).contains(highestSalary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_success() {
        EmployeeResource emp3 =
                new EmployeeResource(UUID.randomUUID(), "Charlie", 90000, 45, "Mgr", "charlie@cheese.com");
        EmployeeService spyService = spy(employeeService);
        doReturn(List.of(emp1, emp2, emp3)).when(spyService).getAllEmployees();

        List<String> top = spyService.getTopTenHighestEarningEmployeeNames();

        assertThat(top).containsExactly("Alice", "Bob", "Charlie"); // sorted descending
    }

    @Test
    void createEmployee_success() {
        EmployeeCommand cmd = new EmployeeCommand("Dan", 200000, 40, "Architect");
        EmployeeResource created =
                new EmployeeResource(UUID.randomUUID(), "Dan", 200000, 40, "Dev", "dan@building.com");
        ApiResponse<EmployeeResource> apiResponse = new ApiResponse<>(created, ApiResponse.Status.HANDLED, null);

        ResponseEntity<ApiResponse<EmployeeResource>> response = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        EmployeeResource result = employeeService.createEmployee(cmd);

        assertThat(result.getName()).isEqualTo("Dan");
    }

    @Test
    void createEmployee_invalidCommand_throwsException() {
        EmployeeCommand invalidCommand = new EmployeeCommand("", -100, 100, "");

        Throwable thrown = assertThrows(RuntimeException.class, () -> employeeService.createEmployee(invalidCommand));

        // Verify the cause is the expected IllegalArgumentException
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertEquals("Employee name is required", thrown.getCause().getMessage());
    }

    @Test
    void deleteEmployeeById_success() {
        // first getEmployeeById returns emp1
        ApiResponse<EmployeeResource> apiResponse = new ApiResponse<>(emp1, ApiResponse.Status.HANDLED, null);
        ResponseEntity<ApiResponse<EmployeeResource>> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        contains(emp1.getId().toString()),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        doNothing().when(restTemplate).delete(contains(emp1.getId().toString()));

        Optional<String> deleted =
                employeeService.deleteEmployeeById(emp1.getId().toString());

        assertThat(deleted).contains("Alice");
    }

    @Test
    void deleteEmployeeById_nullId_throws() {
        assertThatThrownBy(() -> employeeService.deleteEmployeeById(" ")).isInstanceOf(IllegalArgumentException.class);
    }
}
