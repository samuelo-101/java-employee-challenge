package com.example.rqchallenge.employees.service;

import com.example.rqchallenge.employees.dto.Employee;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IEmployeeService {

    public List<Employee> getAllEmployees();

    public List<Employee> getEmployeesByNameSearch(final String searchString);

    public Optional<Employee> getEmployeeById(final String id);

    public Integer getHighestSalaryOfEmployees();

    public List<String> getTopTenHighestEarningEmployeeNames();

    public Employee createEmployee(Map<String, Object> employeeInput);

    public String deleteEmployeeById(String id);


}
