package com.zerodeplibs.webpush.internal;

/**
 * The utility class for helping a method or constructor check its preconditions.
 *
 * <div><b>WARNING:</b></div>
 *
 * <p>
 * This class is intended to be used internally in this library.
 * Therefore, don't use this class from outside this library.
 * </p>
 *
 * @author Tomoki Sato
 */
public class WebPushPreConditions {

    private WebPushPreConditions() {
    }

    /**
     * Checks whether the given object is null or not.
     *
     * @param object an object.
     * @param name   the name of the given object.
     * @throws NullPointerException if the given object is null.
     */
    public static void checkNotNull(Object object, String name) {
        if (object == null) {
            throw new NullPointerException(String.format("%s should not be null.", name));
        }
    }

    /**
     * Checks whether the given condition is met or not.
     *
     * @param condition a condition.
     * @param message   the message for exception.
     * @throws IllegalStateException if the given condition is not met(condition == false).
     */
    public static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks whether the given condition is met or not.
     *
     * @param condition c condition.
     * @param message   the message for exception.
     * @throws IllegalArgumentException if the given condition is not met(condition == false).
     */
    public static void checkArgument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
