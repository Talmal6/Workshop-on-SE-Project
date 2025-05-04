// ui/MainLayout.java
package com.SEGroup.UI;
import com.SEGroup.UI.Views.AllStoresView;
import com.SEGroup.UI.Views.CartView;
import com.SEGroup.UI.Views.CatalogView;
import com.SEGroup.UI.Views.SignInView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;


public class MainLayout extends AppLayout {

    public MainLayout() {

        H2 title = new H2("eCommerce System");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.8em")
                .set("text-shadow", "1px 1px 2px rgba(0,0,0,0.2)");

        TextField search = new TextField();
        search.setPlaceholder("Search");
        search.setWidth("200px");
        search.getStyle()
                .set("border-radius", "20px")
                .set("padding", "0.3em 1em")
                .set("background-color", "#fff");
        Button searchBtn = new Button(VaadinIcon.SEARCH.create());
        searchBtn.getStyle()
                .set("border-radius", "50%")
                .set("background-color", "#3f3f46")
                .set("color", "#fff")
                .set("min-width", "40px")
                .set("min-height", "40px");
        HorizontalLayout searchBar = new HorizontalLayout(search, searchBtn);
        searchBar.setSpacing(true);
        searchBar.setAlignItems(FlexComponent.Alignment.CENTER);
        Span guest = new Span("hello Guest");
        guest.getStyle()
                .set("color", "#1976d2")
                .set("margin-left", "1em");

        HorizontalLayout auth = new HorizontalLayout();
        auth.setSpacing(true);
        auth.setAlignItems(FlexComponent.Alignment.CENTER); // important
        auth.add(
                new Button("sign up", e -> UI.getCurrent().navigate("signup")),
                new Button("sign in", e -> UI.getCurrent().navigate("signin")),
                guest
        );


        HorizontalLayout navbar = new HorizontalLayout(title, searchBar, createSpacer(), auth);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setSpacing(true);

        Div navbarWrapper = new Div(navbar);
        navbarWrapper.setWidthFull();
        navbarWrapper.getStyle()
                .set("background-color", "#e0e0e0")
                .set("padding", "1em 2em")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)");

        addToNavbar(navbarWrapper);


        Tabs tabs = new Tabs(
                nav("Catalog", CatalogView.class, VaadinIcon.LIST),
                nav("My cart", CartView.class, VaadinIcon.CART),
                nav("All Stores", AllStoresView.class, VaadinIcon.STORAGE),
                nav("Notifications", SignInView.class, VaadinIcon.BELL)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);
        setPrimarySection(Section.DRAWER);
    }




    private void addToNavbar(H2 title, TextField search, Component spacer, Component component) {
        HorizontalLayout navbar = new HorizontalLayout(title, search, spacer, component);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setWidthFull();
        addToNavbar(navbar);
    }

    private Component createSpacer() {
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        return spacer;
    }

    private Component authButtons() {
        HorizontalLayout box = new HorizontalLayout();
        Button signIn  = new Button("sign in",  e -> UI.getCurrent().navigate("signin"));
        Button signUp  = new Button("sign up",  e -> UI.getCurrent().navigate("signup"));
        Button logout  = new Button("logout",   e -> {/* TODO: implement logout */});
        box.add(signUp, signIn /* , logout when user logged */);
        return box;
    }

    private Tab nav(String caption, Class<? extends Component> viewClass, VaadinIcon icon) {
        RouterLink link = new RouterLink("", viewClass);
        link.add(icon.create(), new Span(caption));
        link.getElement().getStyle()
                .set("align-items", "center")
                .set("gap", "0.5em")
                .set("display", "flex")
                .set("padding", "0.5em")
                .set("cursor", "pointer");
        return new Tab(link);
    }
}
