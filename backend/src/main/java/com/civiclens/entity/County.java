package com.civiclens.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "counties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class County {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;
}
