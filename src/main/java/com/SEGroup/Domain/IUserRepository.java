package com.SEGroup.Domain;

import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.DTO.BasketDTO;


import java.util.List;
import java.util.Set;

public interface IUserRepository {
    User findUserByEmail(String email);

    void addUser(String username, String email, String password);

    void deleteUser(String email);

    public void addToCart(User findByUsername, int storeID, int productID);

    public void checkIfExist(String email);

    void appointOwner(String storeName, String Email); //put owner to the email

    void removeOwner(String storeName, String Email); //put owner to the email

    void resignOwnership(String storeName, String Email);

    void appointManager(String storeName, String Email);

    void deleteRoles(List<String> Emails,String storeName); //function that delete all emails roles that connected to sepecific store

    List<BasketDTO> getUserCart(String email); //get cart DTO and 

    void clearUserCart(String email);



}
