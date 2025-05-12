//package com.SEGroup.UI.Presenter;
//
//import com.SEGroup.Domain.Store.Store;
//import com.SEGroup.Infrastructure.Repositories.StoreRepository;
//import com.SEGroup.UI.Views.ReviewView;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class ReviewPresenter {
//
//    private final ReviewView reviewView;
//    private final StoreRepository storeRepository;
//
//    @Autowired
//    public ReviewPresenter(ReviewView reviewView, StoreRepository storeRepository) {
//        this.reviewView = reviewView;
//        this.storeRepository = storeRepository;
//    }
//
//    // אתחול של הצגת החנות וטעינת חוות הדעת שלה
//    public void initialize(String storeName) {
//        reviewView.setCurrentStore(storeName);  // הצגת שם החנות
//        loadReviews(storeName);  // טעינת חוות הדעת של החנות
//    }
//
//    // טוען את חוות הדעת על החנות
//    private void loadReviews(String storeName) {
//        Map<String, Store.Rating> ratings = storeRepository.findRatingsByStore(storeName); // מביא את הדירוגים
//
//        List<ReviewView.StoreRatingDisplay> displayList = ratings.entrySet().stream()
//                .map(entry -> new ReviewView.StoreRatingDisplay(entry.getKey(), entry.getValue().getScore(), entry.getValue().getReview()))
//                .collect(Collectors.toList()); // יוצר את הרשימה שתוצג
//
//        reviewView.setReviews(displayList); // שולח את הרשימה לצפייה
//    }
//}
