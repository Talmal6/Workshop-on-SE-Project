package com.SEGroup.UI;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting common data types consistently across the application.
 */
public class FormatUtils {

    private static final DecimalFormat RATING_FORMAT = new DecimalFormat("0.0");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    /**
     * Formats a rating to display consistently with one decimal place.
     * Example: 4.666667 -> 4.7
     *
     * @param rating The rating value to format
     * @return Formatted rating string with one decimal place
     */
    public static String formatRating(double rating) {
        return RATING_FORMAT.format(rating);
    }

    /**
     * Formats a rating to display with a visual star representation.
     *
     * @param rating The rating value to format
     * @return Formatted rating string with stars
     */
    public static String formatRatingWithStars(double rating) {
        StringBuilder result = new StringBuilder(formatRating(rating));
        result.append(" ");

        // Add stars
        int fullStars = (int) Math.floor(rating);
        boolean hasHalfStar = rating - fullStars >= 0.5;

        for (int i = 0; i < fullStars; i++) {
            result.append("★");
        }
        if (hasHalfStar) {
            result.append("⯨");
        }
        for (int i = 0; i < 5 - fullStars - (hasHalfStar ? 1 : 0); i++) {
            result.append("☆");
        }

        return result.toString();
    }

    /**
     * Formats a price as currency with two decimal places.
     * Example: 19.9 -> $19.90
     *
     * @param price The price to format
     * @return Formatted price string
     */
    public static String formatPrice(double price) {
        return CURRENCY_FORMAT.format(price);
    }

    /**
     * Formats a price without the currency symbol.
     * Example: 19.9 -> 19.90
     *
     * @param price The price to format
     * @return Formatted price string without currency symbol
     */
    public static String formatPriceNumeric(double price) {
        return String.format("%.2f", price);
    }

    /**
     * Truncates a string if it exceeds the maximum length.
     *
     * @param str The string to truncate
     * @param maxLength The maximum allowed length
     * @return Truncated string with ellipsis if needed
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}