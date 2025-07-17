package io.fundy.fundyserver.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationPageResponseDTO {
    private List<NotificationResponseDTO> content;
    private int pageNumber;
    private int totalPages;
    private long totalElements;
}
