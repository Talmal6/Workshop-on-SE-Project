package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout {
    public HomeView() {
        add(new H2("Welcome to the marketplace!"));
    }
}
