package com.SEGroup.Domain;

public class Store {
    public Store(String name, String ownerEmail){

        
    }

    public String getName(){
        return null;
    }
    public void setName(String name){
        
    }


    public String addToBalance(double amount){
        return null;
    }
    public double getBalance(){
        return 0;
    }
    public double setBalance(double balance){
        return 0;
    }

    public void rateStore(int rating, String review){
        
    }
    public boolean submitBidToShoppingItem(String itemName, double bidAmount, String bidderEmail) {
        // Logic to submit a bid for a shopping item
        return false; // Return true if the bid is successfully submitted
    }

    public boolean submitAuctionOffer(String itemName, double offerAmount, String bidderEmail) {
        // Logic to submit an auction offer for an item
        return false; // Return true if the offer is successfully submitted
    }

}
