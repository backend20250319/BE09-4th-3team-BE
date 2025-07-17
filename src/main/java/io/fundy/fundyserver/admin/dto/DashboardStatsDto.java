package io.fundy.fundyserver.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    private int totalProjects;
    private int totalUsers;
    private int activeProjects;
}
