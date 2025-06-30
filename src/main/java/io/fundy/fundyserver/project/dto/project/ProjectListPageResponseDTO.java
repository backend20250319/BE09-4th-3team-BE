package io.fundy.fundyserver.project.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListPageResponseDTO {
    private List<ProjectListResponseDTO> data;
    private PaginationDTO pagination;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationDTO {
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
    }
}

