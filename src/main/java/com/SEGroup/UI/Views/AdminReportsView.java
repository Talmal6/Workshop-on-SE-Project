package com.SEGroup.UI.Views;

import com.SEGroup.Service.UserService;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.DTO.ReportDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Route(value = "admin/reports", layout = MainLayout.class)
@PageTitle("Manage Reports")
public class AdminReportsView extends VerticalLayout implements BeforeEnterObserver {
    private final UserService userService = ServiceLocator.getUserService();
    private final Grid<ReportDTO> grid = new Grid<>(ReportDTO.class, false);

    public AdminReportsView() {
        setWidthFull(); setPadding(true); setSpacing(true);
        grid.addColumn(ReportDTO::getId).setHeader("Report ID").setAutoWidth(true);
        grid.addColumn(ReportDTO::getReporter).setHeader("Reporter").setAutoWidth(true);
        grid.addColumn(ReportDTO::getContent).setHeader("Content").setFlexGrow(1);
        grid.addColumn(ReportDTO::getStatus).setHeader("Status").setAutoWidth(true);
        grid.addComponentColumn(r -> {
            Button handle = new Button("Handle");
            handle.setEnabled("PENDING".equalsIgnoreCase(r.getStatus()));
            handle.addClickListener(e -> handleReport(r.getId()));
            return handle;
        }).setHeader("Action");
        add(new H2("All Reports"), grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadAll();
    }

    private void loadAll() {
        var res = userService.getReports(SecurityContextHolder.token());
        if (res.isSuccess()) {
            List<ReportDTO> items = res.getData().stream()
                    .map(r -> new ReportDTO(
                            r.getReportId(),
                            r.getUserId(),
                            r.getReportContent(),
                            r.getStatus().name()
                    ))
                    .collect(Collectors.toList());
            grid.setItems(items);
        }

    }

    private void handleReport(String id) {
        var res = userService.handleReport(SecurityContextHolder.token(), id);
        if (res.isSuccess()) {
            Notification.show("Report " + id + " handled.", 2000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadAll();
        } else {
            Notification.show(res.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}