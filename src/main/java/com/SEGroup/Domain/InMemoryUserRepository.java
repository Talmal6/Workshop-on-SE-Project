package com.SEGroup.Domain;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements IUserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public UserDTO findByUsername(String email) {
        User u = users.get(email);
        return u == null ? null : new UserDTO(u.getEmail(), "***");
    }

    @Override
    public void addUser(UserDTO dto) {
        users.put(dto.getEmail(), new User(dto.getEmail(), dto.getPassword())); // hash already
    }

    @Override
    public void deleteUser(String email) { users.remove(email); }

    public User getRaw(String email) { return users.get(email); }
}
