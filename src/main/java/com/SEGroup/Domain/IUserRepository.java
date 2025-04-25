package com.SEGroup.Domain;

import com.SEGroup.Domain.User.User;

public interface IUserRepository {
    User findUserByEmail(String email);

    void addUser(String username, String email, String password);

    void deleteUser(String email);

    void addNewGuest(String tempName);

    public void addToCart(User findByUsername, int storeID, int productID);

    public void checkIfExist(String email);
}
