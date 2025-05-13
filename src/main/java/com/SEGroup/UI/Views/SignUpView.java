package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.SignUpPresenter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "signup", layout = MainLayout.class)
@PageTitle("Sign Up")
public class SignUpView extends VerticalLayout {

    private final SignUpPresenter presenter;
    private final TextField fullNameField = new TextField("Full Name");
    private final EmailField emailField = new EmailField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");
    private final Binder<UserRegistration> binder = new Binder<>();

    public SignUpView() {
        this.presenter = new SignUpPresenter(this);

        addClassName("signup-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(createSignUpForm());
    }

    private Component createSignUpForm() {
        Div formCard = new Div();
        formCard.addClassName("signup-card");
        formCard.getStyle()
                .set("background-color", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)")
                .set("padding", "40px")
                .set("max-width", "450px")
                .set("width", "100%");

        H2 header = new H2("Create an Account");
        header.getStyle().set("margin-top", "0");

        Paragraph subheader = new Paragraph("Join our marketplace to start shopping!");
        subheader.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "20px");

        // Configure form fields
        fullNameField.setPlaceholder("Enter your full name");
        fullNameField.setPrefixComponent(VaadinIcon.USER.create());
        fullNameField.setRequiredIndicatorVisible(true);
        fullNameField.setWidthFull();

        emailField.setPlaceholder("Enter your email address");
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidthFull();

        passwordField.setPlaceholder("Create a password");
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setWidthFull();
        passwordField.setRevealButtonVisible(true);

        confirmPasswordField.setPlaceholder("Confirm your password");
        confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRevealButtonVisible(true);

        // Setup validation
        setupValidation();

        // Create form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(fullNameField, emailField, passwordField, confirmPasswordField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // Buttons
        Button signUpButton = new Button("Sign Up", new Icon(VaadinIcon.USER_CHECK));
        signUpButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signUpButton.addClickListener(e -> submitForm());
        signUpButton.addClickShortcut(Key.ENTER);
        signUpButton.setWidthFull();

        Button resetButton = new Button("Reset", new Icon(VaadinIcon.REFRESH));
        resetButton.addClickListener(e -> resetForm());
        resetButton.setWidthFull();

        HorizontalLayout buttonsLayout = new HorizontalLayout(signUpButton, resetButton);
        buttonsLayout.setWidthFull();

        // Sign in link
        Paragraph signInText = new Paragraph("Already have an account?");
        signInText.getStyle().set("text-align", "center");

        Button signInLink = new Button("Sign In");
        signInLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        signInLink.addClickListener(e -> UI.getCurrent().navigate("signin"));

        HorizontalLayout signInLayout = new HorizontalLayout(signInText, signInLink);
        signInLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        signInLayout.setWidthFull();
        signInLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add components to form card
        formCard.add(
                header,
                subheader,
                formLayout,
                new Div(),  // Spacer
                buttonsLayout,
                new Hr(),
                signInLayout
        );

        return formCard;
    }

    private void setupValidation() {
        // Name validation
        binder.forField(fullNameField)
                .withValidator(new StringLengthValidator(
                        "Please enter your name (at least 3 characters)",
                        3, null))
                .bind(UserRegistration::getFullName, UserRegistration::setFullName);

        // Email validation
        binder.forField(emailField)
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(UserRegistration::getEmail, UserRegistration::setEmail);

        // Password validation
        binder.forField(passwordField)
                .withValidator(new StringLengthValidator(
                        "Password must be at least 6 characters long",
                        6, null))
                .bind(UserRegistration::getPassword, UserRegistration::setPassword);

        // Password confirmation validation
        // Password confirmation validation
        binder.forField(confirmPasswordField)
                .withValidator((value, context) -> {
                    if (passwordField.getValue().equals(value)) {
                        return ValidationResult.ok();
                    }
                    return ValidationResult.error("Passwords do not match");
                })
                .bind(UserRegistration::getPasswordConfirmation, UserRegistration::setPasswordConfirmation);
    }

    private void submitForm() {
        if (binder.validate().hasErrors()) {
            showError("Please fix the errors in the form");
            return;
        }

        try {
            UserRegistration registration = new UserRegistration();
            binder.writeBean(registration);

            presenter.onSignUp(
                    registration.getFullName(),
                    registration.getEmail(),
                    registration.getPassword()
            );
        } catch (Exception e) {
            showError("Error during registration: " + e.getMessage());
        }
    }

    private void resetForm() {
        binder.readBean(new UserRegistration());
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    public void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public static class UserRegistration {
        private String fullName = "";
        private String email = "";
        private String password = "";
        private String passwordConfirmation = "";

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

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

        public String getPasswordConfirmation() {
            return passwordConfirmation;
        }

        public void setPasswordConfirmation(String passwordConfirmation) {
            this.passwordConfirmation = passwordConfirmation;
        }
    }
}