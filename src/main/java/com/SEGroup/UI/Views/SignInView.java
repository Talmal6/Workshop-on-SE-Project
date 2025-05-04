package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

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
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.lumo.LumoUtility;




@Route(value = "signin", layout = MainLayout.class)
public class SignInView extends VerticalLayout {

    public SignInView() {


        var form = new FormLayout();
        var email = new TextField("Email");
        var pass  = new PasswordField("Password");
        var btn   = new Button("Sign In", e -> UI.getCurrent().navigate("catalog"));
        form.add(email, pass, btn);
        add(new H3("Sign in"), form);

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
    }
}
