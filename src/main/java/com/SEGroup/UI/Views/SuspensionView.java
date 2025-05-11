package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.SuspensionPresenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "admin/suspensions", layout = MainLayout.class)
@PageTitle("User suspensions")
public class SuspensionView extends VerticalLayout {
    private final SuspensionPresenter presenter;
    private final ComboBox<String> users = new ComboBox<>("User");
    private final NumberField days = new NumberField("Days (0 = permanent)");
    private final Grid<SuspensionView.Susp> grid = new Grid<>(SuspensionView.Susp.class, false);

    public SuspensionView() {
        this.presenter = new SuspensionPresenter(this);

        // Initialize UI components
        users.setItems(presenter.getAllUserEmails());

        Button suspend = new Button("Suspend", e -> presenter.suspendUser(users.getValue(), days.getValue().intValue()));
        Button unsuspend = new Button("Remove suspension", e -> presenter.unsuspendUser(users.getValue()));
        Button refresh = new Button("Refresh", e -> presenter.loadSuspensions());

        // Layout
        add(new HorizontalLayout(users, days, suspend, unsuspend, refresh), grid);

        // Initial load
        presenter.loadSuspensions();
    }

    public void showSuspensions(List<Susp> suspensions) {
        grid.setItems(suspensions);
    }

    public void showMessage(String message) {
        Notification.show(message);
    }

    public record Susp(String email, String since, String until) {}
}