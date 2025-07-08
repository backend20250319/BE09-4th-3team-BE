package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminProjectResponseDto;
import io.fundy.fundyserver.admin.dto.AdminTotalProjectDto;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.ProjectStatus;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminProjectService {

    private final ProjectRepository projectRepository;

    /**
     * 전체 프로젝트를 페이징 조회 (관리자용)
     * @param page 페이지 번호 (0부터 시작)
     * @param categoryNo 카테고리 번호 (선택)
     * @return AdminProjectResponseDto 페이지
     */
    public Page<AdminProjectResponseDto> getAllProjects(int page, Long categoryNo) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projects;

        if (categoryNo != null) {
            projects = projectRepository.findByCategory_CategoryNo(categoryNo, pageable);
        } else {
            projects = projectRepository.findAll(pageable);
        }

        return projects.map(AdminProjectResponseDto::fromEntity);
    }

    /**
     * 프로젝트 상태 변경 (승인 또는 거절)
     * @param projectId 대상 프로젝트 ID
     * @param newStatus 변경할 상태 (APPROVED 또는 REJECTED)
     */
    public void updateProjectStatus(Long projectId, ProjectStatus newStatus) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);

        if (optionalProject.isEmpty()) {
            throw new IllegalArgumentException("해당 ID의 프로젝트를 찾을 수 없습니다: " + projectId);
        }

        Project project = optionalProject.get();

        if (project.getProductStatus() != ProjectStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("해당 프로젝트는 승인 대기 상태가 아닙니다.");
        }

        // 상태 업데이트
        project.setProductStatus(newStatus);
        projectRepository.save(project);
    }

    /* 프로젝트 전체 상태 */
    public AdminTotalProjectDto getProjectStatistics() {
        long pendingCount = projectRepository.countByProductStatus(ProjectStatus.WAITING_APPROVAL);
        long approvedCount = projectRepository.countByProductStatus(ProjectStatus.APPROVED);
        long rejectedCount = projectRepository.countByProductStatus(ProjectStatus.REJECTED);

        long totalCount = pendingCount + approvedCount + rejectedCount;

        return AdminTotalProjectDto.builder()
                .total(totalCount)
                .pending(pendingCount)
                .approved(approvedCount)
                .rejected(rejectedCount)
                .build();
    }
}
