package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin Dashboard")
public class AdminView extends VerticalLayout {
    private final Tabs tabs;
    private final VerticalLayout content;
    private AdminSection currentSection;

    public AdminView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Create tabs for different admin functionalities
        tabs = new Tabs();
        tabs.setWidthFull();

        // Create content area
        content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        // Create admin sections
        SuspensionView suspensionsSection = new SuspensionView();
        // TODO: Create other sections
        // UserManagementView usersSection = new UserManagementView();
        // StoreManagementView storesSection = new StoreManagementView();
         AdminReportsView reportsSection = new AdminReportsView();

        // Add tabs for different admin sections
        Tab suspensionsTab = new Tab(suspensionsSection.getTitle());
        Tab usersTab = new Tab("User Management");
        Tab storesTab = new Tab("Store Management");
        Tab reportsTab = new Tab("Reports");

        tabs.add(suspensionsTab, usersTab, storesTab, reportsTab);

        // Add tab change listener
        tabs.addSelectedChangeListener(event -> {
            // Deactivate current section if exists
            if (currentSection != null) {
                currentSection.onDeactivate();
            }

            Tab selectedTab = event.getSelectedTab();
            content.removeAll();

            if (selectedTab == suspensionsTab) {
                currentSection = suspensionsSection;
            } else if (selectedTab == usersTab) {
                // TODO: Add UserManagementView
                currentSection = null;
                content.add(new VerticalLayout()); // Placeholder
            } else if (selectedTab == storesTab) {
                // TODO: Add StoreManagementView
                currentSection = null;
                content.add(new VerticalLayout()); // Placeholder
            } else if (selectedTab == reportsTab) {

                content.add(new VerticalLayout(reportsSection)); // Placeholder
            }

            // Activate new section if exists
            if (currentSection != null) {
                content.add(currentSection.getContent());
                currentSection.onActivate();
            }
        });

        // Add components to layout
        add(tabs, content);
        setFlexGrow(1, content);

        // Select first tab by default
        tabs.setSelectedTab(reportsTab);
    }
}
