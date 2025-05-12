package com.SEGroup.DTO;



/**
 * Data Transfer Object for price range filter.
 * Used in product search operations.
 */
public class PriceRangeDTO {
    private Double min;
    private Double max;

    /**
     * Default constructor.
     */
    public PriceRangeDTO() {
    }

    /**
     * Constructor with min and max values.
     *
     * @param min The minimum price (can be null for no lower bound)
     * @param max The maximum price (can be null for no upper bound)
     */
    public PriceRangeDTO(Double min, Double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Gets the minimum price.
     *
     * @return The minimum price or null if no lower bound
     */
    public Double getMin() {
        return min;
    }

    /**
     * Sets the minimum price.
     *
     * @param min The minimum price (can be null for no lower bound)
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * Gets the maximum price.
     *
     * @return The maximum price or null if no upper bound
     */
    public Double getMax() {
        return max;
    }

    /**
     * Sets the maximum price.
     *
     * @param max The maximum price (can be null for no upper bound)
     */
    public void setMax(Double max) {
        this.max = max;
    }
}