package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.List;

import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaGuestRepository;

public class DbGuestData implements GuestData {

    JpaGuestRepository jpaGuestRepository;

    public DbGuestData(JpaGuestRepository jpaGuestRepository) {
        this.jpaGuestRepository = jpaGuestRepository;
    }

    @Override
    public Guest getGuestById(String guestId) {
        return jpaGuestRepository.findById(guestId).orElse(null);
    }

    @Override
    public void saveGuest(Guest guest) {
        jpaGuestRepository.save(guest);
    }

    @Override
    public void updateGuest(Guest guest) {
        jpaGuestRepository.save(guest);
    }

    @Override
    public void deleteGuest(String guestId) {
        jpaGuestRepository.deleteById(guestId);
    }

}
