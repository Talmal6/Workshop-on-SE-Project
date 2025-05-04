package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.Presenter.SignInPresenter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "signin", layout = MainLayout.class)
@PageTitle("Sign In")
public class SignInView extends VerticalLayout {

    private final TextField email = new TextField("Email");
    private final PasswordField password = new PasswordField("Password");
    private final Button signInBtn = new Button("Sign In");
    private final SignInPresenter presenter;

    public SignInView() {
        this.presenter = new SignInPresenter(this);

        var form = new FormLayout(email, password, signInBtn);
        signInBtn.addClickListener(e -> presenter.onSignIn(email.getValue(), password.getValue()));

        add(new H3("Sign in"), form);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
    }

    public void showSuccess(String sessionKey) {
        Notification.show("Logged in successfully! Session: " + sessionKey);
        UI.getCurrent().navigate("catalog");
    }

    public void showError(String message) {
        Notification.show("Login failed: " + message, 4000, Notification.Position.MIDDLE);
    }
}
