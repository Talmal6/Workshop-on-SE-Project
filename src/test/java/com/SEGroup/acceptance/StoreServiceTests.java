package com.SEGroup.acceptance;
import com.SEGroup.Domain.IGuestRepository;

import com.SEGroup.Domain.ProductCatalog;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Store.ManagerData;
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import com.SEGroup.Service.GuestService;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.Service.UserService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoreServiceTests {

    static IAuthenticationService authenticationService;
    static ITransactionRepository transactionRepository;
    static IStoreRepository storeRepository;
    static IUserRepository userRepository;
    static ProductCatalog productCatalog;
    static IGuestRepository guestRepo;

    static StoreService storeService;
    static UserService userService;
    static GuestService guestService;
    static String defaultUserEmail = "User1@gmail.com";
    static String defaultUserPassword = "defaultPassword123";
    static String defaultStoreName = "Supermarket";
    static String defaultUserName = "Owner1";


    @BeforeEach
    public static void init() {
        authenticationService = mock(IAuthenticationService.class);
        transactionRepository = mock(ITransactionRepository.class);
        storeRepository = mock(IStoreRepository.class);
        userRepository = mock(IUserRepository.class);
        productCatalog = mock(productCatalog.class);
        guestService = mock(GuestService.class);
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository);
        userService = new UserService(guestService ,userRepository, authenticationService);
        // register and login default user
        userService.register(defaultUserName, defaultUserEmail, defaultUserPassword);
        when(authenticationService.authenticate(defaultUserEmail)).thenReturn("Abc123");

    }

    // 3.2 - Create New Store
    @Test
    public void GivenLoggedInUser_WhenCreatingNewStore_ThenStoreCreatedSuccessfully() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert storeService.createStore(sessionKey, defaultStoreName).isSuccess();
    }

    @Test
    public void GivenGuestUser_WhenCreatingNewStore_ThenStoreCreationFails() {
        // login as guest
        String guestSessionKey = userService.guestLogin().getData();
        assert !storeService.createStore(guestSessionKey, "Guest-Store").isSuccess();
    }

    // 4.1 - Manage Store Inventory
    @Test
    public void GivenValidProductDetails_WhenManagingStoreInventory_ThenInventoryUpdated() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        storeService.createStore(sessionKey, defaultStoreName);
        assert storeService.addProductToStore(sessionKey, "Drinks", defaultStoreName, "Milk", "Milk Vanilla","The milk is made out of vanilla and milk", 7.18, 3).isSuccess();
    }

    @Test
    public void GivenInvalidProductDetailsOrUnauthorizedUser_WhenManagingStoreInventory_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert storeService.addProductToStore(sessionKey, "Drinks", defaultStoreName, "Milk", "Milk Vanilla","The milk is made out of vanilla and milk", 7.18, -10).isSuccess();
    }

    // 4.2 - Change Store Purchase and Discount Policies
//    @Test
//    public void GivenValidPolicyChanges_WhenChangingStorePolicies_ThenPoliciesUpdated() {
//        // no need to implement yet im V1
//    }

//    @Test
//    public void GivenInvalidPolicyValuesOrUnauthorizedUser_WhenChangingStorePolicies_ThenOperationFails() {
//        // no need to implement yet im V1
//    }

    // 4.3 - Appoint Co-Owner
    @Test
    public void GivenValidCoOwnerDetails_WhenAppointingCoOwner_ThenCoOwnerAdded() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert storeService.appointOwner(sessionKey, defaultStoreName, "CoOwner1@gmail.com").isSuccess();
    }

    @Test
    public void GivenDuplicateOrInvalidCoOwnerDetails_WhenAppointingCoOwner_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert !storeService.appointOwner(sessionKey, defaultStoreName, "CoOwner1@gail.com").isSuccess();
    }

    // 4.4 - Remove Co-Owner
    @Test
    public void GivenValidCoOwnerRemoval_WhenRemovingCoOwner_ThenCoOwnerRemoved() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        storeService.appointOwner(sessionKey, defaultStoreName, "CoOwner1@gmail.com");
        assert storeService.removeOwner(sessionKey, defaultStoreName, "CoOwner1@gmail.com").isSuccess();
    }

    @Test
    public void GivenInvalidOrUnauthorizedCoOwnerRemoval_WhenRemovingCoOwner_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        storeService.appointOwner(sessionKey, defaultStoreName, "CoOwner1@gmail.com");
        assert !storeService.removeOwner(sessionKey, defaultStoreName, "CoOwner1@gail.com").isSuccess();
    }

    // 4.6 - Proposing a Store Manager Appointment for a Subscribed User
    @Test
    public void GivenValidManagerDetails_WhenProposingStoreManagerAppointment_ThenAppointmentProcessed() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert storeService.appointManager(sessionKey, defaultStoreName, "Manager1@gmail.com", null).isSuccess();
    }

    @Test
    public void GivenInvalidManagerDetailsOrUnauthorizedUser_WhenProposingStoreManagerAppointment_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert !storeService
                .appointManager(sessionKey, defaultStoreName, "InvalidManager", null)
                .isSuccess();
    }

    // 4.7 - Changing Store Manager Permissions
    @Test
    public void GivenValidPermissionUpdate_WhenChangingStoreManagerPermissions_ThenPermissionsUpdated() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        storeService.appointManager(sessionKey, defaultStoreName, "Manager1@gmail.com", null);
        List<String> permissions = new LinkedList<>();
        permissions.add("MANAGER_PRODUCTS");
        assert storeService.updateManagerPermissions(sessionKey, defaultStoreName, "Manager1@gmail.com", permissions)
                .isSuccess();
    }

    @Test
    public void GivenOwner_WhenRemovingManager_ThenOperationSucceeds() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        storeService.appointManager(sessionKey, defaultStoreName, "Manager1@gmail.com", null).isSuccess();
//        storeService.ma
    }
//    @Test
//    public void GivenUnauthorizedPermissionUpdate_WhenChangingStoreManagerPermissions_ThenOperationFails() {
//        String sessionKey = authenticationService.authenticate(defaultUserEmail);
//        assert storeService.appointManager(sessionKey, defaultStoreName, "Manager1@gmail.com", null).isSuccess();
//        List<String> permissions = new LinkedList<>();
//        permissions.add("MANAGER_PRODUCTS");
//        assert storeService.updateManagerPermissions(sessionKey, defaultStoreName, "Manager1@gmail.com", permissions)
//                .isSuccess();
//    }

    // 4.9 - Closing a Store
    @Test
    public void GivenValidStoreClosureRequest_WhenClosingStore_ThenStoreClosedSuccessfully() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert storeService.closeStore(sessionKey, defaultStoreName).isSuccess();
    }

    @Test
    public void GivenUnauthorizedOrAlreadyClosedStore_WhenClosingStore_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        storeService.closeStore(sessionKey, defaultStoreName).isSuccess();
        assert !storeService.closeStore(sessionKey, defaultStoreName).isSuccess();
    }

    // 4.11(a) - Information on Store Roles
    @Test
    public void GivenStoreOwnerRequest_WhenRetrievingStoreRoles_ThenRolesInformationDisplayed() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
//        assert storeService.getStoreRoles(defaultSessionKey, defaultStoreName).isSuccess();
    }

    @Test
    public void GivenUnauthorizedStoreOwnerRequest_WhenRetrievingStoreRoles_ThenAccessDenied() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.getStoreRoles(sessionKey, defaultStoreName).isSuccess();
    }

    // 4.11(b) - View Purchase History in Store (Manager Permissions)
    @Test
    public void GivenStoreOwnerRequest_WhenViewingManagerPermissions_ThenPermissionsDisplayed() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);
        assert storeService.getManagerPermissions(sessionKey, defaultStoreName, ).isSuccess();
    }

    @Test
    public void GivenUnauthorizedStoreOwnerRequest_WhenViewingManagerPermissions_ThenAccessDenied() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.getManagerPermissions(defaultSessionKey, defaultStoreName).isSuccess();
    }

    // 4.11(c) - Retrieving Information About a Specific Role Holder
    @Test
    public void GivenValidRoleHolderRequest_WhenRetrievingSpecificRoleHolderInfo_ThenInformationDisplayed() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.getRoleHolderInfo(defaultSessionKey, defaultStoreName, "Manager1").isSuccess();
    }

    @Test
    public void GivenInvalidOrUnauthorizedRoleHolderRequest_WhenRetrievingSpecificRoleHolderInfo_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.getRoleHolderInfo(defaultSessionKey, defaultStoreName, "InvalidRoleHolder").isSuccess();
    }

    // 4.12(a) - Receive Inquiry from User
    @Test
    public void GivenNewInquirySubmission_WhenReceivingInquiry_ThenInquiryRecordedAndNotified() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.receiveInquiry(defaultSessionKey, defaultStoreName, "Inquiry1").isSuccess();
    }

    @Test
    public void GivenUnauthorizedAccess_WhenReceivingInquiry_ThenAccessDenied() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.receiveInquiry(defaultSessionKey, defaultStoreName, "UnauthorizedInquiry").isSuccess();
    }

    // 4.12(b) - Provide Response to Inquiry
    @Test
    public void GivenValidInquiryResponse_WhenProvidingResponseToInquiry_ThenResponseDelivered() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.provideResponseToInquiry(defaultSessionKey, defaultStoreName, "Inquiry1", "Response1")
                .isSuccess();
    }

    @Test
    public void GivenUnauthorizedOrDuplicateInquiryResponse_WhenProvidingResponseToInquiry_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService
                .provideResponseToInquiry(defaultSessionKey, defaultStoreName, "Inquiry1", "UnauthorizedResponse")
                .isSuccess();
    }

    // 4.13 - View Purchase History in Store (for store management)
    @Test
    public void GivenStoreOwnerRequest_WhenViewingStorePurchaseHistory_ThenHistoryDisplayed() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.getStorePurchaseHistory(defaultSessionKey, defaultStoreName).isSuccess();
    }

    @Test
    public void GivenUnauthorizedUser_WhenViewingStorePurchaseHistory_ThenAccessDenied() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.getStorePurchaseHistory(defaultSessionKey, defaultStoreName).isSuccess();
    }

    // 5 - General Store Management
    @Test
    public void GivenAuthorizedStoreManager_WhenPerformingGeneralStoreManagement_ThenActionSuccessful() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.performGeneralStoreManagement(defaultSessionKey, defaultStoreName, "Action1").isSuccess();
    }

    @Test
    public void GivenUnauthorizedStoreManager_WhenPerformingGeneralStoreManagement_ThenActionBlocked() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.performGeneralStoreManagement(defaultSessionKey, defaultStoreName, "UnauthorizedAction")
                .isSuccess();
    }

    // 6.1 - Closing a Store by Marketplace Administrator
    @Test
    public void GivenValidAdminClosureRequest_WhenClosingStoreByAdmin_ThenStoreClosedSuccessfully() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.closeStoreByAdmin(defaultSessionKey, defaultStoreName).isSuccess();
    }

    @Test
    public void GivenUnauthorizedAdminOrAlreadyClosedStore_WhenClosingStoreByAdmin_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.closeStoreByAdmin(defaultSessionKey, defaultStoreName).isSuccess();
    }
}