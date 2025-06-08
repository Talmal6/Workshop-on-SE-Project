package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.Service.LoggerWrapper;
import com.SEGroup.Service.UserService;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.CheckoutDialog;
import com.SEGroup.UI.Views.PaymentDetailsDialog;
import com.SEGroup.UI.Views.ProfileView;

public class ProfilePresenter {

    private final ProfileView view;
    private final UserService userService;

    public ProfilePresenter(ProfileView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
    }

    public void loadUserProfile(String email) {
        LoggerWrapper.info("Loading user profile for email: " + email);
        Result<AddressDTO> result = userService.getUserAddress(SecurityContextHolder.token(), email);
        Result<String> result1 = userService.getUserName(SecurityContextHolder.token(), email);
        if (result.isSuccess() && result1.isSuccess()) {
            AddressDTO address = result.getData();
            String fullName = result1.getData();
            view.setUserData(fullName, email, address);
        } else {
            view.showError("Failed to load user profile: " + result.getErrorMessage());
        }
    }

    public void updateProfile(String fullName, AddressDTO address) {
        LoggerWrapper.info("Updating user profile");
        Result<Void> result = updateUserProfile(SecurityContextHolder.token(), fullName, address);
        if (result.isSuccess()) {
            view.showSuccess("Profile updated successfully!");
        } else {
            view.showError("Failed to update profile: " + result.getErrorMessage());
        }
    }

    private Result<Void> updateUserProfile(String token,  String fullName, AddressDTO address) {
        LoggerWrapper.info("Updating user profile with token: " + token);
        Result<Void> result1 = userService.setUserName(token, fullName);
        if (!result1.isSuccess()){
            return userService.setUserAddress(token, address);
        }
        else return result1;
    }

    public void navigateToPaymentDetails(String userEmail) {
        LoggerWrapper.info("Navigating to payment details for user: " + userEmail);
        PaymentDetailsDialog dialog = new PaymentDetailsDialog(this);
        dialog.open();
    }

    public boolean doesAddressOnFileExist() {
        if (!SecurityContextHolder.isLoggedIn()) return false;
        Result<AddressDTO> addressResult = userService.getUserAddress(SecurityContextHolder.token(), SecurityContextHolder.email());
        return addressResult.isSuccess() && addressResult.getData() != null && !addressResult.getData().getAddress().isEmpty();
    }

    public CheckoutDialog.CreditCardDetails getPaymentDetails() {
        if (!SecurityContextHolder.isLoggedIn()) return null;
        Result<CheckoutDialog.CreditCardDetails> creditCardDetails = userService.getUserPaymentDetails(SecurityContextHolder.token(), SecurityContextHolder.email());
        if (creditCardDetails.isSuccess()) {
            return creditCardDetails.getData();
        } else {
            LoggerWrapper.warning("Failed to retrieve payment details: " + creditCardDetails.getErrorMessage());
            return null;
        }
    }

    public boolean updatePaymentMethod(CheckoutDialog.CreditCardDetails creditCardDetails, AddressDTO address) {
        if (!SecurityContextHolder.isLoggedIn()) return false;
        Result<Void> result = userService.setUserPaymentDetails(SecurityContextHolder.token(), SecurityContextHolder.email(), creditCardDetails, address);
        if (result.isSuccess()) {
            LoggerWrapper.info("Payment method updated successfully for user: " + SecurityContextHolder.email());
            return true;
        } else {
            LoggerWrapper.error("Failed to update payment method: " + result.getErrorMessage(),
                new Exception("Update Payment Method Error"));
            return false;
        }
    }
}