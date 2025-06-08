package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.List;

import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaUserRepository;
import static com.SEGroup.Infrastructure.Repositories.RepositoryData.DbSafeExecutor.safeExecute;

public class DbUserData implements UserData {
    
    JpaUserRepository jpaUserRepository;
    public DbUserData(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User getUserById(String userId) {
        return safeExecute("getUserById", () ->
                jpaUserRepository.findById(userId).orElse(null));
    }

    @Override
    public User getUserByEmail(String email) {
        return safeExecute("getUserByEmail", () ->
                jpaUserRepository.findByEmail(email));
    }

    @Override
    public void saveUser(User user) {
        safeExecute("saveUser", () -> {
            jpaUserRepository.save(user);
            return null;
        });
    }

    @Override
    public void updateUser(User user) {
        safeExecute("updateUser", () -> {
            jpaUserRepository.save(user);
            return null;
        });
    }

    @Override
    public void deleteUser(String userId) {
        safeExecute("deleteUser", () -> {
            jpaUserRepository.deleteById(userId);
            return null;
        });
    }

    @Override
    public List<User> getAllUsers() {
        return safeExecute("getAllUsers", jpaUserRepository::findAll);
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return safeExecute("getUsersByRole", () -> {
            // אם לא מימשת את findByRole – אפשר להחזיר ריק זמנית או להוסיף מימוש
            return List.of(); // או jpaUserRepository.findByRole(role);
        });
    }

    @Override
    public boolean userExistsByName(String userId) {
        return safeExecute("userExistsByName", () ->
                jpaUserRepository.existsByUsername(userId));
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return safeExecute("userExistsByEmail", () ->
                jpaUserRepository.existsByEmail(email));
    }
}
