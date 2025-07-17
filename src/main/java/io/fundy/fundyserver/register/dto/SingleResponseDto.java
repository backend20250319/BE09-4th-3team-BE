package io.fundy.fundyserver.register.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SingleResponseDto<T> {
    private T data;
}