package com.SEGroup.DTO;

public class ReportDTO {
    private String id;
    private String reporter;
    private String content;
    private String status;

    public ReportDTO() {}
    public ReportDTO(String id, String reporter, String content, String status) {
        this.id = id;
        this.reporter = reporter;
        this.content = content;
        this.status = status;
    }
    public String getId() { return id; }
    public String getReporter() { return reporter; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
}
