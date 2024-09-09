package com.example.rqchallenge.employees;

import com.example.rqchallenge.BaseTest;
import com.example.rqchallenge.employees.constants.ApplicationConstants;
import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.dto.GenericEmployeeResponse;
import com.example.rqchallenge.employees.dto.ApiError;
import com.example.rqchallenge.employees.exception.NoDataException;
import com.example.rqchallenge.employees.exception.handler.ApiControllerAdvice;
import com.example.rqchallenge.employees.service.IEmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeControllerTest extends BaseTest {

    @LocalServerPort
    private Integer port;

    @Mock
    private IEmployeeService iEmployeeService; // service/client calls to employee API will be mocked to avoid 429 "too many requests" exception.

    private IEmployeeController iEmployeeController = new EmployeeControllerImpl(iEmployeeService);

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void test_getAllEmployees_api_request_should_succeed() throws JsonProcessingException {
        GenericEmployeeResponse<List<Employee>> genericEmployeeResponse = objectMapper.readValue(resourcesAsString(getAllEmployeesResponseResource), GenericEmployeeResponse.class);
        List<Employee> mockServiceResponse = genericEmployeeResponse.getData();
        when(iEmployeeService.getAllEmployees()).thenReturn(mockServiceResponse);
        given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1")
                .then().statusCode(HttpStatus.OK.value())
                .log().ifValidationFails()
                .extract().response().prettyPeek();
    }

    @Test
    void test_getAllEmployees_api_request_with_no_data_response_should_throw_exception() throws JsonProcessingException {
        when(iEmployeeService.getAllEmployees()).thenThrow(NoDataException.class);
        given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1")
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .log().ifValidationFails()
                .extract().response().prettyPeek();
    }

    @Test
    void test_getEmployeesByNameSearch_api_request_with_valid_string_should_return_results() throws JsonProcessingException {
        String searchString = "an";
        GenericEmployeeResponse<List<Employee>> genericEmployeeResponse = objectMapper.readValue(resourcesAsString(getEmployeesBySearchStringResponse), GenericEmployeeResponse.class);
        List<Employee> mockServiceResponse = genericEmployeeResponse.getData();
        when(iEmployeeService.getEmployeesByNameSearch(searchString)).thenReturn(mockServiceResponse);
        MockMvcResponse mockMvcResponse = given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/search/" + searchString)
                .then().statusCode(HttpStatus.OK.value())
                .extract().response().prettyPeek();
        List<Employee> actualEmployees = mockMvcResponse.getBody().jsonPath().getList(".", Employee.class);
        assertNotNull(actualEmployees);
        assertEquals(3, actualEmployees.size());
        assertFalse(actualEmployees.stream().anyMatch(employee -> !employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase())));
    }

    @Test
    void test_getEmployeesByNameSearch_api_request_with_non_valid_string_should_throw_exception() throws JsonProcessingException {
        String searchString = "THIS_STRING_SHOULD_NOT_EXIST";
        GenericEmployeeResponse<List<Employee>> genericEmployeeResponse = objectMapper.readValue(resourcesAsString(getNoDataResponse), GenericEmployeeResponse.class);
        List<Employee> mockServiceResponse = genericEmployeeResponse.getData();
        when(iEmployeeService.getEmployeesByNameSearch(searchString)).thenReturn(mockServiceResponse);
        MockMvcResponse mockMvcResponse = given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/search/" + searchString)
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .extract().response().prettyPeek();
        ApiError apiError = mockMvcResponse.getBody().prettyPeek().as(ApiError.class);
        assertNotNull(apiError);
        assertEquals(ApplicationConstants.API_NAME, apiError.getSource());
        assertNotNull(apiError.getReasonCode());
        assertNotNull(apiError.getDescription());
    }

    @Test
    void test_getEmployeeById_should_succeed() throws JsonProcessingException {
        String testId = "12";
        GenericEmployeeResponse<Employee> genericEmployeeResponse = objectMapper.readValue(resourcesAsString(getAllEmployeeByIdResponse), GenericEmployeeResponse.class);
        Employee mockServiceResponse = objectMapper.convertValue(genericEmployeeResponse.getData(), Employee.class);
        when(iEmployeeService.getEmployeeById(testId)).thenReturn(Optional.of(mockServiceResponse));
        MockMvcResponse mockMvcResponse = given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/" + testId)
                .then().statusCode(HttpStatus.OK.value())
                .log().ifValidationFails()
                .extract().response().prettyPeek();
        Employee employeeActual = mockMvcResponse.getBody().as(Employee.class);
        assertNotNull(employeeActual);
    }

    @Test
    void test_getHighestSalaryOfEmployees_should_succeed() {
        when(iEmployeeService.getHighestSalaryOfEmployees()).thenReturn(HIGHEST_SALARY);
        MockMvcResponse mockMvcResponse = given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/highestSalary")
                .then().statusCode(HttpStatus.OK.value())
                .log().ifValidationFails()
                .extract().response().prettyPeek();
        Integer actualHighestSalary = mockMvcResponse.getBody().as(Integer.class);
        assertNotNull(actualHighestSalary);
        assertEquals(HIGHEST_SALARY, actualHighestSalary);
    }

    @Test
    void test_getTopTenHighestEarningEmployeeNames_should_succeed() throws JsonProcessingException {
        when(iEmployeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(Arrays.asList(objectMapper.readValue(resourcesAsString(getTopTenEmployeeEarnerNames), String[].class)));
        MockMvcResponse mockMvcResponse = given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/topTenHighestEarningEmployeeNames")
                .then().statusCode(HttpStatus.OK.value())
                .log().ifValidationFails()
                .extract().response().prettyPeek();
        List<String> actualResult = mockMvcResponse.getBody().jsonPath().getList(".", String.class);
        assertNotNull(actualResult);
        assertEquals(10, actualResult.size());
    }

    @Test
    void test_createEmployee_should_succeed() throws JsonProcessingException {
        Map<String, Object> createEmployeeMap = getCreateEmployeeMap();
        GenericEmployeeResponse<Employee> genericEmployeeResponse = objectMapper.readValue(resourcesAsString(createEmployeeResponse), GenericEmployeeResponse.class);
        when(iEmployeeService.createEmployee(createEmployeeMap)).thenReturn(objectMapper.convertValue(genericEmployeeResponse.getData(), Employee.class));
        MockMvcResponse mockMvcResponse = given()
                .standaloneSetup(new EmployeeControllerImpl(iEmployeeService), new ApiControllerAdvice())
                .contentType(ContentType.JSON)
                .body(createEmployeeMap)
                .when()
                .post("/api/v1/")
                .then().statusCode(HttpStatus.CREATED.value())
                .log().ifValidationFails()
                .extract().response().prettyPeek();
        Employee actualResult = mockMvcResponse.getBody().as(Employee.class);
        assertNotNull(actualResult);
        assertNotNull(actualResult.getId());
        assertEquals(createEmployeeMap.get(ApplicationConstants.FIELD_EMPLOYEE_NAME), actualResult.getEmployeeName());
        assertEquals(createEmployeeMap.get(ApplicationConstants.FIELD_EMPLOYEE_SALARY), actualResult.getEmployeeSalary());
        assertEquals(createEmployeeMap.get(ApplicationConstants.FIELD_EMPLOYEE_AGE), actualResult.getEmployeeAge());
        assertEquals(createEmployeeMap.get(ApplicationConstants.FIELD_EMPLOYEE_PROFILE_IMAGE), actualResult.getProfileImage());

    }

    @Test
    void deleteEmployeeById() {
    }
}