package com.example.rqchallenge.employees.service;

import com.example.rqchallenge.employees.constants.ApplicationConstants;
import com.example.rqchallenge.employees.constants.EmployeesApiPaths;
import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.exception.BadRequestException;
import com.example.rqchallenge.employees.exception.ExternalApiInternalServerException;
import com.example.rqchallenge.employees.exception.NoDataException;
import com.example.rqchallenge.employees.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
class IEmployeeServiceImplTest {

    private final BasicJsonTester jsonTester = new BasicJsonTester(this.getClass());

    public static MockWebServer mockWebServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("classpath:data/getAllEmployeesResponse.json")
    private Resource getAllEmployeesResponseResource;

    @Value("classpath:data/getAllEmployeeByIdResponse.json")
    private Resource getAllEmployeeByIdResponse;

    @Value("classpath:data/getAllEmployeeByIdWithInvalidIdResponse.json")
    private Resource getAllEmployeeByIdWithInvalidIdResponse;

    @Value("classpath:data/getNoDataResponse.json")
    private Resource getNoDataResponse;

    @Value("classpath:data/createEmployeeResponse.json")
    private Resource createEmployeeResponse;

    @Value("classpath:data/deleteEmployeeResponse.json")
    private Resource deleteEmployeeResponse;

    private IEmployeeService iEmployeeService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        iEmployeeService = new IEmployeeServiceImpl(WebClient.builder().baseUrl(baseUrl).build());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void test_getAllEmployees_should_succeed() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeesResponseResource)));
                        //.setBody(objectMapper.writeValueAsString(getResourceBytes(getAllEmployeesResponseResource))));

        List<Employee> employeesResponse = iEmployeeService.getAllEmployees();
        RecordedRequest request = mockWebServer.takeRequest();
        //EmployeeListResponse employeeListResponse = objectMapper.readValue(request.getBody().readUtf8(), EmployeeListResponse.class);

        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());
        assertNotNull(employeesResponse);
        assertEquals(24, employeesResponse.size());
        assertTrue(employeesResponse.stream().anyMatch(employee -> employee.getId().equals(2)));
        assertTrue(employeesResponse.stream().anyMatch(employee -> employee.getEmployeeName().equals("Garrett Winters")));
        assertTrue(employeesResponse.stream().anyMatch(employee -> employee.getEmployeeSalary().equals(170750)));
        assertTrue(employeesResponse.stream().anyMatch(employee -> employee.getEmployeeAge().equals(63)));
    }

   /* @Test
    void test_getAllEmployees_with_should_return_cached_results_after_first_call() throws InterruptedException, IOException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeesResponseResource)));

        List<Employee> firstEmployeesResponse = iEmployeeService.getAllEmployees();
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());

        iEmployeeService.getAllEmployees();

        assertNotNull(firstEmployeesResponse);
        assertEquals(24, firstEmployeesResponse.size());
        assertTrue(firstEmployeesResponse.stream().anyMatch(employee -> employee.getId().equals("2")));
        assertTrue(firstEmployeesResponse.stream().anyMatch(employee -> employee.getEmployeeName().equals("Garrett Winters")));
        assertTrue(firstEmployeesResponse.stream().anyMatch(employee -> employee.getEmployeeSalary().equals("170750")));
        assertTrue(firstEmployeesResponse.stream().anyMatch(employee -> employee.getEmployeeAge().equals("63")));

        int expectedNumberOfRequestsMade = 1;
        assertEquals(expectedNumberOfRequestsMade, mockWebServer.getRequestCount());


    }*/

    @Test
    void test_getAllEmployees_with_extended_delay_should_throw_exception() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBodyDelay(10, TimeUnit.SECONDS));
        assertThrowsExactly(ExternalApiInternalServerException.class, () -> iEmployeeService.getAllEmployees());
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());
    }

    @Test
    void test_getEmployeesByNameSearch_should_succeed() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeesResponseResource)));

        List<Employee> employeesBySearchStringResponse = iEmployeeService.getEmployeesByNameSearch("an");
        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());
        assertNotNull(employeesBySearchStringResponse);
        assertEquals(3, employeesBySearchStringResponse.size());

        Set<Employee> expectedEmployees = Set.of(Employee.builder().id(7).employeeName("Herrod Chandler").employeeSalary(137500).employeeAge(59).profileImage("").build(),
                Employee.builder().id(23).employeeName("Caesar Vance").employeeSalary(106450).employeeAge(21).profileImage("").build(),
                Employee.builder().id(15).employeeName("Tatyana Fitzpatrick").employeeSalary(385750).employeeAge(19).profileImage("").build());

        expectedEmployees.forEach(expectedEmployee -> assertTrue(employeesBySearchStringResponse.contains(expectedEmployee)));
    }

    @Test
    void test_getEmployeesByNameSearch_with_non_present_string_should_return_no_result() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeesResponseResource)));

        List<Employee> employeesBySearchStringResponse = iEmployeeService.getEmployeesByNameSearch("THIS_STRING_SHOULD_NOT_EXIST");
        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());
        assertNotNull(employeesBySearchStringResponse);
        assertEquals(0, employeesBySearchStringResponse.size());

        List<Employee> expectedEmployees = Collections.emptyList();
        assertEquals(expectedEmployees, employeesBySearchStringResponse);
    }

    @Test
    void test_getEmployeeById_should_succeed() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeeByIdResponse)));

        String testEmployeeId = "12";
        Optional<Employee> employeeByIdOptionalResponse = iEmployeeService.getEmployeeById(testEmployeeId);
        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals(EmployeesApiPaths.GET_BY_ID.replace("{id}", testEmployeeId), request.getPath());
        assertTrue(employeeByIdOptionalResponse.isPresent());

        Employee actualEmployeeResponse = employeeByIdOptionalResponse.get();
        assertEquals(testEmployeeId, String.valueOf(actualEmployeeResponse.getId()));
        assertEquals("Quinn Flynn", actualEmployeeResponse.getEmployeeName());
        assertEquals(342000, actualEmployeeResponse.getEmployeeSalary());
        assertEquals(22, actualEmployeeResponse.getEmployeeAge());
        assertEquals("", actualEmployeeResponse.getProfileImage());
    }

    @Test
    void test_getEmployeeById_with_invalid_id_should_throw_exception() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeeByIdWithInvalidIdResponse)));

        String testEmployeeId = "10000000000_invalid_id";
        assertThrowsExactly(NoDataException.class, () -> iEmployeeService.getEmployeeById(testEmployeeId));
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.GET_BY_ID.replace("{id}", testEmployeeId), request.getPath());
    }

    @Test
    void test_getHighestSalaryOfEmployees_should_succeed() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeesResponseResource)));

        final Integer actualHighestSalary = iEmployeeService.getHighestSalaryOfEmployees();

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());

        assertNotNull(actualHighestSalary);
        int expected = 725000;
        assertEquals(expected, actualHighestSalary);
    }

    @Test
    void test_getHighestSalaryOfEmployees_with_no_data_should_throw_exception() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getNoDataResponse)));

        assertThrowsExactly(NoDataException.class, () -> iEmployeeService.getHighestSalaryOfEmployees());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());
    }

    @Test
    void test_getTopTenHighestEarningEmployeeNames_should_succeed() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getAllEmployeesResponseResource)));

        List<String> topTenHighestEarningEmployeeNames = iEmployeeService.getTopTenHighestEarningEmployeeNames();

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());

        assertNotNull(topTenHighestEarningEmployeeNames);
        assertEquals(10, topTenHighestEarningEmployeeNames.size());
    }

    @Test
    void test_getTopTenHighestEarningEmployeeNames_with_no_data_result_should_throw_exception() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(getNoDataResponse)));

        assertThrowsExactly(NoDataException.class, () -> iEmployeeService.getTopTenHighestEarningEmployeeNames());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.GET_ALL, request.getPath());
    }

    @Test
    void test_createEmployee_should_succeed() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(createEmployeeResponse)));

        Map<String, Object> testEmployee = new HashMap<>();
        String expectedName = "Test Employee 1";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_NAME, expectedName);
        int expectedAge = 52;
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_AGE, expectedAge);
        int expectedSalary = 543989;
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_SALARY, expectedSalary);
        String expectedProfileImage = "https://picsum.photos/200";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_PROFILE_IMAGE, expectedProfileImage);

        Employee actualCreatedEmployee = iEmployeeService.createEmployee(testEmployee);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.CREATE, request.getPath());

        assertNotNull(actualCreatedEmployee);
        assertNotNull(actualCreatedEmployee.getId());
        assertEquals(expectedName, actualCreatedEmployee.getEmployeeName());
        assertEquals(expectedAge, actualCreatedEmployee.getEmployeeAge());
        assertEquals(expectedSalary, actualCreatedEmployee.getEmployeeSalary());
        assertEquals(expectedProfileImage, actualCreatedEmployee.getProfileImage());
    }

    @Test
    void test_createEmployee_with_invalid_field_should_throw_exception() {
        mockWebServer.enqueue(new MockResponse());

        Map<String, Object> testEmployee = new HashMap<>();
        String expectedName = "Test Employee 1";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_NAME, expectedName);
        String expectedAge = "54";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_AGE, expectedAge);
        String expectedSalary = "543989";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_SALARY, expectedSalary);
        String expectedProfileImage = "https://picsum.photos/200";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_PROFILE_IMAGE, expectedProfileImage);
        testEmployee.put("AN_INVALID_FIELD", "test value for invalid field");

        assertThrowsExactly(BadRequestException.class, () -> iEmployeeService.createEmployee(testEmployee));

        int requestCount = mockWebServer.getRequestCount();
        assertEquals(0, requestCount);
    }

    @Test
    void test_deleteEmployeeById_should_succeed() throws InterruptedException {
        String expectedEmployeeId = "14";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TestUtil.resourcesAsString(deleteEmployeeResponse)));

        String actualEmployeeId = iEmployeeService.deleteEmployeeById(expectedEmployeeId);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals(EmployeesApiPaths.DELETE.replace("{id}", expectedEmployeeId), request.getPath());

        assertNotNull(actualEmployeeId);
        assertEquals(expectedEmployeeId, actualEmployeeId);
    }

}