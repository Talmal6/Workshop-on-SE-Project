package com.SEGroup.Infrastructure;

import javax.naming.AuthenticationException;

import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityAdapter implements IAuthenticationService{
    @Autowired
    Security sec;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void checkSessionKey(String sessionKey) throws AuthenticationException{
        if(!sec.validateToken(sessionKey)){
            throw new AuthenticationException("Token is Invalid!");
        }
    }

    @Override
    public void invalidateSession(String sessionKey) throws AuthenticationException{
        sec.makeTokenExpire(sessionKey);
    }

    @Override
    public String getUserBySession(String sessionKey) throws AuthenticationException {
        String userEmail = sec.extractUsername(sessionKey);
        if(userEmail == null){
            throw new AuthenticationException("Token is invalid!");
        }
        return userEmail;
    }

    @Override
    public String authenticate(String email) {
        return sec.generateToken(email);
    }

    @Override
    public String encryptPassword(String password) {
        return passwordEncoder.encrypt(password);
    }

    @Override
    public void matchPassword(String encryptedPassword, String realPassword) throws AuthenticationException {
        if(!passwordEncoder.checkPassword(realPassword, encryptedPassword)){
            throw new AuthenticationException("Wrong password.");
        }
    }

}
