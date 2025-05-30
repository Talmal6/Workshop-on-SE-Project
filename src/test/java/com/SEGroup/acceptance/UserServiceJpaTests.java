package com.SEGroup.acceptance;

import com.SEGroup.MarketplaceApplication;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Report.ReportCenter;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.DbGuestRepository;
import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.DbUserRepository;
import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.JpaGuestRepository;
import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.JpaUserRepository;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Infrastructure.SecurityAdapter;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.InMemoryProductCatalog;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.StoreRepository;
import com.SEGroup.Service.GuestService;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.UserService;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MarketplaceApplication.class)
@ActiveProfiles("db")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceJpaTests {

    @Autowired private JpaUserRepository   jpaUserRepository;
    @Autowired private JpaGuestRepository  jpaGuestRepository;
    @Autowired private IAuthenticationService auth;
    @Autowired private ReportCenter         reportCenter;

    private IGuestRepository   guests;
    private IUserRepository    users;
    private DbUserRepository   dbUserRepository;
    private GuestService       guestSvc;
    private UserService        sut;

    private final String email = "owner@shop.com";
    private final String pw    = "P@ssw0rd";
    private String jwt;
    private String adminSeshKey;

    @BeforeEach
    void setUp() throws Exception {
        // --- configure SecurityAdapter ---
        Security security = new Security();
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        auth = new SecurityAdapter(security, new PasswordEncoder());
        ((SecurityAdapter) auth).setPasswordEncoder(new PasswordEncoder());

        // --- wire JPA repositories into our Db* adapters ---
        guests           = new DbGuestRepository(jpaGuestRepository);
        dbUserRepository = new DbUserRepository(jpaUserRepository);
        users            = dbUserRepository;

        guestSvc = new GuestService(guests, auth);
        sut      = new UserService(guestSvc, users, auth, reportCenter);

        // --- register & login a normal user ---
        jwt = regLoginAndGetSession("owner", email, pw);

        // --- ensure Admin exists & log them in ---
        Result<String> adminR = sut.login("Admin@Admin.Admin", "Admin");
        adminSeshKey = adminR.getData();
    }

    private String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        assertTrue(sut.register(userName, email, password).isSuccess());
        return auth.authenticate(email);
    }

    /* ───────── Registration & Login ───────── */
    @Nested
    @DisplayName("UC-3  Subscriber registration & login (JPA)")
    class RegistrationAndLogin {

        @Test @DisplayName("Fresh e-mail → register succeeds")
        void registerSuccess() {
            assertTrue(sut.register("newUserJPA", "newjpa@shop.com", "pw").isSuccess());
        }

        @Test @DisplayName("Duplicate e-mail → register fails")
        void registerDuplicate() {
            assertTrue(sut.register("dupJPA", "dup@shop.com", "pw").isSuccess());
            assertTrue(sut.register("dupJPA", "dup@shop.com", "pw").isFailure());
        }

        @Test @DisplayName("Correct credentials → login returns JWT")
        void loginSuccess() throws Exception {
            String e = "loginjpa@shop.com", p = "pwJPA";
            assertTrue(sut.register("ljpa", e, p).isSuccess());
            assertTrue(sut.login(e, p).isSuccess());
        }

        @Test @DisplayName("Wrong password → login fails")
        void loginWrongPassword() throws AuthenticationException {
            String e = "wrongjpa@shop.com", p = "correct";
            assertTrue(sut.register("wjpa", e, p).isSuccess());
            assertFalse(sut.login(e, "incorrect").isSuccess());
        }

        @Test @DisplayName("Unknown e-mail → login fails")
        void loginUnknownEmail() {
            assertTrue(sut.login("no@jpa.com", "pw").isFailure());
        }
    }

    /* ───────── Logout ───────── */
    @Nested
    @DisplayName("UC-3  Logout (JPA)")
    class Logout {
        @Test @DisplayName("Valid session key → invalidated")
        void logoutHappyPath() throws Exception {
            String session = regLoginAndGetSession("lgout", "lg@jpa.com", "pw");
            assertTrue(sut.logout(session).isSuccess());
        }

        @Test @DisplayName("Expired session key → logout reports failure")
        void logoutExpiredSession() throws Exception {
            sut.register("expJPA", "exp@jpa.com", "pw");
            String s = sut.login("exp@jpa.com", "pw").getData();
            assertTrue(sut.logout(s).isSuccess());
            assertTrue(sut.logout(s).isFailure());
        }
    }

    /* ───────── Guest entrance & cart ───────── */
    @Nested
    @DisplayName("UC-2  Guest entrance & cart (JPA)")
    class GuestFlows {
        private String guestJwt;

        @BeforeEach
        void stubGuest() throws Exception {
            // initiate store
            guestJwt = sut.guestLogin().getData();

            StoreRepository store = new StoreRepository();
            store.createStore("S1", email);
            // initiate product catalog
            InMemoryProductCatalog catalog = new InMemoryProductCatalog();
            store.addProductToStore(email, "S1", "P1", "Product 1", "someDesc", 5.7, 10, false,"",List.of());
        }

        @Test
        @DisplayName("Guest login → id token")
        void guestLogin() {
            Result<String> r = sut.guestLogin();
            assertTrue(r.isSuccess());
        }

        @Test
        @DisplayName("2 guest logins → 2 different ids")
        void guestLoginTwice() {
            String g1 = sut.guestLogin().getData();
            String g2 = sut.guestLogin().getData();
            assertNotEquals(g1, g2);
        }

        @Test @DisplayName("Guest add-to-cart updates basket")
        void guestAddToCart() {
            String g = sut.guestLogin().getData();
            assertTrue(sut.addToGuestCart(g, "P1", "S1").isSuccess());
        }
    }

    /* ───────── Subscriber cart operations ───────── */
    @Nested
    class SubscriberCart {

        @Test
        @DisplayName("Add item → basket created")
        void addItem() {
            assertTrue(sut.addToUserCart(jwt, email, "P42", "S7").isSuccess());
        }

        @Test
        @DisplayName("Modify quantity persists value")
        void modify_quantity_success() {
            sut.addToUserCart(jwt, email, "P42", "S7");
            Result<String> result = sut.modifyProductQuantityInCartItem(jwt, email, "P42", "S7", 3);
            assertTrue(result.isSuccess());
            List<BasketDTO> cart = users.getUserCart(email);
            assertEquals(3, cart.get(0).prod2qty().get("P42"));
        }

        @Test
        @DisplayName("Remove item sets qty=0")
        void remove_item_success() {
            sut.addToUserCart(jwt, email, "P42", "S7");
            assertTrue(sut.removeFromUserCart(jwt, email, "P42", "S7").isSuccess());
            List<BasketDTO> cart = users.getUserCart(email);
            assertTrue(cart.get(0).prod2qty().get("P42") == null || cart.get(0).prod2qty().get("P42") == 0);
        }

        @Test
        @DisplayName("Repository clear wipes basket")
        void clearCart() {
            sut.addToUserCart(jwt, email, "P42", "S7");
            users.clearUserCart(email);
            List<BasketDTO> cart = users.getUserCart(email);
            assertEquals(0, cart.size());
        }
    }



    // /* ───────── Admin operations ───────── */
    @Test
    @DisplayName("Add admin then add him again")
    void WhenAdminAlreadyExists_ThenAddAdminFails() {
        // A hard codede admin User
        String adminEmail = "Admin@Admin.Admin";
        String authKey = sut.login("Admin@Admin.Admin", "Admin").getData();
        // Create a new user
        String newUserEmail = "new@new.new";
        Result<Void> r = sut.register("Admin", newUserEmail, "Admin");
        assertTrue(r.isSuccess());
        // Attempt to add the user as an admin
        Result<Void> r2 = sut.setAsAdmin(authKey, newUserEmail);
        assertTrue(r2.isSuccess());
        // Attempt to add the same user as an admin again
        Result<Void> r3 = sut.setAsAdmin(authKey, newUserEmail);
        assertTrue(r3.isFailure());
    }

    @Test
    @DisplayName("Add admin then remove him")
    void WhenAdminAlreadyExists_ThenRemoveAdmin() {
        // A hard codede admin User
        String adminEmail = "Admin@Admin.Admin";
        String authKey = sut.login("Admin@Admin.Admin", "Admin").getData();
        // Create a new user
        String newUserEmail = "new@new.new";
        Result<Void> r = sut.register("Admin", newUserEmail, "Admin");
        assertTrue(r.isSuccess());
        // Attempt to add the user as an admin
        Result<Void> r2 = sut.setAsAdmin(authKey, newUserEmail);
        assertTrue(r2.isSuccess());
        // Attempt to remove the user as an admin
        Result<Void> r3 = sut.removeAdmin(authKey, newUserEmail);
        // checn if the removed admin can do admin operations
        String newUserAuthKey = sut.login(newUserEmail, "Admin").getData();
        Result<Void> r4 = sut.setAsAdmin(newUserAuthKey, newUserEmail);
        assertTrue(r4.isFailure());
    }

    private void storeForUserAction() {
        // initiate store
        StoreRepository store = new StoreRepository();
        store.createStore("S1", email);
        // initiate product catalog
        InMemoryProductCatalog catalog = new InMemoryProductCatalog();
        store.addProductToStore(email, "S1", "P1", "Product 1", "someDesc", 5.7, 10, false,"",List.of());
        store.addProductToStore(email, "S1", "P2", "Product 2", "someDesc", 5.7, 10, false,"",List.of());
    }

    @Test
    public void WhileUserIsSuspended_AttemptUserAction_ShouldFail() throws Exception {
        storeForUserAction();
        String susKey = regLoginAndGetSession("suspendedUser", "sus@sus.com", "pass");
        // Attemps to add to cart as unsuspended user
        Result<String> r1 = sut.addToUserCart(susKey, "sus@sus.com", "Product 1", "S1");
        assertTrue(r1.isSuccess());
        Result<String> result = sut.suspendUser(adminSeshKey, "sus@sus.com", 100, "suspension reason");
        assertTrue(result.isSuccess());
        Result<String> r2 = sut.addToUserCart(susKey, "sus@sus.com", "Product 2", "S1");
        assertTrue(r2.isFailure());
    }

    @Test
    public void WhileUserIsSuspeded_UnsuspendUser_ThenAttemptUserAction_ShouldSucceed() throws Exception {
        storeForUserAction();
        String susKey = regLoginAndGetSession("suspendedUser", "sus@sus.com", "pass");
        // suspend the user
        Result<String> result = sut.suspendUser(adminSeshKey, "sus@sus.com", 100, "suspension reason");
        assertTrue(result.isSuccess());
        Result<String> r1 = sut.addToUserCart(susKey, "sus@sus.com", "Product 1", "S1");
        assertTrue(r1.isFailure());
        Result<String> r2 = sut.unsuspendUser(susKey, "sus@sus.com");
        Result<String> r3 = sut.addToUserCart(susKey, "sus@sus.com", "Product 2", "S1");
        assertTrue(r3.isSuccess());
    }

}
