package com.civiclens.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Integer countyId;
    private Integer constituencyId;
    private Integer wardId;
}
