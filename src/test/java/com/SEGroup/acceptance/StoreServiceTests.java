package com.SEGroup.acceptance;
import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.IProductRepository;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.Service.UserService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoreServiceTests {

    @Mock IAuthenticationService auth;
    @Mock IStoreRepository       storeRepo;
    @Mock IProductRepository     productRepo;
    @Mock ITransactionRepository txRepo;
    @Mock IUserRepository        userRepo;
    @Mock IGuestRepository       guestRepo;        

    StoreService storeSvc;
    UserService  userSvc;
    GuestService guestSvc;

    final String userEmail      = "default@bgu.ac.il";
    final String userPassword   = "pw123";
    final String storeName      = "Supermarket";
    final String sessionKey     = UUID.randomUUID().toString();
    final String guestToken     = "guestTok-" + UUID.randomUUID();


    @BeforeEach
    void init() {

        // SUTs
        storeSvc = new StoreService(storeRepo, productRepo, auth);
        userSvc  = new UserService(userRepo, auth);
        guestSvc = new GuestService(guestRepo);

        /*  Default stubbing used in many tests  */
        when(auth.authenticate(userEmail)).thenReturn(sessionKey);
        when(auth.checkSessionKey(sessionKey)).thenReturn(null); // void
        when(auth.encryptPassword(anyString())).thenAnswer(i -> "hash(" + i.getArgument(0)+")");

        // userRepo registration path
        when(userRepo.addUser(any(), eq(userEmail), any()))
                     .thenReturn(null); // void

        // createStore positive path
        when(storeRepo.createStore(eq(storeName), eq(userEmail))).thenReturn(null);

        // generic “success” result helpers for storeSvc operations
        when(storeRepo.addProduct(eq(storeName), any(), anyDouble()))
                      .thenReturn(null);
    }

    @Test
    public void GivenGuestUser_WhenCreatingNewStore_ThenStoreCreationFails() {
        //login as guest
        String guestSessionKey = userService.guestLogin().getData();
        assert !storeService.createStore(guestSessionKey, "Guest-Store", defaultUserEmail).isSuccess();
    }

    // 4.1 - Manage Store Inventory
    @Test
    public void GivenValidProductDetails_WhenManagingStoreInventory_ThenInventoryUpdated() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.addProduct(defaultSessionKey, defaultStoreName, "Milk", 7.18).isSuccess();
    }

    @Test
    public void GivenInvalidProductDetailsOrUnauthorizedUser_WhenManagingStoreInventory_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.addProduct(defaultSessionKey, defaultStoreName, "Milk", -10).isSuccess();
    }

    // 4.2 - Change Store Purchase and Discount Policies
    @Test
    public void GivenValidPolicyChanges_WhenChangingStorePolicies_ThenPoliciesUpdated() {
        //no need  to implement yet im V1
    }

    @Test
    public void GivenInvalidPolicyValuesOrUnauthorizedUser_WhenChangingStorePolicies_ThenOperationFails() {
        //no need  to implement yet im V1

    }

    // 4.3 - Appoint Co-Owner
    @Test
    public void GivenValidCoOwnerDetails_WhenAppointingCoOwner_ThenCoOwnerAdded() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.addCoOwner(defaultSessionKey, defaultStoreName, "CoOwner1", defaultUserEmail).isSuccess();
    }

    @Test
    public void GivenDuplicateOrInvalidCoOwnerDetails_WhenAppointingCoOwner_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.addCoOwner(defaultSessionKey, defaultStoreName, "CoOwner1", defaultUserEmail).isSuccess();
    }

    // 4.4 - Remove Co-Owner
    @Test
    public void GivenValidCoOwnerRemoval_WhenRemovingCoOwner_ThenCoOwnerRemoved() {

        assert storeService.removeCoOwner(defaultSessionKey, defaultStoreName, "CoOwner1").isSuccess();
    }

    @Test
    public void GivenInvalidOrUnauthorizedCoOwnerRemoval_WhenRemovingCoOwner_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.removeCoOwner(defaultSessionKey, defaultStoreName, "NonExistentCoOwner").isSuccess();
    }

    // 4.6(a) - Proposing a Store Manager Appointment for a Subscribed User
    @Test
    public void GivenValidManagerDetails_WhenProposingStoreManagerAppointment_ThenAppointmentProcessed() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.proposeStoreManager(defaultSessionKey, defaultStoreName, "Manager1", defaultUserEmail).isSuccess();
    }

    @Test
    public void GivenInvalidManagerDetailsOrUnauthorizedUser_WhenProposingStoreManagerAppointment_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.proposeStoreManager(defaultSessionKey, defaultStoreName, "InvalidManager", defaultUserEmail).isSuccess();
    }

    // 4.6(b) - Approving Store Manager Appointment Proposal
    @Test
    public void GivenPendingAppointmentProposal_WhenApprovingStoreManagerAppointment_ThenManagerAppointed() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.approveStoreManager(defaultSessionKey, defaultStoreName, "Manager1").isSuccess();
    }

    @Test
    public void GivenInvalidOrMultipleAppointmentRequests_WhenApprovingStoreManagerAppointment_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.approveStoreManager(defaultSessionKey, defaultStoreName, "InvalidManager").isSuccess();
    }

    // 4.7 - Changing Store Manager Permissions
    @Test
    public void GivenValidPermissionUpdate_WhenChangingStoreManagerPermissions_ThenPermissionsUpdated() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.updateStoreManagerPermissions(defaultSessionKey, defaultStoreName, "Manager1", true).isSuccess();
    }

    @Test
    public void GivenUnauthorizedPermissionUpdate_WhenChangingStoreManagerPermissions_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.updateStoreManagerPermissions(defaultSessionKey, defaultStoreName, "InvalidManager", false).isSuccess();
    }

    // 4.9 - Closing a Store
    @Test
    public void GivenValidStoreClosureRequest_WhenClosingStore_ThenStoreClosedSuccessfully() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.closeStore(defaultSessionKey, defaultStoreName).isSuccess();
    }

    @Test
    public void GivenUnauthorizedOrAlreadyClosedStore_WhenClosingStore_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.closeStore(defaultSessionKey, defaultStoreName).isSuccess();
    }

    // 4.11(a) - Information on Store Roles
    @Test
    public void GivenStoreOwnerRequest_WhenRetrievingStoreRoles_ThenRolesInformationDisplayed() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.getStoreRoles(defaultSessionKey, defaultStoreName).isSuccess();
    }

    @Test
    public void GivenUnauthorizedStoreOwnerRequest_WhenRetrievingStoreRoles_ThenAccessDenied() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.getStoreRoles(defaultSessionKey, defaultStoreName).isSuccess();
    }

    // 4.11(b) - View Purchase History in Store (Manager Permissions)
    @Test
    public void GivenStoreOwnerRequest_WhenViewingManagerPermissions_ThenPermissionsDisplayed() {
        storeService.createStore(defaultSessionKey, defaultStoreName, defaultUserEmail);
        assert storeService.getManagerPermissions(defaultSessionKey, defaultStoreName).isSuccess();
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
        assert storeService.provideResponseToInquiry(defaultSessionKey, defaultStoreName, "Inquiry1", "Response1").isSuccess();
    }

    @Test
    public void GivenUnauthorizedOrDuplicateInquiryResponse_WhenProvidingResponseToInquiry_ThenOperationFails() {
        String sessionKey = authenticationService.authenticate(defaultUserEmail);

        assert !storeService.provideResponseToInquiry(defaultSessionKey, defaultStoreName, "Inquiry1", "UnauthorizedResponse").isSuccess();
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

        assert !storeService.performGeneralStoreManagement(defaultSessionKey, defaultStoreName, "UnauthorizedAction").isSuccess();
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
