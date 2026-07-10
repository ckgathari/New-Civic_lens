package com.civiclens.repository;

import com.civiclens.entity.Leader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderRepository extends JpaRepository<Leader, String> {
    List<Leader> findByPosition(String position);

    List<Leader> findByCounty_IdAndPositionIn(Integer countyId, List<String> positions);

    Optional<Leader> findByPositionAndConstituency_Id(String position, Integer constituencyId);

    Optional<Leader> findByPositionAndWard_Id(String position, Integer wardId);

    @Query("SELECT l FROM Leader l WHERE l.position = :position")
    Optional<Leader> findLeaderByPosition(@Param("position") String position);

    @Query("SELECT l FROM Leader l WHERE l.position = :position AND l.county.id = :countyId " +
           "AND (:constituencyId IS NULL OR l.constituency.id = :constituencyId) " +
           "AND (:wardId IS NULL OR l.ward.id = :wardId)")
    List<Leader> findByPositionAndCountyIdAndConstituencyIdAndWardId(
        @Param("position") String position,
        @Param("countyId") Integer countyId,
        @Param("constituencyId") Integer constituencyId,
        @Param("wardId") Integer wardId);
}
