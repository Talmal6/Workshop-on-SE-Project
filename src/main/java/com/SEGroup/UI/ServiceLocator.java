package com.SEGroup.UI;

import com.SEGroup.Domain.*;
import com.SEGroup.Service.*;
import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Security;

/**
 * A centralized service locator for retrieving singleton service instances.
 * Use this to inject services into Presenters or UI layers.
 */
public class ServiceLocator {

    // Core Dependencies
    private static final Security security = new Security();
    private static final PasswordEncoder passwordEncoder = new PasswordEncoder();
    private static final IAuthenticationService authService = new SecurityAdapter();

    // Repositories (These must be set externally or mocked for now)
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

        guestService = new GuestService(guestRepository, authService);
        userService = new UserService(guestService, userRepository, authService);
        storeService = new StoreService(storeRepository, productCatalog, authService, userRepository);
        transactionService = new TransactionService(authService, paymentGateway, transactionRepository, storeRepository, userRepository);
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
