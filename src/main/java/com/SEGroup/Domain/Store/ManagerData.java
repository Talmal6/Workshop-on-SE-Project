package com.SEGroup.Domain.Store;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the data of a store manager, including their permissions and who appointed them.
 */
@Entity
@Table(name = "store_managers")
public class ManagerData {
    @Id
    @Column(name = "manager_email")
    private String email;

    @Column(name = "appointed_by")
    private String appointedBy;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "manager_permissions", joinColumns = @JoinColumn(name = "manager_email"))
    @Column(name = "permission")
    private Set<ManagerPermission> permissions;

    // Required by JPA
    protected ManagerData() {}

    public ManagerData(String email, String appointedBy, Set<ManagerPermission> permissions) {
        this.email = email;
        this.appointedBy = appointedBy;
        this.permissions = (permissions != null) ? permissions : new HashSet<>();
    }
    public String getEmail() { return email; }
    public String getAppointedBy() { return appointedBy; }
    public Set<ManagerPermission> getPermissions() { return permissions; }
    public void setPermissions(Set<ManagerPermission> permissions) {
        this.permissions = permissions;
    }
    public boolean hasTheRightPermission(ManagerPermission permission) {
        return permissions.contains(permission);
    }
}
