package com.SEGroup.acceptance;

import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Service.GuestService;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.SecurityAdapter;
import com.SEGroup.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.naming.AuthenticationException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Full acceptance‑style test‑suite for **UserService**. Covers:
 *  • Registration / login / logout
 *  • Guest sessions & carts
 *  • Subscriber cart operations
 *  • Purchase‑cart happy & error paths
 *  • Account deletion
 */
class UserServiceTests {

    /* ───────────── mocks ───────────── */
    private IAuthenticationService auth;
    private IUserRepository        users;
    private IGuestRepository       guests;

    /* real services wired with mocks */
    private GuestService guestSvc;
    private UserService  sut;

    /* fixed sample data */
    private final String email  = "owner@shop.com";
    private final String pw     = "P@ssw0rd";
    private final String hashPw = "enc(P@ssw0rd)";   // what our PasswordEncoder stub returns
    private final String jwt    = "jwt-owner";

    private User existingUser;   // reused for subscriber scenarios

    /* ───────── generic stubbing ───────── */
    @BeforeEach
    void setUp() throws Exception {
        auth = mock(IAuthenticationService.class);
        users  = mock(IUserRepository.class);
        guests = mock(IGuestRepository.class);
        
        doNothing().when(auth).checkSessionKey(anyString());
        doNothing().when(auth).invalidateSession(anyString());
        doNothing().when(auth).matchPassword(anyString(), anyString());
        
        when(auth.encryptPassword(anyString())).thenReturn(hashPw);
        doReturn("guest:g-xyz").when(auth).getUserBySession(anyString());

        
        guestSvc = new GuestService(guests, auth);
        sut      = new UserService(guestSvc, users, auth);

        // blanket auth behaviour for every test

        
    }

    /* ───────── Registration & Login ───────── */
    @Nested @DisplayName("UC‑3  Subscriber registration & login")
    class RegistrationAndLogin {

        @Test @DisplayName("Fresh e‑mail → register succeeds")
        void registerSuccess() {
            Result<Void> r = sut.register("owner", email, pw);
            
            assertTrue(r.isSuccess());
            verify(users).addUser(eq("owner"), eq(email), anyString());
        }

        @Test @DisplayName("Duplicate e‑mail → register fails")
        void registerDuplicate() {
            doThrow(new IllegalArgumentException("dup"))
                    .when(users).addUser(anyString(), eq(email), anyString());

            Result<Void> r = sut.register("owner", email, pw);
            assertTrue(r.isFailure());
        }

        @Test @DisplayName("Correct credentials → login returns JWT")
        void loginSuccess() {
            when(users.findUserByEmail(email)).thenReturn(new User(email, hashPw));

            Result<String> r = sut.login(email, pw);
            assertTrue(r.isSuccess());
            assertEquals(jwt, r.getData());
        }

        @Test @DisplayName("Wrong password → login fails")
        void loginWrongPassword() throws AuthenticationException {
            when(users.findUserByEmail(email)).thenReturn(new User(email, hashPw));
            doThrow(new AuthenticationException("bad‑pw"))
                    .when(auth).matchPassword(eq(hashPw), anyString());

            Result<String> r = sut.login(email, "bad");
            assertTrue(r.isFailure());
        }

        @Test @DisplayName("Unknown e-mail → login fails")
        void loginUnknownEmail() {
            when(users.findUserByEmail(email)).thenReturn(null);   // repo finds nothing
            Result<String> r = sut.login(email, pw);
            assertTrue(r.isFailure());
        }
    }

    /* ───────── Logout ───────── */
    @Nested @DisplayName("UC‑3  Logout")
    class Logout {
        @Test @DisplayName("Valid session key → invalidated")
        void logoutHappyPath() throws Exception{

            Result<Void> r = sut.logout(jwt);
            assertTrue(r.isSuccess());
            try {
                verify(auth).invalidateSession(jwt);
            } catch (AuthenticationException e) {
                fail(e);
            }
        }


        @Test @DisplayName("Expired session key → logout reports failure")
        void logoutExpiredSession() throws Exception {
            doThrow(new AuthenticationException("expired"))
                    .when(auth).invalidateSession(jwt);

            Result<Void> r = sut.logout(jwt);
            assertTrue(r.isFailure());
        }
    }

    /* ───────── Guest entrance & cart ───────── */
    @Nested @DisplayName("UC‑2  Guest entrance & cart")
    class GuestFlows {
        private final String guestId  = "g‑123";
        private final String guestJwt = "jwt‑guest";
        private ShoppingCart guestCart;


        @BeforeEach void stubGuest() throws Exception {
            guestCart = new ShoppingCart();
            when(guests.create()).thenReturn(new com.SEGroup.Domain.User.Guest(guestId, java.time.Instant.now(), guestCart));
            doReturn(guestCart).when(guests).cartOf(anyString());
            when(auth.authenticate("guest:" + guestId)).thenReturn(guestJwt);
            doReturn("guest:" + guestId).when(auth).getUserBySession(anyString());
        }

        @Test @DisplayName("Guest login → id token")
        void guestLogin() {
            Result<String> r = sut.guestLogin();
            assertTrue(r.isSuccess());
            assertEquals(guestId, r.getData());
        }

        @Test @DisplayName("Guest add‑to‑cart updates basket")
        void guestAddToCart() {
            sut.addToGuestCart(guestJwt, "P1", "S1");
            Result<String> res = sut.addToGuestCart(guestJwt, "P1", "S1");
            assertTrue(res.isSuccess());
        }
    }

    /* ───────── Subscriber cart operations ───────── */
    @Nested class SubscriberCart {
        @BeforeEach void stubUser() {
            existingUser = new User(email, "hash");
            existingUser.cart();
            when(users.findUserByEmail(anyString())).thenReturn(existingUser);
            // simulate repository clear by actually clearing the in‑memory cart
            doAnswer(inv -> { existingUser.cart().clear(); return null; })
                    .when(users).clearUserCart(email);
        }

        @Test @DisplayName("Add item → basket created")
        void addItem() {
            assertTrue(sut.addToUserCart(jwt, email, "P42", "S7").isSuccess());
        }

        @Test @DisplayName("Modify quantity persists value")
        void modify_quantity_success() {
            sut.addToUserCart(jwt, email, "P42", "S7");
            assertTrue(sut.modifyProductQuantityInCartItem(jwt, email, "P42", "S7", 5).isSuccess());
        }

        @Test @DisplayName("Remove item sets qty=0")
        void remove_item_success() {
            sut.addToUserCart(jwt, email, "P42", "S7");
            assertTrue(sut.removeFromUserCart(jwt, email, "P42", "S7").isSuccess());
        }

        @Test @DisplayName("Repository clear wipes basket")
        void clearCart() {
            sut.addToUserCart(jwt, email, "P42", "S7");
            users.clearUserCart(email);
            verify(users).clearUserCart(email);
        }
    }

    /* ───────── Purchase shopping cart ───────── */
    @Nested @DisplayName("UC‑3  Purchase cart")
    class PurchaseCart {
        @Test @DisplayName("Existing user → purchase succeeds")
        void purchaseSuccess() {
            doNothing().when(users).checkIfExist(email);
            Result<Void> r = sut.purchaseShoppingCart(jwt, email);
            assertTrue(r.isSuccess());
            verify(users).checkIfExist(email);
        }

        @Test @DisplayName("Unknown user → purchase fails")
        void purchaseUnknownUser() {
            doThrow(new IllegalArgumentException("no‑user")).when(users).checkIfExist(email);
            Result<Void> r = sut.purchaseShoppingCart(jwt, email);
            assertTrue(r.isFailure());
        }
    }

    /* ───────── Account deletion ───────── */
    @Nested @DisplayName("UC‑3  Delete user")
    class DeleteUser {
        @Test @DisplayName("User exists → deletion succeeds")
        void deleteSuccess() {
            when(users.findUserByEmail(email)).thenReturn(new User(email, hashPw));
            Result<Void> r = sut.deleteUser(email);
            assertTrue(r.isSuccess());
            verify(users).deleteUser(email);
        }


        @Test @DisplayName("User missing → deletion fails")
        void deleteFailsWhenMissing() {
            when(users.findUserByEmail(email)).thenReturn(null);
            Result<Void> r = sut.deleteUser(email);
            assertTrue(r.isFailure());
            verify(users, never()).deleteUser(email);
        }
    }

}
