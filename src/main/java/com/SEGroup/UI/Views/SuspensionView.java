package com.SEGroup.UI.Views;

import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "admin/suspensions", layout = MainLayout.class)
@PageTitle("User suspensions")


public class SuspensionView extends VerticalLayout {

    ComboBox<String> users = new ComboBox<>("User");
    NumberField days       = new NumberField("Days (0 = permanent)");
    Grid<Susp> grid        = new Grid<>(Susp.class,false);

    public SuspensionView(){
        users.setItems(ServiceLocator.getUserService().allUsersEmails());

        Button suspend   = new Button("Suspend",
                e -> call(ServiceLocator.getUserService()
                        .suspendUser(users.getValue(),
                                days.getValue().intValue())));
        Button unsuspend = new Button("Remove suspension",
                e -> call(ServiceLocator.getUserService()
                        .unsuspendUser(users.getValue())));
        Button refresh   = new Button("Refresh", e -> load());

        add(new HorizontalLayout(users, days, suspend, unsuspend, refresh),
                grid);
        load();
    }

    private void load() {
        var data = ServiceLocator.getUserService()
                .allSuspensions()
                .stream()
                .map(dto -> new Susp(dto.email(), dto.since(), dto.until()))
                .toList();
        grid.setItems(data);
    }
    private void call(Result<?> r){
        Notification.show(r.isSuccess()? "OK" : r.getErrorMessage());
        load();
    }

    public record Susp(String email, String since, String until){}
}
