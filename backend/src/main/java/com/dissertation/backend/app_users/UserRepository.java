package com.dissertation.backend.app_users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findAppUserById(Long id);
    @Query("SELECT u FROM AppUser u WHERE (:role IS NULL OR u.role = :role)")
    List<AppUser> findAllByRole(@Param("role") UserRole role);
}
