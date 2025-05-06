package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.LoggerWrapper;
import com.SEGroup.Service.UserService;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.SignInView;

public class SignInPresenter {

    private final SignInView view;
    private final UserService userService;

    public SignInPresenter(SignInView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
    }

    public void onSignIn(String email, String password) {
        LoggerWrapper.info("Attempting to sign in with email: " + email);
        Result<String> result = userService.login(email, password); // <-- May be null!
        LoggerWrapper.info("Sign in result: " + result);

        if (result.isSuccess()) {
            view.showSuccess(result.getData(), email);
            MainLayout.getInstance().setSessionKey(result.getData());
            MainLayout.getInstance().setUserEmail(email);
        } else {
            view.showError(result.getErrorMessage());
        }
    }
}
