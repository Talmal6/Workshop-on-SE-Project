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

    public Result<ShoppingCart> guestCart(String guestToken) {
        try {
            auth.checkSessionKey(guestToken);
            String guestId = auth.getUserBySession(guestToken);
            return Result.success(guests.cartOf(guestId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

}
