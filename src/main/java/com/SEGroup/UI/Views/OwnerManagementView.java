package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.OwnerManagementPresenter;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.List;

@Route(value = "store/:storeName/owners", layout = MainLayout.class)
@PageTitle("Owners")
public class OwnerManagementView extends VerticalLayout
        implements HasUrlParameter<String> {

    private String storeName;
    private final ComboBox<String> users = new ComboBox<>("Subscriber");
    private final Grid<String> grid = new Grid<>(String.class, false);
    private OwnerManagementPresenter presenter;

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        this.storeName = event.getRouteParameters().get("storeName").orElse("");
        this.presenter = new OwnerManagementPresenter(this, storeName);

        removeAll(); // Clear the view before rebuilding
        buildUi();
        presenter.loadOwners();
    }

    /* ───────────────────── UI helpers ───────────────────── */
    private void buildUi() {
        users.setItems(ServiceLocator.getUserService().allUsersEmails());
        users.setPlaceholder("Select a user");

        Button appoint = new Button("Appoint owner");
        appoint.addClickListener(e -> {
            if (users.getValue() == null) {
                showError("Please select a user first");
                return;
            }
            presenter.appointOwner(users.getValue());
        });

        Button remove = new Button("Remove owner");
        remove.addClickListener(e -> {
            if (users.getValue() == null) {
                showError("Please select a user first");
                return;
            }
            presenter.removeOwner(users.getValue());
        });

        grid.addColumn(s -> s).setHeader("Current owners");
        add(new HorizontalLayout(users, appoint, remove), grid);
    }

    /**
     * Displays the list of owners in the grid.
     * This method is called by the presenter.
     *
     * @param owners The list of owner emails to display
     */
    public void displayOwners(List<String> owners) {
        grid.setItems(owners);
    }

    /**
     * Shows a success notification to the user.
     *
     * @param message The success message to display
     */
    public void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_START);
    }

    /**
     * Shows an error notification to the user.
     *
     * @param message The error message to display
     */
    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.getElement().getStyle().set("background-color", "var(--lumo-error-color)");
    }
}