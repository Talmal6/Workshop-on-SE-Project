package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.LoggerWrapper;
import com.SEGroup.Service.UserService;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.SignUpView;

public class SignUpPresenter {

    private final SignUpView view;
    private final UserService userService;

    public SignUpPresenter(SignUpView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
    }

    public void onSignUp(String userName, String email, String password) {
        LoggerWrapper.info("Attempting to sign in with email: " + email);
        Result<Void> result = userService.register(userName, email, password); // <-- May be null!
        LoggerWrapper.info("Sign in result: " + result);

        if (result.isSuccess()) {
            if (MainLayout.getInstance() != null) {
                MainLayout.getInstance().setUserName(userName);
            }
            view.showSuccess("Registration successful! You can now sign in.");
        } else {
            view.showError(result.getErrorMessage());
        }
    }
}
