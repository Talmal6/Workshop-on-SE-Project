package com.SEGroup.UI;

import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import com.SEGroup.Service.*;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.NotificationCenter.*;

import com.SEGroup.Infrastructure.Security;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import static org.mockito.Mockito.*;
import javax.crypto.SecretKey;

public class ServiceLocator {
    private static boolean initialised = false;
    public static boolean isInitialized() { return initialised; }

    // Core Dependencies
    private static final Security security = new Security();
    private static final PasswordEncoder passwordEncoder = new PasswordEncoder();

    // Remove initialization here - just declare the variable
    private static IAuthenticationService authService;

    // Repositories (These must be set externally or mocked for now)
    private static IUserRepository userRepository;
    private static IGuestRepository guestRepository;
    private static ITransactionRepository transactionRepository;
    private static IStoreRepository storeRepository;
    public static IProductCatalog productCatalog;
    private static IPaymentGateway paymentGateway;
    private static IShippingService shipping;
    private static NotificationCenter notificationCenter;
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
                                  IPaymentGateway gateway, IShippingService shipping) {
        if (initialised) return;     // already done – skip
        initialised = true;

        guestRepository = guests;

        // If UserRepository was created with a different PasswordEncoder, we need to:
        // Option 1: If possible, inject our PasswordEncoder into the existing repository
        if (users instanceof com.SEGroup.Infrastructure.Repositories.UserRepository) {
            try {
                // Try to set our shared passwordEncoder via reflection if needed
                java.lang.reflect.Field encoderField = users.getClass().getDeclaredField("encoder");
                encoderField.setAccessible(true);
                encoderField.set(users, passwordEncoder);
            } catch (Exception e) {
                // If this fails, just use the repository as-is, but login may not work
                System.err.println("Warning: Could not set shared PasswordEncoder in UserRepository");
            }
        }

        userRepository = users;
        transactionRepository = transactions;
        storeRepository = stores;
        productCatalog = catalog;
        paymentGateway = gateway;

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);

        authService = new SecurityAdapter(security, passwordEncoder);

        NotificationEndpoint endpoint = new NotificationEndpoint();

        guestService = new GuestService(guestRepository, authService);
        userService = new UserService(guestService, userRepository, authService, passwordEncoder);
        notificationCenter = new NotificationCenter(authService, endpoint);

        ServiceLocator.shipping = shipping;
        shippingService = shipping; // Assign the passed shipping service to the static field
        storeService = new StoreService(storeRepository, productCatalog, authService, userRepository, notificationCenter);
        transactionService = new TransactionService(
                authService,
                paymentGateway,
                transactionRepository,
                storeRepository,
                userRepository,
                shipping
        );
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
}