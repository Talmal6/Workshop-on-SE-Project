package com.SEGroup.UI;

import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationEndpoint;
import com.SEGroup.Service.*;
import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Security;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.*;
import javax.crypto.SecretKey;

/**
 * A centralized service locator for retrieving singleton service instances.
 * Use this to inject services into Presenters or UI layers.
 */

public class ServiceLocator {

    // Core Dependencies
    private static ApplicationContext applicationContext;
    private static final Security security = new Security();
    private static final PasswordEncoder passwordEncoder = new PasswordEncoder();
    private static IAuthenticationService authService;

    // Notification components
    private static NotificationEndpoint notificationEndpoint;
    private static NotificationSender notificationSender;
    private static DirectNotificationSender directNotificationSender;
    private static NotificationCenter notificationCenter;

    // Repositories
    private static IUserRepository userRepository;
    private static IGuestRepository guestRepository;
    private static ITransactionRepository transactionRepository;
    private static IStoreRepository storeRepository;
    private static IProductCatalog productCatalog;
    private static IPaymentGateway paymentGateway;

    // Services
    private static GuestService guestService;
    private static UserService userService;
    private static StoreService storeService;
    private static TransactionService transactionService;
    private static IShippingService shippingService;

    public static void initialize(IGuestRepository guests,
                                  IUserRepository users,
                                  ITransactionRepository transactions,
                                  IStoreRepository stores,
                                  IProductCatalog catalog,
                                  IPaymentGateway gateway) {
        guestRepository = guests;
        userRepository = users;
        transactionRepository = transactions;
        storeRepository = stores;
        productCatalog = catalog;
        paymentGateway = gateway;

        // Initialize Security
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        authService = new SecurityAdapter(security, passwordEncoder);
        notificationCenter = new com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter(authService);



        // Initialize services
        guestService = new GuestService(guestRepository, authService);
        userService = new UserService(guestService, userRepository, authService);
        storeService = new StoreService(storeRepository, productCatalog, authService, userRepository, notificationCenter);
        shippingService = mock(IShippingService.class);
        transactionService = new TransactionService(authService, paymentGateway, transactionRepository, storeRepository, userRepository, shippingService, notificationCenter);
    }

    public static NotificationCenter getNotificationCenter() {
        if (applicationContext != null) {
            return applicationContext.getBean(NotificationCenter.class);
        }
        return notificationCenter;
    }

    public static DirectNotificationSender getDirectNotificationSender() {
        // ① if we already have Spring – always use the real bean
        if (applicationContext != null) {
            return applicationContext.getBean(DirectNotificationSender.class);
        }

        // ② fallback only in pure unit tests - no Spring container
        if (directNotificationSender == null) {
            directNotificationSender = new DirectNotificationSender();

            // Manually inject dependencies for test environment
            directNotificationSender.endpoint = getNotificationEndpoint();

            // Create a simple broadcast service if needed
            if (applicationContext == null) {
                System.out.println("Creating temporary broadcast service for DirectNotificationSender");
                directNotificationSender.broadcast = new NotificationBroadcastService();
            }

            directNotificationSender.domainCenter = getNotificationCenter();
        }

        return directNotificationSender;
    }


    public static IAuthenticationService getAuthenticationService() {
        return authService;
    }

    public static GuestService getGuestService() {
        return guestService;
    }

    public static UserService getUserService() {
        return userService;
    }

    public static StoreService getStoreService() {
        return storeService;
    }

    public static TransactionService getTransactionService() {
        return transactionService;
    }
    // Add these methods after your existing methods in ServiceLocator.java

    // Method to access the Spring application context
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // Setter method to configure application context
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
        System.out.println("Application context set in ServiceLocator");

        // If we have an application context, try to get beans from it
        if (context != null) {
            try {
                // These lines are optional - only if you want to try getting beans directly
                if (directNotificationSender == null) {
                    directNotificationSender = context.getBean(DirectNotificationSender.class);
                }
            } catch (Exception e) {
                System.err.println("Could not get beans from context: " + e.getMessage());
            }
        }
    }
    public static NotificationEndpoint getNotificationEndpoint() {
        /* ① primary – grab the singleton managed by Spring */
        if (applicationContext != null) {
            return applicationContext.getBean(NotificationEndpoint.class);
        }

        /* ② fallback – old reflective trick for pure-unit-tests */
        if (notificationEndpoint == null && notificationCenter != null) {
            try {
                var f = notificationCenter.getClass().getDeclaredField("endpoint");
                f.setAccessible(true);
                notificationEndpoint = (NotificationEndpoint) f.get(notificationCenter);
            } catch (Exception e) {
                System.err.println("Cannot access NotificationEndpoint: " + e.getMessage());
            }
        }
        return notificationEndpoint;
    }

    public static BidApprovalManager getBidApprovalManager() {
        return applicationContext.getBean(BidApprovalManager.class);
    }



    }