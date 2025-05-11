package com.SEGroup.Mapper;// in com.SEGroup.Mapper
import com.SEGroup.DTO.*;
import com.SEGroup.Domain.Store.Auction;

public class AuctionMapper {
    public static AuctionDTO toDTO(String storeName,
                                   String productId,
                                   Auction a) {
        Double top = a.getHighestBid() != null
                ? a.getHighestBid().getAmount()
                : null;
        String bidder = a.getHighestBid() != null
                ? a.getHighestBid().getBidderEmail()
                : null;

        long remaining = Math.max(0,
                a.getEndTime().getTime() - System.currentTimeMillis());

        return new AuctionDTO(
                storeName,
                productId,
                a.getStartingPrice(),
                top,
                bidder,
                a.getEndTime(),
                remaining
        );
    }
}
