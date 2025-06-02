package com.SEGroup.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationWithSender;
import com.SEGroup.Infrastructure.Repositories.ProductCatalogRepository;
import com.SEGroup.Infrastructure.Repositories.StoreRepository;
import com.SEGroup.Infrastructure.Repositories.UserRepository;
import com.SEGroup.Infrastructure.Repositories.GuestRepository;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Infrastructure.SecurityAdapter;
import com.SEGroup.Service.*;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

public class NotificationCenterAcceptanceTests {

    // Test constants and dependencies
    private static String VALID_SESSION = "valid-session";
    private static final String INVALID_SESSION = "invalid-session";
    private static final String FOUNDER_EMAIL = "founder@example.com";
    private static final String FOUNDER_NAME = "founder";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_NAME = "user";
    private static final String MANAGER_EMAIL = "manager@example.com";
    private static final String MANAGER_NAME = "manager";
    private static final String PASSWORD = "pass123";
    private static final String STORE_NAME = "TestStore";

    // Dependencies and services
    private IAuthenticationService authService;
    private IUserRepository userRepository;
    private StoreRepository storeRepository;
    private ProductCatalogRepository productCatalog;
    private StoreService storeService;
    private UserService userService;
    private NotificationCenter notificationCenter;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize security and auth service
        Security security = new Security();
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        authService = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
        ((SecurityAdapter) authService).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());
        // Initialize repositories and NotificationCenter
        userRepository = new UserRepository();
        storeRepository = new StoreRepository();
        productCatalog = new ProductCatalogRepository();
        notificationCenter = new NotificationCenter(authService);
        // Inject dependencies into service layer
        storeService = new StoreService(storeRepository, productCatalog, authService, userRepository,
                notificationCenter);
        userService = new UserService(new GuestService(new GuestRepository(), authService),
                userRepository, authService, new com.SEGroup.Domain.Report.ReportCenter());
        // Register founder user and get a valid session token
        VALID_SESSION = registerAndLogin(FOUNDER_NAME, FOUNDER_EMAIL, PASSWORD);
        // Prepare an invalid session constant (not registered in authService)
        // INVALID_SESSION remains "invalid-session"
    }

    // Helper to register a user and return their session key (JWT)
    private String registerAndLogin(String username, String email, String password) throws Exception {
        userService.register(username, email, password);
        return authService.authenticate(email);
    }

    @Test
    @DisplayName("Valid session user sends message to store founder -> notification delivered to founder")
    public void sendMessageToFounder_ValidSession_ShouldDeliverNotification() throws Exception {
        // Arrange: founder already registered and logged in (VALID_SESSION), store
        // created
        storeService.createStore(VALID_SESSION, STORE_NAME);
        // Register a second user (the sender) and get their session
        String senderSession = registerAndLogin(USER_NAME, USER_EMAIL, PASSWORD);

        // Act: sender sends a message to the store founder through the service
        String message = "Hello, I have a question about your store.";
        Result<Void> result = storeService.sendMessageToStoreFounder(senderSession, STORE_NAME, message);

        // Assert: The service indicates success
        assertTrue(result.isSuccess(), "Message sending should succeed with valid session");
        // Allow NotificationCenter dispatcher to process the notification
        Thread.sleep(100); // (Alternatively, loop until notification appears or use latch)
        // Verify the founder's NotificationCenter history contains the new notification
        List<Notification> founderNotifs = notificationCenter.getUserNotifications(FOUNDER_EMAIL);
        assertNotNull(founderNotifs, "Founder should have a notification history entry");
        Notification latest = founderNotifs.get(founderNotifs.size() - 1);
        assertEquals(message, latest.getMessage(), "Notification message should match");
        // If it's a user-to-user notification, it should carry sender info
        if (latest instanceof NotificationWithSender) {
            assertEquals(USER_EMAIL, ((NotificationWithSender) latest).getSenderId(),
                    "Sender ID should match the user");
        }
    }

    @Test
    @DisplayName("Invalid session user sends message -> operation fails and no notification sent")
    public void sendMessageToFounder_InvalidSession_ShouldFail() throws Exception {
        // Arrange: founder with store exists
        storeService.createStore(VALID_SESSION, STORE_NAME);
        // (No need to register a sender user, we'll use an invalid session token)

        // Act: attempt to send a message with an invalid session key
        Result<Void> result = storeService.sendMessageToStoreFounder(INVALID_SESSION, STORE_NAME,
                "Should not go through");

        // Assert: Operation should fail due to authentication
        assertFalse(result.isSuccess(), "Expected failure when session key is invalid");
        // NotificationCenter should not record any notification for the founder
        Thread.sleep(50);
        List<Notification> founderNotifs = notificationCenter.getUserNotifications(FOUNDER_EMAIL);
        assertTrue(founderNotifs == null || founderNotifs.isEmpty(),
                "Founder should not receive any notification on invalid session attempt");
    }

    @Test
    @DisplayName("Closing a store notifies all store staff (owners/managers) of closure")
    public void closeStore_WithManager_ShouldNotifyAllWorkers() throws Exception {
        // Arrange: Set up founder and a manager user
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String managerSession = registerAndLogin(MANAGER_NAME, MANAGER_EMAIL, PASSWORD);
        // Appoint the second user as a manager of the store
        Result<Void> appointResult = storeService.appointManager(VALID_SESSION, STORE_NAME, MANAGER_EMAIL,
                Collections.emptyList());
        assertTrue(appointResult.isSuccess(), "Manager appointment should succeed");

        // Act: founder closes the store
        Result<Void> closeResult = storeService.closeStore(VALID_SESSION, STORE_NAME);

        // Assert: close operation succeeded
        assertTrue(closeResult.isSuccess(), "Store closure should succeed");
        // Wait for notifications to dispatch
        Thread.sleep(100);
        // Verify both founder and manager received closure notifications
        List<Notification> founderNotifs = notificationCenter.getUserNotifications(FOUNDER_EMAIL);
        List<Notification> managerNotifs = notificationCenter.getUserNotifications(MANAGER_EMAIL);
        assertNotNull(managerNotifs, "Manager should have a notification after store closure");
        Notification mgrNote = managerNotifs.get(0);
        assertTrue(mgrNote.getMessage().contains("has been closed"),
                "Manager's notification should mention store closure");
        assertNotNull(founderNotifs, "Founder should also have a notification (closure notice)");
    }

}