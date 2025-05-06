package com.SEGroup.UI;

import com.SEGroup.Domain.*;
import com.SEGroup.Service.*;
import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Security;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import static org.mockito.Mockito.*;
import javax.crypto.SecretKey;

/**
 * A centralized service locator for retrieving singleton service instances.
 * Use this to inject services into Presenters or UI layers.
 */
public class ServiceLocator {

    // Core Dependencies
    private static final Security security = new Security();
    private static final PasswordEncoder passwordEncoder = new PasswordEncoder();
    //io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to create a key
    private static IAuthenticationService authService = new SecurityAdapter();

    // Repositories (These must be set externally or mocked for now)
    private static IUserRepository userRepository;
    private static IGuestRepository guestRepository;
    private static ITransactionRepository transactionRepository;
    private static IStoreRepository storeRepository;
    public static IProductCatalog productCatalog;
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
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        authService = new SecurityAdapter(security, passwordEncoder);
        guestService = new GuestService(guestRepository, authService);
        userService = new UserService(guestService, userRepository, authService);
        storeService = new StoreService(storeRepository, productCatalog, authService, userRepository);
        shippingService = mock(IShippingService.class);
        transactionService = new TransactionService(authService, paymentGateway, transactionRepository, storeRepository, userRepository, shippingService);
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
