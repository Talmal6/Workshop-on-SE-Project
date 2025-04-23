package com.SEGroup.Domain;


public interface IUserRepository {
    UserDTO findByUsername(String email);
    void addUser(UserDTO user);
    void deleteUser(String email);
    void addNewGuest(String tempName);

    public void addToCart(UserDTO findByUsername, int storeID, int productID);
}
