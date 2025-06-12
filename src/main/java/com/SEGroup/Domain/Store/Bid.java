package com.SEGroup.Domain.Store;

import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.BidState;

import jakarta.persistence.*;

/**
 * Represents a bid made by a user in an auction.
 */
@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "bidder_email", nullable = false)
    private String bidderEmail;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private BidState state = BidState.PENDINGFORSELLER; // Default state when a bid is created

    // Constructors
    protected Bid() {
        // Required by JPA
    }

    public Bid(String bidderEmail, double amount) {
        if (bidderEmail == null || bidderEmail.isEmpty())
            throw new IllegalArgumentException("Bidder email cannot be null or empty");
        if (amount <= 0)
            throw new IllegalArgumentException("Bid amount must be positive");
        this.bidderEmail = bidderEmail;
        this.amount = amount;
    }

    public String getBidderEmail() {
        return bidderEmail;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bid amount must be positive");
        }
        this.amount = amount;
    }

    public BidState getState() {
        return state;
    }

    public void setState(BidState state) {
        if (state == null) {
            throw new IllegalArgumentException("Bid state cannot be null");
        }
        this.state = state;
    }


    @Override
    public String toString() {
        return "Bid{email='" + bidderEmail + "', amount=" + amount + ", state=" + state + '}';
    }

    public Integer getId() {
        return id;
    }

    public BidDTO toDTO(String productId, String storeName) {
        return new BidDTO(id, bidderEmail, productId, amount, state, storeName);
    }


}