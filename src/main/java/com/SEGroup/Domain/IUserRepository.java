package com.SEGroup.Domain;


public interface IUserRepository {
    User findByUsername(String email);
    void addUser(User user);
    void deleteUser(String email);
    void addNewGuest(String tempName);

    public void addToCart(User findByUsername, int storeID, int productID);
}
