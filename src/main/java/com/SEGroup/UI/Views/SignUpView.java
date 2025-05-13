package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.SignUpPresenter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "signup", layout = MainLayout.class)
@PageTitle("Sign up")
public class SignUpView extends FlexLayout {

    private final TextField userName = new TextField("User name");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm password");
    private final Button signUp = new Button("Create account", VaadinIcon.USER_CHECK.create());

    private final Binder<UserDTO> binder = new Binder<>(UserDTO.class);
    private SignUpPresenter presenter;

    public SignUpView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        presenter = new SignUpPresenter(this);
        add(buildForm());
        configureBinding();
    }

    private Component buildForm() {
        FormLayout form = new FormLayout();
        form.add(userName, email, password, confirmPassword, signUp);

        form.setWidth("400px");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1));
//move signup button slightly down
        signUp.getElement().getStyle().set("margin-top", "var(--lumo-space-xl)");
        signUp.setWidthFull();
        signUp.addClickListener(e -> submit());

        return new com.vaadin.flow.component.orderedlayout.VerticalLayout(
                new H2("Sign up"),
                form
        );
    }

    private void configureBinding() {
        email.setClearButtonVisible(true);
        email.setErrorMessage("Please enter a valid e‑mail");
        //make sure username is not empty
        binder.forField(userName)
                .asRequired("User name is required")
                .withValidator(userName -> userName.length() >= 3,
                        "User name must be at least 3 characters long")
                .bind(UserDTO::getUserName, UserDTO::setUserName);
        binder.forField(email)
                .withValidator(new EmailValidator("Invalid e‑mail address"))
                .asRequired("E‑mail is required")
                .bind(UserDTO::getEmail, UserDTO::setEmail);

        binder.forField(password)
                .asRequired("Password is required")
                .bind(UserDTO::getPassword, UserDTO::setPassword);

        binder.forField(confirmPassword)
                .asRequired("Please re‑type the password")
                .withValidator(pwd -> pwd.equals(password.getValue()),
                        "Passwords do not match")
                .bind(UserDTO::getPassword, (u, pwd) -> {/*ignored on read*/});
    }

    private void submit() {
        try {
            UserDTO user = new UserDTO();
            binder.writeBean(user);
            presenter.onSignUp(user.getUserName(), user.getEmail(), user.getPassword());
            UI.getCurrent().navigate("login");
        } catch (ValidationException ex) {
            Notification.show("Please fix the errors before continuing: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Simple DTO used for Binder binding.
     */
    public static class UserDTO {
        private String userName;
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

        public String getUserName() {
            return userName;
        }
        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
    public void showSuccess(String sessionKey) {
        Notification.show("Signed up successfully! Session: " + sessionKey, 4000, Notification.Position.MIDDLE);
        UI.getCurrent().navigate("signin");
    }

    public void showError(String message) {
        Notification.show("Sign up failed: " + message, 4000, Notification.Position.MIDDLE);
    }

}
