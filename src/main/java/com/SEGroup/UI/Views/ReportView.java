package com.SEGroup.UI.Views;

import com.SEGroup.Service.Result;
import com.SEGroup.Service.UserService;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.DTO.ReportDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * View for users to submit system or user reports.
 */
@Route(value = "report", layout = MainLayout.class)
@PageTitle("Submit Report")
public class ReportView extends VerticalLayout {
    private final ComboBox<String> typeCombo = new ComboBox<>();
    private final TextField targetUser = new TextField("Report On (user email)");
    private final TextArea contentArea = new TextArea("Report Content");
    private final Button submitBtn = new Button("Submit Report");
    private final UserService userService = ServiceLocator.getUserService();

    public ReportView() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Submit a Report"));

//        typeCombo.setLabel("Type");
//        typeCombo.setItems("System", "User");
//        typeCombo.setValue("System");
//        typeCombo.addValueChangeListener(e -> {
//            targetUser.setVisible("User".equals(e.getValue()));
//        });
//        targetUser.setVisible(false);


        contentArea.setWidthFull();
        contentArea.setHeight("120px");

        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClickListener(e -> submitReport());

//        add(typeCombo, targetUser, contentArea, submitBtn);
        add(contentArea, submitBtn);

    }



    /**
     * Simple DTO to display reports in admin view.
     */
    private void submitReport() {
//        String type = typeCombo.getValue();
        String content = contentArea.getValue().trim();
        String token = SecurityContextHolder.token();
        String self = SecurityContextHolder.email();
        if (content.isEmpty()) {
            Notification.show("Content cannot be empty", 2500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        Result<Void> res;
//        if ("User".equals(type)) {
//            String onUser = targetUser.getValue().trim();
//            if (onUser.isEmpty()) {
//                Notification.show("Please enter a user email to report", 2500, Notification.Position.MIDDLE)
//                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
//                return;
//            }
//            res = userService.makeUserReport(token, content, onUser);
//        } else {
            res = userService.makeSystemReport(token, content);
//        }
        if (res.isSuccess()) {
            Notification.show("Report submitted!", 2500, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            contentArea.clear();
            targetUser.clear();
        } else {
            Notification.show(res.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

}
