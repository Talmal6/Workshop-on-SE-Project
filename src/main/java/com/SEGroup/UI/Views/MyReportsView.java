package com.SEGroup.UI.Views;


import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.MyReportsPresenter;
import com.SEGroup.DTO.ReportDTO;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification.Position;

import java.util.List;

/**
 * View where a user can see all of their submitted reports.
 */
@Route(value = "report/history", layout = MainLayout.class)
@PageTitle("My Reports")
public class MyReportsView extends VerticalLayout implements BeforeEnterObserver {

    private final Grid<ReportDTO> grid = new Grid<>(ReportDTO.class, false);
    private final MyReportsPresenter presenter;

    public MyReportsView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("My Submitted Reports"));

        // Configure grid columns
        grid.addColumn(ReportDTO::getId)
                .setHeader("Report ID")
                .setAutoWidth(true);
        grid.addColumn(ReportDTO::getContent)
                .setHeader("Content")
                .setFlexGrow(1);
        grid.addColumn(ReportDTO::getStatus)
                .setHeader("Status")
                .setAutoWidth(true);

        add(grid);

        // Wire up presenter
        presenter = new MyReportsPresenter(this);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Load the reports for the current user
        presenter.loadUserReports();
    }

    /**
     * Called by the presenter to populate the grid.
     */
    public void setReports(List<ReportDTO> reports) {
        grid.setItems(reports);
    }

    /**
     * Called by the presenter on error.
     */
    public void showError(String message) {
        Notification.show(message, 3000, Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

