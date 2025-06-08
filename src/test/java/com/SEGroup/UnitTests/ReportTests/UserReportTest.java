package com.SEGroup.UnitTests.ReportTests;

import com.SEGroup.Domain.Report.UserReport;
import com.SEGroup.Domain.Report.ReportStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserReportTest {

    @Test
    public void testUserReportGettersAndSetters() {
        UserReport report = new UserReport("r1", "u1", "content", ReportStatus.PENDING, "reportedUser");

        assertEquals("reportedUser", report.getReportOnUserId());

        report.setReportOnUserId("newReportedUser");
        assertEquals("newReportedUser", report.getReportOnUserId());
    }
}
