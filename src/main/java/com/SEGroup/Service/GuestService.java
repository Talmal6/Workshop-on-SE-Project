package com.SEGroup.Service;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Infrastructure.IAuthenticationService;

public class GuestService {


    private final IGuestRepository guests ;
    private final IAuthenticationService auth ;


    public GuestService(IGuestRepository guests, IAuthenticationService auth) {
        this.guests = guests;
        this.auth = auth;

    }

    public Result<String> createGuestSession(){
        String guestId=guests.create().id();
        return Result.success(guestId);
    }

    public ShoppingCart cart(String guestToken) throws Exception {

        auth.checkSessionKey(guestToken);
        String subject = auth.getUserBySession(guestToken);
        if (!subject.startsWith("guest:"))
            throw new IllegalArgumentException("token does not belong to a guest");

        String guestId = subject.substring("guest:".length());

        return guests.cartOf(guestId);
    }

}
