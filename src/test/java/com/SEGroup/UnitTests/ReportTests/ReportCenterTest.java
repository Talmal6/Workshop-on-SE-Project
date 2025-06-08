package com.SEGroup.UnitTests.ReportTests;

import com.SEGroup.Domain.Report.Report;
import com.SEGroup.Domain.Report.ReportCenter;
import com.SEGroup.Domain.Report.ReportStatus;
import com.SEGroup.Domain.Report.UserReport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportCenterTest {

    @Test
    public void testMakeSystemReport() {
        ReportCenter center = new ReportCenter();
        center.makeSystemReport("u1", "content");

        List<Report> reports = center.getReportIdToReport();
        assertEquals(1, reports.size());
        assertTrue(reports.get(0) instanceof Report);
    }

    @Test
    public void testMakeUserReport() {
        ReportCenter center = new ReportCenter();
        center.makeUserReport("u1", "content", "targetUser");

        List<Report> reports = center.getReportIdToReport();
        assertEquals(1, reports.size());
        assertTrue(reports.get(0) instanceof UserReport);
        assertEquals("targetUser", ((UserReport) reports.get(0)).getReportOnUserId());
    }

    @Test
    public void testHandleReportSuccess() {
        ReportCenter center = new ReportCenter();
        center.makeSystemReport("u1", "content");
        String reportId = center.getReportIdToReport().get(0).getReportId();

        center.handleReport(reportId);
        assertEquals(ReportStatus.DONE, center.getReportById(reportId).getStatus());
    }

    @Test
    public void testHandleReportNotFound() {
        ReportCenter center = new ReportCenter();
        assertThrows(IllegalArgumentException.class, () -> center.handleReport("nonexistent"));
    }

    @Test
    public void testGetReportById() {
        ReportCenter center = new ReportCenter();
        center.makeSystemReport("u1", "content");
        Report report = center.getReportIdToReport().get(0);

        assertEquals(report, center.getReportById(report.getReportId()));
    }

    @Test
    public void testGetReportIdToReportEmptyThrows() {
        ReportCenter center = new ReportCenter();
        assertThrows(IllegalStateException.class, center::getReportIdToReport);
    }
}
