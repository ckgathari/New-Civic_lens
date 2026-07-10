package com.civiclens.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderDto {
    private String id;
    private String name;
    private String position;
    private String photoUrl;
    private String bio;
    private String manifesto;
    private String countyId;
    private String countyName;
    private String constituencyId;
    private String constituencyName;
    private String wardId;
    private String wardName;
    private Double avgRating;
    private Integer totalRatings;
}
