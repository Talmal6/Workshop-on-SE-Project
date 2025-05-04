package com.SEGroup.Presenter;

import com.SEGroup.Service.UserService;
import com.SEGroup.Service.Result;
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
        System.out.println("Trying login: " + email);
        Result<String> result = userService.login(email, password); // <-- May be null!
        System.out.println("Got result: " + result);

        if (result.isSuccess()) {
            view.showSuccess(result.getData());
        } else {
            view.showError(result.getErrorMessage());
        }
    }
}
