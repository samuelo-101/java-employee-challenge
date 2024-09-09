package com.example.rqchallenge.employees.service;

import com.example.rqchallenge.employees.constants.ApplicationConstants;
import com.example.rqchallenge.employees.constants.EmployeesApiPaths;
import com.example.rqchallenge.employees.dto.CreateEmployeeRequest;
import com.example.rqchallenge.employees.dto.CreateEmployeeResponse;
import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.dto.GenericEmployeeResponse;
import com.example.rqchallenge.employees.exception.EmployeeApiInternalServerException;
import com.example.rqchallenge.employees.exception.NoDataException;
import com.example.rqchallenge.employees.util.InputValidationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
//@CacheConfig(cacheNames = {"employees"})
@Service
public class EmployeeServiceImpl implements IEmployeeService {

    private final WebClient webClient;

    @Autowired
    public EmployeeServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Cacheable("employees")
    @Override
    public List<Employee> getAllEmployees() {
        final GenericEmployeeResponse<List<Employee>> employeeListResponse = webClient.get().uri(EmployeesApiPaths.GET_ALL)
                .retrieve().bodyToMono(GenericEmployeeResponse.class)
                .retryWhen(Retry.backoff(3, Duration.of(2, ChronoUnit.SECONDS))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new EmployeeApiInternalServerException(retrySignal.failure().getMessage())))
                .block();
        employeeResponseErrorHandler(employeeListResponse);
        return new ObjectMapper().convertValue(employeeListResponse.getData(), new TypeReference<List<Employee>>() {
        });
    }

    @Cacheable("employeesSearch")
    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        InputValidationUtil.validateSearchString(searchString);
        List<Employee> employees = this.getAllEmployees().stream().filter(employee -> employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase())).collect(Collectors.toList());
        if (employees.isEmpty()) {
            throw new NoDataException();
        }
        return employees;
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        InputValidationUtil.validateId(id);
        final GenericEmployeeResponse<Employee> employeeResponse = webClient.get().uri(EmployeesApiPaths.GET_BY_ID, id)
                .retrieve().bodyToMono(GenericEmployeeResponse.class)
                .retryWhen(Retry.backoff(3, Duration.of(2, ChronoUnit.SECONDS))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new EmployeeApiInternalServerException(retrySignal.failure().getMessage())))
                .block();
        employeeResponseErrorHandler(employeeResponse);
        return Optional.of(new ObjectMapper().convertValue(employeeResponse.getData(), Employee.class));
    }

    @Cacheable("employeesHighestSalary")
    @Override
    public Integer getHighestSalaryOfEmployees() {
        return this.getAllEmployees().stream()
                .max(Comparator.comparing(Employee::getEmployeeSalary))
                .orElseThrow(NoDataException::new).getEmployeeSalary();
    }

    @Cacheable("employeesTopTenEarners")
    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return this.getAllEmployees().stream()
                    .sorted(Comparator.comparing((Employee::getEmployeeSalary)).reversed())
                    .limit(10)
                    .map(Employee::getEmployeeName).
                    collect(Collectors.toList());
    }

    @CacheEvict(allEntries = true)
    @Override
    public Employee createEmployee(Map<String, Object> employeeInput) {
        InputValidationUtil.validateEmployeeInputFields(employeeInput);
        GenericEmployeeResponse<Employee> employeeResponse = webClient.post().uri(EmployeesApiPaths.CREATE)
                .body(Mono.just(CreateEmployeeRequest.builder()
                                .name(employeeInput.get(ApplicationConstants.FIELD_EMPLOYEE_NAME).toString())
                                .age(employeeInput.get(ApplicationConstants.FIELD_EMPLOYEE_AGE).toString())
                                .salary(employeeInput.get(ApplicationConstants.FIELD_EMPLOYEE_SALARY).toString())
                                .profileImage(Optional.ofNullable(employeeInput.get(ApplicationConstants.FIELD_EMPLOYEE_PROFILE_IMAGE)).orElse("").toString())
                                .build()),
                        CreateEmployeeRequest.class).retrieve().bodyToMono(GenericEmployeeResponse.class)
                .retryWhen(Retry.backoff(3, Duration.of(2, ChronoUnit.SECONDS))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new EmployeeApiInternalServerException(retrySignal.failure().getMessage())))
                .block();
        employeeResponseErrorHandler(employeeResponse);
        CreateEmployeeResponse createEmployeeResponse = new ObjectMapper().convertValue(employeeResponse.getData(), CreateEmployeeResponse.class);

        return Employee.builder().id(createEmployeeResponse.getId())
                .employeeName(createEmployeeResponse.getName())
                .employeeAge(createEmployeeResponse.getAge())
                .employeeSalary(createEmployeeResponse.getSalary())
                .profileImage(Optional.ofNullable(createEmployeeResponse.getProfileImage()).orElse(""))
                .build();
    }

    @CacheEvict(allEntries = true)
    @Override
    public String deleteEmployeeById(String id) {
        InputValidationUtil.validateId(id);
        GenericEmployeeResponse<String> deleteResponse = webClient.delete().uri(EmployeesApiPaths.DELETE, id)
                .retrieve().bodyToMono(GenericEmployeeResponse.class)
                .retryWhen(Retry.backoff(3, Duration.of(2, ChronoUnit.SECONDS))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new EmployeeApiInternalServerException(retrySignal.failure().getMessage())))
                .block();
        employeeResponseErrorHandler(deleteResponse);
        return deleteResponse.getData();
    }

    private <T> void employeeResponseErrorHandler(final GenericEmployeeResponse<T> employeeResponse) {
        if (employeeResponse == null || !employeeResponse.getStatus().equals("success")) {
            throw new EmployeeApiInternalServerException();
        }
        if (Optional.ofNullable(employeeResponse.getData()).isEmpty()) {
            throw new NoDataException();
        }
    }
}
