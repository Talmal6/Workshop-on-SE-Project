package com.SEGroup.UI.Components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Date;

public class AuctionTimer extends HorizontalLayout {

    private final Div hoursDiv = new Div();
    private final Div minutesDiv = new Div();
    private final Div secondsDiv = new Div();
    private final Div millisDiv = new Div();
    private Date endTime;
    private Runnable onComplete;
    private boolean isCompleted = false;

    public AuctionTimer() {
        addClassName("auction-timer");
        setWidthFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // Style time units
        for (Div timeUnit : new Div[]{hoursDiv, minutesDiv, secondsDiv, millisDiv}) {
            timeUnit.addClassName("time-unit");
            timeUnit.getStyle()
                    .set("background-color", "var(--lumo-primary-color)")
                    .set("color", "white")
                    .set("padding", "0.5rem 1rem")
                    .set("border-radius", "4px")
                    .set("text-align", "center")
                    .set("min-width", "4rem")
                    .set("font-weight", "bold")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)")
                    .set("transition", "all 0.3s ease");
        }

        // Style millisecond div differently
        millisDiv.getStyle()
                .set("font-size", "0.8rem")
                .set("min-width", "3rem")
                .set("padding", "0.5rem 0.5rem");

        // Create time unit labels
        Div hoursLabel = createLabel("HRS");
        Div minutesLabel = createLabel("MIN");
        Div secondsLabel = createLabel("SEC");
        Div millisLabel = createLabel("MS");

        // Create time unit containers with labels
        Div hoursContainer = new Div(hoursDiv, hoursLabel);
        Div minutesContainer = new Div(minutesDiv, minutesLabel);
        Div secondsContainer = new Div(secondsDiv, secondsLabel);
        Div millisContainer = new Div(millisDiv, millisLabel);

        for (Div container : new Div[]{hoursContainer, minutesContainer, secondsContainer, millisContainer}) {
            styleLabelContainer(container);
        }

        // Create separators
        Span separator1 = createSeparator();
        Span separator2 = createSeparator();
        Span separator3 = createSeparator();
        separator3.setText(".");

        // Add all components
        add(hoursContainer, separator1, minutesContainer, separator2, secondsContainer, separator3, millisContainer);

        // Set default values
        hoursDiv.setText("00");
        minutesDiv.setText("00");
        secondsDiv.setText("00");
        millisDiv.setText("000");
    }

    private Div createLabel(String text) {
        Div label = new Div();
        label.setText(text);
        label.getStyle()
                .set("font-size", "0.7rem")
                .set("opacity", "0.8")
                .set("text-align", "center")
                .set("margin-top", "0.3rem");
        return label;
    }

    private void styleLabelContainer(Div container) {
        container.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center");
    }

    private Span createSeparator() {
        Span separator = new Span(":");
        separator.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "bold")
                .set("margin", "0 0.5rem")
                .set("align-self", "center");
        return separator;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        startTimer();
    }

    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    private void startTimer() {
        UI ui = UI.getCurrent();
        if (ui != null && endTime != null) {
            updateDisplay();

            // Use a more reasonable polling interval (500ms instead of 100ms)
            // This will reduce UI load and flickering
            ui.setPollInterval(500);
            ui.addPollListener(event -> {
                if (!isCompleted && updateDisplay()) {
                    isCompleted = true;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    ui.setPollInterval(-1); // Stop polling
                }
            });
        }
    }
    private boolean updateDisplay() {
        if (endTime == null) return false;

        long now = System.currentTimeMillis();
        long end = endTime.getTime();
        long remaining = Math.max(0, end - now);

        if (remaining <= 0) {
            hoursDiv.setText("00");
            minutesDiv.setText("00");
            secondsDiv.setText("00");
            millisDiv.setText("000");

            // Apply completed style
            hoursDiv.getStyle().set("background-color", "var(--lumo-error-color)");
            minutesDiv.getStyle().set("background-color", "var(--lumo-error-color)");
            secondsDiv.getStyle().set("background-color", "var(--lumo-error-color)");
            millisDiv.getStyle().set("background-color", "var(--lumo-error-color)");

            return true;
        }

        // Calculate hours, minutes, seconds, millis
        long hours = remaining / (60 * 60 * 1000);
        remaining %= (60 * 60 * 1000);
        long minutes = remaining / (60 * 1000);
        remaining %= (60 * 1000);
        long seconds = remaining / 1000;
        long millis = remaining % 1000;

        // Update display
        hoursDiv.setText(String.format("%02d", hours));
        minutesDiv.setText(String.format("%02d", minutes));
        secondsDiv.setText(String.format("%02d", seconds));
        millisDiv.setText(String.format("%03d", millis));

        // Apply warning styles when time is running low
        if (hours == 0) {
            if (minutes < 5) {
                String color = minutes == 0 && seconds < 30 ? "var(--lumo-error-color)" : "var(--lumo-warning-color)";
                hoursDiv.getStyle().set("background-color", color);
                minutesDiv.getStyle().set("background-color", color);
                secondsDiv.getStyle().set("background-color", color);
                millisDiv.getStyle().set("background-color", color);

                // Add pulsating animation when under 10 seconds
                if (minutes == 0 && seconds < 10) {
                    String pulseAnimation = "pulse 1s infinite";
                    hoursDiv.getStyle().set("animation", pulseAnimation);
                    minutesDiv.getStyle().set("animation", pulseAnimation);
                    secondsDiv.getStyle().set("animation", pulseAnimation);
                    millisDiv.getStyle().set("animation", pulseAnimation);

                    String pulseAdded = UI.getCurrent().getElement().getStyle().get("--pulse-added");
                    if (!"true".equals(pulseAdded)) {
                        UI.getCurrent().getElement().executeJs(
                                "if (!document.getElementById('pulse-animation')) {" +
                                        "  var style = document.createElement('style');" +
                                        "  style.id = 'pulse-animation';" +
                                        "  style.innerHTML = '@keyframes pulse { 0% { transform: scale(1); } " +
                                        "50% { transform: scale(1.05); } 100% { transform: scale(1); } }';" +
                                        "  document.head.appendChild(style);" +
                                        "  this.style.setProperty('--pulse-added', 'true');" +
                                        "}"
                        );
                    }
                } else {
                    hoursDiv.getStyle().remove("animation");
                    minutesDiv.getStyle().remove("animation");
                    secondsDiv.getStyle().remove("animation");
                    millisDiv.getStyle().remove("animation");
                }
            }
        }

        return false;
    }
}