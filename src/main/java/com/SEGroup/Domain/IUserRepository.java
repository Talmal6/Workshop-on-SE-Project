package com.SEGroup.Domain;

import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.User.Address;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.BidDTOforUser;
import com.SEGroup.DTO.CreditCardDTO;
import com.SEGroup.DTO.UserSuspensionDTO;

import java.util.List;
import java.util.Set;

/**
 * Interface representing a repository for managing users.
 * It provides methods for creating, retrieving, updating, and deleting users,
 * as well as managing user roles and shopping carts.
 */
public interface IUserRepository {

    /**
     * Retrieves a user by their email.
     *
     * @param email The email of the user.
     * @return The user object corresponding to the provided email.
     */
    User findUserByEmail(String email);

    /**
     * Adds a new user to the repository.
     *
     * @param username The username of the new user.
     * @param email    The email of the new user.
     * @param password The password of the new user.
     */
    void addUser(String username, String email, String password);
    void addUserWithaddress(String username, String email, String passwordHash,AddressDTO addressDTO);
    /**
     * Deletes a user from the repository by their email.
     *
     * @param email The email of the user to delete.
     */
    void deleteUser(String email);

    /**
     * Adds a product to the user's shopping cart.
     *
     * @param storeID        The ID of the store where the product is located.
     * @param productID      The ID of the product being added to the cart.
     */
    public void addToCart(String email, String storeID, String productID);

    /**
     * Checks if a user exists in the repository by their email.
     *
     * @param email The email to check for existence.
     */
    public void checkIfExist(String email);

    /**
     * Appoints a user as an owner of a store.
     *
     * @param storeName The name of the store.
     * @param Email     The email of the user to appoint as owner.
     */
    void appointOwner(String storeName, String Email);

    /**
     * Removes a user's ownership of a store.
     *
     * @param storeName The name of the store.
     * @param Email     The email of the user to remove as owner.
     */
    void removeOwner(String storeName, String Email);

    /**
     * The user resigns ownership of a store.
     *
     * @param storeName The name of the store.
     * @param Email     The email of the user resigning ownership.
     */
    void resignOwnership(String storeName, String Email);

    /**
     * Appoints a user as a manager of a store.
     *
     * @param storeName The name of the store.
     * @param Email     The email of the user to appoint as manager.
     */
    void appointManager(String storeName, String Email);

    /**
     * Deletes all roles (owners and managers) for a list of users associated with a
     * specific store.
     *
     * @param Emails    A list of email addresses whose roles are to be deleted.
     * @param storeName The name of the store from which roles will be deleted.
     */
    void deleteRoles(List<String> Emails, String storeName);

    /**
     * Retrieves the user's shopping cart as a list of BasketDTO objects.
     *
     * @param email The email of the user whose cart is being retrieved.
     * @return A list of BasketDTO objects representing the user's shopping cart.
     */
    List<BasketDTO> getUserCart(String email);

    /**
     * Clears the user's shopping cart.
     *
     * @param email The email of the user whose cart is to be cleared.
     */
    void clearUserCart(String email);

    /**
     * Retrieves the username of a user by their email.
     *
     * @param email The email of the user.
     * @return The username of the user.
     */
    String getUserName(String email);

    /**
     * Sets a user as an admin.
     *
     */

    void setAsAdmin(String assignee, String username);

    void removeAdmin(String assignee, String username);

    boolean userIsAdmin(String username);

    void suspendUser(String username, float suspensionTime, String reason);

    void unsuspendUser(String username);

    void checkUserSuspension(String username) throws Exception;

    List<UserSuspensionDTO> getAllSuspendedUsers();

    List<String> getAllEmails();

    Set<Role> getGlobalRoles(String email);

    void modifyCartQuantity(String email,
            String productID,
            String storeName,
            int quantity);

    AddressDTO getAddress(String email);
    void setAddress(String email, AddressDTO address);

    void setUserName(String email, String newUsername);

    CreditCardDTO getCreditCard(String email);
    void setCreditCard(String email, CreditCardDTO creditCardDTO, AddressDTO addressDTO);


    void addBidToUser(String email, BidDTO bid);

    void removeBidFromUser(String email, BidDTO bid);
    Set<BidDTOforUser> getActiveBids(String email);


}
