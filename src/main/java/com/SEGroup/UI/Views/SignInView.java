package com.SEGroup.UI.Views;

import com.SEGroup.Presenter.SignInPresenter;
import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "signin", layout = MainLayout.class)
@PageTitle("Sign in")
public class SignInView extends FlexLayout {

    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Password");
    private final Button signIn = new Button("Sign in", VaadinIcon.SIGN_IN.create());

    private final Binder<LoginDTO> binder = new Binder<>(LoginDTO.class);
    private final SignInPresenter presenter;

    public SignInView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        this.presenter = new SignInPresenter(this);

        add(buildForm());
        configureBinding();
    }

    private Component buildForm() {
        FormLayout form = new FormLayout();
        form.add(email, password, signIn);
        form.setWidth("400px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        signIn.setWidthFull();
        signIn.getElement().getStyle().set("margin-top", "var(--lumo-space-xl)");
        signIn.addClickListener(e -> submit());

        // "Don't have an account? Sign up" line
        Span prompt = new Span("Don't have an account? ");
        RouterLink signUpLink = new RouterLink("Sign up", SignUpView.class);
        HorizontalLayout signUpLine = new HorizontalLayout(prompt, signUpLink);
        signUpLine.setSpacing(false);
        signUpLine.setPadding(false);

        return new VerticalLayout(
                new H2("Sign in"),
                form,
                signUpLine
        );
    }

    private void configureBinding() {
        email.setClearButtonVisible(true);
        email.setErrorMessage("Please enter a valid e‑mail");
        binder.forField(email)
                .withValidator(new EmailValidator("Invalid e‑mail address"))
                .asRequired("E‑mail is required")
                .bind(LoginDTO::getEmail, LoginDTO::setEmail);

        binder.forField(password)
                .asRequired("Password is required")
                .bind(LoginDTO::getPassword, LoginDTO::setPassword);
    }

    private void submit() {
        try {
            LoginDTO login = new LoginDTO();
            binder.writeBean(login);
            presenter.onSignIn(login.getEmail(), login.getPassword());
        } catch (ValidationException ex) {
            Notification.show("Please fix validation errors", 3000, Notification.Position.MIDDLE);
        }
    }

    public void showSuccess(String sessionKey) {
        Notification.show("Logged in successfully! Session: " + sessionKey);
        UI.getCurrent().navigate("catalog");
    }

    public void showError(String message) {
        Notification.show("Login failed: " + message, 4000, Notification.Position.MIDDLE);
    }

    // DTO class for login form
    private static class LoginDTO {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
