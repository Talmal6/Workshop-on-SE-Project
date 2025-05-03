package com.SEGroup.acceptance;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.ProductCatalog.InMemoryProductCatalog;
import com.SEGroup.Domain.Store.StoreRepository;
import com.SEGroup.Domain.User.GuestRepository;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Domain.User.UserRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Infrastructure.SecurityAdapter;
import com.SEGroup.Service.GuestService;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.UserService;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.AdditionalMatchers.not;

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
    private String jwt    = "jwt-owner";

    private User existingUser;   // reused for subscriber scenarios

    /* ───────── generic stubbing ───────── */
    @BeforeEach
    void setUp() throws Exception {

        Security security = new Security();
        //io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to create a key
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        auth   = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
        (( SecurityAdapter)auth).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());

        users  = new UserRepository();
        guests = new GuestRepository();

        guestSvc = new GuestService(guests, auth);
        sut      = new UserService(guestSvc, users, auth);
        jwt = regLoginAndGetSession("owner", email, pw); // register & login to get a session key
    }

    public String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        // Register a new user
        Result<Void> regResult = sut.register(userName, email, password);
        // Authenticate the user and get a session key
        return  auth.authenticate(email);
    }

    /* ───────── Registration & Login ───────── */
    @Nested @DisplayName("UC‑3  Subscriber registration & login")
    class RegistrationAndLogin {

        @BeforeEach
        void setUp() throws Exception {

            Security security = new Security();
            //io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to create a key
            SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            security.setKey(key);
            auth   = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
            (( SecurityAdapter)auth).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());

            users  = new UserRepository();
            guests = new GuestRepository();

            guestSvc = new GuestService(guests, auth);
            sut      = new UserService(guestSvc, users, auth);
        }

        @Test @DisplayName("Fresh e‑mail → register succeeds")
        void registerSuccess() {
            Result<Void> r = sut.register("owner", email, pw);
            assertTrue(r.isSuccess());
        }

        @Test @DisplayName("Duplicate e‑mail → register fails")
        void registerDuplicate() {

            Result<Void> r1 = sut.register("owner", email, pw);
            assertTrue(r1.isSuccess());
            Result<Void> r2 = sut.register("owner", email, pw);
            assertTrue(r2.isFailure());
        }



        @Test
        @DisplayName("Correct credentials → login returns JWT")
        void loginSuccess() throws Exception {
            // 1 - right password → no exception
            Result<Void> r = sut.register("owner", email, pw);
            assertTrue(r.isSuccess());
            assertTrue(sut.login(email, pw).isSuccess());

            // wrong password fails
        }


        @Test @DisplayName("Wrong password → login fails")
        void loginWrongPassword() throws AuthenticationException {
            Result<Void> r = sut.register("owner", email, pw);
            assertTrue(r.isSuccess());
            assertFalse(sut.login(email, "somepass").isSuccess());

            // wrong password fails
        }

        @Test @DisplayName("Unknown e-mail → login fails")
        void loginUnknownEmail() {
            Result<String> r = sut.login(email, pw);
            assertTrue(r.isFailure());
        }
    }

    /* ───────── Logout ───────── */
    @Nested @DisplayName("UC‑3  Logout")
    class Logout {
        @Test @DisplayName("Valid session key → invalidated")
        void logoutHappyPath() throws Exception{
            regLoginAndGetSession("owner", email, pw); // register & login to get a session key
            Result<Void> r = sut.logout(jwt);
            assertTrue(r.isSuccess());

        }


        @Test @DisplayName("Expired session key → logout reports failure")
        void logoutExpiredSession() throws Exception {
            //todo: implement, currently not possible to test
            fail();
        }
    }

    /* ───────── Guest entrance & cart ───────── */
    @Nested @DisplayName("UC‑2  Guest entrance & cart")
    class GuestFlows {
        private final String guestId  = "g‑123";
        private String guestJwt;
        private ShoppingCart guestCart;


        @BeforeEach void stubGuest() throws Exception {

            guestCart = new ShoppingCart();
            //initiate store
            StoreRepository store = new StoreRepository();
            store.createStore("S1", email);
            //initiate product catalog
            InMemoryProductCatalog catalog = new InMemoryProductCatalog();
            store.addProductToStore(email, "S1", "P1", "Product 1", "someDesc", 5.7, 10);
        }

        @Test @DisplayName("Guest login → id token")
        void guestLogin() {
            Result<String> r =sut.guestLogin();
            assertTrue(r.isSuccess());
        }

        @Test @DisplayName("Guest add‑to‑cart updates basket")
        void guestAddToCart() {
            //this test fails both with userservice and guestservice guestLogin!
            guestJwt = sut.guestLogin().getData();
            sut.addToGuestCart(guestJwt, "P1", "S1");
            Result<String> res = sut.addToGuestCart(guestJwt, "P1", "S1");
            assertTrue(res.isSuccess());
        }
    }

    /* ───────── Subscriber cart operations ───────── */
    @Nested class SubscriberCart {
        @BeforeEach void stubUser() {
            existingUser.cart();

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
