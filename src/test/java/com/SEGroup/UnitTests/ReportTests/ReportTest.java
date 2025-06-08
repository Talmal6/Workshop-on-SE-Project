package com.SEGroup.UnitTests.ReportTests;

import com.SEGroup.Domain.Report.Report;
import com.SEGroup.Domain.Report.ReportStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReportTest {

    @Test
    public void testReportGettersAndSetters() {
        Report report = new Report("r1", "u1", "content", ReportStatus.PENDING);

        assertEquals("r1", report.getReportId());
        assertEquals("u1", report.getUserId());
        assertEquals("content", report.getReportContent());
        assertEquals(ReportStatus.PENDING, report.getStatus());

        report.setReportId("r2");
        report.setUserId("u2");
        report.setReportContent("new content");
        report.setStatus(ReportStatus.DONE);

        assertEquals("r2", report.getReportId());
        assertEquals("u2", report.getUserId());
        assertEquals("new content", report.getReportContent());
        assertEquals(ReportStatus.DONE, report.getStatus());
    }
}
