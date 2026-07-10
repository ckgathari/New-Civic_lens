package com.civiclens.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String nationalId;
    private String photoUrl;
    private Boolean isLeader;
    private Boolean isAspirant;
    private Boolean isAdmin;
    private String position;
    private Integer countyId;
    private Integer constituencyId;
    private Integer wardId;
    private String countyName;
    private String constituencyName;
    private String wardName;
}
