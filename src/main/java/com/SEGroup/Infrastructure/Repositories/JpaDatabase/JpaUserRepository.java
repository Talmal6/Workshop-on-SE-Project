package com.SEGroup.Infrastructure.Repositories.JpaDatabase;

import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.User;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for User entities.
 */
@Repository
@Profile("db")
public interface JpaUserRepository extends JpaRepository<User, String> {
    // Derived query methods
    User findByEmail(String email);

    boolean existsByEmail(String email);

    //find all suspended users
    //List<User> findAllBySuspendedTrue();
    @Query("SELECT u FROM users u WHERE u.suspension IS NOT NULL")
    List<User> findAllSuspendedUsers();
}
