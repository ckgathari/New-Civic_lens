package com.civiclens.repository;

import com.civiclens.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Used by auth endpoints — eagerly joins all location associations in one query
    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    @NonNull
    Optional<User> findByEmail(@NonNull String email);

    // Used by /me endpoint — same full load by id
    @EntityGraph(attributePaths = {"county", "constituency", "constituency.county",
                                   "ward", "ward.constituency", "ward.constituency.county"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLocation(@Param("id") String id);

    boolean existsByEmail(@NonNull String email);
}
