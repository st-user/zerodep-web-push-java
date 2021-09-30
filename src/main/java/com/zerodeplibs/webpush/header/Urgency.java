package com.zerodeplibs.webpush.header;

/**
 * The utility class for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
 *
 * <p>
 * <b>Example:</b>
 * </p>
 * <pre class="code">
 * // Set the urgency header field
 * myHeader.addHeader("Urgency", Urgency.veryLow());
 * </pre>
 *
 * @author Tomoki Sato
 */
public class Urgency {

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
     * The enum represents the options for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
     *
     * @author Tomoki Sato
     */
    public enum UrgencyOption {
        VERY_LOW("very-low"),
        LOW("low"),
        NORMAL("normal"),
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
