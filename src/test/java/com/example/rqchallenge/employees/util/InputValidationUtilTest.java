package com.example.rqchallenge.employees.util;

import com.example.rqchallenge.employees.constants.ApplicationConstants;
import com.example.rqchallenge.employees.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InputValidationUtilTest {

    @Test
    void test_validateId_with_non_empty_id_should_succeed() {
        assertDoesNotThrow(() -> InputValidationUtil.validateId("23"));
    }

    @Test
    void test_validateId_with_empty_id_should_throw_exception() {
        assertThrowsExactly(BadRequestException.class, () -> InputValidationUtil.validateId(""));
    }

    @Test
    void test_validateId_with_null_id_should_throw_exception() {
        assertThrowsExactly(BadRequestException.class, () -> InputValidationUtil.validateId(null));
    }

    @Test
    void test_validateSearchString_should_succeed() {
        assertDoesNotThrow(() -> InputValidationUtil.validateSearchString("Tom"));
    }

    @Test
    void test_validateSearchString_with_empty_string_should_throw_exception() {
        assertThrowsExactly(BadRequestException.class, () -> InputValidationUtil.validateId(""));
    }

    @Test
    void test_validateSearchString_with_null_should_throw_exception() {
        assertThrowsExactly(BadRequestException.class, () -> InputValidationUtil.validateId(null));
    }

    @Test
    void test_validateEmployeeInputFields_with_valid_keys_should_succeed() {
        Map<String, Object> testEmployee = getCreateEmployeeInputMap();
        assertDoesNotThrow(() -> InputValidationUtil.validateEmployeeInputFields(testEmployee));
    }

    @Test
    void test_validateEmployeeInputFields_with_missing_field_should_throw_exception() {
        Map<String, Object> testEmployee = getCreateEmployeeInputMap();
        testEmployee.remove(ApplicationConstants.FIELD_EMPLOYEE_AGE);
        assertThrowsExactly(BadRequestException.class, () -> InputValidationUtil.validateEmployeeInputFields(testEmployee));
    }

    @Test
    void test_validateEmployeeInputFields_with_invalid_keys_should_throw_exception() {
        Map<String, Object> testEmployee = getCreateEmployeeInputMap();
        testEmployee.put("THIS_IS_AN_INVALID_KEY", "test_value");
        assertThrowsExactly(BadRequestException.class, () -> InputValidationUtil.validateEmployeeInputFields(testEmployee));
    }

    private static Map<String, Object> getCreateEmployeeInputMap() {
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
}