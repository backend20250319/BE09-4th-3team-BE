package io.fundy.fundyserver.review.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Satisfaction {

    bad(1, "bad"),
    neutral(2, "neutral"),
    good(3, "good");

    private final int value;
    private final String name;

    Satisfaction(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @JsonCreator
    public static Satisfaction fromValue(int value) {
        for (Satisfaction s : values()) {
            if (s.getValue() == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 만족도 값입니다: " + value);
    }
}