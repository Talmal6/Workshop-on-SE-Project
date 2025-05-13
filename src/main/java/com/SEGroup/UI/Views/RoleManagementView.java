// Update your RoleManagementView.java to use strings instead of enums
package com.SEGroup.UI.Views;

import com.SEGroup.UI.Constants.StorePermission;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.RoleManagementPresenter;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "store/:storeName/roles", layout = MainLayout.class)
@PageTitle("Roles & Permissions")
public class RoleManagementView extends VerticalLayout
        implements HasUrlParameter<String> {

    private String storeName;
    private final ComboBox<String> userSelector = new ComboBox<>("User");
    private final CheckboxGroup<String> permissions = new CheckboxGroup<>("Permissions");
    private final Grid<ManagerDetails> grid = new Grid<>(ManagerDetails.class, false);
    private RoleManagementPresenter presenter;

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        this.storeName = event.getRouteParameters().get("storeName").orElse("");
        this.presenter = new RoleManagementPresenter(this, storeName);

        removeAll(); // Clear the view before rebuilding
        buildUi();
        presenter.loadManagers();
    }

    private void buildUi() {
        H3 title = new H3("Manage Store Roles & Permissions: " + storeName);
        add(title);

        userSelector.setItems(ServiceLocator.getUserService().allUsersEmails());
        userSelector.setPlaceholder("Select a user");

        // Set up the permission checkbox group with string values
        permissions.setItems(StorePermission.ALL_PERMISSIONS);
        permissions.setItemLabelGenerator(permission -> permission.replace("_", " "));

        Button appointButton = new Button("Appoint Manager", e -> {
            if (userSelector.getValue() == null) {
                showError("Please select a user first");
                return;
            }

            List<String> selectedPermissions = new ArrayList<>(permissions.getSelectedItems());
            if (selectedPermissions.isEmpty()) {
                showError("Please select at least one permission");
                return;
            }

            presenter.appointManager(userSelector.getValue(), selectedPermissions);
        });

        Button updateButton = new Button("Update Permissions", e -> {
            if (userSelector.getValue() == null) {
                showError("Please select a manager first");
                return;
            }
            List<String> selectedPermissions = new ArrayList<>(permissions.getSelectedItems());
            presenter.updateManagerPermissions(userSelector.getValue(), selectedPermissions);
        });

        Button removeButton = new Button("Remove Manager", e -> {
            if (userSelector.getValue() == null) {
                showError("Please select a manager first");
                return;
            }
            presenter.removeManager(userSelector.getValue());
        });

        HorizontalLayout actions = new HorizontalLayout(userSelector, permissions, appointButton, updateButton, removeButton);
        actions.setPadding(true);
        actions.setAlignItems(Alignment.BASELINE);

        // Configure the grid
        grid.addColumn(ManagerDetails::email).setHeader("Manager Email").setAutoWidth(true);
        grid.addColumn(ManagerDetails::permissionsString).setHeader("Permissions").setAutoWidth(true);

        grid.addSelectionListener(selection -> {
            if (selection.getFirstSelectedItem().isPresent()) {
                ManagerDetails selected = selection.getFirstSelectedItem().get();
                userSelector.setValue(selected.email());
                permissions.clear();
                selected.permissions().forEach(permission ->
                        permissions.select(permission)
                );
            }
        });

        add(actions, grid);
        setSizeFull();
    }

    /**
     * Displays the list of managers in the grid.
     */
    public void displayManagers(List<ManagerDetails> managers) {
        grid.setItems(managers);
    }

    /**
     * Shows a success notification to the user.
     */
    public void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows an error notification to the user.
     */
    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // Helper class to represent manager details in the grid
    public record ManagerDetails(String email, List<String> permissions) {
        public String permissionsString() {
            return String.join(", ", permissions.stream()
                    .map(p -> p.replace("_", " "))
                    .collect(Collectors.toList()));
        }
    }
}