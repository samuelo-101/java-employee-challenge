package com.example.rqchallenge.employees.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeRequest implements Serializable {

    private String name;
    private String salary;
    private String age;
    @JsonProperty("employee_name")
    private String profileImage;
}
