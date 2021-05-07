package com.safenetpay.firstproject.firstproject;

public class Employee {

  private Integer id;
  private String name;
  private String surName;
  private String department;
  private Double salary;

  public Employee() {
  }

  public Employee(String name, String surName, String department, Double salary) {
    this.name = name;
    this.surName = surName;
    this.department = department;
    this.salary = salary;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurName() {
    return surName;
  }

  public void setSurName(String surName) {
    this.surName = surName;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public Double getSalary() {
    return salary;
  }

  public void setSalary(Double salary) {
    this.salary = salary;
  }

  @Override
  public String toString() {
    return "Employee{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", surName='" + surName + '\'' +
      ", department='" + department + '\'' +
      ", salary=" + salary +
      '}';
  }
}
