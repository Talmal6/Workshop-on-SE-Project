package com.SEGroup.DTO;

public class RatingDto {
    private final String raterEmail;
    private final int score;
    private final String review;

    public RatingDto(String raterEmail, int score, String review) {
        this.raterEmail = raterEmail;
        this.score = score;
        this.review = review;
    }

    public String getRaterEmail() {
        return raterEmail;
    }

    public int getScore() {
        return score;
    }

    public String getReview() {
        return review;
    }
}