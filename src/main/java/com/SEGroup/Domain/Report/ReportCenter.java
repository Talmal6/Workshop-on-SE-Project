
package com.SEGroup.Domain.Report;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.checkerframework.checker.units.qual.t;
import org.springframework.stereotype.Component;

@Component
public class ReportCenter {
    private final java.util.concurrent.ConcurrentHashMap<String, Report> reportIdToReport = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(
            0);

    public static int getNextId() {
        return idCounter.incrementAndGet();
    }

    public void makeSystemReport(String userId, String reportContent) {
        String reportId = String.valueOf(getNextId());
        Report report = new Report(reportId, userId, reportContent, ReportStatus.PENDING);
        reportIdToReport.put(reportId, report);
    }

    public void makeUserReport(String userId, String reportContent, String reportOnUserId) {
        String reportId = String.valueOf(getNextId());
        UserReport report = new UserReport(reportId, userId, reportContent, ReportStatus.PENDING, reportOnUserId);
        reportIdToReport.put(reportId, report);
    }

    public void handleReport(String reportId) {
        Report report = reportIdToReport.get(reportId);
        if (report != null) {
            report.setStatus(ReportStatus.DONE);
        } else {
            throw new IllegalArgumentException("Report not found.");
        }
    }

    public List<Report> getReportIdToReport() {
        if (reportIdToReport.isEmpty()) {
            throw new IllegalStateException("No reports available.");
        }
        List<Report> reportList = new CopyOnWriteArrayList<>();
        for (Report report : reportIdToReport.values()) {
            reportList.add(report);
        }

        return reportList;
    }

    public Report getReportById(String reportId) {
        return reportIdToReport.get(reportId);
    }
}
