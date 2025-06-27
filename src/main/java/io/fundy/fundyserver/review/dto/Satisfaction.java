package io.fundy.fundyserver.review.dto;

public enum Satisfaction {

    BAD(1), AVERAGE(3), GOOD(5);

    private final int value;
    Satisfaction(int value) { this.value = value; }

    public int getValue() { return value; }

    public static Satisfaction fromValue(int value) {
        for (Satisfaction s : values()) {
            if (s.getValue() == value) return s;
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
