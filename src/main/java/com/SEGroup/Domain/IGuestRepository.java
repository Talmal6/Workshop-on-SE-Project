package com.SEGroup.Domain;

import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;

public interface IGuestRepository {

    Guest create();
    ShoppingCart cartOf(String guestId);

}
