package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.project.entity.ProjectStatus;
import org.springframework.stereotype.Service;
import io.fundy.fundyserver.admin.dto.DashboardStatsDto;
import io.fundy.fundyserver.register.repository.UserRepository;
import io.fundy.fundyserver.project.repository.ProjectRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public DashboardService(ProjectRepository projectRepository,
                            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /**
     * 대시보드 상단 통계 정보 반환
     */
    public DashboardStatsDto getDashboardStats() {
        long totalProjects = projectRepository.count();               // 전체 프로젝트 수
        int activeProjects = projectRepository.countByProductStatus(ProjectStatus.APPROVED); // 승인된 프로젝트 수
        long totalUsers = userRepository.count();                     // 전체 유저 수

        return new DashboardStatsDto((int) totalProjects, (int) totalUsers, activeProjects);
    }

    /**
     * 프로젝트 상태별 개수 반환
     */
    public Map<String, Integer> getProjectStatusCounts() {
        Map<String, Integer> result = new HashMap<>();
        result.put("PENDING", projectRepository.countByProductStatus(ProjectStatus.WAITING_APPROVAL));
        result.put("APPROVED", projectRepository.countByProductStatus(ProjectStatus.APPROVED));
        result.put("REJECTED", projectRepository.countByProductStatus(ProjectStatus.REJECTED));
        return result;
    }
}


