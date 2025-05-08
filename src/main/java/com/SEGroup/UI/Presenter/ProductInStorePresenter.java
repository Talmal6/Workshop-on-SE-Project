//package com.SEGroup.UI.Presenter;
//
//import com.SEGroup.DTO.ShoppingProductDTO;
//import com.SEGroup.Service.Result;
//import com.SEGroup.Service.StoreService;
//import com.SEGroup.UI.ServiceLocator;
//import com.SEGroup.UI.Views.ProductInStoreView;
//
//import java.util.List;
//
//public class ProductInStorePresenter {
//
//    private ProductInStoreView view;
//    private final StoreService storeService = ServiceLocator.getStoreService();
//
//    public ProductInStorePresenter(ProductInStoreView view,
//                                   String storeName,
//                                   String productId) {
//        this.view = view;
//        Result<List<ShoppingProductDTO>> res =
//                storeService.searchProducts(
//                        /* query */     "",
//                        /* filters */   List.of(),
//                        /* storeName */ storeName,
//                        /* categories */List.of()
//                );
//
//        if (res.isSuccess()) {
//            // find the one with matching ID
//            ShoppingProductDTO product = res.getData().stream()
//                    .filter(dto -> dto.getProductId().equals(productId)).findFirst().get();
//            showProduct(product);
//        }
//    }
//    public void showProduct(ShoppingProductDTO dto) {
//       this.view.title.setText(dto.getName());
//        image.setSrc(dto.getImageUrl());
//        image.setAlt(dto.getName());
//        price.setText("$" + dto.getPrice());
//        avgRating.setValue(dto.getAverageRating());
//    }
//
//}
