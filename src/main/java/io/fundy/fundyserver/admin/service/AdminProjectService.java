package io.fundy.fundyserver.admin.service;

import io.fundy.fundyserver.admin.dto.AdminProjectResponseDto;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminProjectService {

    private final ProjectRepository projectRepository;

    /**
     * 전체 프로젝트를 페이징 조회 (관리자용)
     * @param page 페이지 번호 (0부터 시작)
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
}
