package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.SuspensionPresenter;
import com.vaadin.flow.component.Component;
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
public class SuspensionView extends VerticalLayout implements AdminSection {
    private final SuspensionPresenter presenter;
    private final ComboBox<String> users = new ComboBox<>("User");
    private final NumberField days = new NumberField("Days (0 = permanent)");
    private final Grid<SuspensionView.Susp> grid = new Grid<>(SuspensionView.Susp.class, false);
    private final VerticalLayout content;

    public SuspensionView() {
        this.presenter = new SuspensionPresenter(this);
        this.content = new VerticalLayout();

        // Initialize UI components
        users.setItems(presenter.getAllUserEmails());

        Button suspend = new Button("Suspend", e -> {
            presenter.suspendUser(users.getValue(), days.getValue().intValue());
            presenter.loadSuspensions(); // Refresh the list after suspension
        });
        Button refresh = new Button("Refresh", e -> presenter.loadSuspensions());

        // Configure grid
        grid.addColumn(Susp::email).setHeader("User Email");
        grid.addColumn(Susp::since).setHeader("Suspended Since");
        grid.addColumn(Susp::until).setHeader("Suspended Until");
        grid.addComponentColumn(susp -> {
            Button unsuspendBtn = new Button("Unsuspend", e -> {
                presenter.unsuspendUser(susp.email());
                presenter.loadSuspensions(); // Refresh the list after unsuspension
            });
            return unsuspendBtn;
        }).setHeader("Actions");

        // Layout
        content.add(new HorizontalLayout(users, days, suspend, refresh), grid);
        add(content);
    }

    @Override
    public String getTitle() {
        return "User Suspensions";
    }

    @Override
    public Component getContent() {
        return content;
    }

    @Override
    public void onActivate() {
        presenter.loadSuspensions();
    }

    @Override
    public void onDeactivate() {
        // Clean up if needed
    }

    public void showSuspensions(List<Susp> suspensions) {
        grid.setItems(suspensions);
    }

    public void showMessage(String message) {
        Notification.show(message);
    }

    public record Susp(String email, String since, String until) {}
}