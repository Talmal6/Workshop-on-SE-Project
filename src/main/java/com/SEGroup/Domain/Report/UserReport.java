package com.SEGroup.Domain.Report;

public class UserReport extends Report {
    private String reportOnUserId;

    public UserReport(String reportId, String userId, String reportContent, ReportStatus status, String reportOnUserId) {
        super(reportId, userId, reportContent, status);
        this.reportOnUserId = reportOnUserId;
    }

    public String getReportOnUserId() {
        return reportOnUserId;
    }

    public void setReportOnUserId(String reportOnUserId) {
        this.reportOnUserId = reportOnUserId;
    }
}
