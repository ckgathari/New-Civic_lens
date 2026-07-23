package com.civiclens.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "constituencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Constituency {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "county_id", nullable = false)
    private County county;
}
