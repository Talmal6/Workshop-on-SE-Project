package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;

import com.SEGroup.Domain.User.Guest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Guest entities.
 */
@Repository
@Profile("db")
public interface JpaGuestRepository extends JpaRepository<Guest, String> {
}
