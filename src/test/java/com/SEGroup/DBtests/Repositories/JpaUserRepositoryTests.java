package com.SEGroup.DBtests.Repositories;

import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaUserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("db")
@DisplayName("JpaUserRepository")
class JpaUserRepositoryTests {

    @Autowired
    private JpaUserRepository repo;

    @Test
    @DisplayName("save → findByEmail")
    void saveAndFindByEmail() {
        User u = new User("a@x.com", "alice", "hash");
        repo.save(u);

        User found = repo.findByEmail("a@x.com");
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("a@x.com");
        assertThat(found.getUserName()).isEqualTo("alice");
    }

    @Test
    @DisplayName("existsByEmail")
    void existsByEmail() {
        assertThat(repo.existsByEmail("nop@p")).isFalse();
        repo.save(new User("nop@p", "nop", "h"));
        assertThat(repo.existsByEmail("nop@p")).isTrue();
    }

    @Test
    @DisplayName("findAll & deleteById")
    void findAllAndDelete() {
        repo.save(new User("u1", "u1", "h"));
        repo.save(new User("u2", "u2", "h"));

        List<User> all = repo.findAll();
        assertThat(all).extracting(User::getEmail)
                .containsExactlyInAnyOrder("u1", "u2");

        repo.deleteById("u1");
        Optional<User> maybe = repo.findById("u1");
        assertThat(maybe).isEmpty();
    }
}
