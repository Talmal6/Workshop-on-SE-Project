package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "signup", layout = MainLayout.class)
@PageTitle("Sign up")
public class SignUpView extends Div {

    public SignUpView() {
        setText("This is the sign-up page");
    }
}
