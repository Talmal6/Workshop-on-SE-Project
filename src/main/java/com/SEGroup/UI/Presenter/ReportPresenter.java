//package com.SEGroup.UI.Presenter;
//import com.SEGroup.Service.Result;
//import com.SEGroup.Service.UserService;
//import com.SEGroup.UI.SecurityContextHolder;
//import com.SEGroup.UI.ServiceLocator;
//import com.SEGroup.UI.Views.ReportView;
//import com.SEGroup.DTO.ReportDTO;
//import com.SEGroup.Domain.Report.Report;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Presenter for submitting reports from ReportView.
// */
//public class ReportPresenter {
//    private final ReportView view;
//    public final UserService userService;
//
//    public ReportPresenter(ReportView view) {
//        this.view = view;
//        this.userService = ServiceLocator.getUserService();
//    }

//    public void submit(String type, String content, String targetEmail) {
//        String token = SecurityContextHolder.token();
//        Result<Void> res;
//        if ("User".equals(type)) {
//            res = userService.makeUserReport(token, content, targetEmail);
//        } else {
//            res = userService.makeSystemReport(token, content);
//        }
//    }
//
//}