package com.SEGroup.UnitTests.StoreTests;
import com.SEGroup.Domain.Store.Bid;
import com.SEGroup.Domain.Store.ShoppingProduct;
import static org.junit.Assert.*;

import com.sun.source.tree.AssertTree;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.Optional;

public class ShoppingProductTest {
    ShoppingProduct shoppingProduct;
    @BeforeEach
    public void init(){
        this.shoppingProduct = new ShoppingProduct("Supermarket", "Drink", "Milk", "Milk Vanilla", "" +
                "The milk is made out of vanilla with milk", 7.18, 2,"");
    }

    @Test
    public void GivenProduct_WhenSetQuantity_Fails() throws Exception {
        shoppingProduct.setQuantity(-1);
    }

    @Test
    public void GivenProduct_WhenSetPrice_ThenFails() throws Exception {
        shoppingProduct.setPrice(-1);
    }

    @Test
    void GivenValidBid_WhenAddBid_ThenBidIsAdded() {
        shoppingProduct.addBid("user@gmail.com", 100.0,1);
        assertEquals(1, shoppingProduct.getBids().size());
    }

    @Test
    void GivenInvalidBidEmail_WhenAddBid_ThenFailsToAdd() {
        shoppingProduct.addBid("u", 100.0,1);
    }
    @Test
    void GivenInvalidBidAmount_WhenAddBid_ThenFailsToAdd() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                shoppingProduct.addBid("user@gmail.com", -1,1));
        assertEquals("Bid amount must be positive", exception.getMessage());
    }
    @Test
    void GivenNoBids_WhenGetHighestBid_ThenReturnEmptyOptional() {
        assertTrue(shoppingProduct.getHighestBid().isEmpty());
    }

    @Test
    void GivenBids_WhenGetHighestBid_ThenReturnBidWithHighestAmount() {
        shoppingProduct.addBid("user1@test.com", 50.0,1);
        shoppingProduct.addBid("user2@test.com", 100.0,1);
        shoppingProduct.addBid("user3@test.com", 75.0,1);
        
        Optional<Bid> highestBid = shoppingProduct.getHighestBid();
        assertTrue(highestBid.isPresent());
        assertEquals(100.0, highestBid.get().getAmount(), 0.001);
    }

    @Test
    void GivenAuctionDetails_WhenStartAuction_ThenAuctionIsInitialized() {
        Date endTime = new Date(System.currentTimeMillis() + 10000); // +10 seconds
        shoppingProduct.startAuction(100.0, endTime);

        assertNotNull(shoppingProduct.getAuction());
        assertEquals(100.0, shoppingProduct.getAuction().getStartingPrice(), 0.001);
        assertEquals(endTime, shoppingProduct.getAuction().getEndTime());
    }

    @Test
    void GivenInvalidAuctionDetails_WhenSubmitBid_ThenFails(){
        Date endTime = new Date(System.currentTimeMillis() + 10000); // +10 seconds
        shoppingProduct.startAuction(100.0, endTime);
        shoppingProduct.addBid("User@gmail.com",99,1);
        assertFalse(shoppingProduct.getAuction().submitBid("User@gmail.com",99,1));
    }

    @Test
    void GivenInvalidAuctionDetails_WhenSubmitBid_ThenFails2(){
        Date endTime = new Date(System.currentTimeMillis() - 1000); // +10 seconds
        shoppingProduct.startAuction(100.0, endTime);
        shoppingProduct.addBid("User@gmail.com",110,1);
        assertFalse(shoppingProduct.getAuction().submitBid("User@gmail.com",110,1));
    }

    @Test
    void GivenValidRating_WhenAddRating_ThenRatingIsAdded() {
        shoppingProduct.addRating("rater@test.com", 5, "Great!");
        assertEquals(5.0, shoppingProduct.averageRating(), 0.001);
    }

    @Test
    void GivenInvalidScore_WhenAddRating_ThenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                shoppingProduct.addRating("rater@test.com", 6, "Invalid score"));
        assertEquals("Rating msut be 1-5 ", exception.getMessage());
    }

    @Test
    void GivenNoRatings_WhenAverageRating_ThenReturnZero() {
        assertEquals(0.0, shoppingProduct.averageRating(), 0.001);
    }

    @Test
    void GivenMultipleRatings_WhenAverageRating_ThenReturnCorrectAverage() {
        shoppingProduct.addRating("rater1@test.com", 4, "Good");
        shoppingProduct.addRating("rater2@test.com", 2, "Bad");
        shoppingProduct.addRating("rater3@test.com", 5, "Excellent");

        double avg = shoppingProduct.averageRating();
        assertEquals(3.6666666666666665, avg, 0.0001);
    }


}
