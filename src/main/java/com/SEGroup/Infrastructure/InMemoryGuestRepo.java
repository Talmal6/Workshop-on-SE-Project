package com.SEGroup.Infrastructure;

import com.SEGroup.Domain.GuestSession;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGuestRepo  {
    private final Map<String,GuestSession> byToken = new ConcurrentHashMap<>();
    public void   put(GuestSession g){ byToken.put(g.token(), g); }
    public GuestSession get(String token){ return byToken.get(token); }
    public void   remove(String token){ byToken.remove(token); }
}
