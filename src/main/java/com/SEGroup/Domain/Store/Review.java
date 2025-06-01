package com.SEGroup.Domain.Store;

import jakarta.persistence.*;

@Entity
@Table(name = "Review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reviewer_name", nullable = false)
    private String reviewerName;

    @Column(name = "reviewer_id", nullable = false)
    private String reviewerId;

    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "rating")
    private int rating;

    @Column(name = "store_comment")
    private String storeComment;

    protected Review() {
        // required by JPA
    }

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