package com.civiclens.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String nationalId;
    private String position;
    private Boolean isLeader;
    private Boolean isAspirant;
    private String countyId;
    private String constituencyId;
    private String wardId;
}
