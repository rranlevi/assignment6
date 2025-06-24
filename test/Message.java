package test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Immutable data holder for pub/sub messages.
 */
public final class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    /**
     * Primary constructor that sets all fields.
     */
    private Message(byte[] data, String asText, double asDouble) {
        this.data = data;
        this.asText = asText;
        this.asDouble = asDouble;
        this.date = new Date();
    }

    /**
     * Constructor from byte array.
     */
    public Message(byte[] bytes) {
        this(bytes, new String(bytes, StandardCharsets.UTF_8), parseDouble(new String(bytes, StandardCharsets.UTF_8)));
    }

    /**
     * Constructor from string.
     */
    public Message(String text) {
        this(text.getBytes(StandardCharsets.UTF_8), text, parseDouble(text));
    }

    /**
     * Constructor from double value.
     */
    public Message(double value) {
        this(String.valueOf(value).getBytes(StandardCharsets.UTF_8), String.valueOf(value), value);
    }

    private static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    @Override
    public String toString() {
        return String.format("Message[data=%d bytes|\"%s\", double=%s, date=%s]", 
                           data.length, asText, asDouble, date);
    }
}
