package com.SEGroup.UI;

import com.vaadin.flow.component.html.Image;

/**
 * Helper for image loading with built-in fallback mechanism
 */
public class ImageHelper {
    /**
     * Creates an image with automatic fallback if the source fails to load
     */
    public static Image createWithFallback(String url, String altText) {
        if (url == null || url.isEmpty()) {
            return createPlaceholder(altText);
        }

        // SVG and data URIs work reliably without needing fallbacks
        if (url.startsWith("data:")) {
            Image image = new Image(url, altText);
            image.setMaxHeight("350px");
            image.setMaxWidth("350px");
            image.getStyle().set("object-fit", "contain");
            return image;
        }

        // For external URLs, add a fallback
        Image image = new Image(url, altText);
        image.setMaxHeight("350px");
        image.setMaxWidth("350px");
        image.getStyle().set("object-fit", "contain");

        // Add JavaScript error handler
        image.getElement().executeJs(
                "this.onerror = function() {" +
                        "  this.style.display = 'flex';" +
                        "  this.style.alignItems = 'center';" +
                        "  this.style.justifyContent = 'center';" +
                        "  this.style.backgroundColor = '#e0e0e0';" +
                        "  this.style.color = '#666';" +
                        "  this.style.fontSize = '32px';" +
                        "  this.style.fontWeight = 'bold';" +
                        "  this.removeAttribute('src');" +
                        "  this.textContent = arguments[0];" +
                        "}",
                altText != null && !altText.isEmpty() ? altText.substring(0, 1).toUpperCase() : "P"
        );

        return image;
    }

    private static Image createPlaceholder(String altText) {
        String letter = altText != null && !altText.isEmpty() ? altText.substring(0, 1).toUpperCase() : "P";

        Image placeholder = new Image("", altText);
        placeholder.getElement().setText(letter);
        placeholder.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "#e0e0e0")
                .set("color", "#666")
                .set("font-size", "32px")
                .set("font-weight", "bold")
                .set("width", "200px")
                .set("height", "200px");

        return placeholder;
    }
}