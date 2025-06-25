package io.fundy.fundyserver.projectreview.dto;

public enum Satisfaction {

    BAD(1), AVERAGE(2), GOOD(3);

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
