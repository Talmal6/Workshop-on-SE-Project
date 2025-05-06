//package com.SEGroup.UI.Presenter;
//
//import com.SEGroup.Service.Result;
//import com.SEGroup.Service.StoreService;
//import com.SEGroup.UI.ServiceLocator;
//import com.SEGroup.UI.Views.ProductView;
//
//import java.util.function.Consumer;
//
//public class RatingProductPresenter {
//
//    private final StoreService storeService = ServiceLocator.getStoreService();
//    private final String       productId;
////    private final Consumer<Integer> onSuccess;
////    private final Consumer<String>  onError;
//
////    public RatingProductPresenter(ProductView ratingComponent,
////                                  String productId,
////                                  Consumer<Integer> onSuccess,
////                                  Consumer<String> onError) {
////        this.productId = productId;
////        this.onSuccess = onSuccess;
////        this.onError   = onError;
////        bind(ratingComponent);
////    }
//
//    public RatingProductPresenter(ProductView ratingComponent,
//                                  String productId) {
//        this.productId = productId;
//        bind(ratingComponent);
//    }
//    private void bind(ProductView r) {
//        r.addClickListener(evt -> {
//            int score = r.ratingView.getScore();
//            storeService.rateProduct("", , , score);
////            Result<Void> res = storeService.rateProduct(productId, score);
////            if (res.isSuccess()) {
////                onSuccess.accept(score);
////            } else {
////                onError.accept(res.getErrorMessage());
////            }
//        });
//    }
//}
