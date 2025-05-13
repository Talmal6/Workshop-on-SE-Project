package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "access-denied", layout = MainLayout.class)
@PageTitle("Access Denied")
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Add an icon or image
        Image lockIcon = new Image("/images/lock_icon.png", "Access Denied");
        lockIcon.setWidth("100px");
        lockIcon.setHeight("100px");

        // Provide a clear title
        H2 title = new H2("Access Denied");
        title.getStyle().set("color", "var(--lumo-error-color)");

        // Explanation
        Paragraph explanation = new Paragraph(
                "You don't have permission to access this area. This feature is only available to store owners and administrators."
        );

        // Action buttons
        Button homeButton = new Button("Back to Home", VaadinIcon.HOME.create());
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClickListener(e -> UI.getCurrent().navigate(""));

        Button marketplaceButton = new Button("Go to Marketplace", VaadinIcon.SHOP.create());
        marketplaceButton.addClickListener(e -> UI.getCurrent().navigate("catalog"));

        // Button to sign in if not logged in
        Button signInButton = new Button("Sign In", VaadinIcon.SIGN_IN.create());
        signInButton.addClickListener(e -> UI.getCurrent().navigate("signin"));

        // Add components to the layout
        add(lockIcon, title, explanation, homeButton, marketplaceButton, signInButton);
    }
}