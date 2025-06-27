package io.fundy.fundyserver.review.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Satisfaction {

    BAD(1), AVERAGE(3), GOOD(5);

    private final int value;

    Satisfaction(int value) {
        this.value = value;
    }

    @JsonValue
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