package com.SEGroup.Infrastructure.user;

import com.SEGroup.Domain.GuestSession;
import com.SEGroup.Domain.InMemoryUserRepository;
import com.SEGroup.Domain.ShoppingCart;
import com.SEGroup.Domain.UserDTO;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.InMemoryGuestRepo;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Security;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryAuthService implements IAuthenticationService {
    public final InMemoryGuestRepo guestRepo;
    private final Map<String, String> session2email = new ConcurrentHashMap<>();
    private final InMemoryUserRepository repo;
    private final Security jwt;

    public InMemoryAuthService(InMemoryUserRepository repo, Security jwt,InMemoryGuestRepo guestRepo) {
        this.repo = repo;
        this.jwt  = jwt;
        this.guestRepo=guestRepo;
    }

    @Override public String authenticate(String email, String rawPwd) {
        var user = repo.getRaw(email);
        if (user == null) return null;
        if (!user.matchesPassword(rawPwd, PasswordEncoder::checkPassword)) return null;

        String token = jwt.generateToken(email);
        session2email.put(token, email);
        return token;
    }
    @Override
    public String createGuestSession(){
        String token= jwt.generateToken("guest:"+java.util.UUID.randomUUID());
        guestRepo.put(new GuestSession(token, Instant.now(),new ShoppingCart()));
        session2email.put(token,null);
        return token ;

    }

    @Override
    public ShoppingCart guestCart ( String token){
        var guest= guestRepo.get(token);
        if ( guest == null ) throw new RuntimeException("Unknown guest token");
        return guest.shoppingCart();
    }
    @Override public void checkSessionKey(String key) {
        if (!jwt.validateToken(key) || !session2email.containsKey(key))
            throw new RuntimeException("Session invalid / expired");
    }

    @Override public void invalidateSession(String key) { session2email.remove(key); }

    @Override public UserDTO getUserBySession(String key) {
        String mail = session2email.get(key);
        return mail == null ? null : repo.findByUsername(mail);
    }
}
