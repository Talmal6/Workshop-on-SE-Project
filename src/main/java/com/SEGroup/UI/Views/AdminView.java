package com.SEGroup.UI.Views;

import com.SEGroup.UI.Presenter.AdminPresenter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AdminView extends VerticalLayout {
    Button suspendUser;
    private AdminPresenter presenter;
    public AdminView(){
        this.presenter = new AdminPresenter(this);
        suspendUser = new Button("suspend user");
        add(suspendUser);
        suspendUser.addClickListener((click)->{
            UI.getCurrent().navigate("admin/suspensions");
        });
    }

}
