package com.SEGroup.acceptance;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.Service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;


public class UserServiceTests {
    @Mock
    IAuthenticationService authenticationService;
    @Mock
    IUserRepository userRepository;
    @Mock
    TransactionService transactionService;
    static UserService userService;
    String defaultUserName = "default_Email";
    String defaultUserEmail = "default_Email@myEmail.com";
    String defaultUserPassword = "defaultPassword123";
    String defaultToken;
    boolean isRegistered = false;
    boolean isLoggedIn = false;

    @BeforeAll
    void init() {
        MockitoAnnotations.openMocks(this);
        userRepository = mock(IUserRepository.class);
        authenticationService = mock(IAuthenticationService.class);
        transactionService = mock(TransactionService.class);
        userService = new UserService(userRepository, authenticationService);
    }
    //constructor
    private UserServiceTests(UserService service) {
        userService = service;
    }
    // ---------- User Registration Tests ----------

    void registerDefaultUser(){
        if (userService.register(defaultUserName, defaultUserEmail, defaultUserPassword).isSuccess()) {
            isRegistered = true;
        }
    }
    void loginDefaultUser(){
        Result<String> result = userService.login(defaultUserEmail, defaultUserPassword);
        if (result.isSuccess()) {
            isLoggedIn = true;
            defaultToken = result.getData();
        }
    }

    // Positive Test: New user registration succeeds when given valid details.
    @Test
    public void GivenValidUserDetails_WhenRegisteringUser_ThenRegistrationSucceeds() {
        Result<Void> result = userService.register("email1", "email1@myemail.com", "password123");
        assert result.isSuccess() : "Expected registration to succeed, but it failed.";
    }

    // Negative Test: Registration fails when using an email that is already registered.
    @Test
    public void GivenDuplicateEmail_WhenRegisteringUser_ThenRegistrationFailsDueToDuplicateEmail() {
        String username = "duplicate_check@myemail.com";
        Result<Void> result1 = userService.register("user1", username, "password123");
        Result<Void> result2 = userService.register("user2",username, "differentPassword");
        assert result1.isSuccess() : "Expected first registration to succeed, but it failed.";
        assert !result2.isSuccess() : "Expected second registration to fail due to duplicate email, but it succeeded.";
    }

    // ---------- User Login Tests ----------

    // Positive Test: Login succeeds when given correct credentials.
    @Test
    public void GivenValidCredentials_WhenLoggingIn_ThenLoginSucceeds() {
        String userName = "test1";
        String userEmail = "test1@muEmail.com";
        String userPassword = "testPassword123";
        Result<Void> result = userService.register(userName, userEmail, userPassword);
        assert result.isSuccess() : "Expected registration to succeed, but it failed.";
        Result<String> loginResult = userService.login(userEmail, userPassword);
        assert loginResult.isSuccess() : "Expected login to succeed, but it failed.";
    }

    // Negative Test: Login fails when a wrong password is provided.
    @Test
    public void GivenIncorrectPassword_WhenLoggingIn_ThenLoginFails() {
        String userName = "test2";
        String userEmail = "test2@muEmail.com";
        String userPassword = "testPassword123";
        Result<Void> result = userService.register(userName,userEmail, userPassword);
        assert result.isSuccess() : "Expected registration to succeed, but it failed.";
        Result<String> loginResult = userService.login(userEmail, "fakePassword");
        assert !loginResult.isSuccess() : "Expected login to fail due to incorrect password, but it succeeded.";
    }

    // ---------- User Logout Tests ----------

    // Positive Test: A logged-in user is successfully logged out.
    @Test
    public void GivenLoggedInUser_WhenLoggingOut_ThenUserIsLoggedOut() {
        String userName = "test3";
        String userEmail = "test3@muEmail.com";
        String userPassword = "testPassword123";
        Result<Void> result = userService.register(userName, userEmail, userPassword);
        assert result.isSuccess() : "Expected registration to succeed, but it failed.";
        Result<String> loginResult = userService.login(userEmail, userPassword);
        assert loginResult.isSuccess() : "Expected login to succeed, but it failed.";
        Result<Void> logoutResult = userService.logout(loginResult.getData());
        assert logoutResult.isSuccess() : "Expected logout to succeed, but it failed.";

    }

    // Negative Test: Logout is handled gracefully if the session is already expired.
    @Test
    public void GivenExpiredSession_WhenLoggingOut_ThenLogoutHandledGracefully() {
        String userName = "test4";
        String userEmail = "test4@muEmail.com";
        String userPassword = "testPassword1234";
        Result<Void> result = userService.register(userName,userEmail, userPassword);
        when(authenticationService.authenticate(userEmail,userPassword)).thenReturn("a123");
        String sessionKey = authenticationService.authenticate(userEmail,userPassword);
        authenticationService.invalidateSession(sessionKey);
        assert userService.logout(sessionKey).isSuccess();
        //todo: need  to check what is the expiration timeout for the session and expected behavior
    }

    // ---------- Personal Purchase History Tests ----------

    // Positive Test: A logged-in user can retrieve their purchase history.
    @Test
    public void GivenLoggedInUser_WhenRequestingPurchaseHistory_ThenPurchaseHistoryIsRetrieved() {
        String userName = "test5";
        String userEmail = "test5@muEmail.com";
        String userPassword = "testPassword12345";
        Result<Void> result = userService.register(userName,userEmail, userPassword);
        when(authenticationService.authenticate(userEmail,userPassword)).thenReturn("a1234");
        String sessionKey = authenticationService.authenticate(userEmail, userPassword);
        assert transactionService.getTransactionHistory(sessionKey, userEmail).isSuccess();
        //todo: need to check what is the expected behavior
    }

    // Negative Test: A guest or unregistered user cannot retrieve purchase history.
    @Test
    public void GivenGuestUser_WhenRequestingPurchaseHistory_ThenAccessIsDenied() {
    }
}
