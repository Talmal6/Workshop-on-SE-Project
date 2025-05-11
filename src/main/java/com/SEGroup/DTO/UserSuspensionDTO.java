package com.SEGroup.DTO;
import java.util.Date;

public class UserSuspensionDTO {
    private String userEmail;
    private Date startTime;
    private Date endTime;
    private String reason; // Added reason field

    public UserSuspensionDTO(String userEmail, Date startTime, Date endTime, String reason) {
        this.userEmail = userEmail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason; // Initialize reason
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getReason() { // Getter for reason
        return reason;
    }

    public void setReason(String reason) { // Setter for reason
        this.reason = reason;
    }

    public boolean hasPassedSuspension() {
        return new Date().after(endTime);
    }
}