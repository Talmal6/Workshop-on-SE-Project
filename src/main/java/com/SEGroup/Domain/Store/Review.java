package com.SEGroup.Domain.Store;

public class Review {
    private String reviewerName;
    private String reviewerId;
    private String reviewText;
    private int rating;
    private String storeComment;

    public Review(String reviewerId, String reviewerName, String reviewText, int rating) {
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewText() {
        return reviewText;
    }
    public String getReviewerId() {
        return reviewerId;
    }

    public int getRating() {
        return rating;
    }

    public String getStoreComment() {
        return storeComment;
    }

    public void setStoreComment(String storeComment) {
        this.storeComment = storeComment;
    }
}