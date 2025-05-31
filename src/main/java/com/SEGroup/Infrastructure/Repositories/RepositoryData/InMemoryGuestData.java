package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.SEGroup.Domain.User.Guest;

public class InMemoryGuestData implements GuestData {
// This class implements the GuestData interface and provides in-memory storage for Guest entities.
    private final Map<String, Guest> guests = new ConcurrentHashMap<>();
    

    @Override
    public Guest getGuestById(String guestId) {
        return guests.get(guestId);
    }

    @Override
    public void saveGuest(Guest guest) {
        if(guests.containsKey(guest.getId())) {
            throw new RuntimeException("Guest already exists with ID: " + guest.getId());
        }
        guests.put(guest.getId(), guest);
    }



    @Override
    public void deleteGuest(String guestId) {
        guests.remove(guestId);
    }

    @Override
    public void updateGuest(Guest guest) {
        if(!guests.containsKey(guest.getId())) {
            throw new RuntimeException("Guest not found for update");
        }
        guests.put(guest.getId(), guest);
    }

}
