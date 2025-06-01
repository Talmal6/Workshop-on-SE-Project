package com.SEGroup.UI.Views;

import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.ProfilePresenter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.*;

@Route(value = "profile/:email", layout = MainLayout.class)
@PageTitle("User Profile")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    /* ──────────────────────────── fields ─────────────────────────── */

    private final ProfilePresenter presenter;
    private final TextField  fullNameField = new TextField("Full Name");
    private final EmailField emailField    = new EmailField("Email");
    private final TextField  addressField  = new TextField("Address");
    private final TextField  cityField     = new TextField("City");
    private final TextField  countryField  = new TextField("Country");
    private final TextField  zipField      = new TextField("ZIP Code");
    private final Binder<UserProfile> binder = new Binder<>();
    private String userEmail;

    /* ───────────────────────── constructor ───────────────────────── */

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // This method is called when the view is navigated to
        // You can use it to load user data if needed
        String email = event.getRouteParameters().get("email").orElse("");
        if (!email.isEmpty()) {
            this.userEmail = email;
            presenter.loadUserProfile(email);
        }
    }
    public ProfileView() {
        this.presenter = new ProfilePresenter(this);

        addClassName("profile-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(createProfileForm());
    }

    /* ────────────────── building the form card ───────────────────── */

    private Component createProfileForm() {
        /* card container */
        Div formCard = new Div();
        formCard.addClassName("profile-card");
        formCard.getStyle()
                .set("background-color", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)")
                .set("padding", "40px")
                .set("max-width", "600px")
                .set("width", "100%");

        /* picture + header */
        Image profilePicture = new Image("https://via.placeholder.com/150", "Profile Picture");
        profilePicture.setWidth("150px");
        profilePicture.setHeight("150px");
        profilePicture.getStyle()
                .set("border-radius", "50%")
                .set("margin-bottom", "20px");

        H2 header = new H2("User Profile");
        header.getStyle().set("margin-top", "0");

        /* set up each field */
        configureReadOnlyField(fullNameField,  VaadinIcon.USER);
        configureReadOnlyField(addressField,   VaadinIcon.HOME);
        configureReadOnlyField(cityField,      VaadinIcon.MAP_MARKER);
        configureReadOnlyField(countryField,   VaadinIcon.GLOBE);
        configureReadOnlyField(zipField,       VaadinIcon.MAP_MARKER);

        /* email is read-only for good, no edit button */
        emailField.setPlaceholder("Your email address");
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setWidthFull();
        emailField.setReadOnly(true);

        /* validation rules */
        setupValidation();

        /* save button */
        Button saveButton = new Button("Save Changes", new Icon(VaadinIcon.FILE_ADD));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();
        saveButton.addClickListener(e -> saveChanges());

        /* assemble card */
        VerticalLayout formLayout = new VerticalLayout(
                profilePicture,
                header,
                fullNameField,
                emailField,
                new H3("Address Information"),
                addressField,
                cityField,
                countryField,
                zipField,
                saveButton
        );
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        formCard.add(formLayout);
        return formCard;
    }

    /* ───── helper: create read-only field with in-field edit button ── */

    private void configureReadOnlyField(TextField tf, VaadinIcon prefixIcon) {
        tf.setWidthFull();
        tf.setReadOnly(true);
//        tf.setPlaceholder("type to edit");
        tf.setPrefixComponent(prefixIcon.create());

        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        edit.addClickListener(ev -> {
            tf.setReadOnly(!tf.isReadOnly());
            edit.setIcon(new Icon(tf.isReadOnly() ? VaadinIcon.EDIT : VaadinIcon.CHECK));
        });

        tf.setSuffixComponent(edit); // keeps icon aligned automatically
    }

    /* ────────────────────── binder validation ────────────────────── */

    private void setupValidation() {
        binder.forField(fullNameField)
                .withValidator(new StringLengthValidator(
                        "Please enter your name (at least 3 characters)",
                        3, null))
                .bind(UserProfile::getFullName, UserProfile::setFullName);

        binder.forField(emailField)
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(UserProfile::getEmail, UserProfile::setEmail);

        binder.forField(addressField).bind(UserProfile::getAddress, UserProfile::setAddress);
        binder.forField(cityField).bind(UserProfile::getCity, UserProfile::setCity);
        binder.forField(countryField).bind(UserProfile::getCountry, UserProfile::setCountry);
        binder.forField(zipField).bind(UserProfile::getZip, UserProfile::setZip);
    }

    /* ─────────────────── save / feedback helpers ─────────────────── */

    private void saveChanges() {
        if (binder.validate().hasErrors()) {
            showError("Please fix the errors in the form");
            return;
        }
        try {
            UserProfile profile = new UserProfile();
            binder.writeBean(profile);

            AddressDTO address = new AddressDTO(
                    profile.getAddress(),
                    profile.getCity(),
                    profile.getCountry(),
                    profile.getZip()
            );
            presenter.updateProfile(profile.getFullName(), address);
            //make all fields read-only again
            for (TextField tf : new TextField[]{fullNameField, addressField, cityField, countryField, zipField}) {
                tf.setReadOnly(true); // make all fields read-only before updating
                Button editButton = (Button) tf.getSuffixComponent();
                editButton.setIcon(new Icon(VaadinIcon.EDIT)); // reset icon to edit
            }
        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }

    public void showSuccess(String msg) {
        Notification notification = Notification.show(msg, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showError(String msg) {
        Notification notification = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /* ─────────────────── presenter callbacks etc. ────────────────── */

    public void setParameter(BeforeEvent e, String param) {
        if (param != null && !param.isEmpty()) {
            this.userEmail = param;
            presenter.loadUserProfile(param);
        }
    }

    public void setUserData(String fullName, String email, AddressDTO address) {
        UserProfile profile = new UserProfile();
        profile.setFullName(fullName);
        profile.setEmail(email);
        if (address != null) {
            profile.setAddress(address.getAddress());
            profile.setCity(address.getCity());
            profile.setCountry(address.getCountry());
            profile.setZip(address.getZip());
        }
        binder.readBean(profile);
    }

    public String getUserEmail() {
        return userEmail;
    }

    /* ──────────────────── simple DTO for binder ──────────────────── */

    public static class UserProfile {
        private String fullName = "";
        private String email    = "";
        private String address  = "";
        private String city     = "";
        private String country  = "";
        private String zip      = "";

        /* getters & setters */
        public String getFullName()            { return fullName; }
        public void   setFullName(String n)    { fullName = n; }
        public String getEmail()               { return email; }
        public void   setEmail(String e)       { email = e; }
        public String getAddress()             { return address; }
        public void   setAddress(String a)     { address = a; }
        public String getCity()                { return city; }
        public void   setCity(String c)        { city = c; }
        public String getCountry()             { return country; }
        public void   setCountry(String c)     { country = c; }
        public String getZip()                 { return zip; }
        public void   setZip(String z)         { zip = z; }
    }
}
