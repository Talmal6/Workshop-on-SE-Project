package com.SEGroup.DTO;

import jakarta.persistence.Embeddable;

@Embeddable
public class BidDTOforUser {
    private String productId;
    private String storeName;
    private int bidId;


    public BidDTOforUser(int bidId, String storeName, String productId) {
        this.bidId = bidId;
        this.productId = productId;
        this.storeName = storeName;
    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getStoreName() {
        return storeName;
    }
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    public int getBidId() {
        return bidId;
    }
    public void setBidId(int bidId) {
        this.bidId = bidId;
    }
    @Override
    public String toString() {
        return "BidDTOforUser{" +
                "productId='" + productId + '\'' +
                ", storeName='" + storeName + '\'' +
                ", bidId=" + bidId +
                '}';
    }
}
