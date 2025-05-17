package com.SEGroup.UI.Views;

import com.SEGroup.UI.Constants.Review;
import com.SEGroup.UI.Presenter.ReviewPresenter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("store/:storeName/reviews")
public class ReviewView extends VerticalLayout implements BeforeEnterObserver {
    private final Grid<Review> reviewGrid = new Grid<>(Review.class, false);
    private final ReviewPresenter presenter;
    private String currentStoreName;

    public ReviewView() {
        presenter = new ReviewPresenter(this);

        setSpacing(true);
        setPadding(true);

        reviewGrid.addColumn(Review::email).setHeader("User Email");
        reviewGrid.addColumn(Review::rating).setHeader("Rating");
        reviewGrid.addColumn(Review::text).setHeader("Review Text");

        add(reviewGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        currentStoreName = event.getRouteParameters().get("storeName").orElse("Unknown");
        addComponentAtIndex(0, new H2("Reviews for: " + currentStoreName));
        presenter.loadReviews(currentStoreName);
    }

    public void displayReviews(List<Review> reviews) {
        if (reviews != null && !reviews.isEmpty()) {
            reviewGrid.setItems(reviews);
        } else {
            Notification.show("No reviews found for this store.", 3000, Notification.Position.MIDDLE);
        }
    }

    public void showError(String message) {
        Notification notification = Notification.show("Error: " + message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
