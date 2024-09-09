package com.example.rqchallenge.employees;

import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.exception.EmployeeNotFoundException;
import com.example.rqchallenge.employees.exception.NoDataException;
import com.example.rqchallenge.employees.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class EmployeeControllerImpl implements IEmployeeController {

    private final IEmployeeService iEmployeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(iEmployeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        List<Employee> employeesByNameSearch = iEmployeeService.getEmployeesByNameSearch(searchString);
        if (employeesByNameSearch == null || employeesByNameSearch.isEmpty()) {
            throw new NoDataException();
        }
        return ResponseEntity.ok(employeesByNameSearch);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        return ResponseEntity.ok(iEmployeeService.getEmployeeById(id).orElseThrow(() -> new EmployeeNotFoundException(id)));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(iEmployeeService.getHighestSalaryOfEmployees());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(iEmployeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Override
    public ResponseEntity<Employee> createEmployee(Map<String, Object> employeeInput) {
        return new ResponseEntity<>(iEmployeeService.createEmployee(employeeInput), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        return ResponseEntity.ok(iEmployeeService.deleteEmployeeById(id));
    }
}
