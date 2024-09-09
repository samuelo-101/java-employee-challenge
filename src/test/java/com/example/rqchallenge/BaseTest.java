package com.example.rqchallenge;

import com.example.rqchallenge.employees.constants.ApplicationConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class BaseTest {

    @Value("classpath:data/getAllEmployeesResponse.json")
    protected Resource getAllEmployeesResponseResource;

    @Value("classpath:data/getEmployeesBySearchStringResponse.json")
    protected Resource getEmployeesBySearchStringResponse;

    @Value("classpath:data/getAllEmployeeByIdResponse.json")
    protected Resource getAllEmployeeByIdResponse;

    @Value("classpath:data/getTopTenEmployeeEarnerNames.json")
    protected Resource getTopTenEmployeeEarnerNames;

    @Value("classpath:data/getAllEmployeeByIdWithInvalidIdResponse.json")
    protected Resource getAllEmployeeByIdWithInvalidIdResponse;

    @Value("classpath:data/getNoDataResponse.json")
    protected Resource getNoDataResponse;

    @Value("classpath:data/createEmployeeResponse.json")
    protected Resource createEmployeeResponse;

    @Value("classpath:data/deleteEmployeeResponse.json")
    protected Resource deleteEmployeeResponse;

    protected static final int HIGHEST_SALARY = 725000;

    protected Map<String, Object> getCreateEmployeeMap() {
        Map<String, Object> testEmployee = new HashMap<>();
        String expectedName = "Test Employee 1";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_NAME, expectedName);
        int expectedAge = 52;
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_AGE, expectedAge);
        int expectedSalary = 543989;
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_SALARY, expectedSalary);
        String expectedProfileImage = "https://picsum.photos/200";
        testEmployee.put(ApplicationConstants.FIELD_EMPLOYEE_PROFILE_IMAGE, expectedProfileImage);
        return testEmployee;
    }

    protected String resourcesAsString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
