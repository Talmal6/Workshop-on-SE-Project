package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;
import java.util.List;
import java.util.Set;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.UserSuspensionDTO;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.User;
public class UserRepository implements IUserRepository {

    @Override
    public User findUserByEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findUserByEmail'");
    }

    @Override
    public void addUser(String username, String email, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addUser'");
    }

    @Override
    public void deleteUser(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public void addToCart(User findByUsername, int storeID, int productID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addToCart'");
    }

    @Override
    public void checkIfExist(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkIfExist'");
    }

    @Override
    public void appointOwner(String storeName, String Email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'appointOwner'");
    }

    @Override
    public void removeOwner(String storeName, String Email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeOwner'");
    }

    @Override
    public void resignOwnership(String storeName, String Email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resignOwnership'");
    }

    @Override
    public void appointManager(String storeName, String Email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'appointManager'");
    }

    @Override
    public void deleteRoles(List<String> Emails, String storeName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteRoles'");
    }

    @Override
    public List<BasketDTO> getUserCart(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserCart'");
    }

    @Override
    public void clearUserCart(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearUserCart'");
    }

    @Override
    public String getUserName(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserName'");
    }

    @Override
    public void setAsAdmin(String assignee, String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setAsAdmin'");
    }

    @Override
    public void removeAdmin(String assignee, String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAdmin'");
    }

    @Override
    public boolean userIsAdmin(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'userIsAdmin'");
    }

    @Override
    public void suspendUser(String username, float suspensionTime, String reason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'suspendUser'");
    }

    @Override
    public void unsuspendUser(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unsuspendUser'");
    }

    @Override
    public void checkUserSuspension(String username) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkUserSuspension'");
    }

    @Override
    public List<UserSuspensionDTO> getAllSuspendedUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllSuspendedUsers'");
    }

    @Override
    public List<String> getAllEmails() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllEmails'");
    }

    @Override
    public Set<Role> getGlobalRoles(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGlobalRoles'");
    }

}
