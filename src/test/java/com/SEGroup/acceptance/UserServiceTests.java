package com.SEGroup.acceptance;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.IUserRepository;

import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Infrastructure.SecurityAdapter;
import com.SEGroup.Infrastructure.Repositories.GuestRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;
import com.SEGroup.Infrastructure.Repositories.StoreRepository;
import com.SEGroup.Infrastructure.Repositories.UserRepository;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.GuestData;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.InMemoryGuestData;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.InMemoryUserData;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.UserData;
import com.SEGroup.Service.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Map;
import com.SEGroup.Domain.Report.ReportCenter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.AdditionalMatchers.not;

import static org.mockito.Mockito.*;

/**
 * Full acceptance‑style test‑suite for **UserService**. Covers:
 * • Registration / login / logout
 * • Guest sessions & carts
 * • Subscriber cart operations
 * • Purchase‑cart happy & error paths
 * • Account deletion
 */
class UserServiceTests {

    /* ───────────── mocks ───────────── */
    private IAuthenticationService auth;
    private IUserRepository users;
    private IGuestRepository guests;
    private ReportCenter reportCenter;
    private UserData ud; // add field to hold user data between setups

    /* real services wired with mocks */
    private GuestService guestSvc;
    private UserService sut;

    /* fixed sample data */
    private final String email = "owner@shop.com";
    private final String pw = "P@ssw0rd";
    private final String hashPw = "enc(P@ssw0rd)"; // what our PasswordEncoder stub returns
    private String jwt = "jwt-owner";
    private String adminSeshKey;

    /* ───────── generic stubbing ───────── */
    @BeforeEach
    void setUp() throws Exception {

        Security security = new Security();
        // io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to
        // create a key
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        auth = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
        PasswordEncoder PE = new PasswordEncoder();
        ((SecurityAdapter) auth).setPasswordEncoder(PE);
        ud = new InMemoryUserData(); // assign to field
        users = new UserRepository(ud);
        GuestData gd = new InMemoryGuestData(); // reset the same UserData field
        guests = new GuestRepository(gd);
        reportCenter = new ReportCenter();
        guestSvc = new GuestService(guests, auth);
        sut = new UserService(guestSvc, users, auth, reportCenter);
        jwt = regLoginAndGetSession("owner", email, pw); // register & login to get a session key
        Result<String> adminSeshKeyR = sut.login("Admin@Admin.Admin", "Admin");
        adminSeshKey = adminSeshKeyR.getData();

    }

    public String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        // Register a new user
        Result<Void> regResult = sut.register(userName, email, password);
        // Authenticate the user and get a session key
        return auth.authenticate(email);
    }

    /* ───────── Registration & Login ───────── */
    @Nested
    @DisplayName("UC-3  Subscriber registration & login")
    class RegistrationAndLogin {

        @BeforeEach
        void setUp() throws Exception {

            Security security = new Security();
            // io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to
            // create a key
            SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            security.setKey(key);
            auth = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
            ((SecurityAdapter) auth).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());
            UserData newud = new InMemoryUserData(); // reset the same UserData field
            users = new UserRepository(newud);
            GuestData gd = new InMemoryGuestData(); // reset the same UserData field
            guests = new GuestRepository(gd);
            reportCenter = new ReportCenter(); // also reset report center

            guestSvc = new GuestService(guests, auth);
            sut = new UserService(guestSvc, users, auth, reportCenter);
        }

        @Test
        @DisplayName("Fresh e‑mail → register succeeds")
        void registerSuccess() {
            Result<Void> r = sut.register("owner", email, pw);

            assertTrue(r.isSuccess());
        }

        @Test
        @DisplayName("Duplicate e‑mail → register fails")
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

        @Test
        @DisplayName("Wrong password → login fails")
        void loginWrongPassword() throws AuthenticationException {
            Result<Void> r = sut.register("owner", email, pw);
            assertTrue(r.isSuccess());
            assertFalse(sut.login(email, "somepass").isSuccess());

            // wrong password fails
        }

        @Test
        @DisplayName("Unknown e‑mail → login fails")
        void loginUnknownEmail() {
            Result<String> r = sut.login(email, pw);
            assertTrue(r.isFailure());
        }
    }

    /* ───────── Logout ───────── */
    @Nested
    @DisplayName("UC‑3  Logout")
    class Logout {
        @Test
        @DisplayName("Valid session key → invalidated")
        void logoutHappyPath() throws Exception {
            regLoginAndGetSession("owner", email, pw); // register & login to get a session key
            Result<Void> r = sut.logout(jwt);
            assertTrue(r.isSuccess());

        }

        @Test
        @DisplayName("Expired session key → logout reports failure")
        void logoutExpiredSession() throws Exception {
            // register new user
            Result<Void> r1 = sut.register("zaziBazazi", "bazazi@gmail.com", "zaziBazazi");
            assertTrue(r1.isSuccess());
            // login to get a session key
            String jwt = sut.login("bazazi@gmail.com", "zaziBazazi").getData();
            Result<Void> r2 = sut.logout(jwt);
            assertTrue(r2.isSuccess());
            Result<Void> r3 = sut.logout(jwt);
            assertTrue(r3.isFailure());
        }
    }

    /* ───────── Guest entrance & cart ───────── */
    @Nested
    @DisplayName("UC‑2  Guest entrance & cart")
    class GuestFlows {
        private final String guestId = "g‑123";
        private String guestJwt;

        @BeforeEach
        void stubGuest() throws Exception {
            // initiate store
            guestJwt = sut.guestLogin().getData();

            StoreRepository store = new StoreRepository();
            store.createStore("S1", email);
            // initiate product catalog
            InMemoryProductCatalog catalog = new InMemoryProductCatalog();
            store.addProductToStore(email, "S1", "P1", "Product 1", "someDesc", 5.7, 10, false, "", List.of());
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
            Result<String> r1 = sut.guestLogin();
            Result<String> r2 = sut.guestLogin();
            assertNotEquals(r1.getData(), r2.getData());
        }

        @Test
        @DisplayName("Guest add‑to‑cart updates basket")
        void guestAddToCart() {
            // this test fails both with userservice and guestservice guestLogin!
            String guestJwt = sut.guestLogin().getData();
            Result<String> r = sut.addToGuestCart(guestJwt, "P1", "S1");
            Result<String> res = sut.addToGuestCart(guestJwt, "P1", "S1");
            assertTrue(res.isSuccess());
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
            assertTrue(sut.modifyProductQuantityInCartItem(jwt, email, "P42", "S7", 5).isSuccess());
            List<BasketDTO> cart = users.getUserCart(email);
            assertEquals(5, cart.get(0).prod2qty().get("P42"));
        }

        @Test
        @DisplayName("Remove item sets qty=0")
        void remove_item_success() {
            sut.addToUserCart(jwt, email, "P42", "S7");
            assertTrue(sut.removeFromUserCart(jwt, email, "P42", "S7").isSuccess());
            List<BasketDTO> cart = users.getUserCart(email);
            assertEquals(null, cart.get(0).prod2qty().get("P42"));
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

    /* ───────── Purchase shopping cart ───────── */
    @Nested
    @DisplayName("UC‑3  Purchase cart")
    class PurchaseCart {
        @Test
        @DisplayName("Existing user → purchase succeeds")
        void purchaseSuccess() {
            // already tested in the transactional test
            // Result<Void> r = sut.purchaseShoppingCart(jwt, email);
            // assertTrue(r.isSuccess());
        }

        @Test
        @DisplayName("Unknown user → purchase fails")
        void purchaseUnknownUser() {
            // already tested in the transactional test
        }
    }

    /* ───────── Account deletion ───────── */
    @Nested
    @DisplayName("UC‑3  Delete user")
    class DeleteUser {
        @Test
        @DisplayName("User exists → deletion succeeds")
        void deleteSuccess() {
            String adminAuth = sut.login("Admin@Admin.Admin", "Admin").getData();
            Result<Void> r = sut.deleteUser(adminAuth, email);
            assertTrue(r.isSuccess());
            User user = users.findUserByEmail(email);
            assertNull(user); // user should be deleted
        }

        @Test
        @DisplayName("User missing → deletion fails")
        void deleteFailsWhenMissing() {
            Result<Void> r1 = sut.deleteUser(jwt, email);
            Result<Void> r = sut.deleteUser(jwt, email);
            assertTrue(r.isFailure());
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
        store.addProductToStore(email, "S1", "P1", "Product 1", "someDesc", 5.7, 10, false, "", List.of());
        store.addProductToStore(email, "S1", "P2", "Product 2", "someDesc", 5.7, 10, false, "", List.of());
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

    @Nested
    @DisplayName("UC-X User Profile Management") // Assuming X is the next use case number
    class UserProfileManagementTests {

        private String testUserEmail = "profileuser@example.com";
        private String testUserPassword = "password123";
        private String testUserInitialName = "profileUser";
        private String testUserSessionKey;

        @BeforeEach
        void setUpProfileUser() throws Exception {
            // Ensure this user is fresh for each test, or handle existing user if necessary
            // For simplicity, we assume register will fail if user exists, and login will
            // work.
            // A more robust setup might clean up this user or use unique emails per test.
            sut.register(testUserInitialName, testUserEmail, testUserPassword); // Register if not exists
            Result<String> loginResult = sut.login(testUserEmail, testUserPassword);
            assertTrue(loginResult.isSuccess(), "Login failed in setup");
            testUserSessionKey = loginResult.getData();
        }

        @Test
        @DisplayName("Change username successfully")
        void changeUsername_success() {
            String newUsername = "newProfileUser";
            Result<Void> setResult = sut.setUserName(testUserSessionKey, newUsername);
            assertTrue(setResult.isSuccess(), "Failed to set username");
        }

        @Test
        @DisplayName("Change username to an existing username (simulated by trying to set to another user's name if possible)")
        void changeUsername_toExistingUsername_fails() throws Exception {
            // Register a second user
            String otherUserEmail = "otheruser@example.com";
            String otherUserName = "otherUser";
            Result<Void> r =sut.register(otherUserName, otherUserEmail, testUserPassword); // Assume registration is successful
            assertTrue(r.isSuccess(), "Failed to register other user");
            Result<Void> setResult = sut.setUserName(testUserSessionKey, otherUserName);
            assertTrue(setResult.isFailure());
            // The failure message should ideally indicate "username already exists" or
            // similar.
        }

        @Test
        @DisplayName("Change username with invalid session fails")
        void changeUsername_invalidSession_fails() {
            Result<Void> setResult = sut.setUserName("invalidSessionKey", "anyNewName");
            assertTrue(setResult.isFailure(), "Should have failed with an invalid session.");
        }

        @Test
        @DisplayName("Change username when user is suspended fails")
        void changeUsername_userSuspended_fails() {
            // Suspend the user
            Result<String> suspendResult = sut.suspendUser(adminSeshKey, testUserEmail, 100, "test suspension");
            assertTrue(suspendResult.isSuccess(), "Failed to suspend user");

            Result<Void> setResult = sut.setUserName(testUserSessionKey, "nameWhileSuspended");
            assertTrue(setResult.isFailure(), "Should have failed to change username while suspended.");

            // Clean up: unsuspend user
            sut.unsuspendUser(adminSeshKey, testUserEmail);
        }

        @Test
        @DisplayName("Update and retrieve user address successfully")
        void updateUserAddress_success() {
            AddressDTO newAddress = new AddressDTO("123 Main St", "Anytown", "CountryLand", "12345");
            Result<Void> setResult = sut.setUserAddress(testUserSessionKey, newAddress);
            assertTrue(setResult.isSuccess(), "Failed to set user address");

            Result<AddressDTO> getResult = sut.getUserAddress(testUserSessionKey, testUserEmail);
            assertTrue(getResult.isSuccess(), "Failed to get user address");
            assertNotNull(getResult.getData(), "Retrieved address is null.");
            assertEquals("123 Main St", getResult.getData().getAddress());
            assertEquals("Anytown", getResult.getData().getCity());
            assertEquals("CountryLand", getResult.getData().getCountry());
            assertEquals("12345", getResult.getData().getZip());
        }

        @Test
        @DisplayName("Update user address with invalid session fails")
        void updateUserAddress_invalidSession_fails() {
            AddressDTO newAddress = new AddressDTO("123 Main St", "Anytown", "CountryLand", "12345");
            Result<Void> setResult = sut.setUserAddress("invalidSessionKey", newAddress);
            assertTrue(setResult.isFailure(), "Should have failed to set address with an invalid session.");
        }

        @Test
        @DisplayName("Get user address with invalid session fails")
        void getUserAddress_invalidSession_fails() {
            Result<AddressDTO> getResult = sut.getUserAddress("invalidSessionKey", testUserEmail);
            assertTrue(getResult.isFailure(), "Should have failed to get address with an invalid session.");
        }

        @Test
        @DisplayName("Update user address when user is suspended fails")
        void updateUserAddress_userSuspended_fails() {
            // Suspend the user
            Result<String> suspendResult = sut.suspendUser(adminSeshKey, testUserEmail, 100, "test suspension");
            assertTrue(suspendResult.isSuccess(), "Failed to suspend user");

            AddressDTO newAddress = new AddressDTO("456 Suspended Ave", "Suspendville", "CountryLand", "67890");
            Result<Void> setResult = sut.setUserAddress(testUserSessionKey, newAddress);
            assertTrue(setResult.isFailure(), "Should have failed to update address while suspended.");

            // Clean up: unsuspend user
            sut.unsuspendUser(adminSeshKey, testUserEmail);
        }

        @Test
        @DisplayName("Get user address when user is suspended (should succeed as per current UserService implementation)")
        void getUserAddress_userSuspended_canStillRetrieve() {
            AddressDTO initialAddress = new AddressDTO("Initial St", "InitCity", "InitCountry", "00000");
            Result<Void> setInitialResult = sut.setUserAddress(testUserSessionKey, initialAddress);
            assertTrue(setInitialResult.isSuccess(), "Failed to set initial address.");

            // Suspend the user
            Result<String> suspendResult = sut.suspendUser(adminSeshKey, testUserEmail, 100, "test suspension");
            assertTrue(suspendResult.isSuccess(), "Failed to suspend user");

            // Attempt to get address while suspended
            // UserService.getUserAddress only checks sessionKey, not suspension status for
            // the target email.
            Result<AddressDTO> getResult = sut.getUserAddress(testUserSessionKey, testUserEmail);
            assertTrue(getResult.isSuccess(),
                    "Should be able to get address even when suspended, as long as session is valid.");
            assertNotNull(getResult.getData());
            assertEquals(initialAddress.getAddress(), getResult.getData().getAddress());

            // Clean up: unsuspend user
            sut.unsuspendUser(adminSeshKey, testUserEmail);
        }
    }
}
