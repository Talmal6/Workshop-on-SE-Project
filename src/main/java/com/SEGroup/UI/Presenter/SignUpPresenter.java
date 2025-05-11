package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.LoggerWrapper;
import com.SEGroup.Service.UserService;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.SignUpView;
import com.vaadin.flow.component.UI;

public class SignUpPresenter {

    private final SignUpView view;
    private final UserService userService;

    public SignUpPresenter(SignUpView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
    }

    public void onSignUp(String userName, String email, String password) {
        LoggerWrapper.info("Attempting to sign up with email: " + email);
        Result<Void> result = userService.register(userName, email, password);
        LoggerWrapper.info("Sign up result: " + result);

        if (result.isSuccess()) {
            // Show success message
            view.showSuccess("Registration successful! You can now sign in.");

            // Navigate to sign-in page
            UI.getCurrent().navigate("signin");
        } else {
            view.showError(result.getErrorMessage());
        }
    }
}
