package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.RatingDto;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.Constants.BidRequest;
import com.SEGroup.UI.Constants.Review;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.ReviewView;

import java.util.List;

public class ReviewPresenter {

    private final ReviewView view;
    private final StoreService storeService;

    public ReviewPresenter(ReviewView view) {
        this.view = view;
        this.storeService = ServiceLocator.getStoreService();
    }

    public void loadReviews(String storeName) {
        String token = SecurityContextHolder.token();
        if (token == null || token.isEmpty()) {
            token = null;
        }

        Result<List<RatingDto>> result = storeService.getStoreRatings(token, storeName);
        if (result.isSuccess()) {
            List<Review> reviews = result.getData().stream()
                    .map(dto -> new Review(dto.getRaterEmail(), dto.getScore(), dto.getReview()))
                    .toList();
            view.displayReviews(reviews);
        } else {
            view.showError("Failed to load reviews: " + result.getErrorMessage());
        }
    }

}
