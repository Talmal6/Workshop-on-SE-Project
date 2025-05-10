package com.SEGroup.Infrastructure.Endpoints;

import com.SEGroup.Service.GuestService;
import com.SEGroup.Service.Result;
import com.vaadin.hilla.EndpointExposed;

import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;

@EndpointExposed
@PermitAll           // tighten later if necessary
public class GuestEndpoint {

    private final GuestService guestService;

    public GuestEndpoint(GuestService guestService) {
        this.guestService = guestService;
    }

    /** Creates a new anonymous‑shopping session and returns the guest token */
    @Nonnull
    public String createSession() {
        return unwrap(guestService.createGuestSession());
    }

    /* ---- helper ---- */
    private static <T> T unwrap(Result<T> r) {
        if (r.isSuccess()) return r.getData();
        throw new RuntimeException(r.getErrorMessage());  // Hilla → HTTP 400
    }
}
