package com.zerodeplibs.webpush.header;

/**
 * The utility class used for setting the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
 *
 * <h3>Example:</h3>
 * <pre class="code">
 * // Sets the urgency header field
 * myHeader.addHeader("Urgency", Urgency.veryLow());
 * </pre>
 *
 * @author Tomoki Sato
 */
public class Urgency {

    /**
     * The name of the Urgency header field.
     */
    public static String HEADER_NAME = "Urgency";

    private Urgency() {
    }

    /**
     * "very-low" urgency.
     *
     * @return "very-low".
     */
    public static String veryLow() {
        return UrgencyOption.VERY_LOW.getValue();
    }

    /**
     * "low" urgency.
     *
     * @return "low".
     */
    public static String low() {
        return UrgencyOption.LOW.getValue();
    }

    /**
     * "normal" urgency.
     *
     * @return "normal".
     */
    public static String normal() {
        return UrgencyOption.NORMAL.getValue();
    }

    /**
     * "high" urgency.
     *
     * @return "high".
     */
    public static String high() {
        return UrgencyOption.HIGH.getValue();
    }

    /**
     * This enum represents an available value for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
     *
     * @author Tomoki Sato
     */
    public enum UrgencyOption {

        /**
         * "very-low" urgency.
         */
        VERY_LOW("very-low"),

        /**
         * "low" urgency.
         */
        LOW("low"),

        /**
         * "normal" urgency.
         */
        NORMAL("normal"),

        /**
         * "high" urgency.
         */
        HIGH("high");

        private final String value;

        UrgencyOption(String fieldValue) {
            this.value = fieldValue;
        }

        /**
         * Gets the value for the Urgency header field.
         *
         * @return the value for the Urgency header field.
         */
        public String getValue() {
            return this.value;
        }
    }
}
