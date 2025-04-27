package com.SEGroup.Domain.Store;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the data of a store manager, including their permissions and who appointed them.
 */
public class ManagerData {
    private final String appointedBy;
    private Set<ManagerPermission> permissions;

    public ManagerData(String appointedBy, Set<ManagerPermission> permissions) {
        this.appointedBy = appointedBy;
        this.permissions = (permissions != null) ? permissions : new HashSet<>();
    }

    public String getAppointedBy() { return appointedBy; }
    public Set<ManagerPermission> getPermissions() { return permissions; }
    public void setPermissions(Set<ManagerPermission> permissions) {
        this.permissions = permissions;
    }
    public boolean hasTheRightPermission(ManagerPermission permission) {
        return permissions.contains(permission);
    }
}
