package com.SEGroup.Domain.Store;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Embeddable
public class Rating {
    public int score;
    public String review ;
    public Rating() {}
    Rating (int s , String r ){score =s;review =r; }
    public int getScore(){
        return score;
    }
    public String getReview(){
        return review;
    }
    public void setScore(int score) {
        this.score = score;
    }

    public void setReview(String review) {
        this.review = review;
    }

}
