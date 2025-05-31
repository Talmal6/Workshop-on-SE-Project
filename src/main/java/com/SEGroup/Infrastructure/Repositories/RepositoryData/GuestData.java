package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.Domain.User.Guest;

public interface GuestData {
    Guest getGuestById(String guestId);
    void saveGuest(Guest guest);
    void deleteGuest(String guestId);
    void updateGuest(Guest guest);

}
