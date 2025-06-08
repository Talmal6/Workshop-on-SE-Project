package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.List;

import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaGuestRepository;
import static com.SEGroup.Infrastructure.Repositories.RepositoryData.DbSafeExecutor.safeExecute;
public class DbGuestData implements GuestData {

    JpaGuestRepository jpaGuestRepository;

    public DbGuestData(JpaGuestRepository jpaGuestRepository) {
        this.jpaGuestRepository = jpaGuestRepository;
    }

    @Override
    public Guest getGuestById(String guestId) {
        return safeExecute("getGuestById", () ->
                jpaGuestRepository.findById(guestId).orElse(null));
    }
    @Override
    public void saveGuest(Guest guest) {
        safeExecute("saveGuest", () -> {
            jpaGuestRepository.save(guest);
            return null;
        });
    }

    @Override
    public void updateGuest(Guest guest) {
        safeExecute("updateGuest", () -> {
            jpaGuestRepository.save(guest);
            return null;
        });
    }

    @Override
    public void deleteGuest(String guestId) {
        safeExecute("deleteGuest", () -> {
            jpaGuestRepository.deleteById(guestId);
            return null;
        });
    }

}
