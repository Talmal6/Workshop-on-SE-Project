package com.SEGroup.Domain.Report;

public class Report {
    private String reportId;
    private String userId;
    private String reportContent;
    private ReportStatus status;

    public Report(String reportId, String userId, String reportContent, ReportStatus status) {
        this.reportId = reportId;
        this.userId = userId;
        this.reportContent = reportContent;
        this.status = status;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
    
}
