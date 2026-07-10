package com.civiclens.repository;

import com.civiclens.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @org.springframework.lang.NonNull
    Optional<User> findByEmail(@org.springframework.lang.NonNull String email);
    boolean existsByEmail(@org.springframework.lang.NonNull String email);
}
