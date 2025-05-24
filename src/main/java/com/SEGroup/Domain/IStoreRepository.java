package com.SEGroup.Domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.SEGroup.DTO.*;
import com.SEGroup.Domain.Store.Bid;
import com.SEGroup.Domain.Store.Review;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.InMemoryProductCatalog;
import com.SEGroup.UI.Views.AddProductDialog;

/**
 * Interface representing a repository for managing stores.
 * It provides methods to create, retrieve, update, and delete stores and their
 * products.
 */
public interface IStoreRepository {

        /**
         * Retrieves all the stores in the repository.
         *
         * @return A list of all stores.
         */
        List<StoreDTO> getAllStores();

        /**
         * Retrieves a store by its name.
         *
         * @param storeName The name of the store.
         * @return The store details.
         */
        StoreDTO getStore(String storeName);

        /**
         * Creates a new store with the specified name and founder's email.
         *
         * @param storeName    The name of the store.
         * @param founderEmail The email address of the founder.
         */
        void createStore(String storeName, String founderEmail);

        /**
         * Closes a store by its name and the founder's email.
         *
         * @param name         The name of the store.
         * @param founderEmail The email address of the founder.
         */
        List<String> closeStore(String name, String founderEmail, boolean isAdmin);

        /**
         * Reopens a store by its name and the founder's email.
         *
         * @param storeName    The name of the store.
         * @param founderEmail The email address of the founder.
         */
        List<String> reopenStore(String storeName, String founderEmail, boolean isAdmin);

        // Omri functions

        /**
         * Adds a product to a store.
         *
         * @param email        The email of the user performing the operation.
         * @param storeName    The name of the store.
         * @param catalogID    The ID of the catalog.
         * @param product_name The name of the product.
         * @param description  A description of the product.
         * @param price        The price of the product.
         * @param quantity     The quantity of the product.
         * @param imageURL     The URL of an image
         * @return The ID of the added product.
         */
        String addProductToStore(String email, String storeName, String catalogID, String product_name,
                        String description,
                        double price, int quantity, boolean isAdmin, String imageURL,List<String> categories);

        /**
         * Updates the details of a shopping product.
         *
         * @param email       The email of the user performing the operation.
         * @param storeName   The name of the store.
         * @param catalogID   The ID of the catalog.
         * @param price       The new price of the product.
         * @param description The new description of the product.
         * @return The updated product details.
         */
        ShoppingProductDTO updateShoppingProduct(String email, String storeName, String catalogID, double price,
                        String description);

        /**
         * Deletes a shopping product from a store.
         *
         * @param email     The email of the user performing the operation.
         * @param storeName The name of the store.
         * @param productID The ID of the product to delete.
         * @return The deleted product details.
         */
        ShoppingProductDTO deleteShoppingProduct(String email, String storeName, String productID);

        /**
         * Rates a product in a store.
         *
         * @param email     The email of the user performing the operation.
         * @param storeName The name of the store.
         * @param productID The ID of the product to rate.
         * @param rating    The rating score.
         * @param review    A review for the product.
         * @return The rated product details.
         */
        ShoppingProductDTO rateProduct(String email, String storeName, String productID, int rating, String review);

        /**
         * Rates a store.
         *
         * @param email     The email of the user performing the operation.
         * @param storeName The name of the store.
         * @param rating    The rating score for the store.
         * @param review    A review for the store.
         */
        void rateStore(String email, String storeName, int rating, String review);

        // Ownership and manager operations with operator email authorization


    /**
         * Appoints a new owner for a store.
         *
         * @param storeName     The name of the store.
         * @param operatorEmail The email of the operator performing the operation.
         * @param newOwnerEmail The email of the new owner to appoint.
         */
        void appointOwner(String storeName, String operatorEmail, String newOwnerEmail, boolean isAdmin);

        /**
         * Removes an owner from a store.
         *
         * @param storeName     The name of the store.
         * @param operatorEmail The email of the operator performing the operation.
         * @param ownerToRemove The email of the owner to remove.
         */
        void removeOwner(String storeName, String operatorEmail, String ownerToRemove, boolean isAdmin);

        /**
         * The operator resigns ownership of a store.
         *
         * @param storeName     The name of the store.
         * @param operatorEmail The email of the operator resigning ownership.
         */
        void resignOwnership(String storeName, String operatorEmail);

        /**
         * Appoints a new manager for a store.
         *
         * @param storeName     The name of the store.
         * @param operatorEmail The email of the operator performing the operation.
         * @param managerEmail  The email of the manager to appoint.
         * @param permissions   The list of permissions for the manager.
         */
        void appointManager(String storeName, String operatorEmail, String managerEmail, List<String> permissions,
                        boolean isAdmin);

        /**
         * Updates the permissions of a manager in a store.
         *
         * @param storeName      The name of the store.
         * @param operatorEmail  The email of the operator performing the operation.
         * @param managerEmail   The email of the manager to update.
         * @param newPermissions The new list of permissions for the manager.
         */
        void updateManagerPermissions(String storeName, String operatorEmail, String managerEmail,
                        List<String> newPermissions);

        /**
         * Retrieves the permissions of a manager in a store.
         *
         * @param storeName     The name of the store.
         * @param operatorEmail The email of the operator performing the operation.
         * @param managerEmail  The email of the manager to query.
         * @return A list of permissions assigned to the manager.
         */
        List<String> getManagerPermissions(String storeName, String operatorEmail, String managerEmail);

        /**
         * Retrieves all owners of a store.
         *
         * @param storeName     The name of the store.
         * @param operatorEmail The email of the operator performing the operation.
         * @return A list of all owners' emails.
         */
        List<String> getAllOwners(String storeName, String operatorEmail);

        /**
         * Retrieves all managers of a store.
         *
         * @param storeName     The name of the store.
         * @param operatorEmail The email of the operator performing the operation.
         * @return A list of all managers' emails.
         */
        List<String> getAllManagers(String storeName, String operatorEmail);

        /**
         * Adds funds to a store's balance.
         *
         * @param userBySession The email of the user adding funds.
         * @param storeName     The name of the store to update.
         * @param amount        The amount to add to the balance.
         */
        void addToBalance(String userBySession, String storeName, double amount);

        /**
         * Removes items from stores based on a list of basket items.
         *
         * @param basketDTOList The list of items to remove from the stores.
         * @return A map of basket items to their prices.
         */
        Map<BasketDTO, Double> removeItemsFromStores(List<BasketDTO> basketDTOList);

        Map<String, Double> CalculateDiscountToStores(List<BasketDTO> basketDTOList);

        /**
         * Rolls back items to stores from a basket.
         *
         * @param basketDTO The list of basket items to roll back.
         */
        void rollBackItemsToStores(List<BasketDTO> basketDTO);

        /**
         * Retrieves a product from a store by its ID.
         *
         * @param storeName The name of the store.
         * @param productID The ID of the product to retrieve.
         * @return The product details.
         */
        ShoppingProductDTO getProduct(String storeName, String productID);

        void submitBidToShoppingItem(String Email, String storeName, String productId, double bidAmount);

        void sendAuctionOffer(String Email, String storeName, String productId, double bidAmount);

        Integer getProductQuantity(String storeName, String productId);

        String getStoreFounder(String storeName);

        List<String> getAllBidManagers(String storeName);

        List<BidDTO> getAllBids(String owner, String storeName);

        BidDTO getAuctionHighestBidByProduct(String storeName, String productId);

        Date getAuctionEndDate(String storeName, String productId);

        void acceptBid(String storeName, String assigneeUsername, String productId, BidDTO bidDTO);

        void rollBackByBid(String storeName, BidDTO bidDTO);

        void executeAuctionBid(String Email, String storeName, BidDTO bidDTO);

        void startAuction(String executor, String storeName, String productId, double minPrice, Date endDate);

        List<BidDTO> getProductBids(String storeName, String productId);

        void rejectBid(String owner, String storeName, BidDTO bidDTO);

        void updateStoreDescription(String storeName, String operatorEmail, String description);

        List<StoreDTO> getStoresOwnedBy(String ownerEmail);

        List<ShoppingProductDTO> getStoreProducts(String storeName);

        Map<String, Store.Rating> findRatingsByStore(String storeName);

        List<RatingDto> getStoreRatings(String storeName);

        List<RatingDto> getProductRatings(String storeName,String productId);

        void giveStoreReview(String storeName, String userId, String review, String rating);

        List<Review> getStoreReviews(String storeName);

        void giveStoreComment(String userName,String storeName,String reviewId,String comment);

        Review getReviewById(String storeName, String reviewId);

        void closeAuction(String storeName, String userId, String productId);


        void addSimpleDiscountToEntireStore(String storeName, String operatorEmail,int percentage,String Coupon);
        void addSimpleDiscountToEntireCategoryInStore(String storeName, String operatorEmail, String category, int percentage, String coupon);
        void addSimpleDiscountToSpecificProductInStorePercentage(String storeName, String operatorEmail, String productId, int percentage, String coupon);
        void addConditionalDiscountToEntireStore(String storeName, String operatorEmail, int percentage,int minimumPrice, String coupon);
        void addConditionalDiscountToEntireCategoryInStore(String storeName, String operatorEmail, String category, int percentage,int minimumPrice, String coupon);
        void addConditionalDiscountToSpecificProductInStorePercentage(String storeName, String operatorEmail, String productId, int percentage,int minimumPrice,int minAmount ,String coupon);
        void applyCouponToCart(List<BasketDTO> basketDTOList,String Coupon);

        void addLogicalCompositeConditionalDiscountToSpecificProductInStorePercentage(String storeName, String email, String productId, int percentage, List<ShoppingProduct> products, List<Integer> amounts, int minPrice, String coupon, String logicType);
}