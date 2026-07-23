package com.civiclens.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String position;

    @Column
    private String party;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String manifesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "county_id")
    private County county;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "constituency_id")
    private Constituency constituency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
