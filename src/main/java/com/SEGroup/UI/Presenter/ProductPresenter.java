package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.AuctionDTO;
import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationType;
import com.SEGroup.Infrastructure.NotificationCenter.RichNotification;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.UserService;
import com.SEGroup.UI.*;
import com.SEGroup.UI.Views.ProductView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ProductPresenter {
    private static final Logger logger = Logger.getLogger(ProductPresenter.class.getName());
    private Registration pollRegistration;
    private final ProductView view;
    private final String productId;
    private final String storeName;
    private final StoreService storeService;
    private final UserService userService;
    private ShoppingProductDTO product;
    private AuctionDTO auction;
    private final DirectNotificationSender notificationSender;
    private final NotificationCenter notificationCenter;
    private List<String> owners = List.of();
    // Bid approval tracking - will be refreshed from server each time
    private int totalOwnersCount = 0;
    private int currentApprovals = 0;

    public ProductPresenter(ProductView view, String productId, String storeName, DirectNotificationSender notificationSender) {
        this.view = view;
        this.productId = productId;
        this.storeName = storeName;
        this.storeService = ServiceLocator.getStoreService();
        this.userService = ServiceLocator.getUserService();
        this.notificationCenter = ServiceLocator.getNotificationCenter();
        this.notificationSender = notificationSender;
    }

    public AuctionDTO getAuction() {
        return auction;
    }
    private void notify(NotificationType t,
                        String target,
                        String text,
                        double price,
                        String extra)
    {
        notificationSender.send(t, target, text, price, productId, extra);
    }




    /**
     * Sends a notification to all store owners
     * @param message The message to send
     * @return The number of owners notified
     */
    private int notifyStoreOwners(String message) {
        try {
            Result<List<String>> ownersResult = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    storeName,
                    SecurityContextHolder.email()
            );

            if (ownersResult.isSuccess() && ownersResult.getData() != null) {
                int count = 0;
                for (String ownerEmail : ownersResult.getData()) {
                    notificationSender.sendSystemNotification(ownerEmail, message);
                    count++;
                }
                logger.info("Notified " + count + " store owners: " + message);
                return count;
            }
        } catch (Exception e) {
            logger.severe("Error notifying store owners: " + e.getMessage());
        }
        return 0;
    }
    private List<String> loadOwners() {
        if (owners.isEmpty()) {
            try {
                Result<List<String>> r = storeService.getAllOwners(
                        SecurityContextHolder.token(), storeName, SecurityContextHolder.email());

                if (r.isSuccess() && r.getData() != null) {
                    owners = r.getData();
                    System.out.println("Successfully loaded " + owners.size() + " owners for store: " + storeName);
                } else {
                    System.out.println("Failed to load owners: " +
                            (r.isSuccess() ? "No data returned" : r.getErrorMessage()));
                    owners = List.of(); // Empty list
                }
            } catch (Exception e) {
                System.err.println("Error loading owners: " + e.getMessage());
                // Return empty list on error instead of failing
                owners = List.of();
            }
        }
        return owners;
    }

    /**
     * Loads the total count of store owners for approval tracking
     * This ensures we always have the latest count from the server
     */
    private void loadOwnersCount() {
        try {
            Result<List<String>> ownersResult = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    storeName,
                    SecurityContextHolder.email()
            );

            if (ownersResult.isSuccess() && ownersResult.getData() != null) {
                this.totalOwnersCount = ownersResult.getData().size();
                logger.info("Store has " + totalOwnersCount + " owners for approval tracking");
            } else {
                logger.warning("Failed to load owners count: " +
                        (ownersResult.isSuccess() ? "Empty result" : ownersResult.getErrorMessage()));
                // Fallback to at least 1 owner (the current user if owner)
                this.totalOwnersCount = isOwner() ? 1 : 2; // Assume at least one more owner if not owner
            }
        } catch (Exception e) {
            logger.severe("Error loading owners count: " + e.getMessage());
            this.totalOwnersCount = 2; // Default fallback
        }
    }

    /**
     * Loads the current approval count for a specific bid from the server
     * @param bidderEmail The bidder's email
     * @param bidPrice The bid price
     * @return Number of current approvals
     */
    private int loadCurrentApprovals(String bidderEmail, double bidPrice) {
        try {
            // This would be replaced with an actual call to your backend service
            // For example: storeService.getBidApprovalCount(storeName, productId, bidderEmail, bidPrice);
            // For now, we'll simulate this by returning our in-memory counter
            return currentApprovals;
        } catch (Exception e) {
            logger.severe("Error loading current approvals: " + e.getMessage());
            return 0;
        }
    }


    /**
     * Loads auction information for the current product
     */
    public void loadAuctionInfo() {
        try {
            String token = SecurityContextHolder.token();
            if (token == null || !SecurityContextHolder.isLoggedIn()) {
                return; // Can't load auction info without authentication
            }

            // Try to get auction end date - this determines if an auction exists
            Result<Date> endDateResult = storeService.getAuctionEndDate(token, storeName, productId);

            // If we can't get an end date, there's no auction
            if (!endDateResult.isSuccess() || endDateResult.getData() == null) {
                return;
            }

            Date endDate = endDateResult.getData();

            // If auction has ended, process end and don't display auction UI
            if (endDate.before(new Date())) {
                processAuctionEnd();
                return;
            }

            // Get highest bid for the auction
            Result<BidDTO> highestBidResult =
                    storeService.getAuctionHighestBidByProduct(token, storeName, productId);

// ── Determine starting- and current-price ─────────────
            double startingPrice;
            AuctionDTO cached = ProductCache.getAuction(productId);
            if (cached != null) {
                startingPrice = cached.getStartingPrice();
            } else if (product != null) {
                startingPrice = product.getPrice();
            } else {
                startingPrice = 0.0;
            }

            Double  highestBid    = null;
            String  highestBidder = null;

// If there's a highest bid, use it
            if (highestBidResult.isSuccess() && highestBidResult.getData() != null) {
                highestBid    = highestBidResult.getData().getPrice();
                highestBidder = highestBidResult.getData().getBidderEmail();
            }

            if (highestBid == null && cached != null && cached.getHighestBid() != null) {
                highestBid    = cached.getHighestBid();
                highestBidder = cached.getHighestBidder();
            }

            // Calculate time remaining
            long timeRemaining = Math.max(0, endDate.getTime() - System.currentTimeMillis());

            // Create the auction DTO
            auction = new AuctionDTO(
                    storeName,
                    productId,
                    startingPrice,
                    highestBid,
                    highestBidder,
                    endDate,
                    timeRemaining
            );

            // Display the auction info in the view
            view.displayAuctionInfo(auction);
            ProductCache.put(productId, auction);
            // Set up polling for real-time updates
            if (timeRemaining > 0) {
                scheduleAuctionRefresh();
            }
        } catch (Exception e) {
            logger.severe("Error loading auction info: " + e.getMessage());
        }
    }

    /**
     * Sets up automatic polling to refresh auction data
     */
// --- add at the top with the other fields ---------------
    private void scheduleAuctionRefresh() {
        // Avoid stacking listeners
        if (pollRegistration != null) {        // already polling – nothing to do
            return;
        }

        UI currentUI = UI.getCurrent();
        if (currentUI != null) {
            // Use a less frequent polling interval to avoid conflicts with timer
            // 10 seconds instead of 5 seconds
            currentUI.setPollInterval(10000);

            pollRegistration = currentUI.addPollListener(event -> {
                if (auction == null) {         // auction was removed
                    currentUI.setPollInterval(-1);
                    pollRegistration.remove();
                    pollRegistration = null;
                    return;
                }

                if (auction.getEndTime().before(new Date())) {
                    processAuctionEnd();
                    currentUI.setPollInterval(-1);
                    pollRegistration.remove();
                    pollRegistration = null;
                } else {
                    // Don't do a full refresh, just quietly check for updates
                    checkForAuctionUpdates();
                }
            });
        }
    }

    private void checkForAuctionUpdates() {
        try {
            String token = SecurityContextHolder.token();
            if (token == null || !SecurityContextHolder.isLoggedIn()) {
                return;
            }

            // Only get the highest bid - we don't need to refresh everything
            Result<BidDTO> highestBidResult = storeService.getAuctionHighestBidByProduct(
                    token, storeName, productId);

            if (highestBidResult.isSuccess() && highestBidResult.getData() != null) {
                BidDTO highestBid = highestBidResult.getData();

                // Only update UI if there's a new bid that's not from current user
                if (auction == null || auction.getHighestBid() == null ||
                        highestBid.getPrice() > auction.getHighestBid() &&
                                !highestBid.getBidderEmail().equals(SecurityContextHolder.email())) {

                    // Update auction object
                    auction.setHighestBid(highestBid.getPrice());
                    auction.setHighestBidder(highestBid.getBidderEmail());

                    // Update UI with minimal changes
                    view.updateAuctionHighestBid(highestBid.getPrice(), highestBid.getBidderEmail());
                }
            }
        } catch (Exception e) {
            logger.severe("Error checking for auction updates: " + e.getMessage());
        }
    }

    /**
     * Called when the UI timer finishes or immediately if end-date is in the past.
     */

    /**
     * Called when the UI timer finishes or immediately if end-date is in the past.
     */

    /**
     * Called when the auction ends—determine winner, notify all, and update UI.
     */
    public void processAuctionEnd() {
        if (auction == null) return;
        if (!GlobalAuctionRegistry.markClosed(productId)) return;

        // 1) Gather all participants
        Set<String> all = GlobalAuctionRegistry.getBidders(productId);
        GlobalAuctionRegistry.clear(productId);

        // 2) No bids → just show “ended” with no sale
        if (all.isEmpty()) {
            view.showAuctionEnded("—", 0);
            return;
        }

        // 3) Authoritative winner from backend
        Result<BidDTO> highRes = storeService.getAuctionHighestBidByProduct(
                SecurityContextHolder.token(), storeName, productId);
        BidDTO winBid = highRes.isSuccess() ? highRes.getData() : null;

        double finalPrice = winBid  != null
                ? winBid.getPrice()
                : auction.getHighestBid() != null
                ? auction.getHighestBid()
                : auction.getStartingPrice();

        String winner = winBid != null
                ? winBid.getBidderEmail()
                : auction.getHighestBidder();

        // 4) Update UI
        view.showAuctionEnded(
                winner != null ? winner : "—",
                finalPrice
        );

        String ownerEmail = SecurityContextHolder.email();

        // 5) Notify the winner
        if (winner != null) {
            String winMsg = "Congratulations! You won the auction for "
                    + getProductName() + " at $" + String.format("%.2f", finalPrice);
            notificationSender.send(
                    NotificationType.AUCTION_WIN,
                    winner,
                    winMsg,
                    finalPrice,
                    productId,
                    null
            );
        }

        // 6) Notify all the others as “LOSE”, sent _from_ the owner
        all.stream()
                .filter(u -> !u.equalsIgnoreCase(winner))
                .forEach(loser -> {
                    String loseMsg = "You lost the auction for "
                            + getProductName() + ". Final price: $"
                            + String.format("%.2f", finalPrice);
                    notificationSender.send(
                            NotificationType.AUCTION_LOSE,
                            loser,
                            loseMsg,
                            finalPrice,
                            productId,
                            null
                    );
                });

        // 7) Audit notification to store-owners
        String audit = String.format(
                "Auction ended for %s. Winner: %s at $%.2f",
                getProductName(),
                winner != null ? winner : "—",
                finalPrice
        );
        notifyStoreOwners(audit);
    }



    /**
     * Loads the product details from the service and updates the view.
     */
    public void loadProductDetails() {
        try {
            logger.info("Loading product details for: " + productId + " in store: " + storeName);

            Result<ShoppingProductDTO> productResult = storeService.getProductFromStore(
                    SecurityContextHolder.token(),
                    storeName,
                    productId
            );

            // Remove this premature notification - it's causing the NPE
            // if (notificationSender != null && SecurityContextHolder.isLoggedIn()) {
            //    notificationSender.sendSystemNotification(
            //        SecurityContextHolder.email(),
            //        "Test notification: Viewed product " + product.getName()
            //    );
            // }

            if (productResult.isSuccess() && productResult.getData() != null) {
                this.product = productResult.getData();
                view.displayProduct(product);
                logger.info("Successfully loaded product: " + product.getName());

                // Load owners count for bid approval tracking
                loadOwnersCount();
            } else {
                // Fallback method - try to find the product in all products
                logger.info("Product not found directly, trying fallback approach...");
                Result<List<ShoppingProductDTO>> allResult = storeService.getAllProducts();

                if (allResult.isSuccess()) {
                    Optional<ShoppingProductDTO> foundProduct = allResult.getData().stream()
                            .filter(p -> p.getProductId().equals(productId) && p.getStoreName().equals(storeName))
                            .findFirst();

                    if (foundProduct.isPresent()) {
                        this.product = foundProduct.get();
                        view.displayProduct(this.product);
                        logger.info("Found product using fallback: " + this.product.getName());

                        // Load owners count for bid approval tracking
                        loadOwnersCount();
                    } else {
                        view.showError("Product not found");
                        logger.warning("Product not found with fallback either");
                    }
                } else {
                    view.showError("Error loading product: " +
                            (productResult.isSuccess() ? "No data found" : productResult.getErrorMessage()));
                }
            }
        } catch (Exception e) {
            logger.severe("Error loading product details: " + e.getMessage());
            e.printStackTrace();
            view.showError("Error loading product: " + e.getMessage());
        }

        // Now that product is loaded, it's safe to send test notification
    }

    /**
     * Starts an auction for the current product
     * @param startingPrice The starting price
     * @param endDate The end date/time
     */
    // ────────────────────────────────────────
// startAuction(...) → broadcast AUCTION_START to all registered users
// ────────────────────────────────────────
    public void startAuction(double startingPrice, Date endDate) {
        // 1) Permission checks
        if (!SecurityContextHolder.isLoggedIn()) {
            view.showError("You must be logged in to start an auction");
            return;
        }
        if (!isOwner()) {
            view.showError("Only store owners can start auctions");
            return;
        }

        // 2) Call backend to start auction
        Result<Void> res = storeService.startAuction(
                SecurityContextHolder.token(),
                storeName, productId,
                startingPrice, endDate
        );
        if (!res.isSuccess()) {
            view.showError("Failed to start auction: " + res.getErrorMessage());
            return;
        }

        // 3) Update UI & cache
        view.showSuccess("Auction started successfully!");
        auction = new AuctionDTO(
                storeName,
                productId,
                startingPrice,
                null,        // no bids yet
                null,
                endDate,
                endDate.getTime() - System.currentTimeMillis()
        );
        ProductCache.put(productId, auction);
        view.displayAuctionInfo(auction);
        scheduleAuctionRefresh();

        // 4) Broadcast AUCTION_START to every user in the system
        List<String> everyone = userService.allUsersEmails();
        String startMsg = SecurityContextHolder.email()
                + " started an auction for " + getProductName()
                + " (starting at $" + String.format("%.2f", startingPrice) + ")";
        for (String userEmail : everyone) {
            notificationSender.send(
                    NotificationType.AUCTION_START,
                    userEmail,
                    startMsg,
                    startingPrice,
                    productId,
                    null
            );
        }
    }


    /**
     * Adds the current product to the user's cart
     */
    public void addToCart() {
        try {
            logger.info("Adding to cart: " + productId + " from store: " + storeName);
            String token = SecurityContextHolder.token();
            logger.info("Current token: " + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null"));

            // For guests, create a session if needed
            if (token == null || token.isEmpty()) {
                logger.info("No token found, creating guest session");
                Result<String> guestResult = userService.guestLogin();
                if (guestResult.isSuccess()) {
                    token = guestResult.getData();
                    logger.info("Created guest token: " + token.substring(0, Math.min(10, token.length())) + "...");
                    // Store token in session for future use
                    SecurityContextHolder.storeGuestToken(token);
                } else {
                    view.showError("Failed to create guest session: " + guestResult.getErrorMessage());
                    return;
                }
            }

            // Add to cart using the token
            logger.info("Adding to cart with token");
            Result<String> result = userService.addToCart(token, productId, storeName);

            if (result.isSuccess()) {
                view.showSuccess("Added to cart!");
                logger.info("Successfully added to cart");
            } else {
                view.showError("Error: " + result.getErrorMessage());
                logger.warning("Failed to add to cart: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            logger.severe("Exception adding to cart: " + e.getMessage());
            e.printStackTrace();
            view.showError("Error adding to cart: " + e.getMessage());
        }
    }

    /**
     * Load all bids for the current product (regular bids, not auction)
     * @return List of bids or empty list if none found
     */
    public Result<List<BidDTO>> loadProductBids() {
        try {
            return storeService.getProductBids(
                    SecurityContextHolder.token(),
                    storeName,
                    productId
            );
        } catch (Exception e) {
            logger.severe("Error loading bids: " + e.getMessage());
            return Result.failure("Failed to load bids: " + e.getMessage());
        }
    }

    /**
     * Accept a bid from a specific user - requires approval from all owners
     * @param bidderEmail Email of the bidder
     * @param bidPrice Price of the bid
     * @return Success or failure result
     */
    public Result<Void> acceptBid(String bidderEmail, double bidPrice) {
        try {
            if (!isOwner()) {
                return Result.failure("Only store owners can accept bids");
            }

            // Reset approval counter for this new approval process
            currentApprovals = 1; // Including this owner

            // Get all other owners to request approval
            Result<List<String>> ownersResult = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    storeName,
                    SecurityContextHolder.email()
            );

            if (ownersResult.isSuccess() && ownersResult.getData() != null) {
                List<String> owners = ownersResult.getData();
                totalOwnersCount = owners.size();

                // If I'm the only owner, accept immediately
                if (totalOwnersCount <= 1) {
                    // Notify bidder their bid was accepted
                    String acceptMessage = "Your offer of $" + bidPrice + " for " + getProductName() +
                            " has been accepted by " + storeName + "!";
                    sendSimpleNotification(bidderEmail, acceptMessage);

                    return Result.success(null);
                }

                // Request approval from all other owners
                for (String ownerEmail : owners) {
                    if (!ownerEmail.equals(SecurityContextHolder.email())) {
                        // Send approval request to each owner
                        notify(NotificationType.BID_APPROVAL_NEEDED, ownerEmail, null, bidPrice, bidderEmail);
                    }
                }

                // Record my approval
                notify(NotificationType.BID_APPROVAL_OK, null, null, bidPrice, bidderEmail);

                // Check if all approvals are already received (e.g., from a database)
                currentApprovals = loadCurrentApprovals(bidderEmail, bidPrice);

                // If all owners have now approved, finalize
                if (currentApprovals >= totalOwnersCount) {
                    finalizeAcceptedBid(bidderEmail, bidPrice);
                }

                return Result.success(null);
            } else {
                return Result.failure("Failed to get store owners");
            }
        } catch (Exception e) {
            logger.severe("Error accepting bid: " + e.getMessage());
            return Result.failure("Failed to accept bid: " + e.getMessage());
        }
    }

    /**
     * Finalizes an accepted bid after all approvals
     */
    private void finalizeAcceptedBid(String bidder, double price){
        notificationSender.send(
                NotificationType.BID_ACCEPTED,
                bidder,
                null,
                price,
                productId,
                null);

        /* let owners know the process is done */
        notificationSender.sendToMany(
                loadOwners(),
                NotificationType.BID_ACCEPTED,
                "Bid accepted by all owners",         // human-text for owners
                price,
                productId,
                bidder);
    }


    /**
     * Records an owner's approval for a bid
     * In a real implementation, this would persist the approval in a database
     */
    public void recordOwnerApproval(String ownerEmail,
                                    String bidderEmail,
                                    double bidPrice) {

        currentApprovals++;

        notificationSender.send(                    // NEW
                NotificationType.BID_APPROVAL_OK,
                bidderEmail,
                null,
                bidPrice,
                productId,
                ownerEmail);    // who approved

        if(currentApprovals >= totalOwnersCount){
            finalizeAcceptedBid(bidderEmail, bidPrice);
        }
    }

    /**
     * Places a bid in an auction with validation
     */
// In ProductPresenter.java - Fix the placeBid method
    /**
     * Places a bid in an auction with validation
     */

    /**
     * Places a bid in an auction with optimistic UI update
     * and correct previous‐bidder notifications.
     */
    /**
     * Places a bid in an auction, always syncing with server to get the real highest bidder.
     * Ensures only the actual winner gets "win" notification, and losers get "lose" notification.
     * Store owners get full bid event notifications.
     */
    /**
     * Places a bid in an auction with optimistic UI update
     * and resilient notification handling so the server sync never fails.
     */

    public void placeBid(double amount) {
        Date now = new Date();
        if (auction.getEndTime().before(now)) {
            view.showError("Sorry, this auction has already ended.");
            return;
        }
        if (!SecurityContextHolder.isLoggedIn()) {
            view.showError("You must be logged in to place a bid");
            return;
        }

        Double currentHigh = auction.getHighestBid();
        if (currentHigh != null && amount <= currentHigh) {
            view.showError("Your bid must be higher than the current highest bid ($"
                    + String.format("%.2f", currentHigh) + ")");
            return;
        }

        String prevLeader   = auction.getHighestBidder();
        Double prevAmount   = auction.getHighestBid();
        String me           = SecurityContextHolder.email();

        auction.setHighestBid(amount);
        auction.setHighestBidder(me);
        ProductCache.put(productId, auction);
        view.updateAuctionHighestBid(amount, me);

        Result<Void> r = storeService.sendAuctionOffer(
                        SecurityContextHolder.token(), storeName, productId, amount);
        if (!r.isSuccess()) {

            auction.setHighestBid(prevAmount);
            auction.setHighestBidder(prevLeader);
            ProductCache.put(productId, auction);
            view.updateAuctionHighestBid(prevAmount, prevLeader);
            view.showError("Failed to place bid: " + r.getErrorMessage());
            return;
        }

        view.showSuccess("Bid placed successfully!");
        AuctionParticipationTracker.mark(productId);
        GlobalAuctionRegistry.addBidder(productId, me);


        List<String> ownerEmails = loadOwners();
        if (!ownerEmails.isEmpty()) {
            String notifText = me + " placed a bid of $"
                    + String.format("%.2f", amount) + " on " + getProductName();
            for (String owner : ownerEmails) {
                notificationSender.send(
                        NotificationType.AUCTION_BID,
                        owner,
                        notifText,
                        amount,
                        productId,
                        null
                );
            }
        }
    }



    /**
     * Submit a bid offer for a product (regular bid, not auction)
     */

    /**
     * Submit a bid offer for a product (regular bid, not auction)
     */
    /**
     * Submit a bid offer for a product (regular bid, not auction)
     */
    public void bidBuy(double amount) {
        // 1) basic validation
        if (!SecurityContextHolder.isLoggedIn()) {
            view.showError("You must be logged in to make an offer");
            return;
        }
        if (amount <= 0) {
            view.showError("Offer must be a positive number");
            return;
        }

        // 2) hand it off to the service (which persists the bid and notifies DB-side listeners)
        Result<Void> res = storeService.submitBidToShoppingItem(
                SecurityContextHolder.token(),
                storeName,
                productId,
                amount
        );
        if (!res.isSuccess()) {
            view.showError("Failed to submit offer: " + res.getErrorMessage());
            return;
        }

        // 3) build your human-friendly message
        String buyer = SecurityContextHolder.email();
        String msg   = String.format("%s made an offer of $%.2f on %s",
                buyer, amount, getProductName());

        // 4) fetch the real owner list (no more guard! 🎉)
        Result<List<String>> ownersResult =
                storeService.getAllOwners(
                        SecurityContextHolder.token(),
                        storeName,
                        /* operatorEmail—now irrelevant */ buyer
                );

        List<String> owners = ownersResult.isSuccess()
                ? ownersResult.getData()
                : List.of();

        if (owners.isEmpty()) {
            view.showError("Offer submitted, but no store-owners could be found");
            return;
        }

        // 5) fan-out a BID notification to each owner
        for (String ownerEmail : owners) {
            notificationSender.send(
                    NotificationType.BID,
                    ownerEmail,
                    msg,
                    amount,
                    productId,
                    buyer   // extra → who placed the bid
            );
        }

        view.showSuccess("Offer submitted — notifying store owners");
    }



    /**
     * Called by each owner when clicking “Approve”.
     */
    public void recordOwnerApproval(String bidId, String ownerEmail) {
        boolean everyoneNow = BidApprovalTracker.approve(bidId);
        if (!everyoneNow) {
            // still waiting on others
            return;
        }
        // all approved → notify buyer
        String buyer = BidApprovalTracker.finish(bidId);
        double amt   = BidApprovalTracker.amount(bidId);
        sendFinalAccept(buyer, amt);
    }

    /**
     * Called by an owner when clicking “Reject”.
     */
    public void recordOwnerRejection(String bidId, String ownerEmail) {
        String buyer = BidApprovalTracker.reject(bidId);
        if (buyer == null) {
            return;
        }
        double amt = BidApprovalTracker.amount(bidId);
        ServiceLocator.getDirectNotificationSender().send(
                NotificationType.BID_REJECTED,
                buyer,
                "Your offer of $"
                        + String.format("%.2f", amt)
                        + " was rejected by "
                        + ownerEmail,
                amt,
                productId,
                null
        );
    }

    /**
     * Helper: send “accepted by all owners” once.
     */
    private void sendFinalAccept(String buyer, double amount) {
        ServiceLocator.getDirectNotificationSender().send(
                NotificationType.BID_ACCEPTED,
                buyer,
                "Your offer of $"
                        + String.format("%.2f", amount)
                        + " has been accepted by all owners!",
                amount,
                productId,
                null
        );
    }
    /**
     * Gets the product name
     */
    public String getProductName() {
        return product != null
                ? product.getName()
                : "product";
    }

    /**
     * Checks if the current user is a store owner
     */
    public boolean isOwner() {
        String me = SecurityContextHolder.email();
        if (me == null || me.isEmpty()) {
            return false;
        }

        try {
            return storeService.isOwner(me, storeName);
        } catch (Exception e) {
            // if the user isn't in the owners list (or any error), treat as not owner
            return false;
        }
    }

    /**
     * Gets the product ID
     */
    public String getProductId() {
        return productId;
    }

    private void sendSimpleNotification(String receiverId, String message) {
        notificationSender.sendSystemNotification(receiverId, message);
    }

    /**
     * Open bids view for store owners
     */
    public void viewAllBids() {
        if (!isOwner()) {
            view.showError("Only store owners can view all bids");
            return;
        }

        view.openBidsDialog();
    }

    /**
     * Reject a regular bid and notify the bidder.
     */
    public void rejectBid(String bidderEmail, double bidPrice) {
        // plain red toast for the bidder
        notify(NotificationType.BID_REJECTED, bidderEmail, null, bidPrice, null);
    }

    /**
     * Send a counter-offer to the bidder.
     *
     * @param bidderEmail  customer’s e-mail
     * @param counterPrice new price you propose
     * @param note         optional free-text message (may be null / empty)
     */
    public void counterBid(String bidderEmail, double counterPrice, String note) {
        notify(NotificationType.BID_COUNTER, bidderEmail, note, counterPrice, null);
    }

    /**
     * Gets the store name
     */
    public String getStoreName() {
        return storeName;
    }
    // Add this to ProductPresenter.java


    // Helper method for currency formatting
    private String formatCurrency(double amount) {
        return String.format("%,.2f", amount);
    }
    public ShoppingProductDTO getProduct() {
        return product;
    }

}