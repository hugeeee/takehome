package com.reliaquest.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO Representing an employee payload
 *
 * Ideally I would use bean validation on this, like what is on the mock api
 * but I didn't want to change the build.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCommand {

    private String name;

    private Integer salary;

    private Integer age;

    private String title;
}
