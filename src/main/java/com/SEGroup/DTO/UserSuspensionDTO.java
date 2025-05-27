package com.SEGroup.DTO;

import java.util.Date;
import jakarta.persistence.Embeddable;

@Embeddable // Added @Embeddable annotation
public class UserSuspensionDTO {
    /**
     * Data Transfer Object for user suspension details.
     * Contains user email, suspension start and end times, and reason for
     * suspension.
     */

    private String userEmail; // This might be redundant if User entity already has email as ID
    private Date startTime;
    private Date endTime;
    private String reason; // Added reason field

    public UserSuspensionDTO() { // Added no-arg constructor for JPA
    }

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