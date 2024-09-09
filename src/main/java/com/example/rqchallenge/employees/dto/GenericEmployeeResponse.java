package com.example.rqchallenge.employees.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEmployeeResponse<T> implements Serializable {

    public String status;
    private String message;
    private T data;
}
