package com.SEGroup.Domain.Store;

import java.util.*;

public class Store {
    //fields
    private static int idCounter = 0;
    private final int id;
    private String name;
    private String founderEmail;
    private boolean isActive;
    private double balance;

    //products and reviews
    private final Map<String, ShoppingProduct> products;
    private final List<Integer> ratings = new ArrayList<>();
    private final List<String> reviews = new ArrayList<>();

    // Disocunt and policy fields
    private PurchasePolicy purchasePolicy;
    private List<Discount> discounts;

    //Owners and managers
    private final Map<String, String> ownersAppointer = new HashMap<>(); // email → appointedBy
    private final Map<String, ManagerData> managers = new HashMap<>(); // email → metadata

    public Store(String name, String founderEmail) {
        //field
        this.id = ++idCounter;
        this.name = name;
        this.founderEmail = founderEmail;
        this.isActive = true;
        this.balance = 0.0;

        //product and reviews
        this.products = new HashMap<>();
        this.ratings.clear();
        this.reviews.clear();

        // Disocunt and policy fields
        this.purchasePolicy = new PurchasePolicy(0,0);
        this.discounts = new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getfounderEmail() { return founderEmail; }
    public boolean isActive() { return isActive; }
    public double getBalance() { return balance; }

    // Setters
    public void setName(String name) {
        if (name != null && !name.isEmpty())
            this.name = name;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void close() {
        this.isActive = false;
    }
    public void open(){
        this.isActive = true;
    }

    // Products (ShoppingProduct) 4.1
    public void addProduct(ShoppingProduct product) {
        products.put(product.getProductId(), product);
    }

    public ShoppingProduct getProduct(String productId) {
        return products.get(productId);
    }
    public void updateProduct(String productId, ShoppingProduct product) {
        this.products.put(productId, product);
    }

    public void removeProduct(String productId) {
        products.remove(productId);
    }

    public Collection<ShoppingProduct> getAllProducts() {
        return products.values();
    }
    // Store rating
    public void rateStore(int rating, String review) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        ratings.add(rating);
        if (review != null && !review.isBlank()) {
            reviews.add(review);
        }
    }
    public void addToBalance(double amount){
        this.balance += amount;
    }
    public boolean submitBidToShoppingItem(String itemName, double bidAmount, String bidderEmail) {
        ShoppingProduct product = products.get(itemName);

        if (product == null) {
            return false;
        }

        if (bidAmount <= 0 || bidderEmail == null || bidderEmail.isBlank()) {
            return false;
        }

        product.addBid(bidderEmail, bidAmount);
        return true;
    }

    public boolean submitAuctionOffer(String productId, double offerAmount, String bidderEmail) {
        ShoppingProduct product = products.get(productId);
        if (product == null || product.getAuction() == null) {
            return false;
        }

        Auction auction = product.getAuction();
        return auction.submitBid(bidderEmail, offerAmount);
    }
    public boolean isOwner(String email) {
        return founderEmail.equals(email) || ownersAppointer.containsKey(email);
    }
    //Management 4.3
    public boolean appointOwner(String appointerEmail, String newOwnerEmail) {
        // Only Owner can appoint owner
        if (!isOwner(appointerEmail))
            throw new IllegalArgumentException("appointer email is not owner of this store");

        // Can't reappoint
        if (ownersAppointer.containsKey(newOwnerEmail) || founderEmail.equals(newOwnerEmail))
            throw new IllegalArgumentException("appointer email is already owner of this store");

        ownersAppointer.put(newOwnerEmail, appointerEmail);
        return true;
    }
    //4.4 part A
    public boolean removeOwner(String removerEmail, String ownerToRemove) {
        if (!ownersAppointer.containsKey(ownerToRemove))
            throw new IllegalArgumentException("owner email is not owner of this store");

        String appointedBy = ownersAppointer.get(ownerToRemove);
        if (!removerEmail.equals(appointedBy))
            throw new IllegalArgumentException("appointer email is not owner of this store");

        removeAppointedCascade(ownerToRemove);
        ownersAppointer.remove(ownerToRemove);
        return true;
    }
    //4.4 part B
    private void removeAppointedCascade(String email) {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : ownersAppointer.entrySet()) {
            if (entry.getValue().equals(email)) toRemove.add(entry.getKey());
        }
        for (String e : toRemove) {
            removeAppointedCascade(e);
            ownersAppointer.remove(e);
        }

        // Remove the managers too
        managers.entrySet().removeIf(entry -> entry.getValue().getAppointedBy().equals(email));
    }
    //4.5
    public boolean resignOwnership(String ownerEmail) {
        if (founderEmail.equals(ownerEmail)) {
            throw new IllegalArgumentException("ownerEmail is already founder of this store");
        }
        if (!ownersAppointer.containsKey(ownerEmail)) {
            throw new IllegalArgumentException("owner email is not owner of this store");
        }
        removeAppointedCascade(ownerEmail);
        ownersAppointer.remove(ownerEmail);
        return true;
    }
    //4.6
    public boolean appointManager(String ownerEmail, String managerEmail, Set<ManagerPermission> permissions) {
        if (!isOwner(ownerEmail) || managers.containsKey(managerEmail))
            throw new IllegalArgumentException("manager email is not owner of this store");
        managers.put(managerEmail, new ManagerData(ownerEmail, permissions));
        return true;
    }
    //4.7
    public boolean updateManagerPermissions(String ownerEmail, String managerEmail, Set<ManagerPermission> newPermissions) {
        ManagerData manager = managers.get(managerEmail);
        if (manager == null || !manager.getAppointedBy().equals(ownerEmail))
            throw new IllegalArgumentException("manager email is not owner of this store");
        manager.setPermissions(newPermissions);
        return true;
    }
    //4.11 Part A
    public Set<String> getAllOwners() {
        Set<String> owners = new HashSet<>(ownersAppointer.keySet());
        owners.add(founderEmail);
        return owners;
    }
    //4.11 Part B
    public Map<String, ManagerData> getAllManagers() {
        return Collections.unmodifiableMap(managers);
    }
    //4.11 Part C
    public boolean hasManagerPermission(String managerEmail, ManagerPermission permission) {
        ManagerData manager = managers.get(managerEmail);
        if (manager == null) {
            return false; // לא מוגדר כמנהל בכלל
        }
        return manager.hasTheRightPermission(permission);
    }

    //4.11 Part D
    public Set<ManagerPermission> getManagerPermissions(String managerEmail) {
        ManagerData manager = managers.get(managerEmail);
        if (manager == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(manager.getPermissions());
    }


    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ownerEmail='" + founderEmail + '\'' +
                ", isActive=" + isActive +
                ", balance=" + balance +
                ", products=" + products.keySet() +
                '}';
    }
}
