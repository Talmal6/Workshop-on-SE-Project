package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.LoggerWrapper;
import com.SEGroup.Service.UserService;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.CatalogView;
import com.SEGroup.UI.Views.SignInView;
import com.vaadin.flow.component.UI;

public class SignInPresenter {

    private final SignInView view;
    private final UserService userService;

    public SignInPresenter(SignInView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
    }

    public void onSignIn(String email, String password) {
        LoggerWrapper.info("Attempting to sign in with email: " + email);
        Result<String> result = userService.login(email, password);
        LoggerWrapper.info("Sign in result: " + result);

        if (result.isSuccess()) {
            // Get token from successful login
            final String token = result.getData();

            // Get user's name if available - use the token we just received
            String userNameValue; // Temporary variable to hold the value
            try {
                Result<String> userNameResult = userService.getUserName(token, email);
                if (userNameResult.isSuccess() && userNameResult.getData() != null) {
                    userNameValue = userNameResult.getData();
                } else {
                    userNameValue = email;
                }
            } catch (Exception e) {
                System.out.println("Couldn't get username: " + e.getMessage());
                userNameValue = email;
            }

            // Now assign to final variable for use in lambda
            final String userName = userNameValue;

            // IMPORTANT: Store username along with token and email in SecurityContextHolder
            SecurityContextHolder.openSession(token, email);

            // Final variable for lambda
            final String finalEmail = email;

            // Update MainLayout directly through UI access to ensure it happens on UI thread
            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.access(() -> {
                    if (MainLayout.getInstance() != null) {
                        MainLayout layout = MainLayout.getInstance();
                        layout.setUserName(userName);
                        layout.setUserEmail(finalEmail);
                        layout.setSessionKey(token);
                        layout.refreshHeader(); // Force header refresh
                    }
                });
            }

            // Show success message and navigate to catalog
            view.showSuccess(token, userName);
        } else {
            // Add this line to show error message when login fails
            view.showError(result.getErrorMessage());
        }
    }
}