package com.SEGroup.UI.Views;

import com.SEGroup.Infrastructure.NotificationCenter.*;
import com.SEGroup.UI.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Unified "Notifications" page: purchase-offers + auction activity.
 */
@Route(value = "notifications", layout = MainLayout.class)
@PageTitle("Notifications")
public class NotificationView extends VerticalLayout {

    /* ─── infra ─────────────────────────────────────────────────────── */
    private static final Logger log = Logger.getLogger(NotificationView.class.getName());
    private final NotificationEndpoint          endpoint;
    private final NotificationBroadcastService  broadcast;
    private final DirectNotificationSender      sender;

    /* ─── ui elements ───────────────────────────────────────────────── */
    private final Grid<Item> offerGrid   = new Grid<>();
    private final Grid<Item> auctionGrid = new Grid<>();
    private final List<Item> offers      = new ArrayList<>();
    private final List<Item> auctions    = new ArrayList<>();
    private final Span badge             = new Span("0");
    private final Span emptyMsg          = new Span("You have no notifications");

    private Registration broadcastReg;
    private Disposable   endpointSub;

    /* ─── ctor ──────────────────────────────────────────────────────── */
    @Autowired
    public NotificationView(NotificationEndpoint ep,
                            NotificationBroadcastService bc){
        this.endpoint  = ep;
        this.broadcast = bc;
        this.sender    = ServiceLocator.getDirectNotificationSender();

        setSizeFull(); setPadding(true); setSpacing(true);

        /* header with badge */
        badge.getElement().getThemeList().add("badge primary pill");
        badge.getStyle().set("margin-left","8px");
        H2 title = new H2("Notifications");
        add(new HorizontalLayout(title,badge));

        /* tabs */
        Tab t1 = new Tab("Purchase offers");
        Tab t2 = new Tab("Auctions");
        Tabs tabs = new Tabs(t1,t2);

        Div box1 = new Div(offerGrid);   box1.setSizeFull();
        Div box2 = new Div(auctionGrid); box2.setSizeFull();
        Map<Tab,Div> map = Map.of(t1,box1,t2,box2);
        map.values().forEach(v -> v.setVisible(false));
        box1.setVisible(true);
        tabs.addSelectedChangeListener(e ->
                map.forEach((k,v)->v.setVisible(k==tabs.getSelectedTab())));

        /* grids */
        configOfferGrid();
        configAuctionGrid();

        add(tabs, box1, box2, emptyMsg);
        updateGrids();      // initial state
    }

    /* ───────────────── grid builders ──────────────────────────────── */
    private void configOfferGrid(){
        commonColumns(offerGrid);
        offerGrid.addColumn(it -> it.price>0? f$(it.price):"")
                .setHeader("Amount").setWidth("100px").setFlexGrow(0);
        offerGrid.addComponentColumn(this::actionButtons)
                .setHeader("Actions").setWidth("200px").setFlexGrow(0);
        offerGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        offerGrid.setHeight("450px");
    }
    private void configAuctionGrid(){
        commonColumns(auctionGrid);
        auctionGrid.addColumn(it -> it.price>0? f$(it.price):"")
                .setHeader("Bid").setWidth("90px").setFlexGrow(0);
        auctionGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        auctionGrid.setHeight("450px");
    }
    private void commonColumns(Grid<Item> g){
        g.addColumn(Item::ts).setHeader("Time").setWidth("120px").setFlexGrow(0);
        g.addColumn(it -> it.sender==null || it.sender.isBlank() ? "System": it.sender)
                .setHeader("From").setWidth("150px").setFlexGrow(0);
        g.addColumn(Item::msg).setHeader("Message").setFlexGrow(1);
    }

    /* ───────────────── attach / detach ────────────────────────────── */
    @Override
    protected void onAttach(AttachEvent e) {
        if (!SecurityContextHolder.isLoggedIn()) {
            emptyMsg.setText("Please log in to view your notifications");
            return;
        }

        String me = SecurityContextHolder.email();
        System.out.println("NotificationView attached for user: " + me);

        /* history load */
        System.out.println("Loading notification history for: " + me);
        broadcast.getHistory(me).forEach(n -> {
            System.out.println("Found in history: " + n);
            this.push(n);
        });

        broadcast.getAuctionHistory(me).forEach(n -> {
            System.out.println("Found in auction history: " + n);
            this.push(n);
        });

        /* live via broadcast hub */
        System.out.println("Registering for broadcasts");
        broadcastReg = broadcast.register(me, e.getUI(), this::push);

        /* direct endpoint (fallback) */
        System.out.println("Subscribing to endpoint");
        endpointSub = endpoint.subscribe(me)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        n -> e.getUI().access(() -> {
                            System.out.println("Received from endpoint: " + n);
                            push(n);
                        }),
                        err -> log.warning("endpoint error: " + err)
                );
    }
    /* ───────────────── push handler ───────────────────────────────── */
    private void push(Notification n) {
        if(n == null) return;
        System.out.println("NotificationView.push: " + n);

        /* toast notification with improved styling */
        com.vaadin.flow.component.notification.Notification toast =
                com.vaadin.flow.component.notification.Notification
                        .show(n.getMessage(), 5000, // Increase duration to 5 seconds
                                com.vaadin.flow.component.notification.Notification.Position.TOP_END);

        /* Apply theme variants based on notification type */
        if(n instanceof RichNotification rn) {
            switch (rn.getType()){
                case BID, AUCTION_BID -> toast.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                case BID_ACCEPTED, AUCTION_WIN -> {
                    toast.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Play a success sound (in a real app, you'd use a sound API)
                    UI.getCurrent().getPage().executeJs("new Audio('/sounds/success.mp3').play();");
                }
                case BID_REJECTED -> {
                    toast.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    // Add error icon
                    toast.getElement().getStyle().set("--lumo-error-color-50pct", "rgba(231, 76, 60, 0.5)");
                }
                case BID_APPROVAL_NEEDED, AUCTION_OUTBID, BID_COUNTER -> {
                    toast.addThemeVariants(NotificationVariant.LUMO_WARNING);
                    // Make it more prominent
                    toast.getElement().getStyle().set("font-weight", "bold");
                }
            }
        }

        /* convert → Item for the grid */
        Item it = Item.of(n);
        List<Item> list = it.type.startsWith("AUCTION_") ? auctions : offers;

        // Check if notification already exists
        boolean exists = list.stream().anyMatch(ex -> ex.sameAs(it));
        if(!exists) {
            System.out.println("Adding notification to " +
                    (it.type.startsWith("AUCTION_") ? "auctions" : "offers") + " list: " + it);
            list.add(0, it); // Add to top of list
            updateGrids();
        } else {
            System.out.println("Notification already exists in list: " + it);
        }
    }
    /* ───────────────── ui helpers ─────────────────────────────────── */
    private Component actionButtons(Item it) {
        /* delete */
        Button del = new Button(new Icon(VaadinIcon.TRASH),
                c -> {
                    // Remove from both UI list and backend storage
                    if (it.type.startsWith("AUCTION_")) {
                        auctions.remove(it);
                        // Also remove from persistent storage
                        broadcast.removeFromAuctionHistory(SecurityContextHolder.email(), it.msg(), it.price(), it.productId());
                    } else {
                        offers.remove(it);
                        // Also remove from persistent storage
                        broadcast.removeFromHistory(SecurityContextHolder.email(), it.msg(), it.price(), it.productId());
                    }
                    updateGrids();
                });
        del.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout hl = new HorizontalLayout(del); hl.setSpacing(true);

        /* contextual actions */
        if(it.type.equals("BID_APPROVAL_NEEDED")){
            Button ok = new Button("Approve", new Icon(VaadinIcon.CHECK),
                    c -> { approve(it); offers.remove(it); updateGrids(); });
            ok.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            Button no = new Button("Reject", new Icon(VaadinIcon.CLOSE_SMALL),
                    c -> { reject(it); offers.remove(it); updateGrids(); });
            no.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            hl.add(ok,no);
        }
        if(it.type.equals("BID_COUNTER")){
            Button acc = new Button("Accept", new Icon(VaadinIcon.CHECK),
                    c -> { acceptCounter(it); offers.remove(it); updateGrids(); });
            acc.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            hl.add(acc);
        }
        return hl;
    }

    private void approve(Item it){
        sender.send(NotificationType.BID_APPROVAL_OK, null,
                "Bid approved by "+SecurityContextHolder.email(),
                it.price, it.productId, it.extra);
        toast("You've approved the bid from "+it.extra, true);
    }
    private void reject(Item it){
        sender.send(NotificationType.BID_REJECTED, it.extra,
                "Your bid was rejected by "+SecurityContextHolder.email(),
                it.price, it.productId, null);
        toast("You've rejected the bid from "+it.extra, false);
    }
    private void acceptCounter(Item it){
        sender.send(NotificationType.BID_ACCEPTED, it.sender,
                "Counter offer of "+f$(it.price)+" accepted",
                it.price, it.productId, SecurityContextHolder.email());
        toast("Accepted counter offer of "+f$(it.price), true);
    }

    private void toast(String m, boolean ok){
        var n = com.vaadin.flow.component.notification.Notification
                .show(m,3000,
                        com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        n.addThemeVariants(ok? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
    }

    private void updateGrids(){
        offerGrid.setItems(offers);
        auctionGrid.setItems(auctions);
        int total = offers.size()+auctions.size();
        badge.setText(String.valueOf(total));
        emptyMsg.setVisible(total==0);
    }

    /* ───────────────── misc ───────────────────────────────────────── */
    private static String f$(double d){ return "$"+String.format("%.2f",d); }

    /* small POJO for grid rows */
    private record Item(String msg, String sender, String ts, LocalDateTime at,
                        String type, double price, String productId, String extra) {

        static Item of(Notification n) {
            LocalDateTime now = LocalDateTime.now();
            String sender = n instanceof NotificationWithSender s ? s.getSenderId() : "";
            String type = n instanceof RichNotification rn ? rn.getType().name() : "SYSTEM";
            double price = n instanceof RichNotification rn ? rn.getPrice() : 0;
            String pid = n instanceof RichNotification rn ? rn.getProductId() : null;
            String extra = n instanceof RichNotification rn ? rn.getExtra() : null;

            // Format message for bid notifications
            String message = n.getMessage();
            if (n instanceof RichNotification rn && rn.getType() == NotificationType.BID_ACCEPTED) {
                message = "Bid of " + f$(price) + " accepted" +
                        (sender != null && !sender.isEmpty() ? " by " + sender : "");
            } else if (n instanceof RichNotification rn && rn.getType() == NotificationType.BID_REJECTED) {
                message = "Bid of " + f$(price) + " rejected" +
                        (sender != null && !sender.isEmpty() ? " by " + sender : "");
            }

            return new Item(message, sender,
                    now.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    now, type, price, pid, extra);
        }
        boolean sameAs(Item b){
            return Objects.equals(msg,b.msg) &&
                    Objects.equals(type,b.type) &&
                    Math.abs(price-b.price)<1e-3 &&
                    Objects.equals(productId,b.productId) &&
                    Objects.equals(extra,b.extra);
        }
    }



}
