package com.SEGroup.Service;

/**
 * A generic class that represents the result of an operation.
 * It contains information about whether the operation was successful,
 * the result data (if successful), and an error message (if failed).
 *
 * @param <T> The type of data that the result contains.
 */
public class Result<T> {

    private final boolean success;      // Indicates if the operation was successful
    private final T data;               // The data returned if the operation was successful
    private final String errorMessage;  // The error message if the operation failed

    /**
     * Private constructor to create a Result object with the specified parameters.
     *
     * @param success The success flag indicating if the operation succeeded.
     * @param data The data returned by the operation (if successful).
     * @param errorMessage The error message (if the operation failed).
     */
    private Result(boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful Result object with the specified data.
     *
     * @param <T> The type of the result data.
     * @param data The data to be returned in the result.
     * @return A Result object indicating success and containing the data.
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);  // Success result with no error message
    }

    /**
     * Creates a failed Result object with the specified error message.
     *
     * @param <T> The type of the result data (null in the case of failure).
     * @param message The error message explaining the failure.
     * @return A Result object indicating failure with the provided error message.
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message);  // Failure result with the error message
    }

    /**
     * Checks if the operation was successful.
     *
     * @return true if the operation was successful, false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if the operation failed.
     *
     * @return true if the operation failed, false otherwise.
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Retrieves the data returned by the operation.
     * This is only valid if the operation was successful.
     *
     * @return The data returned by the operation, or null if the operation failed.
     */
    public T getData() {
        return data;
    }

    /**
     * Retrieves the error message if the operation failed.
     * This is only valid if the operation was unsuccessful.
     *
     * @return The error message explaining the failure, or null if the operation succeeded.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (success) {
            return "Result{success=" + success + ", data=" + data + "}";
        } else {
            return "Result{success=" + success + ", errorMessage='" + errorMessage + "'}";
        }
    }
}
