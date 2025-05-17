package com.SEGroup.UI.Presenter;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.UserService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.DTO.ReportDTO;
import com.SEGroup.UI.Views.AdminReportsView;
import com.SEGroup.UI.Views.MyReportsView;
import com.SEGroup.UI.Views.MyReportsView;
import com.SEGroup.Domain.Report.Report;

import java.util.List;
import java.util.stream.Collectors;

public class MyReportsPresenter {
    private final MyReportsView view;
    private final UserService userService;

    public MyReportsPresenter(MyReportsView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
    }

    public void loadUserReports() {
        String token = SecurityContextHolder.token();
        var res = userService.getReports(token);
        if (res.isSuccess()) {
            List<ReportDTO> dtos = res.getData().stream()
                    .map(r -> new ReportDTO(
                            r.getReportId(),
                            r.getUserId(),
                            r.getReportContent(),
                            r.getStatus().name()
                    ))
                    .collect(Collectors.toList());
            view.setReports(dtos);
        } else {
            view.showError(res.getErrorMessage());
        }
    }
}
