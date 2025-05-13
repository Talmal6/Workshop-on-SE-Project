package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.StoreCardDto;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.AllStoresView;
import com.vaadin.flow.component.UI;

import java.util.List;
import java.util.stream.Collectors;

public class AllStoresPresenter {
    private final AllStoresView view;
    private final StoreService storeService;
    private boolean showMyStoresOnly = false;

    public AllStoresPresenter(AllStoresView view) {
        this.view = view;
        this.storeService = ServiceLocator.getStoreService();
    }

    /**
     * Loads stores based on the myStoresOnly flag.
     */
    public void loadStores(boolean myStoresOnly) {
        try {
            this.showMyStoresOnly = myStoresOnly;
            List<StoreCardDto> stores;

            if (myStoresOnly && SecurityContextHolder.isLoggedIn()) {
                String email = SecurityContextHolder.email();
                System.out.println("Loading stores owned by: " + email);
                stores = storeService.listStoresOwnedBy(email);
                if (stores.isEmpty()) {
                    view.showInfo("You don't own any stores yet");
                }
            } else {
                stores = storeService.listAllStores();
            }

            view.displayStores(stores);
        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Failed to load stores: " + e.getMessage());
            view.displayStores(List.of());
        }
    }

    /**
     * Sorts stores by rating in descending order.
     */
    public void sortByRating() {
        try {
            List<StoreCardDto> stores;

            if (showMyStoresOnly && SecurityContextHolder.isLoggedIn()) {
                stores = storeService.listStoresOwnedBy(SecurityContextHolder.email());
            } else {
                stores = storeService.listAllStores();
            }

            // Sort by rating (highest first)
            List<StoreCardDto> sortedStores = stores.stream()
                    .sorted((s1, s2) -> Double.compare(s2.rating(), s1.rating()))
                    .collect(Collectors.toList());

            view.displayStores(sortedStores);
        } catch (Exception e) {
            view.showError("Failed to sort stores: " + e.getMessage());
        }
    }

    /**
     * Creates a new store for the current user.
     */
    public void createStore(String storeName) {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to create a store");
                return;
            }

            if (storeName == null || storeName.trim().isEmpty()) {
                view.showError("Store name cannot be empty");
                return;
            }

            // Get session token
            String token = SecurityContextHolder.token();

            System.out.println("Creating store with name: " + storeName);
            System.out.println("User token: " + token);

            // Use the simple version of createStore which only needs token and storeName
            // The StoreService will extract the email from the token and handle parameter order
            Result<Void> result = storeService.createStore(token, storeName);

            if (result.isSuccess()) {
                view.showSuccess("Store created successfully");

                // Use a background thread to avoid UI freezing
                new Thread(() -> {
                    try {
                        // Give the backend a moment to process the store creation
                        Thread.sleep(1000);

                        // Then access the UI thread to update the view
                        UI ui = UI.getCurrent();
                        if (ui != null) {
                            ui.access(() -> {
                                System.out.println("Reloading stores after creation");
                                // Show user's stores to highlight the new store
                                loadStores(true);
                            });
                        }
                    } catch (InterruptedException ignored) {}
                }).start();
            } else {
                view.showError("Failed to create store: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Failed to create store: " + e.getMessage());
        }
    }

    /**
     * Checks if the user is logged in.
     */
    public boolean isUserLoggedIn() {
        return SecurityContextHolder.isLoggedIn();
    }
}