package com.example.rqchallenge.employees.util;

import com.example.rqchallenge.employees.constants.ApplicationConstants;
import com.example.rqchallenge.employees.exception.BadRequestException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InputValidationUtil {

    private static final Set<String> allowedFields = Set.of(ApplicationConstants.FIELD_EMPLOYEE_NAME,
            ApplicationConstants.FIELD_EMPLOYEE_SALARY,
            ApplicationConstants.FIELD_EMPLOYEE_AGE,
            ApplicationConstants.FIELD_EMPLOYEE_PROFILE_IMAGE);


    private static boolean isValid(final String input) {
        return (input != null && !input.trim().isBlank());
    }

    public static void validateId(final String id) {
        if (!isValid(id)) {
            throw new BadRequestException("Id cannot be empty.");
        }
    }

    public static void validateSearchString(final String searchString) {
        if (!isValid(searchString)) {
            throw new BadRequestException("Search string cannot be empty.");
        }
    }

    public static void validateEmployeeInputFields(Map<String, Object> employeeInput) {
        Optional<String> invalidFieldOptional = employeeInput.keySet().stream().filter(key -> !allowedFields.contains(key.toLowerCase())).findFirst();
        if (invalidFieldOptional.isPresent()) {
            throw new BadRequestException("Invalid field supplied: " + invalidFieldOptional.get());
        }
    }

}
