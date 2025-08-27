package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.*;

/**
 * POJO Representing an EmployeeResource/Entity
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResource {
    private UUID id;
    private String name;
    private Integer salary;
    private Integer age;
    private String title;
    private String email;
}
