package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;

import com.SEGroup.Domain.User.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
