package com.safenetpay.firstproject.firstproject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Employee {

    private Integer id;
    private String name;
    private String surName;
    private String department;
    private Double salary;
}
